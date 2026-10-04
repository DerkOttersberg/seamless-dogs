package io.github.derkottersberg.seamlessdogs.network;
import net.minecraft.network.FriendlyByteBuf;
import java.util.function.BiConsumer;
import java.util.function.Function;
public record PacketCodec<T>(BiConsumer<FriendlyByteBuf,T> encoder, Function<FriendlyByteBuf,T> decoder) {
    public void encode(FriendlyByteBuf buffer, T value) { encoder.accept(buffer, value); }
    public T decode(FriendlyByteBuf buffer) { return decoder.apply(buffer); }
    public static <T> PacketCodec<T> of(BiConsumer<FriendlyByteBuf,T> encoder, Function<FriendlyByteBuf,T> decoder) {
        return new PacketCodec<>(encoder, decoder);
    }
}
