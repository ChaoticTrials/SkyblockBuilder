package de.melanx.skyblockbuilder.coremods.compat;

import cpw.mods.modlauncher.api.ITransformer;
import cpw.mods.modlauncher.api.ITransformerVotingContext;
import cpw.mods.modlauncher.api.TargetType;
import cpw.mods.modlauncher.api.TransformerVoteResult;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import javax.annotation.Nonnull;
import java.util.Set;

// Fire Crafting Recipe requires dimension
public class EnderIoCompat implements ITransformer<MethodNode> {

    private static final String FIRE_CRAFTING_RECIPE = "com.enderio.enderio.content.fire_crafting.FireCraftingRecipe";
    // targets use the class name, instructions the internal name
    private static final String FIRE_CRAFTING_RECIPE_CLASS = FIRE_CRAFTING_RECIPE.replace('.', '/');
    private static final String CORE_UTIL = "de/melanx/skyblockbuilder/util/CoreUtil";

    @Nonnull
    @Override
    public MethodNode transform(MethodNode method, ITransformerVotingContext context) {
        method.instructions.clear();
        method.tryCatchBlocks.clear();
        method.localVariables = null;

        method.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
        method.instructions.add(new FieldInsnNode(Opcodes.GETFIELD, FIRE_CRAFTING_RECIPE_CLASS, "dimensions", "Ljava/util/List;"));
        method.instructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
        method.instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, CORE_UTIL, "isDimensionValid",
                "(Ljava/util/List;Lnet/minecraft/resources/ResourceKey;)Z",
                false));
        method.instructions.add(new InsnNode(Opcodes.IRETURN));

        return method;
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
                        FIRE_CRAFTING_RECIPE,
                        "isDimensionValid",
                        "(Lnet/minecraft/resources/ResourceKey;)Z"
                )
        );
    }

    @Nonnull
    @Override
    public TargetType<MethodNode> getTargetType() {
        return TargetType.METHOD;
    }
}
