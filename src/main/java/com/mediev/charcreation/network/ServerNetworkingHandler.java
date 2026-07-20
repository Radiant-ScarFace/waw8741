package com.mediev.charcreation.network;

import com.mediev.charcreation.data.CharacterData;
import com.mediev.charcreation.data.CharacterDataManager;
import com.mediev.charcreation.lock.PlayerLockManager;
import com.mediev.charcreation.validation.CharacterValidationResult;
import com.mediev.charcreation.validation.CharacterValidator;
import com.mediev.charcreation.validation.NameValidator;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

public final class ServerNetworkingHandler {
    private ServerNetworkingHandler() {
    }

    public static void register() {
        ServerPlayNetworking.registerGlobalReceiver(NetworkingConstants.SUBMIT_CHARACTER, (server, player, handler, buf, sender) -> {
            CharacterData data;
            try {
                data = CharacterData.readFields(buf, player.getUuid());
            } catch (Exception e) {
                server.execute(() -> sendResult(player, false, "Invalid submission."));
                return;
            }
            server.execute(() -> processSubmission(server, player, data));
        });
    }

    private static void processSubmission(MinecraftServer server, ServerPlayerEntity player, CharacterData data) {
        if (!PlayerLockManager.isLocked(player.getUuid())) {
            return;
        }
        CharacterValidationResult result = CharacterValidator.validate(
                data.getFirstName(), data.getLastName(), data.getBirthDay(), data.getBirthMonth(), data.getBirthYear()
        );
        if (!result.isValid()) {
            sendResult(player, false, result.getErrorMessage());
            return;
        }
        CharacterData normalized = new CharacterData(
                player.getUuid(),
                NameValidator.normalize(data.getFirstName()),
                NameValidator.normalize(data.getLastName()),
                data.getNationality(),
                data.getGender(),
                data.getBirthDay(),
                data.getBirthMonth(),
                data.getBirthYear(),
                data.getBackground()
        );
        CharacterDataManager.get(server).saveCharacter(normalized);
        PlayerLockManager.unlock(player.getUuid());
        sendResult(player, true, "");
    }

    private static void sendResult(ServerPlayerEntity player, boolean success, String message) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeBoolean(success);
        buf.writeString(message);
        ServerPlayNetworking.send(player, NetworkingConstants.SUBMIT_RESULT, buf);
    }
}