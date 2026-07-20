package com.mediev.charcreation.lock;

import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.util.ActionResult;
import net.minecraft.util.TypedActionResult;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class PlayerLockManager {
    private static final Set<UUID> LOCKED_PLAYERS = Collections.synchronizedSet(new HashSet<>());

    private PlayerLockManager() {
    }

    public static void lock(UUID playerId) {
        LOCKED_PLAYERS.add(playerId);
    }

    public static void unlock(UUID playerId) {
        LOCKED_PLAYERS.remove(playerId);
    }

    public static boolean isLocked(UUID playerId) {
        return LOCKED_PLAYERS.contains(playerId);
    }

    public static void registerEvents() {
        AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) ->
                isLocked(player.getUuid()) ? ActionResult.FAIL : ActionResult.PASS);
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) ->
                isLocked(player.getUuid()) ? ActionResult.FAIL : ActionResult.PASS);
        UseItemCallback.EVENT.register((player, world, hand) ->
                isLocked(player.getUuid())
                        ? TypedActionResult.fail(player.getStackInHand(hand))
                        : TypedActionResult.pass(player.getStackInHand(hand)));
        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) ->
                isLocked(player.getUuid()) ? ActionResult.FAIL : ActionResult.PASS);
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) ->
                isLocked(player.getUuid()) ? ActionResult.FAIL : ActionResult.PASS);
        PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, blockEntity) ->
                !isLocked(player.getUuid()));
    }
}