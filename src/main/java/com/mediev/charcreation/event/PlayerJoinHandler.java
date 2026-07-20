package com.mediev.charcreation.event;

import com.mediev.charcreation.data.CharacterDataManager;
import com.mediev.charcreation.lock.PlayerLockManager;
import com.mediev.charcreation.network.NetworkingConstants;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;

public final class PlayerJoinHandler {
    private PlayerJoinHandler() {
    }

    public static void register() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity player = handler.getPlayer();
            CharacterDataManager manager = CharacterDataManager.get(server);
            if (!manager.hasCharacter(player.getUuid())) {
                PlayerLockManager.lock(player.getUuid());
                ServerPlayNetworking.send(player, NetworkingConstants.OPEN_CREATION_SCREEN, PacketByteBufs.create());
            }
        });
    }
}