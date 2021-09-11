function initializeCoreMod() {
  return {
    'biomefix': {
      'target': {
        'type': 'METHOD',
        'class': 'net.minecraft.world.level.chunk.ChunkBiomeContainer',
        'methodName': 'm_62131_',
        'methodDesc': '()[I'
      },
      'transformer': function(method) {
        var ASMAPI = Java.type('net.minecraftforge.coremod.api.ASMAPI');
        var Opcodes = Java.type('org.objectweb.asm.Opcodes');
        var VarInsnNode = Java.type('org.objectweb.asm.tree.VarInsnNode');
        var InsnList = Java.type('org.objectweb.asm.tree.InsnList');

        var target = ASMAPI.buildMethodCall(
            'de/melanx/skyblockbuilder/core/BiomeFix',
            'getId', '(Lnet/minecraft/core/IdMap;Ljava/lang/Object;)I',
            ASMAPI.MethodType.STATIC
        );

        for (var i = 0; i < method.instructions.size(); i++) {
          var inst = method.instructions.get(i);
          if (inst.getOpcode() == Opcodes.INVOKEINTERFACE
              && inst.owner == 'net/minecraft/core/IdMap'
              && inst.name == ASMAPI.mapMethod('m_7447_')) {
            method.instructions.set(inst, target);
            return method;
          }
        }
        throw new Error("Failed to patch ChunkBiomeContainer.class");
      }
    }
  }
}
