import {ASMAPI, CoreMods, FieldInsnNode, InsnList, InsnNode, MethodNode, Opcodes, VarInsnNode} from "./coremods";

function initializeCoreMod(): CoreMods {
    return {
        'it_is_valid_registry': {
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraft.core.Holder$Reference',
                'methodName': 'm_203401_',
                'methodDesc': '(Lnet/minecraft/core/Registry;)Z'
            },
            'transformer': function (method: MethodNode) {
                let target = new InsnList();
                target.add(new VarInsnNode(Opcodes.ALOAD, 0));
                target.add(new FieldInsnNode(Opcodes.GETFIELD, 'net/minecraft/core/Holder$Reference',
                    ASMAPI.mapField('f_205748_'), 'Lnet/minecraft/core/Registry;'));
                target.add(new VarInsnNode(Opcodes.ALOAD, 1));
                target.add(ASMAPI.buildMethodCall('de/melanx/skyblockbuilder/core/BiomeFix', 'isValidRegistry',
                    '(Lnet/minecraft/core/Registry;Lnet/minecraft/core/Registry;)Z', ASMAPI.MethodType.STATIC));
                target.add(new InsnNode(Opcodes.IRETURN));

                method.instructions.insert(target);
                return method;
            }
        }
    }
}