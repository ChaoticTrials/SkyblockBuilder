package de.melanx.skyblockbuilder.network;

import com.google.common.collect.Lists;
import de.melanx.skyblockbuilder.template.TemplateLoader;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import org.moddingx.libx.network.PacketSerializer;

import java.util.List;
import java.util.function.Supplier;

public class UpdateTemplateNamesHandler {

    public static void handle(UpdateTemplateNamesHandler.Message msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> TemplateLoader.updateTemplateNames(msg.names));
        ctx.get().setPacketHandled(true);
    }

    public static class Serializer implements PacketSerializer<UpdateTemplateNamesHandler.Message> {

        @Override
        public Class<Message> messageClass() {
            return Message.class;
        }

        @Override
        public void encode(Message msg, FriendlyByteBuf buffer) {
            buffer.writeVarInt(msg.names.size());
            msg.names.forEach(buffer::writeUtf);
        }

        @Override
        public Message decode(FriendlyByteBuf buffer) {
            List<String> names = Lists.newArrayList();
            int size = buffer.readVarInt();
            for (int i = 0; i < size; i++) {
                names.add(buffer.readUtf());
            }

            return new Message(names);
        }
    }

    public record Message(List<String> names) {

    }
}
