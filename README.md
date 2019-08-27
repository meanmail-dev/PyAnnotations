# PyAnnotations plugin

Code Inspections with quick fixes for python annotations

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
