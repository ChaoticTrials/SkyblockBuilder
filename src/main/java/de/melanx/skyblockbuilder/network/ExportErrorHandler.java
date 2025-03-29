package de.melanx.skyblockbuilder.network;

import de.melanx.skyblockbuilder.SkyblockBuilder;
import de.melanx.skyblockbuilder.util.SkyComponents;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.HandlerThread;
import org.moddingx.libx.network.PacketHandler;
import org.moddingx.libx.screen.text.ComponentLayout;
import org.moddingx.libx.screen.text.TextScreen;

import javax.annotation.Nonnull;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class ExportErrorHandler extends PacketHandler<ExportErrorHandler.Message> {

    public static final CustomPacketPayload.Type<ExportErrorHandler.Message> TYPE = new CustomPacketPayload.Type<>(SkyblockBuilder.getInstance().resource("export_error"));

    protected ExportErrorHandler() {
        super(TYPE, PacketFlow.CLIENTBOUND, ExportErrorHandler.Message.CODEC, HandlerThread.MAIN);
    }

    @Override
    public void handle(Message msg, IPayloadContext ctx) {
        Path configPath = Paths.get(msg.configPath);
        Minecraft.getInstance().setScreen(new TextScreen(ComponentLayout.simple(
                SkyComponents.SCREEN_ERROR_TITLE,
                List.of(Component.literal("Failed to overwrite config " + FMLPaths.GAMEDIR.get().relativize(configPath)), Component.literal(msg.reason))
        ), 230) {

            @Override
            protected void init() {
                super.init();

                this.addRenderableWidget(Button.builder(CommonComponents.GUI_OK, button -> Minecraft.getInstance().setScreen(null))
                        .pos(this.width / 2 - 115, this.height - 27)
                        .width(110)
                        .build());
                this.addRenderableWidget(Button.builder(Component.literal("Open Config"), button -> {
                            Util.getPlatform().openFile(configPath.toFile());
                        })
                        .pos(this.width / 2 + 5, this.height - 27)
                        .width(110)
                        .build());
            }
        });
    }

    public record Message(String configPath, String reason) implements CustomPacketPayload {

        public static final StreamCodec<RegistryFriendlyByteBuf, Message> CODEC = StreamCodec.of(
                (buffer, msg) -> {
                    buffer.writeUtf(msg.configPath);
                    buffer.writeUtf(msg.reason);
                }, buffer -> {
                    return new Message(buffer.readUtf(), buffer.readUtf());
                }
        );

        @Nonnull
        @Override
        public Type<? extends CustomPacketPayload> type() {
            return ExportErrorHandler.TYPE;
        }
    }
}
