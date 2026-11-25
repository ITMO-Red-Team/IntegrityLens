alter table analysis_run_inputs add column error_code text;

-- Keep historical core/2 runs; new jobs use core/3 with explicit input failures.
alter table analysis_runs drop constraint analysis_runs_check2;
alter table analysis_runs add constraint analysis_runs_method_contract check (
    (method = 'structsim' and contract_version in ('integritylens.core/2', 'integritylens.core/3')
     and assignment_id is not null and revision is not null and revision >= 0 and submission_id is null)
    or (method = 'ai' and contract_version = 'integritylens.ai/1'
        and submission_id is not null and assignment_id is null and revision is null)
);
