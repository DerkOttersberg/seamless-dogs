package io.github.derkottersberg.seamlessdogs.client;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.derkottersberg.seamlessdogs.SeamlessDogs;
import net.minecraft.client.KeyMapping;

/** Every loader exposes the same rebindable action in Minecraft Controls. */
public final class DogsKeys {
    public static final KeyMapping PET = new KeyMapping("key.seamlessdogs.pet", InputConstants.KEY_G,
        "key.categories.seamlessdogs");
    private DogsKeys() { }
}
