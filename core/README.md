# core

Файловый адаптер методов анализа. Пока реализован только детерминированный мок `mock-v1`.
Контракт для подключения настоящих JPlag и structsim описан в [CONTRACT.md](CONTRACT.md).

```sh
python -m core run --input input/ --output output/
python -m unittest discover -s core/tests -v
```

Для тестов установите зависимости из `worker/requirements.txt`. Вызов из Python:
`core.run(input_dir, output_dir)`.
