package io.github.derkottersberg.seamlessdogs.internal;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import io.github.derkottersberg.seamlessdogs.internal.DogsPayload;

/** Loader entrypoints provide an explicit adapter; common never discovers one. */
public interface PlatformServices {
    void sendToPlayer(ServerPlayer player, DogsPayload payload);
    void sendToTrackingAndSelf(ServerPlayer player, Entity dog, DogsPayload payload);
}
