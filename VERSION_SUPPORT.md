# Version Support Policy

Redone Storage is intended to support future Minecraft and Refined Storage releases.

## Current release line

- Minecraft 1.21.1
- NeoForge 21.1.234 or newer
- Refined Storage 2.0.9
- Java 21

## Future versioning

Each uploaded JAR will clearly identify its compatible Minecraft version, loader, and Redone Storage version.

Recommended release filename format:

```text
redonestorage-<minecraft>-<mod-version>-<loader>.jar
```

Example:

```text
redonestorage-1.21.1-6.0.0-alpha.6-neoforge.jar
```

## Branches

- `main` should track the newest actively developed Minecraft version.
- Maintenance branches may use names such as `1.21.1` or `mc/1.21.1`.
- A compatibility fix for an older branch should not silently change the Minecraft or Refined Storage target.
- Major ports should receive their own changelog section and GitHub release.

## Support expectations

Not every older Minecraft version can be maintained indefinitely. The README and release page are the source of truth for currently supported versions.
