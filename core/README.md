# core

Файловый адаптер анализа. Сейчас реализован только [мок](CONTRACT.md#мок).
[Контракт](CONTRACT.md) нужен для подключения настоящих методов.

```sh
python -m core run --input input/ --output output/
python -m unittest discover -s core/tests -v
```

Зависимости: [worker/requirements.txt](../worker/requirements.txt).
Python API: `core.run(input_dir, output_dir)`.
