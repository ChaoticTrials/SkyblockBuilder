package de.melanx.skyblockbuilder.coremods.compat;

import net.neoforged.neoforgespi.transformation.ProcessorName;
import net.neoforged.neoforgespi.transformation.SimpleMethodProcessor;
import net.neoforged.neoforgespi.transformation.SimpleTransformationContext;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import javax.annotation.Nonnull;
import java.util.Set;

// Fire Crafting Recipe requires dimension
public class EnderIoCompat extends SimpleMethodProcessor {

    @Override
    public void transform(MethodNode method, SimpleTransformationContext context) {
        method.instructions.clear();
        method.localVariables = null;

        method.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
        method.instructions.add(new FieldInsnNode(Opcodes.GETFIELD, "com/enderio/enderio/content/fire_crafting/FireCraftingRecipe", "dimensions", "Ljava/util/List;"));
        method.instructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
        method.instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "de/melanx/skyblockbuilder/util/CoreUtil", "isDimensionValid",
                "(Ljava/util/List;Lnet/minecraft/resources/ResourceKey;)Z",
                false));
        method.instructions.add(new InsnNode(Opcodes.IRETURN));
    }

    @Nonnull
    @Override
    public Set<Target> targets() {
        return Set.of(
                new Target(
                        "com.enderio.enderio.content.fire_crafting.FireCraftingRecipe",
                        "isDimensionValid",
                        "(Lnet/minecraft/resources/ResourceKey;)Z"
                )
        );
    }

    @Override
    public ProcessorName name() {
        return new ProcessorName("skyblockbuilder", "compat/enderio");
    }
}
