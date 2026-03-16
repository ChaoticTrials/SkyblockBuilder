package de.melanx.skyblockbuilder.client.screens;

import de.melanx.skyblockbuilder.SkyblockBuilder;
import de.melanx.skyblockbuilder.client.SizeableCheckbox;
import de.melanx.skyblockbuilder.permissions.PermissionManager;
import de.melanx.skyblockbuilder.util.DumpUtil;
import de.melanx.skyblockbuilder.util.SkyComponents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.neoforged.fml.loading.FMLPaths;
import org.moddingx.libx.render.RenderHelper;

import javax.annotation.Nonnull;
import java.awt.Color;
import java.nio.file.Path;

public class DumpScreen extends BaseScreen {

    private static final ResourceLocation INFO_ICON = ResourceLocation.withDefaultNamespace("icon/info");
    private static final int CHECKBOX_SIZE = 10;
    private static final int CHECKBOX_X = 10;
    private static final int CHECKBOX_FIRST_Y = 25;
    private static final int TEXT_X = CHECKBOX_X + 17;
    private static final int TEXT_FIRST_Y = CHECKBOX_FIRST_Y + 2;
    private static final int ROW_OFFSET = 15;
    public MinecraftServer server;
    public SizeableCheckbox includeConfigs;
    public SizeableCheckbox includeTemplates;
    public SizeableCheckbox includeLevelDat;
    public SizeableCheckbox includeLog;
    public SizeableCheckbox includeCrashReport;
    public SizeableCheckbox includeSkyblockBuilderWorldData;
    public SizeableCheckbox generateOnServer;

    public DumpScreen() {
        super(174, DumpScreen.isOpPlayer() ? 172 : 172 - ROW_OFFSET, SkyComponents.SCREEN_DUMP_TITLE);
    }

    @Override
    protected void init() {
        super.init();
        int i = 1;
        this.includeConfigs = this.addRenderableWidget(new SizeableCheckbox(this.x(CHECKBOX_X), this.y(CHECKBOX_FIRST_Y), CHECKBOX_SIZE, true));
        this.includeTemplates = this.addRenderableWidget(new SizeableCheckbox(this.x(CHECKBOX_X), this.y(CHECKBOX_FIRST_Y + ROW_OFFSET * i++), CHECKBOX_SIZE, true));
        this.includeLevelDat = this.addRenderableWidget(new SizeableCheckbox(this.x(CHECKBOX_X), this.y(CHECKBOX_FIRST_Y + ROW_OFFSET * i++), CHECKBOX_SIZE, true));
        this.includeLog = this.addRenderableWidget(new SizeableCheckbox(this.x(CHECKBOX_X), this.y(CHECKBOX_FIRST_Y + ROW_OFFSET * i++), CHECKBOX_SIZE, true));
        this.includeCrashReport = this.addRenderableWidget(new SizeableCheckbox(this.x(CHECKBOX_X), this.y(CHECKBOX_FIRST_Y + ROW_OFFSET * i++), CHECKBOX_SIZE, true));
        this.includeSkyblockBuilderWorldData = this.addRenderableWidget(new SizeableCheckbox(this.x(CHECKBOX_X), this.y(CHECKBOX_FIRST_Y + ROW_OFFSET * i++), CHECKBOX_SIZE, true));
        this.generateOnServer = this.addRenderableWidget(new SizeableCheckbox(this.x(CHECKBOX_X), this.y(CHECKBOX_FIRST_Y + ROW_OFFSET * i), CHECKBOX_SIZE, false));
        this.generateOnServer.visible = DumpScreen.isOpPlayer();
        ImageWidget infoImageWidget = ImageWidget.sprite(20, 20, DumpScreen.INFO_ICON);
        infoImageWidget.setTooltip(Tooltip.create(Component.translatable("skyblockbuilder.screen.dump.info")));
        infoImageWidget.setX(this.x(this.getXSize() - 22));
        infoImageWidget.setY(this.y(4));
        this.addRenderableWidget(infoImageWidget);
        this.addRenderableWidget(Button.builder(SkyComponents.SCREEN_DUMP_BUTTON_CREATE, button -> {
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
                        Minecraft.getInstance().player.sendSystemMessage(SkyComponents.SCREEN_DUMP_SUCCESS.apply(FMLPaths.GAMEDIR.get().relativize(zip).toString())
                                .withStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, zip.getParent().toString()))));
                        Minecraft.getInstance().player.sendSystemMessage(SkyComponents.SCREEN_DUMP_CREATE_ISSUE.append(" ").append(DumpUtil.getIssueUrl()));
                    }
                    this.onClose();
                })
                .bounds(this.centeredX(Button.SMALL_WIDTH), this.y(DumpScreen.isOpPlayer() ? 137 : 137 - ROW_OFFSET), Button.SMALL_WIDTH, Button.DEFAULT_HEIGHT)
                .build());
    }

    @Override
    public void renderBackground(@Nonnull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(guiGraphics, mouseX, mouseY, partialTick);

        RenderHelper.renderGuiBackground(guiGraphics, this.x(0), this.y(0), this.getXSize(), this.getYSize());
        guiGraphics.drawString(this.font, this.title, this.centeredX(this.font.width(this.title)), this.y(8), Color.DARK_GRAY.getRGB(), false);

        int i = 1;
        guiGraphics.drawString(this.font, SkyComponents.SCREEN_DUMP_TEXT_CONFIGS, this.x(TEXT_X), this.y(TEXT_FIRST_Y), Color.DARK_GRAY.getRGB(), false);
        guiGraphics.drawString(this.font, SkyComponents.SCREEN_DUMP_TEXT_TEMPLATES, this.x(TEXT_X), this.y(TEXT_FIRST_Y + ROW_OFFSET * i++), Color.DARK_GRAY.getRGB(), false);
        guiGraphics.drawString(this.font, SkyComponents.SCREEN_DUMP_TEXT_LEVEL_DAT, this.x(TEXT_X), this.y(TEXT_FIRST_Y + ROW_OFFSET * i++), Color.DARK_GRAY.getRGB(), false);
        guiGraphics.drawString(this.font, SkyComponents.SCREEN_DUMP_TEXT_LATEST_LOG, this.x(TEXT_X), this.y(TEXT_FIRST_Y + ROW_OFFSET * i++), Color.DARK_GRAY.getRGB(), false);
        guiGraphics.drawString(this.font, SkyComponents.SCREEN_DUMP_TEXT_CRASH_REPORT, this.x(TEXT_X), this.y(TEXT_FIRST_Y + ROW_OFFSET * i++), Color.DARK_GRAY.getRGB(), false);
        guiGraphics.drawString(this.font, SkyComponents.SCREEN_DUMP_TEXT_DATA_FILE, this.x(TEXT_X), this.y(TEXT_FIRST_Y + ROW_OFFSET * i++), Color.DARK_GRAY.getRGB(), false);

        if (DumpScreen.isOpPlayer()) {
            guiGraphics.drawString(this.font, SkyComponents.SCREEN_DUMP_TEXT_CREATE_ON_SERVER, this.x(TEXT_X), this.y(TEXT_FIRST_Y + ROW_OFFSET * i), Color.DARK_GRAY.getRGB(), false);
        }
    }

    private static boolean isOpPlayer() {
        return Minecraft.getInstance().player != null && PermissionManager.INSTANCE.mayExecuteOpCommand(Minecraft.getInstance().player);
    }
}
