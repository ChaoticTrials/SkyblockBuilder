import {ASMAPI, CoreMods, MethodInsnNode, MethodNode, Opcodes} from "coremods";

function initializeCoreMod(): CoreMods {
    return {
        'biomefix': {
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraft.world.level.chunk.ChunkBiomeContainer',
                'methodName': 'm_62131_',
                'methodDesc': '()[I'
            },
            'transformer': function (method: MethodNode) {
                const target = ASMAPI.buildMethodCall(
                    'de/melanx/skyblockbuilder/core/BiomeFix',
                    'getId', '(Lnet/minecraft/core/IdMap;Ljava/lang/Object;)I',
                    ASMAPI.MethodType.STATIC
                );

                for (let i = 0; i < method.instructions.size(); i++) {
                    const insn = method.instructions.get(i);
                    if (insn != null && insn.getOpcode() == Opcodes.INVOKEINTERFACE) {
                        const methodInsn = insn as MethodInsnNode;
                        if (methodInsn.owner == 'net/minecraft/core/IdMap'
                            && methodInsn.name == ASMAPI.mapMethod('m_7447_')) {
                            method.instructions.set(insn, target);
                            return method;
                        }
                    }
                }

                throw new Error("Failed to patch ChunkBiomeContainer.class");
            }
        }
    }
}