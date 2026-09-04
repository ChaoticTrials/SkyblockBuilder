package de.melanx.skyblockbuilder.coremods;

import cpw.mods.modlauncher.api.ITransformer;
import de.melanx.skyblockbuilder.coremods.compat.EnderIoCompat;
import de.melanx.skyblockbuilder.coremods.compat.TeleportCakesCompat;
import net.neoforged.neoforgespi.coremod.ICoreMod;

import java.util.List;

public class SkyblockBuilderCoreMod implements ICoreMod {

    @Override
    public Iterable<? extends ITransformer<?>> getTransformers() {
        return List.of(new WorldPresetCodec(), new TeleportCakesCompat(), new EnderIoCompat());
    }
}
