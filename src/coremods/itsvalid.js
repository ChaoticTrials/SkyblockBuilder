"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
var coremods_1 = require("./coremods");
function initializeCoreMod() {
    return {
        'it_is_valid_registry': {
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraft.core.Holder$Reference',
                'methodName': 'm_203401_',
                'methodDesc': '(Lnet/minecraft/core/Registry;)Z'
            },
            'transformer': function (method) {
                var target = new coremods_1.InsnList();
                target.add(new coremods_1.VarInsnNode(coremods_1.Opcodes.ALOAD, 0));
                target.add(new coremods_1.FieldInsnNode(coremods_1.Opcodes.GETFIELD, 'net/minecraft/core/Holder$Reference', coremods_1.ASMAPI.mapField('f_205748_'), 'Lnet/minecraft/core/Registry;'));
                target.add(new coremods_1.VarInsnNode(coremods_1.Opcodes.ALOAD, 1));
                target.add(coremods_1.ASMAPI.buildMethodCall('de/melanx/skyblockbuilder/core/BiomeFix', 'isValidRegistry', '(Lnet/minecraft/core/Registry;Lnet/minecraft/core/Registry;)Z', coremods_1.ASMAPI.MethodType.STATIC));
                target.add(new coremods_1.InsnNode(coremods_1.Opcodes.IRETURN));
                method.instructions.insert(target);
                return method;
            }
        }
    };
}
