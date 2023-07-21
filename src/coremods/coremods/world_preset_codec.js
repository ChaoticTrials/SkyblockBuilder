"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
var coremods_1 = require("coremods");
function initializeCoreMod() {
    return {
        'modify_codec': {
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraft.world.level.levelgen.presets.WorldPreset',
                'methodName': '<clinit>',
                'methodDesc': '()V'
            },
            'transformer': function (method) {
                var target = new coremods_1.InsnList();
                target.add(new coremods_1.MethodInsnNode(coremods_1.Opcodes.INVOKESTATIC, 'de/melanx/skyblockbuilder/util/CoreUtil', 'augmentWorldPresetCodec', '(Lcom/mojang/serialization/Codec;)Lcom/mojang/serialization/Codec;'));
                for (var i = 0; i < method.instructions.size(); i++) {
                    var node = method.instructions.get(i);
                    if (node.getOpcode() == coremods_1.Opcodes.PUTSTATIC) {
                        var fieldNode = node;
                        if (fieldNode.owner == 'net/minecraft/world/level/levelgen/presets/WorldPreset' && fieldNode.name == coremods_1.ASMAPI.mapField('f_226414_')) {
                            method.instructions.insertBefore(fieldNode, target);
                            return method;
                        }
                    }
                }
                throw new Error('Failed to patch WorldPreset.class');
            }
        }
    };
}
