# core

Файловый адаптер анализа. Сейчас реализован только [мок](CONTRACT.md#мок).
[Контракт](CONTRACT.md) нужен для подключения настоящих методов.

```sh
python -m worker.core run --input input/ --output output/
python -m unittest discover -s worker/core/tests -v
```

Зависимости: [worker/requirements.txt](../requirements.txt).
Python API: `worker.core.run(input_dir, output_dir)`.
