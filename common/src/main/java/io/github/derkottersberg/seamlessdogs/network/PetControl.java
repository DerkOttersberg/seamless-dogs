package io.github.derkottersberg.seamlessdogs.network;

import io.github.derkottersberg.seamlessdogs.SeamlessDogs;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** HELLO/READ=0, own digging preference=1, administrative rules=2. */
public record PetControl(int operation, int flags, long revision) implements CustomPacketPayload {
    public static final Type<PetControl> TYPE = new Type<>(SeamlessDogs.id("pet_control_v2"));
    public static final StreamCodec<RegistryFriendlyByteBuf, PetControl> CODEC = StreamCodec.of(
        (b,p) -> { b.writeByte(p.operation); b.writeByte(p.flags); b.writeLong(p.revision); },
        b -> new PetControl(b.readUnsignedByte(), b.readUnsignedByte(), b.readLong()));
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
