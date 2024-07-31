package com.github.alexmodguy.mediumcore.misc;

import com.github.alexmodguy.mediumcore.Mediumcore;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;

import java.util.HashMap;
import java.util.UUID;

public class MediumCoreData extends SavedData
{
    public HashMap<UUID, PlayerData> players = new HashMap<>();
    private static final String HEALTH_MODIFIER_TAG = "MediumcoreHealthModifier";

    public MediumCoreData() { }

    #if AFTER_21
    private static final SavedData.Factory<MediumCoreData> type = new SavedData.Factory<>(
            MediumCoreData::new, // If there's no 'StateSaverAndLoader' yet create one
            MediumCoreData::loadServerData, // If there is a 'StateSaverAndLoader' NBT, parse it with 'createFromNbt'
            null // Supposed to be an 'DataFixTypes' enum, but we can just pass null
    );
    #endif

    @Override
    public CompoundTag save(CompoundTag tag #if AFTER_21, HolderLookup.Provider registries #endif) {
        CompoundTag list = new CompoundTag();
        players.forEach((uuid, playerData) -> {
            playerData.save(uuid, list);
        });

        tag.put("players", list);
        return tag;
    }

    public static MediumCoreData loadServerData(CompoundTag tag #if AFTER_21, HolderLookup.Provider registries #endif) {
        MediumCoreData state = new MediumCoreData();
        var data = tag.getCompound("players");
        data.getAllKeys().forEach(key -> {
            state.players.put(UUID.fromString(key), PlayerData.load(data.getCompound(key)));
        });

        return state;
    }


    public static MediumCoreData getServerState(MinecraftServer server) {
        DimensionDataStorage storage = server.getLevel(Level.OVERWORLD).getDataStorage();
        #if AFTER_21
        MediumCoreData state = storage.computeIfAbsent(type, Mediumcore.MOD_ID);
        #else
        MediumCoreData state = storage.computeIfAbsent(MediumCoreData::loadServerData, MediumCoreData::new, Mediumcore.MOD_ID);
        #endif

        state.setDirty();

        return state;
    }

    public static PlayerData getPlayerData(LivingEntity player) {
        MediumCoreData serverState = getServerState(player.getServer());

        // Either get the player by the uuid, or we don't have data for him yet, make a new player state
        PlayerData playerState = serverState.players.computeIfAbsent(player.getUUID(), uuid -> new PlayerData());

        return playerState;
    }

    public static class PlayerData {
        public double healthModifiedBy = 0;

        public static PlayerData load(CompoundTag playerData)
        {
            PlayerData ths = new PlayerData();
            ths.healthModifiedBy = playerData.getDouble(HEALTH_MODIFIER_TAG);
            return ths;
        }

        public void save(UUID uuid, CompoundTag list)
        {
            CompoundTag playerNbt = new CompoundTag();
            playerNbt.putDouble(HEALTH_MODIFIER_TAG, healthModifiedBy);

            list.put(uuid.toString(), playerNbt);
        }
    }
}

