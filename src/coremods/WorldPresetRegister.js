"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
var coremods_1 = require("coremods");
var coremods_2 = require("./coremods");
function initializeCoreMod() {
    return {
        'WorldPresetRegister': {
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraft.world.level.levelgen.presets.WorldPreset',
                'methodName': 'm_238378_',
                'methodDesc': '(Lnet/minecraft/world/level/levelgen/presets/WorldPreset;)Lcom/mojang/serialization/DataResult;'
            },
            'transformer': function (method) {
                var target = new coremods_1.InsnList();
                target.add(new coremods_1.VarInsnNode(coremods_1.Opcodes.ALOAD, 0));
                target.add(coremods_1.ASMAPI.buildMethodCall('de/melanx/skyblockbuilder/world/presets/SkyblockPreset', 'register', '(Lnet/minecraft/world/level/levelgen/presets/WorldPreset;)Lcom/mojang/serialization/DataResult;', coremods_1.ASMAPI.MethodType.STATIC));
                target.add(new coremods_2.InsnNode(coremods_1.Opcodes.ARETURN));
                method.instructions.clear();
                method.instructions.add(target);
                // const newInstructions = new InsnList();
                // newInstructions.add(target);
                // newInstructions.add(method.instructions);
                // method.instructions = newInstructions;
                return method;
            }
        }
    };
}
