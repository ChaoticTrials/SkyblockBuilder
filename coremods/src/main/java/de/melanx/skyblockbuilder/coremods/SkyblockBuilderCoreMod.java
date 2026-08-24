package de.melanx.skyblockbuilder.coremods;

import cpw.mods.modlauncher.api.ITransformer;
import net.neoforged.neoforgespi.coremod.ICoreMod;

import java.util.List;

public class SkyblockBuilderCoreMod implements ICoreMod {

    @Override
    public Iterable<? extends ITransformer<?>> getTransformers() {
        return List.of(new WorldPresetCodec(), new TeleportCakesCompat());
    }
}
