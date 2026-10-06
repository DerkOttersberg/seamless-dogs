package io.github.derkottersberg.seamlessdogs.network;

import io.github.derkottersberg.seamlessdogs.SeamlessDogs;
import java.util.UUID;
import net.minecraft.network.FriendlyByteBuf;

import io.github.derkottersberg.seamlessdogs.internal.DogsPayload;

/** 0 cancels; 5 is settings. GROOM uses chest cleaning (flag 2); flags 0/1 remain reserved for older peers.
 * Extended clips 6..8 require the HELLO expressive capability; older clients render variant zero. */
public record PetUpdate(UUID owner, UUID pet, int action, int elapsed, long sequence,
                        int flags, long revision, String status) implements DogsPayload {
    public static final UUID NONE = new UUID(0,0);
    public static final Type<PetUpdate> TYPE = new Type<>(SeamlessDogs.id("pet_update_v2"));
    public static final PacketCodec<PetUpdate> CODEC = PacketCodec.of(
        (b,p) -> { b.writeUUID(p.owner); b.writeUUID(p.pet); b.writeByte(p.action); b.writeByte(p.elapsed);
            b.writeLong(p.sequence); b.writeByte(p.flags); b.writeLong(p.revision); b.writeUtf(p.status, 192); },
        b -> new PetUpdate(b.readUUID(), b.readUUID(), b.readUnsignedByte(), b.readUnsignedByte(),
            b.readLong(), b.readUnsignedByte(), b.readLong(), b.readUtf(192)));
    public PetUpdate {
        if (action < 0 || action > 8 || elapsed < 0 || elapsed > 120 || status.length() > 192)
            throw new IllegalArgumentException("Invalid pet update");
        if(sequence<0||revision<0||flags<0||flags>63||(action==7&&flags>2)||(action!=5&&action!=7&&flags!=0))
            throw new IllegalArgumentException("Invalid pet update metadata");
    }
    public Type<? extends DogsPayload> type() { return TYPE; }
}
