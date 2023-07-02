import {
    ASMAPI,
    CoreMods,
    InsnList,
    MethodNode,
    Opcodes,
    VarInsnNode
} from "coremods";
import {InsnNode} from "./coremods";

function initializeCoreMod(): CoreMods {
    return {
        'WorldPresetRegister': {
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraft.world.level.levelgen.presets.WorldPreset',
                'methodName': 'm_238378_',
                'methodDesc': '(Lnet/minecraft/world/level/levelgen/presets/WorldPreset;)Lcom/mojang/serialization/DataResult;'
            },
            'transformer': function (method: MethodNode) {
                const target = new InsnList();
                target.add(new VarInsnNode(Opcodes.ALOAD, 0));
                target.add(ASMAPI.buildMethodCall(
                    'de/melanx/skyblockbuilder/world/presets/SkyblockPreset',
                    'register', '(Lnet/minecraft/world/level/levelgen/presets/WorldPreset;)Lcom/mojang/serialization/DataResult;',
                    ASMAPI.MethodType.STATIC
                ));
                target.add(new InsnNode(Opcodes.ARETURN))

                method.instructions.clear();
                method.instructions.add(target);
                // const newInstructions = new InsnList();
                // newInstructions.add(target);
                // newInstructions.add(method.instructions);
                // method.instructions = newInstructions;
                return method;
            }
        }
    }
}
