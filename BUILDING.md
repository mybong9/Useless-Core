# Building the Forge 1.20.1 source workspace

The four repositories contain separate modules of one Gradle multi-project workspace.
They are not four unrelated standalone Gradle projects. Java 17 is required.

Clone them into one empty parent directory with these exact folder names:

```powershell
git clone https://github.com/mybong9/Useless-Core.git UselessCore
git clone https://github.com/mybong9/Useless-CE-Unofficial.git UselessMod
git clone https://github.com/mybong9/Useless-Stretcher-Unofficial.git UselessStretcher
git clone https://github.com/mybong9/Useless-Generators.git UselessGenerators
Copy-Item -Path ./UselessMod/build-support/workspace/* -Destination . -Recurse
```

Restore the external local build dependencies listed in
`UselessMod/build-support/local-dependencies.json` to their recorded relative paths.
Third-party mod JARs and release JARs are deliberately not committed as source.
These external APIs are needed to compile integrations; this does not make every
optional integration a mandatory runtime dependency.

Then run from the parent workspace, not from inside an individual module:

```powershell
./gradlew.bat :useless_core:reobfJarJar :useless_mod:reobfJar :useless_stretcher:reobfJar :useless_generators:reobfJar
```

Module versions and minimum runtime dependency versions are managed separately.
Original license files, copyright notices and third-party asset notices are retained.
