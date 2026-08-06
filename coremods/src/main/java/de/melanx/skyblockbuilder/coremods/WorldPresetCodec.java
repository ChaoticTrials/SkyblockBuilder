package de.melanx.skyblockbuilder.coremods;

import net.neoforged.neoforgespi.transformation.ProcessorName;
import net.neoforged.neoforgespi.transformation.SimpleMethodProcessor;
import net.neoforged.neoforgespi.transformation.SimpleTransformationContext;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import javax.annotation.Nonnull;
import java.util.Set;

public class WorldPresetCodec extends SimpleMethodProcessor {

    @Override
    public void transform(MethodNode method, SimpleTransformationContext context) {
        InsnList target = new InsnList();
        target.add(
                new MethodInsnNode(Opcodes.INVOKESTATIC,
                        "de/melanx/skyblockbuilder/util/CoreUtil",
                        "augmentWorldPresetCodec",
                        "(Lcom/mojang/serialization/Codec;)Lcom/mojang/serialization/Codec;",
                        false)
        );

        for (AbstractInsnNode insn : method.instructions) {
            if (insn.getOpcode() == Opcodes.PUTSTATIC) {
                FieldInsnNode field = (FieldInsnNode) insn;
                if (field.owner.equals("net/minecraft/world/level/levelgen/presets/WorldPreset") && field.name.equals("DIRECT_CODEC")) {
                    method.instructions.insertBefore(insn, target);
                    return;
                }
            }
        }

        throw new IllegalStateException("Could not find field DIRECT_CODEC");
    }

    @Nonnull
    @Override
    public Set<Target> targets() {
        return Set.of(
                new Target(
                        "net.minecraft.world.level.levelgen.presets.WorldPreset",
                        "<clinit>",
                        "()V"
                )
        );
    }

    @Override
    public ProcessorName name() {
        return new ProcessorName("skyblockbuilder", "world_preset_codec");
    }
}
