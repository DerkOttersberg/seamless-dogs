# Animation and asset provenance

This MVP reuses the **Minecraft 26.3 vanilla rig** already installed in the game:
`WolfModel` (adult/baby), `WolfRenderer`, `PlayerModel`, and
`FirstPersonHandsAndItemsRenderer`. Signatures and pivots were inspected from
the actual 26.3 development classes. No third-party rig/animation was imported;
no Minecraft texture or sound file is distributed inside the mod.

The original two-second procedural clip has a six-tick smooth entrance and an
eight-tick smooth exit, with small sinusoidal strokes. The wolf receives an
additive head tilt and tail wag after vanilla resets the model. Each extracted
wolf render state contains its own pose, so rendering one wolf cannot carry the
pose into another. Third-person arm transforms preserve sleeve child poses,
skin visibility, handedness, and the vanilla rest animation.

First person calls vanilla's skin-aware arm renderer with a small translated and
rotated pose. It requires an empty main hand; maps, held items, and item-use
animations do not enter the petting path. It is a stylized clip, with no root
motion, camera takeover, or exact hand-to-head IK.

Adult eyes blink for three ticks every 97 ticks, with a UUID-specific offset.
During petting, a tiny closed-eye smile replaces the eye region of a temporary
copy of the **currently active resource-pack texture**. Fur colors are sampled
from that texture. Derived textures are released and regenerated on resource
reload. Puppy UVs differ in 26.3; the MVP deliberately retains their eyes.

The server sends an entity-bound pant from the wolf's adult/baby sound variant.
The sound travels through Minecraft's normal spatial sound and category-volume
controls. There are no global/UI sound effects or separately licensed samples.

The icon is original pixel art drawn for this project. Project code and icon are
all rights reserved. Minecraft and dependency assets retain their own licenses.
