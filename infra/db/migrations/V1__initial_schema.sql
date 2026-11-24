create table users (
    id uuid primary key,
    full_name text not null check (char_length(full_name) between 1 and 200),
    email text not null unique check (char_length(email) between 1 and 320),
    password_hash text not null,
    role text not null check (role in ('teacher', 'student')),
    created_at timestamptz not null default now()
);

create table sessions (
    token_hash bytea primary key check (octet_length(token_hash) = 32),
    user_id uuid not null references users(id) on delete cascade,
    expires_at timestamptz not null
);
create index sessions_expiry on sessions(expires_at);

create table assignments (
    id uuid primary key,
    teacher_id uuid not null references users(id),
    title text not null check (char_length(title) between 1 and 200),
    submission_token text not null unique,
    similarity_revision bigint not null default 0 check (similarity_revision >= 0),
    created_at timestamptz not null default now(),
    deleted_at timestamptz
);
create index assignments_teacher on assignments(teacher_id, created_at, id) where deleted_at is null;

create table assignment_sources (
    assignment_id uuid not null references assignments(id),
    source_assignment_id uuid not null references assignments(id),
    primary key (assignment_id, source_assignment_id),
    check (assignment_id <> source_assignment_id)
);
create index assignment_sources_reverse on assignment_sources(source_assignment_id, assignment_id);

create table submission_slots (
    assignment_id uuid not null references assignments(id),
    student_id uuid not null references users(id),
    current_submission_id uuid,
    can_submit boolean not null default true,
    primary key (assignment_id, student_id),
    check (can_submit or current_submission_id is not null)
);

create table submissions (
    id uuid primary key,
    assignment_id uuid not null,
    student_id uuid not null,
    storage_key text not null unique,
    size_bytes int not null check (size_bytes between 1 and 1000000),
    sha256 char(64) not null check (sha256 ~ '^[0-9a-f]{64}$'),
    loc int not null check (loc > 0),
    submitted_at timestamptz not null,
    expires_at timestamptz not null check (expires_at > submitted_at),
    foreign key (assignment_id, student_id) references submission_slots(assignment_id, student_id),
    unique (assignment_id, student_id, id)
);
alter table submission_slots add constraint current_submission_in_slot
    foreign key (assignment_id, student_id, current_submission_id) references submissions(assignment_id, student_id, id);
create index submissions_expiry on submissions(expires_at, id);

create table object_deletions (
    storage_key text primary key,
    queued_at timestamptz not null default now()
);
create index object_deletions_age on object_deletions(queued_at, storage_key);

create table analysis_runs (
    id uuid primary key,
    method text not null,
    contract_version text not null,
    method_version text not null check (method_version <> ''),
    params jsonb not null default '{}' check (jsonb_typeof(params) = 'object'),
    assignment_id uuid references assignments(id),
    revision bigint,
    submission_id uuid references submissions(id) on delete cascade,
    status text not null default 'queued' check (status in ('queued', 'running', 'done', 'failed', 'cancelled')),
    attempts int not null default 0,
    max_attempts int not null default 3 check (max_attempts > 0),
    locked_by text,
    heartbeat_at timestamptz,
    created_at timestamptz not null default now(),
    started_at timestamptz,
    finished_at timestamptz,
    error_code text,
    check (attempts between 0 and max_attempts),
    check ((status = 'running' and locked_by is not null and heartbeat_at is not null)
        or (status <> 'running' and locked_by is null and heartbeat_at is null)),
    check ((method = 'structsim' and contract_version = 'integritylens.core/2'
            and assignment_id is not null and revision is not null and revision >= 0 and submission_id is null)
        or (method = 'ai' and contract_version = 'integritylens.ai/1'
            and submission_id is not null and assignment_id is null and revision is null)),
    unique (assignment_id, revision, method),
    unique (submission_id, method_version)
);
create index analysis_runs_pick on analysis_runs(method, method_version, created_at, id)
    where status in ('queued', 'running');

create table analysis_run_inputs (
    run_id uuid not null references analysis_runs(id) on delete cascade,
    submission_id uuid not null references submissions(id) on delete cascade,
    scope text not null check (scope in ('target', 'source')),
    primary key (run_id, submission_id)
);
create index analysis_run_inputs_submission on analysis_run_inputs(submission_id, run_id);

create table similarity_results (
    run_id uuid not null,
    a uuid not null,
    b uuid not null,
    value double precision check (value between 0 and 1),
    reason_code text,
    details jsonb not null check (jsonb_typeof(details) = 'object'),
    fragments jsonb not null check (jsonb_typeof(fragments) = 'array'),
    primary key (run_id, a, b),
    foreign key (run_id, a) references analysis_run_inputs(run_id, submission_id) on delete cascade,
    foreign key (run_id, b) references analysis_run_inputs(run_id, submission_id) on delete cascade,
    check (a < b),
    check ((value is not null and reason_code is null)
        or (value is null and reason_code is not null and fragments = '[]'))
);
create index similarity_results_b on similarity_results(run_id, b);

create table ai_results (
    run_id uuid primary key references analysis_runs(id) on delete cascade,
    score double precision check (score between 0 and 1),
    suspicious jsonb not null check (jsonb_typeof(suspicious) = 'array'),
    reason_code text,
    check ((score is not null and reason_code is null)
        or (score is null and reason_code is not null and suspicious = '[]'))
);
