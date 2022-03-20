"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
var coremods_1 = require("coremods");
function initializeCoreMod() {
    return {
        'palette_biome_fix': {
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraft.world.level.chunk.LinearPalette',
                'methodName': 'm_5678_',
                'methodDesc': '(Lnet/minecraft/network/FriendlyByteBuf;)V'
            },
            'transformer': function (method) {
                var target = coremods_1.ASMAPI.buildMethodCall('de/melanx/skyblockbuilder/core/BiomeFix', 'getId', '(Lnet/minecraft/core/IdMap;Ljava/lang/Object;)I', coremods_1.ASMAPI.MethodType.STATIC);
                for (var i = 0; i < method.instructions.size(); i++) {
                    var insn = method.instructions.get(i);
                    if (insn != null && insn.getOpcode() == coremods_1.Opcodes.INVOKEINTERFACE) {
                        var methodInsn = insn;
                        if (methodInsn.owner == 'net/minecraft/core/IdMap'
                            && methodInsn.name == coremods_1.ASMAPI.mapMethod('m_7447_')) {
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
            'transformer': function (method) {
                for (var i = 0; i < method.instructions.size(); i++) {
                    var insn = method.instructions.get(i);
                    if (insn != null && insn.getOpcode() == coremods_1.Opcodes.INVOKESTATIC) {
                        var methodInsn = insn;
                        if (methodInsn.owner == 'net/minecraft/world/level/chunk/storage/ChunkSerializer'
                            && methodInsn.name == coremods_1.ASMAPI.mapMethod('m_188260_')) {
                            method.instructions.insertBefore(insn, new coremods_1.VarInsnNode(coremods_1.Opcodes.ALOAD, 0));
                            method.instructions.set(insn, coremods_1.ASMAPI.buildMethodCall('de/melanx/skyblockbuilder/core/BiomeFix', 'modifiedCodec', '(Lnet/minecraft/core/Registry;Lnet/minecraft/server/level/ServerLevel;)Lcom/mojang/serialization/Codec;', coremods_1.ASMAPI.MethodType.STATIC));
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
            'transformer': function (method) {
                for (var i = 0; i < method.instructions.size(); i++) {
                    var insn = method.instructions.get(i);
                    if (insn != null && insn.getOpcode() == coremods_1.Opcodes.INVOKESTATIC) {
                        var methodInsn = insn;
                        if (methodInsn.owner == 'net/minecraft/world/level/chunk/storage/ChunkSerializer'
                            && methodInsn.name == coremods_1.ASMAPI.mapMethod('m_188260_')) {
                            method.instructions.insertBefore(insn, new coremods_1.VarInsnNode(coremods_1.Opcodes.ALOAD, 0));
                            method.instructions.set(insn, coremods_1.ASMAPI.buildMethodCall('de/melanx/skyblockbuilder/core/BiomeFix', 'modifiedCodec', '(Lnet/minecraft/core/Registry;Lnet/minecraft/server/level/ServerLevel;)Lcom/mojang/serialization/Codec;', coremods_1.ASMAPI.MethodType.STATIC));
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
            'transformer': function (method) {
                var target = new coremods_1.InsnList();
                var label = new coremods_1.LabelNode();
                target.add(new coremods_1.LabelNode());
                target.add(new coremods_1.VarInsnNode(coremods_1.Opcodes.ALOAD, 0));
                target.add(coremods_1.ASMAPI.buildMethodCall('net/minecraft/server/level/ServerLevel', coremods_1.ASMAPI.mapMethod('m_7726_'), '()Lnet/minecraft/server/level/ServerChunkCache;', coremods_1.ASMAPI.MethodType.VIRTUAL));
                target.add(coremods_1.ASMAPI.buildMethodCall('net/minecraft/server/level/ServerChunkCache', coremods_1.ASMAPI.mapMethod('m_8481_'), '()Lnet/minecraft/world/level/chunk/ChunkGenerator;', coremods_1.ASMAPI.MethodType.VIRTUAL));
                target.add(new coremods_1.TypeInsnNode(coremods_1.Opcodes.INSTANCEOF, 'de/melanx/skyblockbuilder/world/dimensions/multinoise/SkyblockNoiseBasedChunkGenerator'));
                target.add(new coremods_1.JumpInsnNode(coremods_1.Opcodes.IFEQ, label));
                target.add(new coremods_1.LabelNode());
                target.add(new coremods_1.VarInsnNode(coremods_1.Opcodes.ALOAD, 0));
                target.add(new coremods_1.VarInsnNode(coremods_1.Opcodes.ALOAD, 1));
                target.add(new coremods_1.VarInsnNode(coremods_1.Opcodes.ALOAD, 2));
                target.add(new coremods_1.VarInsnNode(coremods_1.Opcodes.ILOAD, 3));
                target.add(new coremods_1.VarInsnNode(coremods_1.Opcodes.ILOAD, 4));
                target.add(coremods_1.ASMAPI.buildMethodCall('de/melanx/skyblockbuilder/core/BiomeFix', 'findNearestBiome', '(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/level/biome/Biome;Lnet/minecraft/core/BlockPos;II)Lnet/minecraft/core/BlockPos;', coremods_1.ASMAPI.MethodType.STATIC));
                target.add(new coremods_1.InsnNode(coremods_1.Opcodes.ARETURN));
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
            transformer: function (method) {
                var target = new coremods_1.InsnList();
                target.add(new coremods_1.VarInsnNode(coremods_1.Opcodes.ALOAD, 0));
                target.add(new coremods_1.VarInsnNode(coremods_1.Opcodes.ALOAD, 1));
                target.add(new coremods_1.VarInsnNode(coremods_1.Opcodes.ALOAD, 2));
                target.add(coremods_1.ASMAPI.buildMethodCall('de/melanx/skyblockbuilder/core/BiomeFix', 'replaceMissingSections', '(Lnet/minecraft/world/level/LevelHeightAccessor;Lnet/minecraft/core/Registry;[Lnet/minecraft/world/level/chunk/LevelChunkSection;)V', coremods_1.ASMAPI.MethodType.STATIC));
                target.add(new coremods_1.InsnNode(coremods_1.Opcodes.RETURN));
                method.instructions.insert(target);
                return method;
            }
        }
    };
}
