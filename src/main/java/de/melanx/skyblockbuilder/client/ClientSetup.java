package de.melanx.skyblockbuilder.client;

import de.melanx.skyblockbuilder.world.SkyblockWorldPresets;
import net.minecraft.client.gui.screens.worldselection.PresetEditor;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.presets.WorldPreset;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import org.moddingx.libx.impl.reflect.ReflectionHacks;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@OnlyIn(Dist.CLIENT)
public class ClientSetup {

    public static void clientSetup() {
        Map<Optional<ResourceKey<WorldPreset>>, PresetEditor> editorMap = new HashMap<>(PresetEditor.EDITORS);
        editorMap.put(BuiltinRegistries.WORLD_PRESET.getResourceKey(SkyblockWorldPresets.skyblock), (parent, context) -> new ScreenCustomizeSkyblock(parent));
        try {
            Field editors = ObfuscationReflectionHelper.findField(PresetEditor.class, "f_232950_");
            ReflectionHacks.setFinalField(editors, null, editorMap);
        } catch (ObfuscationReflectionHelper.UnableToFindFieldException e) {
            throw new RuntimeException(e);
        }
    }
}
