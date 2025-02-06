package de.melanx.skyblockbuilder.client.screens;

import de.melanx.skyblockbuilder.SkyblockBuilder;
import de.melanx.skyblockbuilder.client.SizeableCheckbox;
import de.melanx.skyblockbuilder.util.DumpUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.MinecraftServer;
import net.neoforged.fml.loading.FMLPaths;
import org.moddingx.libx.render.RenderHelper;

import javax.annotation.Nonnull;
import java.awt.Color;
import java.nio.file.Path;

public class DumpScreen extends BaseScreen {

    private static final int CHECKBOX_X = 10;
    private static final int CHECKBOX_FIRST_Y = 25;
    private static final int TEXT_X = CHECKBOX_X + 17;
    private static final int TEXT_FIRST_Y = CHECKBOX_FIRST_Y + 2;
    private static final int ROW_OFFSET = 15;
    public boolean isOpPlayer = DumpScreen.isOpPlayer();
    public MinecraftServer server;
    public SizeableCheckbox includeConfigs;
    public SizeableCheckbox includeTemplates;
    public SizeableCheckbox includeLevelDat;
    public SizeableCheckbox includeLog;
    public SizeableCheckbox includeCrashReport;
    public SizeableCheckbox includeSkyblockBuilderWorldData;
    public SizeableCheckbox generateOnServer;

    public DumpScreen() {
        super(174, DumpScreen.isOpPlayer() ? 172 : 172 - ROW_OFFSET, Component.translatable("skyblockbuilder.screen.dump.title"));
    }

    @Override
    protected void init() {
        super.init();
        int i = 1;
        this.includeConfigs = this.addRenderableWidget(new SizeableCheckbox(this.x(CHECKBOX_X), this.y(CHECKBOX_FIRST_Y), 10, true));
        this.includeTemplates = this.addRenderableWidget(new SizeableCheckbox(this.x(CHECKBOX_X), this.y(CHECKBOX_FIRST_Y + ROW_OFFSET * i++), 10, true));
        this.includeLevelDat = this.addRenderableWidget(new SizeableCheckbox(this.x(CHECKBOX_X), this.y(CHECKBOX_FIRST_Y + ROW_OFFSET * i++), 10, true));
        this.includeLog = this.addRenderableWidget(new SizeableCheckbox(this.x(CHECKBOX_X), this.y(CHECKBOX_FIRST_Y + ROW_OFFSET * i++), 10, true));
        this.includeCrashReport = this.addRenderableWidget(new SizeableCheckbox(this.x(CHECKBOX_X), this.y(CHECKBOX_FIRST_Y + ROW_OFFSET * i++), 10, true));
        this.includeSkyblockBuilderWorldData = this.addRenderableWidget(new SizeableCheckbox(this.x(CHECKBOX_X), this.y(CHECKBOX_FIRST_Y + ROW_OFFSET * i++), 10, true));
        this.generateOnServer = this.addRenderableWidget(new SizeableCheckbox(this.x(CHECKBOX_X), this.y(CHECKBOX_FIRST_Y + ROW_OFFSET * i), 10, false));
        this.generateOnServer.visible = this.isOpPlayer;
        this.addRenderableWidget(Button.builder(Component.translatable("skyblockbuilder.screen.dump.button.create"), button -> {
                    if (this.generateOnServer.selected()) {
                        SkyblockBuilder.getNetwork().createSkyblockDump(
                                this.includeConfigs.selected(),
                                this.includeTemplates.selected(),
                                this.includeLevelDat.selected(),
                                this.includeLog.selected(),
                                this.includeCrashReport.selected(),
                                this.includeSkyblockBuilderWorldData.selected()
                        );
                    } else {
                        Path zip = DumpUtil.createZip(
                                this.includeConfigs.selected(),
                                this.includeTemplates.selected(),
                                this.includeLevelDat.selected(),
                                this.includeLog.selected(),
                                this.includeCrashReport.selected(),
                                this.includeSkyblockBuilderWorldData.selected()
                        );
                        //noinspection DataFlowIssue
                        Minecraft.getInstance().player.sendSystemMessage(Component.translatable("skyblockbuilder.screen.dump.success", FMLPaths.GAMEDIR.get().relativize(zip).toString())
                                .withStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, zip.getParent().toString()))));
                        Minecraft.getInstance().player.sendSystemMessage(Component.translatable("skyblockbuilder.screen.dump.create_issue").append(" ").append(DumpUtil.getIssueUrl()));
                    }
                    this.onClose();
                })
                .bounds(this.centeredX(Button.SMALL_WIDTH), this.y(this.isOpPlayer ? 137 : 137 - ROW_OFFSET), Button.SMALL_WIDTH, Button.DEFAULT_HEIGHT)
                .build());
    }

    @Override
    public void renderBackground(@Nonnull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(guiGraphics, mouseX, mouseY, partialTick);

        RenderHelper.renderGuiBackground(guiGraphics, this.x(0), this.y(0), this.getXSize(), this.getYSize());
        guiGraphics.drawString(this.font, this.title, this.centeredX(this.font.width(this.title)), this.y(8), Color.DARK_GRAY.getRGB(), false);

        int i = 1;
        guiGraphics.drawString(this.font, Component.translatable("skyblockbuilder.screen.dump.text.configs"), this.x(TEXT_X), this.y(TEXT_FIRST_Y), Color.DARK_GRAY.getRGB(), false);
        guiGraphics.drawString(this.font, Component.translatable("skyblockbuilder.screen.dump.text.templates"), this.x(TEXT_X), this.y(TEXT_FIRST_Y + ROW_OFFSET * i++), Color.DARK_GRAY.getRGB(), false);
        guiGraphics.drawString(this.font, Component.translatable("skyblockbuilder.screen.dump.text.level_dat"), this.x(TEXT_X), this.y(TEXT_FIRST_Y + ROW_OFFSET * i++), Color.DARK_GRAY.getRGB(), false);
        guiGraphics.drawString(this.font, Component.translatable("skyblockbuilder.screen.dump.text.latest_log"), this.x(TEXT_X), this.y(TEXT_FIRST_Y + ROW_OFFSET * i++), Color.DARK_GRAY.getRGB(), false);
        guiGraphics.drawString(this.font, Component.translatable("skyblockbuilder.screen.dump.text.crash_report"), this.x(TEXT_X), this.y(TEXT_FIRST_Y + ROW_OFFSET * i++), Color.DARK_GRAY.getRGB(), false);
        guiGraphics.drawString(this.font, Component.translatable("skyblockbuilder.screen.dump.text.data_file"), this.x(TEXT_X), this.y(TEXT_FIRST_Y + ROW_OFFSET * i++), Color.DARK_GRAY.getRGB(), false);

        if (this.isOpPlayer) {
            guiGraphics.drawString(this.font, Component.translatable("skyblockbuilder.screen.dump.text.create_on_server"), this.x(TEXT_X), this.y(TEXT_FIRST_Y + ROW_OFFSET * i), Color.DARK_GRAY.getRGB(), false);
        }
    }

    private static boolean isOpPlayer() {
        return Minecraft.getInstance().player != null && Minecraft.getInstance().player.hasPermissions(2);
    }
}
