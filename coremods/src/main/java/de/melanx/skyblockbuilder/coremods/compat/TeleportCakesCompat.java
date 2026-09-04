package de.melanx.skyblockbuilder.coremods.compat;

import cpw.mods.modlauncher.api.ITransformer;
import cpw.mods.modlauncher.api.ITransformerVotingContext;
import cpw.mods.modlauncher.api.TargetType;
import cpw.mods.modlauncher.api.TransformerVoteResult;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import javax.annotation.Nonnull;
import java.util.Set;

// Cake spawn platform needs to be created in the correct level
public class TeleportCakesCompat implements ITransformer<MethodNode> {

    private static final String CAKE_TELEPORTER = "top.ilov.mcmods.tc.utils.CakeTeleporter";
    private static final String CORE_UTIL = "de/melanx/skyblockbuilder/util/CoreUtil";

    @Nonnull
    @Override
    public MethodNode transform(MethodNode method, ITransformerVotingContext context) {
        switch (method.name) {
            case "teleportToNether" -> TeleportCakesCompat.transformNether(method);
            case "teleportToOverworld" -> TeleportCakesCompat.transformOverworld(method);
            default -> throw new IllegalStateException("Unexpected method " + method.name);
        }

        return method;
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
    public TransformerVoteResult castVote(ITransformerVotingContext context) {
        return TransformerVoteResult.YES;
    }

    @Nonnull
    @Override
    public Set<Target<MethodNode>> targets() {
        return Set.of(
                Target.targetMethod(
                        CAKE_TELEPORTER,
                        "teleportToNether",
                        "(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/server/level/ServerPlayer;)Z"
                ),
                Target.targetMethod(
                        CAKE_TELEPORTER,
                        "teleportToOverworld",
                        "(Lnet/minecraft/server/level/ServerPlayer;)Z"
                )
        );
    }

    @Nonnull
    @Override
    public TargetType<MethodNode> getTargetType() {
        return TargetType.METHOD;
    }
}
