package de.melanx.skyblockbuilder.network;

import de.melanx.skyblockbuilder.SkyblockBuilder;
import de.melanx.skyblockbuilder.util.DumpUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.HandlerThread;
import org.moddingx.libx.network.PacketHandler;

import java.nio.file.Path;

public class CreateSkyblockDumpHandler extends PacketHandler<CreateSkyblockDumpHandler.Message> {

    public static final CustomPacketPayload.Type<Message> TYPE = new CustomPacketPayload.Type<>(SkyblockBuilder.getInstance().resource("create_skyblock_dump"));

    protected CreateSkyblockDumpHandler() {
        super(TYPE, PacketFlow.SERVERBOUND, Message.CODEC, HandlerThread.MAIN);
    }

    @Override
    public void handle(Message msg, IPayloadContext ctx) {
        if (!(ctx.player() instanceof ServerPlayer player)) {
            return;
        }

        if (!player.hasPermissions(3)) {
            player.sendSystemMessage(Component.translatable("skyblockbuilder.screen.dump.failure").withStyle(ChatFormatting.RED));
            return;
        }

        Path zip = DumpUtil.createZip(msg.includeConfigs, msg.includeTemplates, msg.includeLevelDat, msg.includeLog, msg.includeCrashReport, msg.includeSkyblockBuilderWorldData);
        player.sendSystemMessage(Component.translatable("skyblockbuilder.screen.dump.success", FMLPaths.GAMEDIR.get().relativize(zip)).append(" ").append(Component.translatable("skyblockbuilder.screen.dump.success.server")));
        player.sendSystemMessage(Component.translatable("skyblockbuilder.screen.dump.create_issue").append(" ").append(DumpUtil.getIssueUrl()));
    }

    public record Message(boolean includeConfigs, boolean includeTemplates, boolean includeLevelDat, boolean includeLog,
                          boolean includeCrashReport,
                          boolean includeSkyblockBuilderWorldData) implements CustomPacketPayload {

        public static final StreamCodec<RegistryFriendlyByteBuf, Message> CODEC = StreamCodec.of(
                (buffer, msg) -> {
                    buffer.writeBoolean(msg.includeConfigs);
                    buffer.writeBoolean(msg.includeTemplates);
                    buffer.writeBoolean(msg.includeLevelDat);
                    buffer.writeBoolean(msg.includeLog);
                    buffer.writeBoolean(msg.includeCrashReport);
                    buffer.writeBoolean(msg.includeSkyblockBuilderWorldData);
                },
                buffer -> new Message(buffer.readBoolean(), buffer.readBoolean(), buffer.readBoolean(), buffer.readBoolean(), buffer.readBoolean(), buffer.readBoolean())
        );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return CreateSkyblockDumpHandler.TYPE;
        }
    }
}
