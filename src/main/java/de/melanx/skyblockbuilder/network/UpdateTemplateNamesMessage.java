package de.melanx.skyblockbuilder.network;

import com.google.common.collect.Lists;
import de.melanx.skyblockbuilder.template.TemplateLoader;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import org.moddingx.libx.network.PacketHandler;
import org.moddingx.libx.network.PacketSerializer;

import java.util.List;
import java.util.function.Supplier;

public record UpdateTemplateNamesMessage(List<String> names) {

    public static class Handler implements PacketHandler<UpdateTemplateNamesMessage> {

        @Override
        public Target target() {
            return Target.MAIN_THREAD;
        }

        @Override
        public boolean handle(UpdateTemplateNamesMessage msg, Supplier<NetworkEvent.Context> ctx) {
            TemplateLoader.updateTemplateNames(msg.names);
            return true;
        }
    }

    public static class Serializer implements PacketSerializer<UpdateTemplateNamesMessage> {

        @Override
        public Class<UpdateTemplateNamesMessage> messageClass() {
            return UpdateTemplateNamesMessage.class;
        }

        @Override
        public void encode(UpdateTemplateNamesMessage msg, FriendlyByteBuf buffer) {
            buffer.writeVarInt(msg.names.size());
            msg.names.forEach(buffer::writeUtf);
        }

        @Override
        public UpdateTemplateNamesMessage decode(FriendlyByteBuf buffer) {
            List<String> names = Lists.newArrayList();
            int size = buffer.readVarInt();
            for (int i = 0; i < size; i++) {
                names.add(buffer.readUtf());
            }

            return new UpdateTemplateNamesMessage(names);
        }
    }
}
