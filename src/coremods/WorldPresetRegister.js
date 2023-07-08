"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
var coremods_1 = require("coremods");
function initializeCoreMod() {
    return {
        'registry_load': {
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraft.server.WorldLoader',
                'methodName': 'm_214362_',
                'methodDesc': '(Lnet/minecraft/server/WorldLoader$InitConfig;Lnet/minecraft/server/WorldLoader$WorldDataSupplier;Lnet/minecraft/server/WorldLoader$ResultFactory;Ljava/util/concurrent/Executor;Ljava/util/concurrent/Executor;)Ljava/util/concurrent/CompletableFuture;'
            },
            'transformer': function (method) {
                var target = new coremods_1.InsnList();
                target.add(new coremods_1.InsnNode(coremods_1.Opcodes.DUP));
                target.add(new coremods_1.MethodInsnNode(coremods_1.Opcodes.INVOKESTATIC, 'de/melanx/skyblockbuilder/util/CoreUtil', 'afterWorldGenLayerLoad', '(Lnet/minecraft/core/LayeredRegistryAccess;)V'));
                var foundWorldGenField = false;
                for (var i = 0; i < method.instructions.size(); i++) {
                    var node = method.instructions.get(i);
                    if (node.getOpcode() == coremods_1.Opcodes.GETSTATIC && !foundWorldGenField) {
                        var fieldNode = node;
                        if (fieldNode.owner == 'net/minecraft/server/RegistryLayer' && fieldNode.name == 'WORLDGEN') {
                            foundWorldGenField = true;
                        }
                    }
                    else if (node.getOpcode() == coremods_1.Opcodes.INVOKESTATIC && foundWorldGenField) {
                        var methodNode = node;
                        if (methodNode.owner == 'net/minecraft/server/WorldLoader' && methodNode.name == coremods_1.ASMAPI.mapMethod('m_245736_') && methodNode.desc == '(Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/core/LayeredRegistryAccess;Lnet/minecraft/server/RegistryLayer;Ljava/util/List;)Lnet/minecraft/core/LayeredRegistryAccess;') {
                            method.instructions.insert(node, target);
                            return method;
                        }
                    }
                }
                throw new Error('Failed to patch WorldLoader.class');
            }
        }
    };
}
