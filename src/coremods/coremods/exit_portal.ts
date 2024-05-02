import {
    AbstractInsnNode,
    ASMAPI,
    CoreMods,
    InsnList,
    InsnNode,
    MethodInsnNode,
    MethodNode,
    Opcodes,
    VarInsnNode
} from "coremods";

function initializeCoreMod(): CoreMods {
    return {
        'redirect_exit_portal': {
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraft.server.level.ServerPlayer',
                'methodName': 'm_183318_',
                'methodDesc': '(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;ZLnet/minecraft/world/level/border/WorldBorder;)Ljava/util/Optional;'
            },
            'transformer': (method: MethodNode) => {
                const target = new InsnList();
                // target.add(new VarInsnNode(Opcodes.ALOAD, 0)); // this already loaded
                target.add(new VarInsnNode(Opcodes.ALOAD, 1)); // destination
                target.add(new VarInsnNode(Opcodes.ALOAD, 2)); // findFrom
                target.add(new VarInsnNode(Opcodes.ILOAD, 3)); // isToNether
                target.add(new VarInsnNode(Opcodes.ALOAD, 4)); // worldBorder
                target.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                    'de/melanx/skyblockbuilder/util/CoreUtil',
                    'getExitPortal', '(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;ZLnet/minecraft/world/level/border/WorldBorder;)Ljava/util/Optional;'
                ));
                target.add(new InsnNode(Opcodes.ARETURN));

                for (let i = 0; i < method.instructions.size(); i++) {
                    const node = method.instructions.get(i) as AbstractInsnNode;
                    if (node.getOpcode() == Opcodes.INVOKEVIRTUAL) {
                        const methodNode = node as MethodInsnNode;
                        if (methodNode.owner == 'net/minecraft/server/level/ServerPlayer'
                            && methodNode.name == ASMAPI.mapMethod('m_9236_')) {
                            method.instructions.insertBefore(methodNode, target);
                            return method;
                        }
                    }
                }

                throw new Error('Failed to patch ServerPlayer.class');
            }
        }
    }
}
