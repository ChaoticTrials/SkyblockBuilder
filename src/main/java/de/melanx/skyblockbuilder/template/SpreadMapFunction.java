package de.melanx.skyblockbuilder.template;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import de.melanx.skyblockbuilder.ModLootItemFunctions;
import de.melanx.skyblockbuilder.SkyblockBuilder;
import de.melanx.skyblockbuilder.data.SkyblockSavedData;
import de.melanx.skyblockbuilder.data.Team;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

public class SpreadMapFunction extends LootItemConditionalFunction {

    public static final MapDecoration.Type DEFAULT_DECORATION = MapDecoration.Type.RED_X;
    private final String spreadName;
    private final MapDecoration.Type mapDecoration;
    private final byte zoom;

    public SpreadMapFunction(LootItemCondition[] predicates, String spreadName, MapDecoration.Type mapDecoration, byte zoom) {
        super(predicates);
        this.spreadName = spreadName;
        this.mapDecoration = mapDecoration;
        this.zoom = zoom;
    }

    @Nonnull
    @Override
    protected ItemStack run(@Nonnull ItemStack stack, @Nonnull LootContext context) {
        if (!stack.is(Items.MAP)) {
            return stack;
        }

        Entity entity = context.getParamOrNull(LootContextParams.THIS_ENTITY);
        Vec3 pos = context.getParamOrNull(LootContextParams.ORIGIN);
        if (!(entity instanceof ServerPlayer player) || pos == null) {
            return stack;
        }

        ServerLevel level = (ServerLevel) player.level();
        SkyblockSavedData data = SkyblockSavedData.get(level);
        Team team = data.getTeamFromPlayer(player);
        if (team == null) {
            SkyblockBuilder.getLogger().error("Player {} does not have a team.", player);
            return stack;
        }

        if (team.getPlacedSpreads().isEmpty()) {
            SkyblockBuilder.getLogger().error("Team {} has no placed spreads", team.getName());
            return stack;
        }

        Set<Team.PlacedSpread> placedSpreads = team.getPlacedSpreads(this.spreadName);
        if (placedSpreads.isEmpty()) {
            SkyblockBuilder.getLogger().error("No spread {} for team {}", this.spreadName, team.getName());
            return stack;
        }

        BlockPos middle = SpreadMapFunction.getMiddle(placedSpreads.stream().map(Team.PlacedSpread::pos).collect(Collectors.toSet()));
        ItemStack map = MapItem.create(level, middle.getX(), middle.getZ(), this.zoom, true, true);
        MapItem.renderBiomePreviewMap(level, map);

        int i = 0;
        for (Team.PlacedSpread placedSpread : placedSpreads) {
            MapItemSavedData.addTargetDecoration(map, placedSpread.pos(), String.valueOf(i++), this.mapDecoration);
        }

        return map;
    }

    private static BlockPos getMiddle(Set<BlockPos> blocks) {
        int x = 0, z = 0;
        for (BlockPos pos : blocks) {
            x += pos.getX();
            z += pos.getZ();
        }

        return new BlockPos(x / blocks.size(), 0, z / blocks.size());
    }

    @Nonnull
    @Override
    public LootItemFunctionType getType() {
        return ModLootItemFunctions.spreadMap;
    }

    public static class Serializer extends LootItemConditionalFunction.Serializer<SpreadMapFunction> {

        @Override
        public void serialize(@Nonnull JsonObject json, @Nonnull SpreadMapFunction function, @Nonnull JsonSerializationContext context) {
            super.serialize(json, function, context);
            json.addProperty("destination", function.spreadName);

            if (function.mapDecoration != SpreadMapFunction.DEFAULT_DECORATION) {
                json.add("decoration", context.serialize(function.mapDecoration.toString().toLowerCase(Locale.ROOT)));
            }

            if (function.zoom != 2) {
                json.addProperty("zoom", function.zoom);
            }
        }

        @Nonnull
        @Override
        public SpreadMapFunction deserialize(@Nonnull JsonObject json, @Nonnull JsonDeserializationContext context, @Nonnull LootItemCondition[] conditions) {
            String decoration = json.has("decoration") ? GsonHelper.getAsString(json, "decoration") : "red_x";
            byte zoom = json.has("zoom") ? GsonHelper.getAsByte(json, "zoom") : (byte) 2;
            MapDecoration.Type mapDecorationType = SpreadMapFunction.DEFAULT_DECORATION;

            try {
                mapDecorationType = MapDecoration.Type.valueOf(decoration.toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException e) {
                SkyblockBuilder.getLogger().error("Error while parsing loot table decoration entry. Found {}. Defaulting to {}", decoration, mapDecorationType);
            }

            String destination = GsonHelper.getAsString(json, "destination");
            if (destination.isEmpty()) {
                SkyblockBuilder.getLogger().error("Spread destination is empty");
            }

            return new SpreadMapFunction(conditions, destination, mapDecorationType, zoom);
        }
    }
}
