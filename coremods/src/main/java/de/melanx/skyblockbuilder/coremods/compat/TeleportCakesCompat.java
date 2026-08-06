package de.melanx.skyblockbuilder.coremods.compat;

import net.neoforged.neoforgespi.transformation.ProcessorName;
import net.neoforged.neoforgespi.transformation.SimpleMethodProcessor;
import net.neoforged.neoforgespi.transformation.SimpleTransformationContext;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import javax.annotation.Nonnull;
import java.util.Set;

// Cake spawn platform needs to be created in the correct level
public class TeleportCakesCompat extends SimpleMethodProcessor {

    private static final String CAKE_TELEPORTER = "top.ilov.mcmods.tc.utils.CakeTeleporter";
    private static final String CORE_UTIL = "de/melanx/skyblockbuilder/util/CoreUtil";

    @Override
    public void transform(MethodNode method, SimpleTransformationContext context) {
        switch(method.name) {
            case "teleportToNether" -> TeleportCakesCompat.transformNether(method);
            case "teleportToOverworld" -> TeleportCakesCompat.transformOverworld(method);
            default -> throw new IllegalStateException("Unexpected method " + method.name);
        }
    }

    private static void transformNether(MethodNode method) {
        MethodInsnNode getLevel = null;
        for (AbstractInsnNode insn : method.instructions) {
            if (insn.getOpcode() == Opcodes.INVOKEVIRTUAL) {
                MethodInsnNode methodNode = (MethodInsnNode) insn;
                if (methodNode.owner.equals("net/minecraft/server/MinecraftServer") && methodNode.name.equals("getLevel")) {
                    getLevel = methodNode;
                    break;
                }
            }
        }

        if (getLevel == null) {
            throw new IllegalStateException("Could not find the dimension lookup in " + CAKE_TELEPORTER + "#teleportToNether");
        }

        // the player is the second parameter, it decides which nether is looked up
        method.instructions.insertBefore(getLevel, new VarInsnNode(Opcodes.ALOAD, 1));
        method.instructions.set(getLevel, new MethodInsnNode(Opcodes.INVOKESTATIC, CORE_UTIL, "resolveLevel",
                "(Lnet/minecraft/server/MinecraftServer;Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/server/level/ServerPlayer;)Lnet/minecraft/server/level/ServerLevel;",
                false));
    }

    private static void transformOverworld(MethodNode method) {
        LabelNode teleportCakes = new LabelNode();
        InsnList target = new InsnList();

        target.add(new VarInsnNode(Opcodes.ALOAD, 0));
        target.add(new FieldInsnNode(Opcodes.GETSTATIC, "net/minecraft/world/level/Level", "OVERWORLD", "Lnet/minecraft/resources/ResourceKey;"));
        target.add(new MethodInsnNode(Opcodes.INVOKESTATIC, CORE_UTIL, "redirectToTeamIsland",
                "(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/resources/ResourceKey;)Z",
                false));
        target.add(new JumpInsnNode(Opcodes.IFEQ, teleportCakes));
        target.add(new InsnNode(Opcodes.ICONST_1));
        target.add(new InsnNode(Opcodes.IRETURN));
        target.add(teleportCakes);

        method.instructions.insert(target);
    }

    @Nonnull
    @Override
    public Set<Target> targets() {
        return Set.of(
                new Target(
                        CAKE_TELEPORTER,
                        "teleportToNether",
                        "(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/server/level/ServerPlayer;)Z"
                ),
                new Target(
                        CAKE_TELEPORTER,
                        "teleportToOverworld",
                        "(Lnet/minecraft/server/level/ServerPlayer;)Z"
                )
        );
    }

    @Override
    public ProcessorName name() {
        return new ProcessorName("skyblockbuilder", "compat/teleport_cakes");
    }
}
