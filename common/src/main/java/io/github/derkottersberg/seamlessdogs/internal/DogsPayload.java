package io.github.derkottersberg.seamlessdogs.internal;
import net.minecraft.resources.ResourceLocation;
public interface DogsPayload {
    record Type<T extends DogsPayload>(ResourceLocation id) { }
    Type<? extends DogsPayload> type();
}
