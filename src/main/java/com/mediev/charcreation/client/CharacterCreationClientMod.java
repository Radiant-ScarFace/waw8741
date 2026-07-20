package com.mediev.charcreation.client;

import net.fabricmc.api.ClientModInitializer;

public final class CharacterCreationClientMod implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientNetworkingHandler.register();
    }
}