package com.mediev.charcreation.mixin;

import com.mediev.charcreation.lock.PlayerLockManager;
import net.minecraft.network.packet.c2s.play.CommandExecutionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class ServerPlayNetworkHandlerMixin {

    @Inject(method = "onPlayerMove", at = @At("HEAD"), cancellable = true)
    private void charcreation$onPlayerMove(PlayerMoveC2SPacket packet, CallbackInfo ci) {
        ServerPlayNetworkHandler self = (ServerPlayNetworkHandler) (Object) this;
        if (PlayerLockManager.isLocked(self.player.getUuid())) {
            ci.cancel();
        }
    }

    @Inject(method = "onCommandExecution", at = @At("HEAD"), cancellable = true)
    private void charcreation$onCommandExecution(CommandExecutionC2SPacket packet, CallbackInfo ci) {
        ServerPlayNetworkHandler self = (ServerPlayNetworkHandler) (Object) this;
        if (PlayerLockManager.isLocked(self.player.getUuid())) {
            ci.cancel();
        }
    }
}