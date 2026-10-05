package io.github.derkottersberg.seamlessdogs.internal;

import java.nio.file.Path;
import net.minecraft.client.KeyMapping;
import io.github.derkottersberg.seamlessdogs.internal.DogsPayload;

public interface ClientPlatformServices {
    Path configDirectory();
    KeyMapping petKey();
    boolean serverSupportsPetting();
    default boolean usesVanillaHud() { return true; }
    void sendToServer(DogsPayload payload);
}
