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

            // private static replaceMissingSections(Lnet/minecraft/world/level/LevelHeightAccessor;Lnet/minecraft/core/Registry;[Lnet/minecraft/world/level/chunk/LevelChunkSection;)V
            //    L0
            //     LINENUMBER 102 L0
            //     ICONST_0
            //     ISTORE 3
            //    L1
            //    FRAME APPEND [I]
            //     ILOAD 3
            //     ALOAD 2
            //     ARRAYLENGTH
            //     IF_ICMPGE L2
            //    L3
            //     LINENUMBER 103 L3
            //     ALOAD 2
            //     ILOAD 3
            //     AALOAD
            //     IFNONNULL L4
            //    L5
            //     LINENUMBER 104 L5
            //     ALOAD 2
            //     ILOAD 3
            //     NEW net/minecraft/world/level/chunk/LevelChunkSection
            //     DUP
            //     ALOAD 0
            //     ILOAD 3
            //     INVOKEINTERFACE net/minecraft/world/level/LevelHeightAccessor.getSectionYFromSectionIndex (I)I (itf)
            //     ALOAD 1
            //     INVOKESPECIAL net/minecraft/world/level/chunk/LevelChunkSection.<init> (ILnet/minecraft/core/Registry;)V
            //     AASTORE
            //    L4
            //     LINENUMBER 102 L4
            //    FRAME SAME
            //     IINC 3 1
            //     GOTO L1
            //    L2
            //     LINENUMBER 108 L2
            //    FRAME CHOP 1
            //     RETURN
            //    L6
            //     LOCALVARIABLE i I L1 L2 3
            //     LOCALVARIABLE p_187635_ Lnet/minecraft/world/level/LevelHeightAccessor; L0 L6 0
            //     LOCALVARIABLE p_187636_ Lnet/minecraft/core/Registry; L0 L6 1
            //     // signature Lnet/minecraft/core/Registry<Lnet/minecraft/world/level/biome/Biome;>;
            //     // declaration: p_187636_ extends net.minecraft.core.Registry<net.minecraft.world.level.biome.Biome>
            //     LOCALVARIABLE p_187637_ [Lnet/minecraft/world/level/chunk/LevelChunkSection; L0 L6 2
            //     MAXSTACK = 6
            //     MAXLOCALS = 4
        }
    }
}