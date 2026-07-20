package com.mediev.charcreation.data;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.PersistentState;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class CharacterDataManager extends PersistentState {
    private static final String STORAGE_KEY = "charcreation_data";
    private final Map<UUID, CharacterData> characters = new HashMap<>();

    public static CharacterDataManager get(MinecraftServer server) {
        return server.getOverworld().getPersistentStateManager().getOrCreate(
                CharacterDataManager::fromNbt,
                CharacterDataManager::new,
                STORAGE_KEY
        );
    }

    public boolean hasCharacter(UUID playerId) {
        return characters.containsKey(playerId);
    }

    public CharacterData getCharacter(UUID playerId) {
        return characters.get(playerId);
    }

    public void saveCharacter(CharacterData data) {
        characters.put(data.getPlayerId(), data);
        markDirty();
    }

    public void resetCharacter(UUID playerId) {
        characters.remove(playerId);
        markDirty();
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        NbtList list = new NbtList();
        for (CharacterData data : characters.values()) {
            list.add(data.toNbt());
        }
        nbt.put("Characters", list);
        return nbt;
    }

    private static CharacterDataManager fromNbt(NbtCompound nbt) {
        CharacterDataManager manager = new CharacterDataManager();
        NbtList list = nbt.getList("Characters", NbtElement.COMPOUND_TYPE);
        for (int i = 0; i < list.size(); i++) {
            CharacterData data = CharacterData.fromNbt(list.getCompound(i));
            manager.characters.put(data.getPlayerId(), data);
        }
        return manager;
    }
}