-- Прогон ai привязан к сдаче, методы сравнения остаются пакетными.
alter table runs drop constraint runs_method_check;
alter table runs add constraint runs_method_check check (method in ('jplag', 'structsim', 'ai'));
alter table runs add column submission_id uuid references submissions(id);
alter table runs drop constraint runs_batch_id_method_key;
create unique index runs_batch_method on runs(batch_id, method) where method in ('jplag', 'structsim');
create unique index runs_submission on runs(submission_id) where method = 'ai';
alter table runs add constraint runs_ai_submission check ((method = 'ai') = (submission_id is not null));
