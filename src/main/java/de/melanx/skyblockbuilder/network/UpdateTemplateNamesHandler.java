package de.melanx.skyblockbuilder.network;

import com.google.common.collect.Lists;
import de.melanx.skyblockbuilder.SkyblockBuilder;
import de.melanx.skyblockbuilder.template.TemplateLoader;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.HandlerThread;
import org.moddingx.libx.network.PacketHandler;

import javax.annotation.Nonnull;
import java.util.List;

public class UpdateTemplateNamesHandler extends PacketHandler<UpdateTemplateNamesHandler.Message> {

    public static final CustomPacketPayload.Type<UpdateTemplateNamesHandler.Message> TYPE = new CustomPacketPayload.Type<>(SkyblockBuilder.getInstance().resource("update_template_names"));

    protected UpdateTemplateNamesHandler() {
        super(TYPE, PacketFlow.CLIENTBOUND, Message.CODEC, HandlerThread.MAIN);
    }

    @Override
    public void handle(Message msg, IPayloadContext ctx) {
        TemplateLoader.updateTemplateNames(msg.names);
    }

    public record Message(List<String> names) implements CustomPacketPayload {

        public static final StreamCodec<RegistryFriendlyByteBuf, UpdateTemplateNamesHandler.Message> CODEC = StreamCodec.of(
                (buffer, msg) -> {
                    buffer.writeVarInt(msg.names.size());
                    msg.names.forEach(buffer::writeUtf);
                },
                buffer -> {
                    List<String> names = Lists.newArrayList();
                    int size = buffer.readVarInt();
                    for (int i = 0; i < size; i++) {
                        names.add(buffer.readUtf());
                    }

                    return new UpdateTemplateNamesHandler.Message(names);
                }
        );

        @Nonnull
        @Override
        public Type<? extends CustomPacketPayload> type() {
            return UpdateTemplateNamesHandler.TYPE;
        }
    }
}
