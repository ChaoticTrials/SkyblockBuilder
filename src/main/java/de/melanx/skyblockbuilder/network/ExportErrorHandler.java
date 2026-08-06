package de.melanx.skyblockbuilder.network;

import de.melanx.skyblockbuilder.SkyblockBuilder;
import de.melanx.skyblockbuilder.client.ClientUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.HandlerThread;
import org.moddingx.libx.network.PacketHandler;

import javax.annotation.Nonnull;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ExportErrorHandler extends PacketHandler<ExportErrorHandler.Message> {

    public static final CustomPacketPayload.Type<ExportErrorHandler.Message> TYPE = new CustomPacketPayload.Type<>(SkyblockBuilder.getInstance().id("export_error"));

    protected ExportErrorHandler() {
        super(TYPE, PacketFlow.CLIENTBOUND, ExportErrorHandler.Message.CODEC, HandlerThread.MAIN);
    }

    @Override
    public void handle(Message msg, IPayloadContext ctx) {
        Path configPath = Paths.get(msg.configPath);
        ClientUtil.openErrorScreen(configPath, msg.reason);
    }

    public record Message(String configPath, String reason) implements CustomPacketPayload {

        public static final StreamCodec<RegistryFriendlyByteBuf, Message> CODEC = StreamCodec.of(
                (buffer, msg) -> {
                    buffer.writeUtf(msg.configPath);
                    buffer.writeUtf(msg.reason);
                }, buffer -> {
                    return new Message(buffer.readUtf(), buffer.readUtf());
                }
        );

        @Nonnull
        @Override
        public Type<? extends CustomPacketPayload> type() {
            return ExportErrorHandler.TYPE;
        }
    }
}
