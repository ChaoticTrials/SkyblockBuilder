import { AbstractInsnNode, ASMAPI, CoreMods, FieldInsnNode, InsnList, InsnNode, MethodInsnNode, MethodNode, Opcodes } from "coremods";

function initializeCoreMod(): CoreMods {
  return {
    'registry_load': {
      'target': {
        'type': 'METHOD',
        'class': 'net.minecraft.server.WorldLoader',
        'methodName': 'm_214362_',
        'methodDesc': '(Lnet/minecraft/server/WorldLoader$InitConfig;Lnet/minecraft/server/WorldLoader$WorldDataSupplier;Lnet/minecraft/server/WorldLoader$ResultFactory;Ljava/util/concurrent/Executor;Ljava/util/concurrent/Executor;)Ljava/util/concurrent/CompletableFuture;'
      },
      'transformer': (method: MethodNode) => {
        const target = new InsnList();
        target.add(new InsnNode(Opcodes.DUP));
        target.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
          'de/melanx/skyblockbuilder/util/CoreUtil',
          'afterWorldGenLayerLoad', '(Lnet/minecraft/core/LayeredRegistryAccess;)V'
        ));

        let foundWorldGenField = false;
        for (let i = 0; i < method.instructions.size(); i++) {
          const node = method.instructions.get(i) as AbstractInsnNode;
          if (node.getOpcode() == Opcodes.GETSTATIC && !foundWorldGenField) {
            const fieldNode = node as FieldInsnNode;
            if (fieldNode.owner == 'net/minecraft/server/RegistryLayer' && fieldNode.name == 'WORLDGEN') {
              foundWorldGenField = true;
            }
          } else if (node.getOpcode() == Opcodes.INVOKESTATIC && foundWorldGenField) {
            const methodNode = node as MethodInsnNode;
            if (methodNode.owner == 'net/minecraft/server/WorldLoader' && methodNode.name == ASMAPI.mapMethod('m_245736_') && methodNode.desc == '(Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/core/LayeredRegistryAccess;Lnet/minecraft/server/RegistryLayer;Ljava/util/List;)Lnet/minecraft/core/LayeredRegistryAccess;') {
              method.instructions.insert(node, target);
              return method;
            }
          }
        }

        throw new Error('Failed to patch WorldLoader.class');
      }
    }
  }
}
