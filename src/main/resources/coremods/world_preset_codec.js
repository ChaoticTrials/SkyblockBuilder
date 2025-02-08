function initializeCoreMod() {
    var Opcodes = Java.type('org.objectweb.asm.Opcodes');
    var InsnList = Java.type('org.objectweb.asm.tree.InsnList');
    var MethodInsnNode = Java.type('org.objectweb.asm.tree.MethodInsnNode');

    return {
        'modify_codec': {
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraft.world.level.levelgen.presets.WorldPreset',
                'methodName': '<clinit>',
                'methodDesc': '()V'
            },
            'transformer': function (method) {
                const target = new InsnList();
                target.add(new MethodInsnNode(
                    Opcodes.INVOKESTATIC,
                    'de/melanx/skyblockbuilder/util/CoreUtil',
                    'augmentWorldPresetCodec',
                    '(Lcom/mojang/serialization/Codec;)Lcom/mojang/serialization/Codec;',
                    false
                ));
                for (let i = 0; i < method.instructions.size(); i++) {
                    const node = method.instructions.get(i);
                    if (node.getOpcode() == Opcodes.PUTSTATIC) {
                        var fieldNode = node;
                        if (fieldNode.owner == 'net/minecraft/world/level/levelgen/presets/WorldPreset' && fieldNode.name == 'DIRECT_CODEC') {
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
