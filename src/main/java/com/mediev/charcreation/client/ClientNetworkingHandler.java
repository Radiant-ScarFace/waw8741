package com.mediev.charcreation.client;

import com.mediev.charcreation.client.gui.CharacterCreationScreen;
import com.mediev.charcreation.network.NetworkingConstants;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public final class ClientNetworkingHandler {
    private ClientNetworkingHandler() {
    }

    public static void register() {
        ClientPlayNetworking.registerGlobalReceiver(NetworkingConstants.OPEN_CREATION_SCREEN, (client, handler, buf, sender) ->
                client.execute(() -> client.setScreen(new CharacterCreationScreen())));

        ClientPlayNetworking.registerGlobalReceiver(NetworkingConstants.SUBMIT_RESULT, (client, handler, buf, sender) -> {
            boolean success = buf.readBoolean();
            String message = buf.readString(256);
            client.execute(() -> {
                if (client.currentScreen instanceof CharacterCreationScreen screen) {
                    if (success) {
                        client.setScreen(null);
                    } else {
                        screen.showServerError(message);
                    }
                }
            });
        });
    }
}