# Release

Manage releases in an organized way in a monorepo

Put the `make` folder on PATH.

```bash
export PATH="$PATH:/path/to/this/project/make"
```

Change versions in the `model/gradle.properties` and `world/gradle.properties` to the next SNAPSHOT versions.

Then release:

```bash
release <here-goes-the-previous-version-tag> <here-goes-the-current-version-tag>
```
