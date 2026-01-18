# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

PyAnnotations is an IntelliJ Platform plugin that provides code inspections with quick fixes for Python type annotations. It helps simplify Union types and Optional types in Python code.

## Build Commands

```bash
# Build the plugin
./gradlew build

# Run tests
./gradlew test

# Run a specific test class
./gradlew test --tests "ClassName"

# Run IDE with plugin installed for testing
./gradlew runIde

# Verify plugin compatibility
./gradlew verifyPlugin

# Build plugin distribution (ZIP)
./gradlew buildPlugin

# Publish to JetBrains Marketplace (requires token.txt)
./gradlew publishPlugin
```

## Architecture

### Plugin Structure
- **Package**: `dev.meanmail` (migrated from `ru.meanmail`)
- **Plugin ID**: `ru.meanmail.plugin.pyannotations` (legacy ID kept for compatibility)
- **Depends on**: `PythonCore` bundled plugin

### Core Components

**Inspections** (`src/main/kotlin/dev/meanmail/codeInspection/`):
- `BaseInspectionVisitor` - Base class extending `PyInspectionVisitor`, handles visiting `PySubscriptionExpression` for Union types
- Each inspection extends `PyInspection` and creates a visitor extending `BaseInspectionVisitor`
- Inspections are registered in `src/main/resources/META-INF/python-config.xml`

**Quick Fixes** (`src/main/kotlin/dev/meanmail/quickfix/`):
- Each quick fix implements the replacement logic for its corresponding inspection
- Quick fixes modify the PSI tree to transform annotations

**Inspection Registration**:
- Main plugin descriptor: `src/main/resources/META-INF/plugin.xml`
- Python-specific extensions: `src/main/resources/META-INF/python-config.xml` (loaded conditionally when Python plugin is available)
- Inspection descriptions: `src/main/resources/inspectionDescriptions/*.html`

### Adding a New Inspection

1. Create inspection class in `codeInspection/` extending `PyInspection`
2. Create inner `Visitor` class extending `BaseInspectionVisitor`
3. Create quick fix class in `quickfix/`
4. Add inspection description HTML in `inspectionDescriptions/`
5. Register inspection in `python-config.xml`

## Configuration

Key settings in `gradle.properties`:
- `platformVersion` - Target IDE version for development
- `platformSinceBuild` - Minimum supported IDE build
- `platformType=PC` - PyCharm Community
- `platformBundledPlugins=PythonCore` - Required bundled plugin

## Important Notes

- Never use `SwingUtilities.invokeLater` - use `ApplicationManager` equivalents instead
- Code must be in English only
- Do not run `gradlew clean` without permission
