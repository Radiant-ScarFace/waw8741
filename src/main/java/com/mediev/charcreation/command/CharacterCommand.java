package com.mediev.charcreation.command;

import com.mediev.charcreation.data.CharacterData;
import com.mediev.charcreation.data.CharacterDataManager;
import com.mediev.charcreation.lock.PlayerLockManager;
import com.mediev.charcreation.network.NetworkingConstants;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public final class CharacterCommand {
    private CharacterCommand() {
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("character")
                .requires(source -> source.hasPermissionLevel(4))
                .then(CommandManager.literal("reset")
                        .then(CommandManager.argument("player", EntityArgumentType.player())
                                .executes(CharacterCommand::executeReset)))
                .then(CommandManager.literal("info")
                        .then(CommandManager.argument("player", EntityArgumentType.player())
                                .executes(CharacterCommand::executeInfo)))
        );
    }

    private static int executeReset(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity target = EntityArgumentType.getPlayer(context, "player");
        CharacterDataManager.get(context.getSource().getServer()).resetCharacter(target.getUuid());
        PlayerLockManager.lock(target.getUuid());
        ServerPlayNetworking.send(target, NetworkingConstants.OPEN_CREATION_SCREEN, PacketByteBufs.create());
        context.getSource().sendFeedback(() -> Text.literal("Character reset for " + target.getGameProfile().getName() + "."), true);
        return 1;
    }

    private static int executeInfo(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity target = EntityArgumentType.getPlayer(context, "player");
        CharacterData data = CharacterDataManager.get(context.getSource().getServer()).getCharacter(target.getUuid());
        if (data == null) {
            context.getSource().sendFeedback(() -> Text.literal(target.getGameProfile().getName() + " has no character created."), false);
            return 0;
        }
        context.getSource().sendFeedback(() -> Text.literal(
                data.getFirstName() + " " + data.getLastName() + " | " +
                        data.getNationality().getDisplayName() + " | " +
                        data.getGender().getDisplayName() + " | " +
                        data.getBirthDay() + "/" + data.getBirthMonth() + "/" + data.getBirthYear() + " | " +
                        data.getBackground().getDisplayName()
        ), false);
        return 1;
    }
}