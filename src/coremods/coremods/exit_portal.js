"use strict";
Object.defineProperty(exports, "__esModule", {value: true});
var coremods_1 = require("coremods");
function initializeCoreMod() {
    return {
        'redirect_exit_portal': {
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraft.server.level.ServerPlayer',
                'methodName': 'm_183318_',
                'methodDesc': '(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;ZLnet/minecraft/world/level/border/WorldBorder;)Ljava/util/Optional;'
            },
            'transformer': function (method) {
                var target = new coremods_1.InsnList();
                // target.add(new VarInsnNode(Opcodes.ALOAD, 0)); // this already loaded
                target.add(new coremods_1.VarInsnNode(coremods_1.Opcodes.ALOAD, 1)); // destination
                target.add(new coremods_1.VarInsnNode(coremods_1.Opcodes.ALOAD, 2)); // findFrom
                target.add(new coremods_1.VarInsnNode(coremods_1.Opcodes.ILOAD, 3)); // isToNether
                target.add(new coremods_1.VarInsnNode(coremods_1.Opcodes.ALOAD, 4)); // worldBorder
                target.add(new coremods_1.MethodInsnNode(coremods_1.Opcodes.INVOKESTATIC, 'de/melanx/skyblockbuilder/util/CoreUtil', 'getExitPortal', '(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;ZLnet/minecraft/world/level/border/WorldBorder;)Ljava/util/Optional;'));
                target.add(new coremods_1.InsnNode(coremods_1.Opcodes.ARETURN));
                for (var i = 0; i < method.instructions.size(); i++) {
                    var node = method.instructions.get(i);
                    if (node.getOpcode() == coremods_1.Opcodes.INVOKEVIRTUAL) {
                        var methodNode = node;
                        if (methodNode.owner == 'net/minecraft/server/level/ServerPlayer'
                            && methodNode.name == coremods_1.ASMAPI.mapMethod('m_9236_')) {
                            method.instructions.insertBefore(methodNode, target);
                            return method;
                        }
                    }
                }
                throw new Error('Failed to patch ServerPlayer.class');
            }
        }
    };
}
