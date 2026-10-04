package io.github.derkottersberg.seamlessdogs.internal;

import java.nio.file.Path;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public interface ClientPlatformServices {
    Path configDirectory();
    KeyMapping petKey();
    boolean serverSupportsPetting();
    void sendToServer(CustomPacketPayload payload);
}
