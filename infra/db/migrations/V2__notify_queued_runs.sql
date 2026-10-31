-- Воркер не опрашивает очередь, а ждёт уведомление: задание начинается сразу после
-- фиксации транзакции. Канал у каждого метода свой, чужой воркер не просыпается.
create function notify_queued_run() returns trigger language plpgsql as $$
begin
    perform pg_notify('runs_' || new.method, new.id::text);
    return new;
end;
$$;

create trigger runs_queued after insert or update of status on runs
    for each row when (new.status = 'queued') execute function notify_queued_run();
