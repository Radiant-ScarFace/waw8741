package com.mediev.charcreation;

import com.mediev.charcreation.command.CharacterCommand;
import com.mediev.charcreation.event.PlayerJoinHandler;
import com.mediev.charcreation.lock.PlayerLockManager;
import com.mediev.charcreation.network.ServerNetworkingHandler;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

public final class CharacterCreationMod implements ModInitializer {
    @Override
    public void onInitialize() {
        ServerNetworkingHandler.register();
        PlayerLockManager.registerEvents();
        PlayerJoinHandler.register();
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                CharacterCommand.register(dispatcher));
    }
}