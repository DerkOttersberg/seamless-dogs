# Animation and asset provenance — 0.2.0

The original clips are authored as keyframes on each version's vanilla wolf,
cat and player rigs. Adult/baby retargeting is native to the branch. No foreign
animation, replacement rig, Minecraft texture or audio file is distributed.
Blockbench-style authoring sources are in animation-source. Grooming only
cleans the chest: both paws stay grounded. Paw-to-face washing clips were
removed at the owner's request on 6 October 2026. Historical snapshots remain
source-only and are never included as runtime animations.

Vanilla animation prepares each rig before the additive action pose. Legacy
models reset part poses before that vanilla preparation to prevent accumulation.
Every clip blends back to vanilla on completion or interruption. Hand withdrawal
finishes the stroke and uses a steady client clock, with frozen fractional
frames on cancellation and a gradual return of vanilla swing.

Eye expressions derive from the currently active vanilla-layout resource-pack
texture, preserving fur, iris and collar colors. Modern adult/baby UV layouts
and legacy shared layouts use native pixel formats. Closed lids cover the whole
iris; open eyes use the original texture. Unsupported dimensions fall back.
Custom UV layouts can disable eyes in settings. Derived textures are released
and rebuilt on resource reload.

Purring, a stretch voice, panting and the target block's eleven alternating
digging scrapes originate from the pet through native entity-bound sound paths.
Modern baby/adult variants and resource-pack sound replacements are respected.
Background tests use a silent audio device and verify the entity and sound IDs;
audible playback requires an interactive check.

The icon, clips and product code are All Rights Reserved. This product does not
include or require SeamlessLib. Game and optional integration assets retain
their own licenses. Motion is stylized on Minecraft's block rigs; hand contact
is not guaranteed at every viewing angle or distance.
