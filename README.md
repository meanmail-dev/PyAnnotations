# PyAnnotations plugin
[![JetBrains IntelliJ Plugins](https://img.shields.io/jetbrains/plugin/r/stars/12035?label=JetBrans%20Marketplace)](https://plugins.jetbrains.com/plugin/12035)
[![JetBrains IntelliJ plugins](https://img.shields.io/jetbrains/plugin/d/12035)](https://plugins.jetbrains.com/plugin/12035)
[![Twitter Follow](https://img.shields.io/twitter/follow/meanmaildev?style=plastic)](https://twitter.com/meanmaildev)

Code Inspections with quick fixes for python annotations

https://meanmail.dev/plugin/3

## Supported versions of Intellij:

All product with python — 2020.1 — 2020.3(eap)


## Examples

1. Replace `Union` with `None` with `Optional`

```python
# before

def str_to_int(value: str) -> Union[int, None]:
    ...

# after quickfix

def str_to_int(value: str) -> Optional[int]:
    ...
```

2. Replace `Union` with `object` with `object`

```python
# before

def some_func(value: Any) -> Union[int, object]:
    ...

# after quickfix

def some_func(value: Any) -> object:
    ...
```


3. Replace `Union` with one item with item

```python
# before

def parse_json(value: str) -> Union[dict]:
    ...

# after quickfix

def parse_json(value: str) -> dict:
    ...
```
