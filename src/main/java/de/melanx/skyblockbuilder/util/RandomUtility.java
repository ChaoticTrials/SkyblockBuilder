package de.melanx.skyblockbuilder.util;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.yggdrasil.ProfileResult;
import com.mojang.serialization.JsonOps;
import de.melanx.skyblockbuilder.SkyblockBuilder;
import de.melanx.skyblockbuilder.compat.CuriosCompat;
import de.melanx.skyblockbuilder.config.StartingInventory;
import de.melanx.skyblockbuilder.config.common.CustomizationConfig;
import de.melanx.skyblockbuilder.config.common.TemplatesConfig;
import de.melanx.skyblockbuilder.data.SkyblockSavedData;
import de.melanx.skyblockbuilder.data.Team;
import de.melanx.skyblockbuilder.registration.ModBlocks;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.gui.Font;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.UsernameCache;

import javax.annotation.Nonnull;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class RandomUtility {

    public static JsonObject serializeItem(ItemStack stack, HolderLookup.Provider provider) {
        Tag tag = stack.save(provider);

        JsonObject json = NbtOps.INSTANCE.convertTo(JsonOps.INSTANCE, tag).getAsJsonObject();
//        json.addProperty("item", tag.getString("id"));
//
//        int count = tag.getInt("Count");
//        if (count > 1) {
//            json.addProperty("count", count);
//        }
//
//        if (tag.contains("tag")) {
//            //noinspection ConstantConditions
//            json.addProperty("nbt", tag.get("tag").toString());
//        }
//
//        if (tag.contains("ForgeCaps")) {
//            //noinspection ConstantConditions
//            json.addProperty("ForgeCaps", tag.get("ForgeCaps").toString());
//        }

        return json;
    }

    public static void dropInventories(Player player) {
        if (player.isSpectator() || player.isCreative()) {
            return;
        }

        player.getInventory().dropAll();
        if (ModList.get().isLoaded(CuriosCompat.MODID)) {
            CuriosCompat.dropInventory(player);
        }
    }

    public static void setStartInventory(ServerPlayer player) {
        if (player.isSpectator() || player.isCreative()) {
            return;
        }

        // vanilla inventory
        StartingInventory.getStarterItems().forEach(entry -> {
            if (entry.getLeft() == EquipmentSlot.MAINHAND) {
                player.getInventory().add(entry.getRight().copy());
            } else {
                player.setItemSlot(entry.getLeft(), entry.getRight().copy());
            }
        });

        if (ModList.get().isLoaded(CuriosCompat.MODID)) {
            CuriosCompat.setStartInventory(player);
        }
    }

    public static Component getFormattedPos(BlockPos pos) {
        return ComponentUtils.wrapInSquareBrackets(Component.translatable("chat.coordinates", pos.getX(), pos.getY(), pos.getZ()).withStyle(style -> style
                .withColor(ChatFormatting.GREEN)
                .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/tp @s " + pos.getX() + " " + pos.getY() + " " + pos.getZ()))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.translatable("chat.coordinates.tooltip")))));
    }

    public static String formattedCooldown(long ticks) {
        int realTime = (int) (ticks / 20);
        int min = realTime / 60;
        String sec = String.format("%02d", realTime % 60);

        return String.format("%s:%s", min, sec);
    }

    public static void deleteTeamIfEmpty(SkyblockSavedData data, Team team) {
        if (team.isEmpty() && CustomizationConfig.deleteTeamsAutomatically) {
            data.deleteTeam(team.getId());
            SkyblockBuilder.getLogger().info("Team {} ({}) was deleted. No player left.", team.getName(), team.getId());
        }
    }

    public static Set<GameProfile> getGameProfiles(ServerLevel level) {
        MinecraftServer server = level.getServer();

        net.minecraft.server.players.GameProfileCache profileCache = server.getProfileCache();
        Set<GameProfile> profiles = Sets.newConcurrentHashSet();
        Set<UUID> handledIds = Sets.newConcurrentHashSet();
        handledIds.add(Util.NIL_UUID);

        // load the cache and look for all profiles
        //noinspection DataFlowIssue
        profileCache.load().forEach(profileInfo -> {
            GameProfile profile = profileInfo.getProfile();
            profiles.add(profile);
            handledIds.add(profile.getId());
        });

        int cachedProfilesAmount = profiles.size() - 1;
        int usedCachedProfilesAmount = 0;
        int uncachedProfilesAmount = 0;
        int totalProfilesAmount = 0;

        // check if all the members were in the cache and add these tags if needed
        for (Team team : SkyblockSavedData.get(level).getTeams()) {
            for (UUID id : team.getPlayers()) {
                totalProfilesAmount++;
                if (handledIds.contains(id)) {
                    usedCachedProfilesAmount++;
                    continue;
                }

                String lastKnownUsername = UsernameCache.getLastKnownUsername(id);
                if (lastKnownUsername != null) {
                    profiles.add(new GameProfile(id, lastKnownUsername));
                    continue;
                }

                uncachedProfilesAmount++;
                Optional<GameProfile> gameProfile = profileCache.get(id);
                if (gameProfile.isPresent()) {
                    profiles.add(gameProfile.get());
                } else {
                    GameProfile profile;
                    GameProfile unnamedProfile = new GameProfile(id, "Unknown");
                    boolean enforceProfileSecurity = CustomizationConfig.forceUnsecureProfileNames || level.getServer().enforceSecureProfile();
                    try {
                        ProfileResult profileResult = server.getSessionService().fetchProfile(id, enforceProfileSecurity);
                        profile = profileResult != null ? profileResult.profile() : unnamedProfile;
                    } catch (IllegalArgumentException e) {
                        SkyblockBuilder.getLogger().error("Problems filling profile properties for id {} with requiring secure {}", id, enforceProfileSecurity);
                        profile = unnamedProfile;
                    }

                    if (profile.getName() != null) {
                        profileCache.add(profile);
                        profiles.add(profile);
                    } else {
                        SkyblockBuilder.getLogger().info("No profile found for id {}", id);
                        profiles.add(new GameProfile(profile.getId(), "Unknown"));
                    }
                }
            }
        }

        SkyblockBuilder.getLogger().info("Cached profiles: {} ({} unused), uncached profiles: {}, total profiles: {}", cachedProfilesAmount, cachedProfilesAmount - usedCachedProfilesAmount, uncachedProfilesAmount, totalProfilesAmount);

        return profiles;
    }

    /**
     * @return Set of set spawn points
     */
    // [Vanilla copy]
    public static Set<TemplatesConfig.Spawn> fillTemplateFromWorld(StructureTemplate template, Level level, BlockPos pos, Vec3i box, boolean withEntities, Collection<Block> toIgnore) {
        Set<TemplatesConfig.Spawn> spawns = new HashSet<>();
        if (box.getX() >= 1 && box.getY() >= 1 && box.getZ() >= 1) {
            BlockPos blockpos = pos.offset(box).offset(-1, -1, -1);
            List<StructureTemplate.StructureBlockInfo> specialBlocks = Lists.newArrayList();
            List<StructureTemplate.StructureBlockInfo> blocksWithTag = Lists.newArrayList();
            List<StructureTemplate.StructureBlockInfo> normalBlocks = Lists.newArrayList();
            BlockPos minPos = new BlockPos(Math.min(pos.getX(), blockpos.getX()), Math.min(pos.getY(), blockpos.getY()), Math.min(pos.getZ(), blockpos.getZ()));
            BlockPos maxPos = new BlockPos(Math.max(pos.getX(), blockpos.getX()), Math.max(pos.getY(), blockpos.getY()), Math.max(pos.getZ(), blockpos.getZ()));
            template.size = box;

            for (BlockPos actPos : BlockPos.betweenClosed(minPos, maxPos)) {
                BlockPos relPos = actPos.subtract(minPos);
                BlockState state = level.getBlockState(actPos);
                if (toIgnore.isEmpty() || !toIgnore.contains(state.getBlock())) {
                    if (state.is(ModBlocks.spawnBlock)) {
                        WorldUtil.SpawnDirection direction = WorldUtil.SpawnDirection.fromDirection(state.getValue(BlockStateProperties.HORIZONTAL_FACING));
                        spawns.add(new TemplatesConfig.Spawn(relPos, direction));
                        // prevent spawn block being replaced by solid block in a cave
                        if (toIgnore.contains(Blocks.AIR)) {
                            continue;
                        }

                        state = Blocks.AIR.defaultBlockState();
                    }
                    BlockEntity blockEntity = level.getBlockEntity(actPos);
                    StructureTemplate.StructureBlockInfo blockInfo;
                    if (blockEntity != null) {
                        blockInfo = new StructureTemplate.StructureBlockInfo(relPos, state, blockEntity.saveWithId(level.registryAccess()));
                    } else {
                        blockInfo = new StructureTemplate.StructureBlockInfo(relPos, state, null);
                    }

                    StructureTemplate.addToLists(blockInfo, specialBlocks, blocksWithTag, normalBlocks);
                }
            }

            List<StructureTemplate.StructureBlockInfo> sortedBlocks = StructureTemplate.buildInfoList(specialBlocks, blocksWithTag, normalBlocks);
            template.palettes.clear();
            template.palettes.add(new StructureTemplate.Palette(sortedBlocks));
            if (withEntities) {
                template.fillEntityList(level, minPos, maxPos.offset(1, 1, 1));
            } else {
                template.entityInfoList.clear();
            }
        }

        return spawns;
    }

    public static String normalize(String s) {
        return s.toLowerCase(Locale.ROOT).replaceAll("\\W+", "_");
    }

    public static Path getFilePath(Path parentFolder, String name, String extension) {
        int index = 0;
        String filename;
        Path filepath;
        do {
            filename = (name == null ? "template" : RandomUtility.normalize(name)) + ((index == 0) ? "" : "_" + index) + "." + extension;
            index++;
            filepath = parentFolder.resolve(filename);
        } while (Files.exists(filepath));

        return filepath;
    }

    public static String shorten(@Nonnull Font font, String name, int length) {
        String s = name;
        int k = 0;
        while (font.width(s) > length) {
            s = name.substring(0, name.length() - k).trim() + "...";
            k++;
        }

        return s;
    }
}
