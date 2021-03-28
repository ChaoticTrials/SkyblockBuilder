package de.melanx.skyblockbuilder.util;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

import java.util.UUID;

public class RandomUtility {
    public static final ITextComponent UNKNOWN_PLAYER = new TranslationTextComponent("skyblockbuilder.unknown_player");

    public static ITextComponent getDisplayNameByUuid(World world, UUID id) {
        PlayerEntity player = world.getPlayerByUuid(id);
        return player != null ? player.getDisplayName() : UNKNOWN_PLAYER;
    }
}
