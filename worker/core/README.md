# core

Файловый адаптер анализа: вход и выход — каталоги, формат задан [контрактом](CONTRACT.md).
Сами методы не здесь: JPlag и structsim лежат в [tools/](../../tools/README.md), core только
запускает их и приводит результат к контракту. Перед запуском из копии работ стираются
[шаблонные алгоритмы](CONTRACT.md#шаблонные-алгоритмы) из набора [`templates/`](templates/).
Есть ещё [мок](CONTRACT.md#мок).

```sh
python -m worker.core run --input input/ --output output/
python -m worker.core.selfcheck jplag
python -m unittest discover -s worker/core/tests -v
```

Тесты настоящих методов идут там, где есть их инструменты, то есть в образах воркеров.

Зависимости: [worker/requirements.txt](../requirements.txt).
Python API: `worker.core.run(input_dir, output_dir)`.

| Переменная | Для чего |
| --- | --- |
| `JPLAG_JAVA`, `JPLAG_JAR`, `JPLAG_JVM_OPTS` | JVM и jar JPlag, ключи JVM |
| `SEMGREP_CORE` | бинарник `semgrep-core` |
| `STRUCTSIM_NATIVE` | [native](../../tools/structsim/docs/native.md); `off` — эталон на Python |
| `CORE_THREADS` | сколько ядер занимать; по умолчанию лимит контейнера |
