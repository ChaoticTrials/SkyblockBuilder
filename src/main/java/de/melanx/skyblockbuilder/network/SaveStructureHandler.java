package de.melanx.skyblockbuilder.network;

import de.melanx.skyblockbuilder.SkyblockBuilder;
import de.melanx.skyblockbuilder.item.ItemStructureSaver;
import de.melanx.skyblockbuilder.util.SkyPaths;
import net.minecraft.ChatFormatting;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.HandlerThread;
import org.moddingx.libx.network.PacketHandler;

import javax.annotation.Nonnull;
import java.nio.file.Path;

public class SaveStructureHandler extends PacketHandler<SaveStructureHandler.Message> {

    public static final CustomPacketPayload.Type<Message> TYPE = new CustomPacketPayload.Type<>(SkyblockBuilder.getInstance().resource("save_structure"));

    protected SaveStructureHandler() {
        super(TYPE, PacketFlow.SERVERBOUND, Message.CODEC, HandlerThread.MAIN);
    }

    @Override
    public void handle(Message msg, IPayloadContext ctx) {
        if (!(ctx.player() instanceof ServerPlayer player)) {
            return;
        }

        ServerLevel level = (ServerLevel) player.level();
        String name = ItemStructureSaver.saveSchematic(level, msg.stack, msg.saveToConfig, msg.ignoreAir, msg.asSnbt, msg.netherValidation, msg.name);
        if (name == null) {
            player.displayClientMessage(Component.literal("Failed to save, look at latest.log for more information").withStyle(ChatFormatting.RED), false);
            return;
        }
        ItemStack stack = ItemStructureSaver.removeTags(msg.stack);
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        Path fullPath = msg.saveToConfig ? SkyPaths.MOD_CONFIG.resolve(name) : SkyPaths.MOD_EXPORTS.resolve(name);
        Path savedPath = FMLPaths.GAMEDIR.get().relativize(fullPath);
        MutableComponent component = Component.translatable("skyblockbuilder.schematic.saved", savedPath.toString().replace('\\', '/'));
        SkyblockBuilder.getLogger().info("Saved structure (and spawn points) to: {}", fullPath);
        player.displayClientMessage(component, true);
    }

    public record Message(ItemStack stack, String name, boolean saveToConfig, boolean ignoreAir, boolean asSnbt,
                          boolean netherValidation) implements CustomPacketPayload {

        public static final StreamCodec<RegistryFriendlyByteBuf, SaveStructureHandler.Message> CODEC = StreamCodec.of(
                (buffer, msg) -> {
                    ItemStack.STREAM_CODEC.encode(buffer, msg.stack);
                    buffer.writeUtf(msg.name);
                    buffer.writeBoolean(msg.saveToConfig);
                    buffer.writeBoolean(msg.ignoreAir);
                    buffer.writeBoolean(msg.asSnbt);
                    buffer.writeBoolean(msg.netherValidation);
                },
                buffer -> new Message(ItemStack.STREAM_CODEC.decode(buffer), buffer.readUtf(Short.MAX_VALUE), buffer.readBoolean(), buffer.readBoolean(), buffer.readBoolean(), buffer.readBoolean())
        );

        @Nonnull
        @Override
        public Type<? extends CustomPacketPayload> type() {
            return SaveStructureHandler.TYPE;
        }
    }
}
