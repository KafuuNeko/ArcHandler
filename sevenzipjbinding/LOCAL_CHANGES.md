# ArcHandler integration notes

This module vendors `7-Zip-JBinding-4Android` from tag `Release-16.02-2.03`
(commit `875f38aac441f41e6eb693177e020e97971dca97`). The upstream license and
notices are retained in this directory and under `src/main/cpp`.

ArcHandler-specific changes:

- The Android library build is aligned with the app's SDK, NDK, CMake, Java,
  and ABI configuration.
- Native JUnit hooks are opt-in and excluded from normal debug builds, so the
  library does not try to load instrumentation-only classes in applications.
- Writable 7z and Zip archives expose a typed compression-method API.
- Compression method, level, header encryption, thread count, and solid-block
  settings are applied in one native `ISetProperties` call. The 7-Zip handlers
  reset their output configuration for each call, so applying these settings
  atomically prevents one option from silently overriding another.

Build the module with:

```shell
./gradlew :sevenzipjbinding:assembleDebug
```
