import {AbstractInsnNode, ASMAPI, CoreMods, InsnList, MethodInsnNode, MethodNode, Opcodes, VarInsnNode} from "coremods";

function initializeCoreMod(): CoreMods {
    return {
        'palette_biome_fix': {
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraft.world.level.chunk.LinearPalette',
                'methodName': 'm_5678_',
                'methodDesc': '(Lnet/minecraft/network/FriendlyByteBuf;)V'
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

                throw new Error("Failed to patch LinearPalette.class");
            }
        },
        'chunk_biome_fix': {
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraft.world.level.chunk.storage.ChunkSerializer',
                'methodName': 'm_63454_',
                'methodDesc': '(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/level/chunk/ChunkAccess;)Lnet/minecraft/nbt/CompoundTag;'
            },
            'transformer': function (method: MethodNode) {
                for (let i = 0; i < method.instructions.size(); i++) {
                    const insn = method.instructions.get(i);
                    if (insn != null && insn.getOpcode() == Opcodes.INVOKESTATIC) {
                        const methodInsn = insn as MethodInsnNode;
                        if (methodInsn.owner == 'net/minecraft/world/level/chunk/storage/ChunkSerializer'
                            && methodInsn.name == ASMAPI.mapMethod('m_188260_')) {
                            method.instructions.insertBefore(insn, new VarInsnNode(Opcodes.ALOAD, 0));
                            method.instructions.set(insn, ASMAPI.buildMethodCall(
                                'de/melanx/skyblockbuilder/core/BiomeFix',
                                'modifiedCodec', '(Lnet/minecraft/core/Registry;Lnet/minecraft/server/level/ServerLevel;)Lcom/mojang/serialization/Codec;',
                                ASMAPI.MethodType.STATIC
                            ));
                            return method;
                        }
                    }
                }

                throw new Error("Failed to patch ChunkSerializer.class");
            }
        },
        'find_structure_biome_fix': {
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraft.world.level.chunk.ChunkGenerator',
                'methodName': 'm_62161_',
                'methodDesc': '(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/level/levelgen/feature/StructureFeature;Lnet/minecraft/core/BlockPos;IZ)Lnet/minecraft/core/BlockPos;'
            },
            'transformer': function (method: MethodNode) {
                let insertAfter: AbstractInsnNode | null = null;

                for (let i = 0; i < method.instructions.size(); i++) {
                    const insn = method.instructions.get(i);
                    if (insn != null && insn.getOpcode() == Opcodes.INVOKEVIRTUAL) {
                        const methodInsn = insn as MethodInsnNode;
                        if (methodInsn.owner == 'net/minecraft/core/RegistryAccess'
                            && methodInsn.name == ASMAPI.mapMethod('m_175515_')) {
                            const nextInsn = insn.getNext();
                            if (nextInsn != null) {
                                insertAfter = nextInsn;
                                break;
                            }
                        }
                    }
                }

                if (insertAfter == null) throw new Error("Failed to patch ChunkGenerator.class");

                const target = new InsnList();
                target.add(new VarInsnNode(Opcodes.ALOAD, 8));
                target.add(new VarInsnNode(Opcodes.ALOAD, 1));
                target.add(ASMAPI.buildMethodCall(
                    'de/melanx/skyblockbuilder/core/BiomeFix',
                    'modifiedRegistry', '(Lnet/minecraft/core/Registry;Lnet/minecraft/server/level/ServerLevel;)Lnet/minecraft/core/Registry;',
                    ASMAPI.MethodType.STATIC
                ));
                target.add(new VarInsnNode(Opcodes.ASTORE, 8));

                method.instructions.insert(insertAfter, target);

                return method;
            }
        }
    }
}