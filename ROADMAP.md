# PyAnnotations Plugin Roadmap

## Current State (v2026.1)

The plugin provides **21 inspections** for Python type annotation simplification and validation:

### Core Union Inspections
- `UnionWithNoneInspection` — `Union[X, None]` → `Optional[X]`
- `UnionWithObjectInspection` — `Union[X, object]` → `object`
- `UnionWithOneChildToChildInspection` — `Union[X]` → `X`
- `RedundantOptionalInspection` — `Optional[Optional[X]]` → `Optional[X]`
- `AnyInUnionInspection` — `Union[X, Any]` → `Any`
- `DuplicateTypesInUnionInspection` — `Union[int, int, str]` → `Union[int, str]`
- `RedundantUnionInspection` — `Union[int, bool]` → `int`
- `NestedUnionInspection` — `Union[Union[X, Y], Z]` → `Union[X, Y, Z]`
- `EmptyUnionInspection` — detect invalid `Union[]`

### Pipe Syntax Inspections (Python 3.10+)
- `PipeSyntaxWithObjectInspection` — `X | object` → `object`
- `PipeSyntaxWithAnyInspection` — `X | Any` → `Any`
- `RedundantParenthesesInPipeUnionInspection` — `(X) | Y` → `X | Y`
- `NestedPipeUnionInspection` — `(X | Y) | Z` → `X | Y | Z`

### Syntax Conversion (disabled by default)
- `ConvertUnionToModernSyntaxInspection` — `Union[X, Y]` → `X | Y`
- `ConvertOptionalToModernSyntaxInspection` — `Optional[X]` → `X | None`
- `ConvertPipeToUnionInspection` — `X | Y` → `Union[X, Y]`
- `ConvertPipeNoneToOptionalInspection` — `X | None` → `Optional[X]`
- `DeprecatedTypingCollectionInspection` — `List[X]` → `list[X]`
- `ConvertToTypeStatementInspection` — `TypeAlias` → `type` statement

### Advanced Inspections (disabled by default)
- `MissingOptionalInspection` — detect `= None` without `Optional`
- `SimplifyCallableInspection` — `Callable[..., Any]` → `Callable`
- `RedundantGenericInspection` — redundant `Generic[T]` in class
- `UnboundTypeVarInspection` — TypeVar used only once
- `ProtocolMethodAnnotationInspection` — Protocol methods without annotations

---

## Backlog

### Testing
- [ ] Add test infrastructure with IntelliJ test framework
- [ ] Write tests for all inspections and quick fixes
- [ ] Set up CI test coverage reporting

### Advanced Features
- [ ] Handle forward references in string annotations

### User Experience
- [ ] Add settings page for inspection configuration
- [ ] Allow choosing Python version target
- [ ] Option to prefer `Optional[X]` vs `X | None`
- [ ] Batch fix action for entire file/project

### Documentation
- [ ] Improve inspection descriptions with more examples
- [ ] Add "Learn more" links to PEP documentation
- [ ] Create plugin documentation website

### Localization
- [ ] Extract strings to resource bundles
- [ ] Add Russian localization
- [ ] Add Chinese localization

### Tool Integration
- [ ] Integration with mypy error messages
- [ ] Integration with pyright suggestions
- [ ] Support for stub files (.pyi)

### IDE Features
- [ ] Add type annotation intentions (not just inspections)
- [ ] Quick documentation for type patterns
- [ ] Type annotation completion suggestions

---

## Contributing

Contributions are welcome! See [GitHub Issues](https://github.com/meanmail/PyAnnotations/issues) for current tasks.
