package de.melanx.skyblockbuilder.mixin;

import de.melanx.skyblockbuilder.SkyblockBuilder;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Mixin(PackRepository.class)
public class PackRepositoryMixin {

    @Inject(method = "rebuildSelected", at = @At(value = "RETURN"), cancellable = true)
    private void reorderIds(Collection<String> ids, CallbackInfoReturnable<List<Pack>> cir) {
        List<Pack> list = new ArrayList<>(cir.getReturnValue());
        Optional<Pack> vanilla = list.stream().filter(entry -> entry.getId().equals("vanilla")).findAny();
        SkyblockBuilder.getLogger().info("Sorting datapack list to load data correctly.");
        if (vanilla.isPresent()) {
            list.remove(vanilla.get());
            list.add(0, vanilla.get());
            cir.setReturnValue(List.copyOf(list));
        }
    }
}
