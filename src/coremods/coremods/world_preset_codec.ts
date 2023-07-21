import { AbstractInsnNode, ASMAPI, CoreMods, FieldInsnNode, InsnList, InsnNode, MethodInsnNode, MethodNode, Opcodes } from "coremods";

function initializeCoreMod(): CoreMods {
  return {
    'modify_codec': {
      'target': {
        'type': 'METHOD',
        'class': 'net.minecraft.world.level.levelgen.presets.WorldPreset',
        'methodName': '<clinit>',
        'methodDesc': '()V'
      },
      'transformer': (method: MethodNode) => {
        const target = new InsnList();
        target.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
          'de/melanx/skyblockbuilder/util/CoreUtil',
          'augmentWorldPresetCodec', '(Lcom/mojang/serialization/Codec;)Lcom/mojang/serialization/Codec;'
        ));

        for (let i = 0; i < method.instructions.size(); i++) {
          const node = method.instructions.get(i) as AbstractInsnNode;
          if (node.getOpcode() == Opcodes.PUTSTATIC) {
            const fieldNode = node as FieldInsnNode;
            if (fieldNode.owner == 'net/minecraft/world/level/levelgen/presets/WorldPreset' && fieldNode.name == ASMAPI.mapField('f_226414_')) {
              method.instructions.insertBefore(fieldNode, target);
              return method;
            }
          }
        }

        throw new Error('Failed to patch WorldPreset.class');
      }
    }
  }
}
