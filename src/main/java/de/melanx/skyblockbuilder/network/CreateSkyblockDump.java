package de.melanx.skyblockbuilder.network;

import de.melanx.skyblockbuilder.util.DumpUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.network.NetworkEvent;
import org.moddingx.libx.network.PacketHandler;
import org.moddingx.libx.network.PacketSerializer;

import java.nio.file.Path;
import java.util.function.Supplier;

public record CreateSkyblockDump(boolean includeConfigs, boolean includeTemplates, boolean includeLevelDat, boolean includeLog, boolean includeCrashReport, boolean includeSkyblockBuilderWorldData) {

    public static class Handler implements PacketHandler<CreateSkyblockDump> {

        @Override
        public Target target() {
            return Target.MAIN_THREAD;
        }

        @Override
        public boolean handle(CreateSkyblockDump msg, Supplier<NetworkEvent.Context> ctx) {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) {
                return true;
            }

            if (!player.hasPermissions(3)) {
                player.sendSystemMessage(Component.translatable("skyblockbuilder.screen.dump.failure").withStyle(ChatFormatting.RED));
                return true;
            }

            Path zip = DumpUtil.createZip(msg.includeConfigs, msg.includeTemplates, msg.includeLevelDat, msg.includeLog, msg.includeCrashReport, msg.includeSkyblockBuilderWorldData);
            player.sendSystemMessage(Component.translatable("skyblockbuilder.screen.dump.success", FMLPaths.GAMEDIR.get().relativize(zip)).append(" ").append(Component.translatable("skyblockbuilder.screen.dump.success.server")));
            player.sendSystemMessage(Component.translatable("skyblockbuilder.screen.dump.create_issue").append(" ").append(DumpUtil.getIssueUrl()));
            return true;
        }
    }

    public static class Serializer implements PacketSerializer<CreateSkyblockDump> {

        @Override
        public Class<CreateSkyblockDump> messageClass() {
            return CreateSkyblockDump.class;
        }

        @Override
        public void encode(CreateSkyblockDump msg, FriendlyByteBuf buffer) {
            buffer.writeBoolean(msg.includeConfigs);
            buffer.writeBoolean(msg.includeTemplates);
            buffer.writeBoolean(msg.includeLevelDat);
            buffer.writeBoolean(msg.includeLog);
            buffer.writeBoolean(msg.includeCrashReport);
            buffer.writeBoolean(msg.includeSkyblockBuilderWorldData);
        }

        @Override
        public CreateSkyblockDump decode(FriendlyByteBuf buffer) {
            return new CreateSkyblockDump(buffer.readBoolean(), buffer.readBoolean(), buffer.readBoolean(), buffer.readBoolean(), buffer.readBoolean(), buffer.readBoolean());
        }
    }
}
