package de.melanx.skyblockbuilder.compat.heracles;

import com.teamresourceful.resourcefullib.client.scissor.ScissorBoxStack;
import de.melanx.skyblockbuilder.data.SkyblockSavedData;
import de.melanx.skyblockbuilder.data.Team;
import earth.terrarium.heracles.api.client.DisplayWidget;
import earth.terrarium.heracles.api.client.WidgetUtils;
import earth.terrarium.heracles.api.client.theme.QuestScreenTheme;
import earth.terrarium.heracles.api.tasks.client.display.TaskTitleFormatter;
import earth.terrarium.heracles.common.handlers.progress.TaskProgress;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.nbt.NumericTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class SpreadTaskWidget implements DisplayWidget {

    private final SpreadLocationTask task;
    private final TaskProgress<NumericTag> progress;
    private final Team team;

    public SpreadTaskWidget(SpreadLocationTask task, TaskProgress<NumericTag> progress) {
        this.task = task;
        this.progress = progress;
        //noinspection DataFlowIssue
        this.team = SkyblockSavedData.get(Minecraft.getInstance().level).getTeamFromPlayer(Minecraft.getInstance().player);
    }

    @Override
    public void render(GuiGraphics graphics, ScissorBoxStack scissor, int x, int y, int width, int mouseX, int mouseY, boolean hovered, float partialTicks) {
        Font font = Minecraft.getInstance().font;
        WidgetUtils.drawBackground(graphics, x, y, width, this.getHeight(width));
        int iconSize = 32;
        this.task.icon().renderOrStack(Items.FILLED_MAP.getDefaultInstance(), graphics, scissor, x + 5, y + (this.getHeight(width) / 2) - (iconSize / 2), iconSize);
        graphics.drawString(font, this.task.titleOr(TaskTitleFormatter.create(this.task)), x + iconSize + 16, y + 6, QuestScreenTheme.getTaskTitle(), false);

        int baseX = x + iconSize + 16;
        int baseY = y + 8 + font.lineHeight;
        if (this.team == null || this.team.getPlacedSpreads().isEmpty()) {
            graphics.drawString(font, Component.translatable("setting.skyblockbuilder.spread_location.no_valid_spreads"), baseX, baseY, QuestScreenTheme.getTaskDescription(), false);
        } else if (this.task.predicate() == SpreadPredicate.ALWAYS_TRUE) {
            graphics.drawString(font, Component.translatable("setting.skyblockbuilder.spread_location.no_spreads_defined"), baseX, baseY, QuestScreenTheme.getTaskDescription(), false);
        } else {
            List<String> spreads = this.task.predicate().getSpreads();
            Map<String, Set<Team.PlacedSpread>> teamSpreads = this.team.getPlacedSpreads();
            List<String> validSpreads = spreads.stream().filter(teamSpreads::containsKey).toList();

            if (!validSpreads.isEmpty()) {
                int i = 2;
                graphics.drawString(font, Component.translatable("setting.skyblockbuilder.spread_location.visit_spreads"), baseX, baseY, QuestScreenTheme.getTaskDescription(), false);

                for (String spread : spreads) {
                    graphics.drawString(font, Component.literal("  - " + spread), baseX, y + 8 + ((font.lineHeight + 1) * i++), QuestScreenTheme.getTaskDescription(), false);
                }
            } else {
                graphics.drawString(font, Component.translatable("setting.skyblockbuilder.spread_location.no_valid_spreads"), baseX, baseY, QuestScreenTheme.getTaskDescription(), false);
            }
        }

        WidgetUtils.drawProgressText(graphics, x, y, width, this.task, this.progress);

        int height = this.getHeight(width);
        WidgetUtils.drawProgressBar(graphics, x + iconSize + 16, y + height - font.lineHeight - 5, x + width - 5, y + height - 6, this.task, this.progress);
    }

    @Override
    public int getHeight(int width) {
        if (this.team == null || this.team.getPlacedSpreads().isEmpty() || this.task.predicate() == SpreadPredicate.ALWAYS_TRUE) {
            return 42;
        }

        return (this.task.predicate().getSpreads().size() * (Minecraft.getInstance().font.lineHeight + 1)) + 42;
    }
}
