package de.melanx.skyblockbuilder.network;

import de.melanx.skyblockbuilder.item.ItemStructureSaver;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import org.moddingx.libx.network.PacketHandler;
import org.moddingx.libx.network.PacketSerializer;

import java.util.function.Supplier;

public record SaveStructureMessage(ItemStack stack, String name, boolean ignoreAir, boolean asSnbt) {

    public static class Handler implements PacketHandler<SaveStructureMessage> {

        @Override
        public Target target() {
            return Target.MAIN_THREAD;
        }

        @Override
        public boolean handle(SaveStructureMessage msg, Supplier<NetworkEvent.Context> ctx) {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) {
                return true;
            }

            ServerLevel level = (ServerLevel) player.level();
            String name = ItemStructureSaver.saveSchematic(level, msg.stack, msg.ignoreAir, msg.asSnbt, msg.name);
            ItemStack stack = ItemStructureSaver.removeTags(msg.stack);
            player.setItemInHand(InteractionHand.MAIN_HAND, stack);
            MutableComponent component = Component.translatable("skyblockbuilder.schematic.saved", name);
            player.displayClientMessage(component, true);
            return true;
        }
    }

    public static class Serializer implements PacketSerializer<SaveStructureMessage> {

        @Override
        public Class<SaveStructureMessage> messageClass() {
            return SaveStructureMessage.class;
        }

        @Override
        public void encode(SaveStructureMessage msg, FriendlyByteBuf buffer) {
            buffer.writeItem(msg.stack);
            buffer.writeUtf(msg.name);
            buffer.writeBoolean(msg.ignoreAir);
            buffer.writeBoolean(msg.asSnbt);
        }

        @Override
        public SaveStructureMessage decode(FriendlyByteBuf buffer) {
            return new SaveStructureMessage(buffer.readItem(), buffer.readUtf(Short.MAX_VALUE), buffer.readBoolean(), buffer.readBoolean());
        }
    }
}
