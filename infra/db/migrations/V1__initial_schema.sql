create table batches (
    id uuid primary key,
    title text not null,
    language text not null check (language = 'java'),
    archive_name text not null,
    archive_sha256 text not null unique,
    submission_count int not null check (submission_count between 5 and 50),
    created_at timestamptz not null default now()
);

create table submissions (
    id uuid primary key,
    batch_id uuid not null references batches(id),
    original_name text not null,
    language text not null check (language = 'java'),
    loc int not null check (loc >= 0),
    size_bytes int not null check (size_bytes >= 0),
    sha256 text not null,
    storage_key text not null unique,
    unique (batch_id, id)
);
create index submissions_batch on submissions(batch_id);

create table runs (
    id uuid primary key,
    batch_id uuid not null references batches(id),
    method text not null check (method in ('jplag', 'structsim')),
    params jsonb not null default '{}',
    status text not null default 'queued' check (status in ('queued', 'running', 'done', 'failed')),
    attempts int not null default 0,
    max_attempts int not null default 3 check (max_attempts > 0),
    locked_by text,
    heartbeat_at timestamptz,
    created_at timestamptz not null default now(),
    started_at timestamptz,
    finished_at timestamptz,
    method_version text,
    error text,
    result jsonb,
    artifacts_prefix text,
    unique (batch_id, method)
);
create index runs_pick on runs(method, created_at) where status in ('queued', 'running');

create table feedback (
    batch_id uuid not null references batches(id),
    a uuid not null,
    b uuid not null,
    decision text not null check (decision in ('confirmed', 'acceptable', 'false_positive')),
    comment text not null default '',
    updated_at timestamptz not null default now(),
    primary key (batch_id, a, b),
    foreign key (batch_id, a) references submissions(batch_id, id),
    foreign key (batch_id, b) references submissions(batch_id, id),
    check (a < b)
);
