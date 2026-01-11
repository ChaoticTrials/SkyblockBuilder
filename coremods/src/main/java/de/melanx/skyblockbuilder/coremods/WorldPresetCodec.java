package de.melanx.skyblockbuilder.coremods;

import cpw.mods.modlauncher.api.ITransformer;
import cpw.mods.modlauncher.api.ITransformerVotingContext;
import cpw.mods.modlauncher.api.TargetType;
import cpw.mods.modlauncher.api.TransformerVoteResult;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import javax.annotation.Nonnull;
import java.util.Set;

public class WorldPresetCodec implements ITransformer<MethodNode> {

    @Nonnull
    @Override
    public MethodNode transform(MethodNode method, ITransformerVotingContext context) {
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
                    return method;
                }
            }
        }

        throw new IllegalStateException("Could not find field DIRECT_CODEC");
    }

    @Nonnull
    @Override
    public TransformerVoteResult castVote(ITransformerVotingContext context) {
        return TransformerVoteResult.YES;
    }

    @Nonnull
    @Override
    public Set<Target<MethodNode>> targets() {
        return Set.of(
                Target.targetMethod(
                        "net.minecraft.world.level.levelgen.presets.WorldPreset",
                        "<clinit>",
                        "()V"
                )
        );
    }

    @Nonnull
    @Override
    public TargetType<MethodNode> getTargetType() {
        return TargetType.METHOD;
    }
}
