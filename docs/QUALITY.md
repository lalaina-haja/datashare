# Quality

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

## Building

| Target           | Description                                      |
| ---------------- | ------------------------------------------------ |
| `make build-api` | Build the API (`datashare-api`)                  |
| `make build-web` | Build the Web (`datashare-web`)                  |
| `make build-all` | Build both API and Web                           |

## Linting

| Target          | Description                            |
| --------------- | -------------------------------------- |
| `make lint-api` | API lint (Compile / Spotless / Checkstyle) |
| `make lint-web` | Web lint (Angular Lint / Prettier)     |
| `make lint`     | Run all lint checks (API + Web)        |

## Git hooks

Some git hooks is provided in the script folder :

```bash
scripts/git-hooks/
```

| Hook         | It runs          |
| ------------ | ---------------- |
| `pre-commit` | `make link`      |
| `pre-push`   | `make test-unit` |

To install git hooks run:

```bash
make install-hooks
```

To uninstall git hooks run:

```bash
make uninstall-hooks
```
---

### | [⬅ Back to DataShare README](../README.md) |
