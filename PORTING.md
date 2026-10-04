# Architecture and future ports

The local product repository is `seamless-dogs`, version branch `26.3`.
Only `common` and `fabric` are included for the requested MVP. Add another
loader as a sibling module on that version branch after verifying its own
Minecraft APIs and runtime; never advertise a version by widening metadata.

Common Java uses official Minecraft names. `SeamlessDogs` owns server sessions,
validation, cooldown, dog reactions, and lifecycle. `PetRequest` and `PetState`
own the bounded packet format. The separate `DogsClient` bootstrap handles
visual state and client configuration. Render mixins are declared client-only.

Fabric explicitly injects `PlatformServices` and `ClientPlatformServices`.
Networking, key registration, ticks, tracking, disconnect, and Mod Menu are
loader-local. There is no runtime Architectury API, reflection, or ServiceLoader
adapter discovery. Architectury/Loom are build tools only.

The matching SeamlessLib common/Fabric jars are task-backed composite inputs
and runtime dependencies. No API classes are copied or shaded. Its current
visual contracts cover thrown items and trails; this dog clip has no relevant
public API operation, so it does not make an artificial call or change the
shared library.

`qa-client` is a separate packaged-client launcher and test-only harness. It is
excluded from the product build and runtime jar. A loader port should adapt its
event hooks and run the same behavioral scenarios with real packaged jars.
