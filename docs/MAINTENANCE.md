# Maintenance

## Version Management

Current version stored in `VERSION` file.

Show version:
```bash
make version
```

Bump version (uses `scripts/bump-version.sh`):
```bash
make bump-version TYPE=patch
make bump-version TYPE=minor
make bump-version TYPE=major
```

Sync VERSION -> datashare-web/package.json
```bash
make sync-version
```

## Building

| Target           | Description                                      |
| ---------------- | ------------------------------------------------ |
| `make build-api` | Build the API (`datashare-api`)                  |
| `make build-web` | Build the Web (`datashare-web`) production build |
| `make build-all` | Build both API and Web                           |

## Linting
| Target          | Description                            |
| --------------- | -------------------------------------- |
| `make lint-api` | API lint (Checkstyle / SpotBugs / PMD) |
| `make lint-web` | Web lint (ESLint / Angular)            |
| `make lint`     | Run all lint checks (API + Web)        |

## Pre-commit hooks
A pre-commit hook is provided:
```bash
scripts/git-hooks/pre-commit
```
It runs: 
- `make test-all`

Install it:
```bash
ln -s ../../scripts/git-hooks/pre-commit .git/hooks/pre-commit

or

git config core.hooksPath scripts/git-hooks
```
⚠️ **TODO:** This page is under construction

---
### | [⬅ Back to DataShare README](../README.md) |