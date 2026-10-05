package io.github.derkottersberg.seamlessdogs.internal;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Loader entrypoints provide an explicit adapter; common never discovers one. */
public interface PlatformServices {
    void sendToPlayer(ServerPlayer player, CustomPacketPayload payload);
    void sendToTrackingAndSelf(ServerPlayer player, Entity dog, CustomPacketPayload payload);
}
