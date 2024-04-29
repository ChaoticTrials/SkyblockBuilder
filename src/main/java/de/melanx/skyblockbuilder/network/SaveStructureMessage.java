package de.melanx.skyblockbuilder.network;

import de.melanx.skyblockbuilder.SkyblockBuilder;
import de.melanx.skyblockbuilder.item.ItemStructureSaver;
import de.melanx.skyblockbuilder.util.SkyPaths;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.network.NetworkEvent;
import org.moddingx.libx.network.PacketHandler;
import org.moddingx.libx.network.PacketSerializer;

import java.nio.file.Path;
import java.util.function.Supplier;

public record SaveStructureMessage(ItemStack stack, String name, boolean saveToConfig, boolean ignoreAir, boolean asSnbt, boolean netherValidation) {

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
            String name = ItemStructureSaver.saveSchematic(level, msg.stack, msg.saveToConfig, msg.ignoreAir, msg.asSnbt, msg.netherValidation, msg.name);
            if (name == null) {
                player.displayClientMessage(Component.literal("Failed to save, look at latest.log for more information").withStyle(ChatFormatting.RED), false);
                return true;
            }
            ItemStack stack = ItemStructureSaver.removeTags(msg.stack);
            player.setItemInHand(InteractionHand.MAIN_HAND, stack);
            Path fullPath = msg.saveToConfig ? SkyPaths.MOD_CONFIG.resolve(name) : SkyPaths.MOD_EXPORTS.resolve(name);
            Path savedPath = FMLPaths.GAMEDIR.get().relativize(fullPath);
            MutableComponent component = Component.translatable("skyblockbuilder.schematic.saved", savedPath.toString().replace('\\', '/'));
            SkyblockBuilder.getLogger().info("Saved structure (and spawn points) to: {}", fullPath);
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
            buffer.writeBoolean(msg.saveToConfig);
            buffer.writeBoolean(msg.ignoreAir);
            buffer.writeBoolean(msg.asSnbt);
            buffer.writeBoolean(msg.netherValidation);
        }

        @Override
        public SaveStructureMessage decode(FriendlyByteBuf buffer) {
            return new SaveStructureMessage(buffer.readItem(), buffer.readUtf(Short.MAX_VALUE), buffer.readBoolean(), buffer.readBoolean(), buffer.readBoolean(), buffer.readBoolean());
        }
    }
}
