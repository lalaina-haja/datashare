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

## Git hooks

Some git hooks is provided in the script folder :

```bash
scripts/git-hooks/pre-commit
```

| Hook         | It runs         |
| ------------ | --------------- |
| `pre-commit` | `make link`     |
| `pre-push`   | `make test-all` |

To install git hooks run:

```bash
make install-hooks
```

To uninstall git hooks run:

```bash
make uninstall-hooks
```

⚠️ **TODO:** This page is under construction

---

### | [⬅ Back to DataShare README](../README.md) |
