package de.melanx.skyblockbuilder.network;

import de.melanx.skyblockbuilder.SkyblockBuilder;
import de.melanx.skyblockbuilder.item.StructureSaverSettings;
import de.melanx.skyblockbuilder.registration.ModDataComponentTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.HandlerThread;
import org.moddingx.libx.network.PacketHandler;

import javax.annotation.Nonnull;

public class UpdateStructureSaverSettingsHandler extends PacketHandler<UpdateStructureSaverSettingsHandler.Message> {

    public static final CustomPacketPayload.Type<Message> TYPE = new CustomPacketPayload.Type<>(SkyblockBuilder.getInstance().resource("update_structure_saver_settings"));

    protected UpdateStructureSaverSettingsHandler() {
        super(TYPE, PacketFlow.SERVERBOUND, Message.CODEC, HandlerThread.MAIN);
    }

    @Override
    public void handle(UpdateStructureSaverSettingsHandler.Message msg, IPayloadContext ctx) {
        if (ctx.player() instanceof ServerPlayer player) {
            player.getMainHandItem().set(ModDataComponentTypes.structureSaverSettings, msg.settings);
        }
    }

    public record Message(ItemStack stack, StructureSaverSettings settings) implements CustomPacketPayload {

        public static final StreamCodec<RegistryFriendlyByteBuf, UpdateStructureSaverSettingsHandler.Message> CODEC = StreamCodec.of(
                ((buffer, msg) -> {
                    ItemStack.STREAM_CODEC.encode(buffer, msg.stack);
                    StructureSaverSettings.STREAM_CODEC.encode(buffer, msg.settings);
                }), buffer -> {
                    ItemStack stack = ItemStack.STREAM_CODEC.decode(buffer);
                    StructureSaverSettings settings = StructureSaverSettings.STREAM_CODEC.decode(buffer);
                    return new UpdateStructureSaverSettingsHandler.Message(stack, settings);
                }
        );

        @Nonnull
        @Override
        public Type<? extends CustomPacketPayload> type() {
            return UpdateStructureSaverSettingsHandler.TYPE;
        }
    }
}
