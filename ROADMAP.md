# PyAnnotations Plugin Roadmap

## Current State (v2026.1)

The plugin provides 5 inspections for Python type annotation simplification:
- `UnionWithNoneInspection` — `Union[X, None]` → `Optional[X]`
- `UnionWithObjectInspection` — `Union[X, object]` → `object`
- `UnionWithOneChildToChildInspection` — `Union[X]` → `X`
- `RedundantOptionalInspection` — `Optional[Optional[X]]` → `Optional[X]`
- `AnyInUnionInspection` — `Union[X, Any]` → `Any`

---

## Phase 1: Quality & Testing

### 1.1 Unit Tests
- [ ] Add test infrastructure with IntelliJ test framework
- [ ] Write tests for all 5 existing inspections
- [ ] Write tests for all 5 quick fixes
- [ ] Add edge case tests (nested types, string annotations, imports)
- [ ] Set up CI test coverage reporting

### 1.2 Code Quality
- [ ] Add integration tests with real Python files
- [x] Improve error handling in quick fixes
- [x] Add logging for debugging

---

## Phase 2: Modern Python Support (PEP 604)

> **Python version reference:**
> - `Optional[X]`, `Union[X, Y]` — Python 3.5+ (classic syntax)
> - `X | Y` in annotations — Python 3.10+ (PEP 604)
> - `X | Y` with `from __future__ import annotations` — Python 3.9+

### 2.1 Simplification for Pipe Syntax (Python 3.10+)
- [ ] `PipeSyntaxWithObjectInspection` — `X | object` → `object`
- [ ] `PipeSyntaxWithAnyInspection` — `X | Any` → `Any`
- [ ] `PipeSyntaxSingleTypeInspection` — `(X)` in union → `X`
- [ ] `NestedPipeUnionInspection` — `(X | Y) | Z` → `X | Y | Z`

### 2.2 Modernization (Python 3.10+)
Inspections to convert classic syntax to modern pipe syntax:
- [ ] `ConvertUnionToModernSyntaxInspection` — `Union[X, Y]` → `X | Y`
- [ ] `ConvertOptionalToModernSyntaxInspection` — `Optional[X]` → `X | None`

### 2.3 Backward Compatibility (Python < 3.10)
Inspections to convert modern syntax to classic (for projects targeting older Python):
- [ ] `ConvertPipeToUnionInspection` — `X | Y` → `Union[X, Y]`
- [ ] `ConvertPipeNoneToOptionalInspection` — `X | None` → `Optional[X]`

---

## Phase 3: Additional Type Inspections

### 3.1 Redundancy Detection
- [ ] `DuplicateTypesInUnionInspection` — `Union[int, int, str]` → `Union[int, str]`
- [ ] `RedundantUnionInspection` — `Union[int, bool]` → `int` (bool is subtype of int)
- [ ] `NestedUnionInspection` — `Union[Union[X, Y], Z]` → `Union[X, Y, Z]`
- [ ] `EmptyUnionInspection` — detect `Union[]` or `Union[()]`

### 3.2 Optional Patterns
- [ ] `OptionalWithNoneDefaultInspection` — suggest `Optional` when default is `None`
- [ ] `MissingOptionalInspection` — detect `= None` without `Optional` in annotation

### 3.3 Collection Type Inspections (Python 3.9+)
Generic built-in types available since Python 3.9 (PEP 585):
- [ ] `DeprecatedTypingListInspection` — `typing.List[X]` → `list[X]`
- [ ] `DeprecatedTypingDictInspection` — `typing.Dict[K, V]` → `dict[K, V]`
- [ ] `DeprecatedTypingSetInspection` — `typing.Set[X]` → `set[X]`
- [ ] `DeprecatedTypingTupleInspection` — `typing.Tuple[X, Y]` → `tuple[X, Y]`

---

## Phase 4: Advanced Features

### 4.1 Type Alias Support
- [ ] Detect issues in `TypeAlias` definitions (Python 3.10+)
- [ ] Support `type` statement (Python 3.12+, PEP 695)
- [ ] Handle forward references in string annotations

### 4.2 Callable & Protocol
- [ ] `SimplifyCallableInspection` — detect redundant Callable patterns
- [ ] `ProtocolMethodAnnotationInspection` — check Protocol method signatures

### 4.3 Generic Types
- [ ] `UnboundTypeVarInspection` — detect TypeVar used without binding
- [ ] `RedundantGenericInspection` — `Generic[T]` when not needed

---

## Phase 5: User Experience

### 5.1 Configuration
- [ ] Add settings page for inspection configuration
- [ ] Allow choosing Python version target
- [ ] Option to prefer `Optional[X]` vs `X | None`
- [ ] Batch fix action for entire file/project

### 5.2 Documentation
- [ ] Improve inspection descriptions with more examples
- [ ] Add "Learn more" links to PEP documentation
- [ ] Create plugin documentation website

### 5.3 Localization
- [ ] Extract strings to resource bundles
- [ ] Add Russian localization
- [ ] Add Chinese localization

---

## Phase 6: Integration & Ecosystem

### 6.1 Tool Integration
- [ ] Integration with mypy error messages
- [ ] Integration with pyright suggestions
- [ ] Support for stub files (.pyi)

### 6.2 IDE Features
- [ ] Add type annotation intentions (not just inspections)
- [ ] Quick documentation for type patterns
- [ ] Type annotation completion suggestions

---

## Version Planning

| Version | Target | Focus |
|---------|--------|-------|
| 2026.2 | Q2 2026 | Phase 1 (Testing) |
| 2026.3 | Q3 2026 | Phase 2 (PEP 604) |
| 2026.4 | Q4 2026 | Phase 3 (New Inspections) |
| 2027.1 | Q1 2027 | Phase 4 (Advanced) |
| 2027.2 | Q2 2027 | Phase 5-6 (UX & Integration) |

---

## Contributing

Contributions are welcome! Priority areas:
1. Unit tests for existing functionality
2. PEP 604 syntax support
3. New inspection implementations

See [GitHub Issues](https://github.com/meanmail/PyAnnotations/issues) for current tasks.
