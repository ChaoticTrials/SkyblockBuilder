package de.melanx.skyblockbuilder;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.resources.IResource;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.gen.feature.template.Template;

import java.io.IOException;

public class TemplateLoader {
    private static final ResourceLocation ID = new ResourceLocation(SkyblockBuilder.MODID, "structures/custom_skyblock.nbt");
    public static final Template TEMPLATE = new Template();

    public static void loadSchematic(IResourceManager manager) throws IOException {
        IResource file = manager.getResource(ID);
        CompoundNBT nbt = CompressedStreamTools.readCompressed(file.getInputStream());
        TEMPLATE.read(nbt);
    }
}
