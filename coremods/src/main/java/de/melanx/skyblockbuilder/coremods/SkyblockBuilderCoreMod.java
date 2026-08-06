package de.melanx.skyblockbuilder.coremods;

import de.melanx.skyblockbuilder.coremods.compat.EnderIoCompat;
import de.melanx.skyblockbuilder.coremods.compat.TeleportCakesCompat;
import net.neoforged.neoforgespi.transformation.ClassProcessorProvider;

public class SkyblockBuilderCoreMod implements ClassProcessorProvider {

    @Override
    public void createProcessors(Context context, Collector collector) {
        collector.add(new WorldPresetCodec());
        collector.add(new TeleportCakesCompat());
        collector.add(new EnderIoCompat());
    }
}
