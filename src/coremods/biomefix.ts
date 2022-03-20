import {
    ASMAPI,
    CoreMods,
    InsnList,
    InsnNode,
    JumpInsnNode,
    LabelNode,
    MethodInsnNode,
    MethodNode,
    Opcodes,
    TypeInsnNode,
    VarInsnNode
} from "coremods";

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
        'chunk_biome_write_fix': {
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

                throw new Error("Failed to patch 'write' ChunkSerializer.class");
            }
        },
        'chunk_biome_read_fix': {
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraft.world.level.chunk.storage.ChunkSerializer',
                'methodName': 'm_188230_',
                'methodDesc': '(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/ai/village/poi/PoiManager;Lnet/minecraft/world/level/ChunkPos;Lnet/minecraft/nbt/CompoundTag;)Lnet/minecraft/world/level/chunk/ProtoChunk;'
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

                throw new Error("Failed to patch 'read' ChunkSerializer.class");
            }
        },
        'find_nearest_biome_redirection': {
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraft.server.level.ServerLevel',
                'methodName': 'm_8705_',
                'methodDesc': '(Lnet/minecraft/world/level/biome/Biome;Lnet/minecraft/core/BlockPos;II)Lnet/minecraft/core/BlockPos;'
            },
            'transformer': function (method: MethodNode) {
                const target = new InsnList();
                const label = new LabelNode();
                target.add(new LabelNode());
                target.add(new VarInsnNode(Opcodes.ALOAD, 0));
                target.add(ASMAPI.buildMethodCall('net/minecraft/server/level/ServerLevel',
                    ASMAPI.mapMethod('m_7726_'), '()Lnet/minecraft/server/level/ServerChunkCache;',
                    ASMAPI.MethodType.VIRTUAL));
                target.add(ASMAPI.buildMethodCall('net/minecraft/server/level/ServerChunkCache',
                    ASMAPI.mapMethod('m_8481_'), '()Lnet/minecraft/world/level/chunk/ChunkGenerator;',
                    ASMAPI.MethodType.VIRTUAL));
                target.add(new TypeInsnNode(Opcodes.INSTANCEOF, 'de/melanx/skyblockbuilder/world/dimensions/multinoise/SkyblockNoiseBasedChunkGenerator'));
                target.add(new JumpInsnNode(Opcodes.IFEQ, label));

                target.add(new LabelNode());
                target.add(new VarInsnNode(Opcodes.ALOAD, 0));
                target.add(new VarInsnNode(Opcodes.ALOAD, 1));
                target.add(new VarInsnNode(Opcodes.ALOAD, 2));
                target.add(new VarInsnNode(Opcodes.ILOAD, 3));
                target.add(new VarInsnNode(Opcodes.ILOAD, 4));
                target.add(ASMAPI.buildMethodCall('de/melanx/skyblockbuilder/core/BiomeFix',
                    'findNearestBiome', '(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/level/biome/Biome;Lnet/minecraft/core/BlockPos;II)Lnet/minecraft/core/BlockPos;',
                    ASMAPI.MethodType.STATIC));
                target.add(new InsnNode(Opcodes.ARETURN));
                target.add(label);

                method.instructions.insert(target);

                return method;
            }
        },
        'replace_missing_sections': {
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraft.world.level.chunk.ChunkAccess',
                'methodName': 'm_187634_',
                'methodDesc': '(Lnet/minecraft/world/level/LevelHeightAccessor;Lnet/minecraft/core/Registry;[Lnet/minecraft/world/level/chunk/LevelChunkSection;)V'
            },
            transformer: function (method: MethodNode) {
                const target = new InsnList();
                target.add(new VarInsnNode(Opcodes.ALOAD, 0));
                target.add(new VarInsnNode(Opcodes.ALOAD, 1));
                target.add(new VarInsnNode(Opcodes.ALOAD, 2));
                target.add(ASMAPI.buildMethodCall('de/melanx/skyblockbuilder/core/BiomeFix',
                    'replaceMissingSections', '(Lnet/minecraft/world/level/LevelHeightAccessor;Lnet/minecraft/core/Registry;[Lnet/minecraft/world/level/chunk/LevelChunkSection;)V',
                    ASMAPI.MethodType.STATIC));
                target.add(new InsnNode(Opcodes.RETURN));

                method.instructions.insert(target);

                return method;
            }
        }
    }
}