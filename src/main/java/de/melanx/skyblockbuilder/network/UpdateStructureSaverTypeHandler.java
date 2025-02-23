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

public class UpdateStructureSaverTypeHandler extends PacketHandler<UpdateStructureSaverTypeHandler.Message> {

    public static final CustomPacketPayload.Type<UpdateStructureSaverTypeHandler.Message> TYPE = new CustomPacketPayload.Type<>(SkyblockBuilder.getInstance().resource("update_structure_saver_type"));

    protected UpdateStructureSaverTypeHandler() {
        super(TYPE, PacketFlow.SERVERBOUND, Message.CODEC, HandlerThread.MAIN);
    }

    @Override
    public void handle(Message msg, IPayloadContext ctx) {
        if (ctx.player() instanceof ServerPlayer player) {
            player.getMainHandItem().set(ModDataComponentTypes.structureSaverType, msg.screenType);
        }
    }

    public record Message(ItemStack stack, StructureSaverSettings.Type screenType) implements CustomPacketPayload {

        public static final StreamCodec<RegistryFriendlyByteBuf, UpdateStructureSaverTypeHandler.Message> CODEC = StreamCodec.of(
                ((buffer, msg) -> {
                    ItemStack.STREAM_CODEC.encode(buffer, msg.stack);
                    buffer.writeEnum(msg.screenType);
                }), buffer -> {
                    ItemStack stack = ItemStack.STREAM_CODEC.decode(buffer);
                    StructureSaverSettings.Type type = buffer.readEnum(StructureSaverSettings.Type.class);
                    return new Message(stack, type);
                }
        );

        @Nonnull
        @Override
        public Type<? extends CustomPacketPayload> type() {
            return UpdateStructureSaverTypeHandler.TYPE;
        }
    }
}
