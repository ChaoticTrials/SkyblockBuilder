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
        'chunk_biome_fix': {
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
                throw new Error("Failed to patch ChunkSerializer.class");
            }
        }
    };
}
