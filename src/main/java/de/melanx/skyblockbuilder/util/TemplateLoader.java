package de.melanx.skyblockbuilder.util;

import de.melanx.skyblockbuilder.SkyblockBuilder;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.resources.IResource;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.gen.feature.template.Template;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;

public class TemplateLoader {
    private static final ResourceLocation ID = new ResourceLocation(SkyblockBuilder.MODID, "skyblock_builder.nbt");
    private static final Path PATH = FMLPaths.CONFIGDIR.get().resolve("skyblock_builder.nbt");
    public static final Template TEMPLATE = new Template();

    public static void loadSchematic(IResourceManager manager) throws IOException {
        File file = new File(PATH.toUri());
        CompoundNBT nbt;

        if (file.exists()) {
            nbt = CompressedStreamTools.readCompressed(new FileInputStream(file));
        } else {
            IResource resource = manager.getResource(ID);
            nbt = CompressedStreamTools.readCompressed(resource.getInputStream());
        }

        TEMPLATE.read(nbt);
    }
}
