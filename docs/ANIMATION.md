# Animation and asset provenance

The clip is original procedural motion on each Minecraft version's vanilla wolf
and player rigs. No third-party animation, replacement model, GeckoLib, Minecraft
texture, or sound file is distributed. The small additive pose is applied after
vanilla prepares the rig and fades over a six-tick entrance/eight-tick exit.
It tilts the head, wags the tail and strokes the player's main arm for two seconds.
First person calls vanilla's skin-aware arm renderer. Both handedness settings
are supported. Motion is stylized; hand contact is not guaranteed at every angle
or distance. There is no camera takeover, root motion, or inverse kinematics.

Idle eyes blink for three ticks every 97 ticks with a UUID offset. Petting adds
closed-eye smiles. Adult and puppy eye regions derive from the currently active
resource-pack texture and preserve sampled fur colors. Newer 32x32 puppy UVs
and the older adult-layout puppy skin are handled by their matching version
branches. Integer-scaled vanilla UV layouts are supported. Unsupported dimensions
fall back to the original texture; a pack with custom UVs can disable expressions
in settings. Temporary textures are released and regenerated on resource reload.

A real entity-bound wolf pant packet follows the dog's spatial position. Newer
versions preserve the adult/baby sound variant; older versions use vanilla wolf
pant with puppy pitch. Minecraft's neutral-sound volume controls apply.
Background tests use a silent output device and verify the real packet/entity ID.

The icon is original pixel art. Product code and original icon are all rights
reserved. SeamlessLib remains MIT; game and dependency assets retain their licenses.
