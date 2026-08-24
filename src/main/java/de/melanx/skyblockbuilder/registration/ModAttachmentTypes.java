package de.melanx.skyblockbuilder.registration;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.attachment.AttachmentType;
import org.moddingx.libx.annotation.registration.RegisterClass;

@RegisterClass(registry = "ATTACHMENT_TYPES")
public class ModAttachmentTypes {

    public static final AttachmentType<ResourceKey<Level>> data = AttachmentType.builder(() -> Level.OVERWORLD)
            .serialize(Level.RESOURCE_KEY_CODEC.fieldOf("normalized_dimension").codec())
            .copyOnDeath()
            .build();
}
