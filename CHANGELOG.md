# Changelog

- 0.2.0 is standalone on the four active branches. Removed SeamlessLib from builds, loader metadata, CI and install bundles; Fabric still requires Fabric API. This is an explicit product exception to the ecosystem dependency convention.

## 0.2.0+mc1.21.1 (unreleased)

Owned cat and kitten petting, original stretch/knead/groom clips, rare dog digging
with protected terrain checks and datapack loot, world/owner settings, and
server-authoritative tracking. Dogs react to an owner's gaze with a head tilt
and folded ear. Idle cooldowns persist in the world.

Cat eyes preserve the full open-eye texture and draw visible closed lids.
Hand withdrawal finishes the stroke, freezes interrupted render fractions,
uses a steady client clock and blends vanilla swing back smoothly. Kneading
uses small paw lifts with shoulders beneath the coat. Digging keeps supporting
paws grounded and plays the target block's hit sound from the dog on eleven
alternating contacts. Cat idle actions share a 3–6 minute interval.

On 6 October 2026, both paw-to-face washing clips were removed from the
server choices, runtime assets and authoring exporter on every active branch.
Chest cleaning remains. Existing action IDs and world cooldowns are preserved.
Delayed tracking resends keep the current animation clock, and starting petting
blends out an interrupted idle pose over six ticks.
Acceptance is recorded separately for the final standalone JAR hashes.

Petting uses Minecraft's rebindable controls on every loader. Client visuals
now offers a Petting key button showing the current keyboard/mouse binding and
opening Key Binds. Escape unbinds the action and hides its prompt. The settings
key is also registered on 26.2/26.3 NeoForge. The 1.20.1 Forge HUD entrypoint
now renders its contextual prompt correctly.


## 0.1.0+mc1.21.1

Fabric-only MVP: G keybind and target prompt, server-authorized petting,
first/third-person arm stroke, head tilt/tail wag, adult happy/blinking eyes,
the wolf's own entity-bound pant, client settings, and isolated QA harness.
