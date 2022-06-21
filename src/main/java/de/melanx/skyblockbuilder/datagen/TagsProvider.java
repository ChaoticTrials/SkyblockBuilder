package de.melanx.skyblockbuilder.datagen;

import de.melanx.skyblockbuilder.world.SkyblockWorldPresets;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.WorldPresetTagsProvider;
import net.minecraft.tags.WorldPresetTags;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;
import org.moddingx.libx.annotation.data.Datagen;
import org.moddingx.libx.mod.ModX;

@Datagen
public class TagsProvider extends WorldPresetTagsProvider {

    public TagsProvider(DataGenerator generator, ModX mod, @Nullable ExistingFileHelper existingFileHelper) {
        super(generator, mod.modid, existingFileHelper);
    }

    @Override
    protected void addTags() {
        this.tag(WorldPresetTags.NORMAL).add(SkyblockWorldPresets.skyblock);
    }
}
