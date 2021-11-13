"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
var coremods_1 = require("coremods");
function initializeCoreMod() {
    return {
        'biomefix': {
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraft.world.level.chunk.ChunkBiomeContainer',
                'methodName': 'm_62131_',
                'methodDesc': '()[I'
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
                throw new Error("Failed to patch ChunkBiomeContainer.class");
            }
        }
    };
}
