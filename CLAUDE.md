# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

```bash
./gradlew build              # Build the plugin
./gradlew test               # Run tests
./gradlew test --tests "ClassName"  # Run specific test
./gradlew runIde             # Run IDE with plugin for testing
./gradlew verifyPlugin       # Verify plugin compatibility
./gradlew buildPlugin        # Build plugin ZIP distribution
./gradlew publishPlugin      # Publish to Marketplace (requires token.txt)
```

## Architecture

PyAnnotations is an IntelliJ Platform plugin providing code inspections with quick fixes for Python type annotations (Union, Optional, pipe syntax).

### Two Base Visitor Classes

**`BaseInspectionVisitor`** — For `Union[...]` and `Optional[...]` syntax:
- Extends `PyInspectionVisitor`
- Visits `PySubscriptionExpression` nodes
- Override `visitPyAnnotationUnionExpression(node, items)` for Union with multiple types
- Override `visitPyAnnotationUnionWithOneChildExpression(node, item)` for Union with single type
- Use `hasChildren(node, text)` to check for specific types in Union

**`BasePipeUnionVisitor`** — For pipe syntax `X | Y` (Python 3.10+):
- Extends `PyInspectionVisitor`
- Visits `PyBinaryExpression` with `|` operator in annotation context only
- Override `visitPipeUnionExpression(node)` to handle pipe unions
- Use `collectPipeUnionTypes(node)` to get all types in chain (`X | Y | Z` → `[X, Y, Z]`)
- Use `hasType(node, "None", "Any")` to check for specific types

### Adding a New Inspection

1. Create inspection class in `codeInspection/` extending `PyInspection`
2. Create inner `Visitor` class extending appropriate base visitor
3. Create quick fix class in `quickfix/` implementing `LocalQuickFix`
4. Add HTML description in `inspectionDescriptions/`
5. Register in `python-config.xml` with `enabledByDefault`, `level`, etc.

### Key Files

- `plugin.xml` — Main plugin descriptor
- `python-config.xml` — All inspection registrations (loaded when Python plugin available)
- `gradle.properties` — Platform version, plugin metadata

## Important Notes

- Never use `SwingUtilities.invokeLater` — use `ApplicationManager` equivalents
- Code must be in English only
- Do not run `gradlew clean` without permission
- Keep `ROADMAP.md` up to date when completing features
