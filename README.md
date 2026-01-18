# PyAnnotations

[![JetBrains IntelliJ Plugins](https://img.shields.io/jetbrains/plugin/r/stars/12035?label=JetBrains%20Marketplace)](https://plugins.jetbrains.com/plugin/12035)
[![JetBrains IntelliJ plugins](https://img.shields.io/jetbrains/plugin/d/12035)](https://plugins.jetbrains.com/plugin/12035)
[![GitHub](https://img.shields.io/github/license/meanmail-dev/PyAnnotations)](https://github.com/meanmail-dev/PyAnnotations/blob/main/LICENSE)

**Smart code inspections for Python type annotations** — simplify, modernize, and validate your type hints with one-click quick fixes.

## Features

### 21 Inspections with Quick Fixes

**Union & Optional Simplification**
```python
Union[int, None]           →  Optional[int]
Union[X, object]           →  object
Union[X, Any]              →  Any
Union[dict]                →  dict
Optional[Optional[X]]      →  Optional[X]
Union[int, int, str]       →  Union[int, str]
Union[int, bool]           →  int  # bool is subtype of int
Union[Union[A, B], C]      →  Union[A, B, C]
```

**Modern Pipe Syntax (Python 3.10+)**
```python
X | object                 →  object
X | Any                    →  Any
(X | Y) | Z                →  X | Y | Z
(X) | Y                    →  X | Y
```

**Syntax Conversion** *(disabled by default)*
```python
# Modernization (Python 3.10+)
Union[X, Y]                →  X | Y
Optional[X]                →  X | None

# Backward Compatibility (Python < 3.10)
X | Y                      →  Union[X, Y]
X | None                   →  Optional[X]

# PEP 585 (Python 3.9+)
List[int]                  →  list[int]
Dict[str, int]             →  dict[str, int]

# PEP 695 (Python 3.12+)
MyType: TypeAlias = int    →  type MyType = int
```

**Advanced Inspections** *(disabled by default)*
- Missing `Optional` for parameters with `None` default
- Simplify `Callable[..., Any]` → `Callable`
- Redundant `Generic[T]` in class definitions
- Unbound TypeVar (used only once)
- Protocol methods without type annotations

## Installation

1. Open **Settings/Preferences** → **Plugins** → **Marketplace**
2. Search for **"Python Annotations"**
3. Click **Install**

Or install from [JetBrains Marketplace](https://plugins.jetbrains.com/plugin/12035)

## Supported IDEs

All JetBrains IDEs with Python support:
- PyCharm (Community & Professional)
- IntelliJ IDEA with Python plugin
- Other JetBrains IDEs with Python plugin

**Requires:** IDE version 2025.2+

## Configuration

All inspections can be configured in **Settings** → **Editor** → **Inspections** → **Python Annotations**

- Enable/disable individual inspections
- Change severity levels (Error, Warning, Weak Warning)
- Suppress for specific files or scopes

## Contributing

Contributions are welcome! See [ROADMAP.md](ROADMAP.md) for planned features.

## License

[Apache License 2.0](LICENSE)
