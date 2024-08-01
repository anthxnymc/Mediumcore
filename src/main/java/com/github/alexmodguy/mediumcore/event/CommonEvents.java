package com.github.alexmodguy.mediumcore.event;

import com.github.alexmodguy.mediumcore.GameRuleRegistry;
import com.github.alexmodguy.mediumcore.Mediumcore;
import toni.lib.VersionUtils;
import com.github.alexmodguy.mediumcore.misc.DedicatedServerPropertiesAccessor;
import com.github.alexmodguy.mediumcore.misc.MediumCoreData;
import com.github.alexmodguy.mediumcore.packet.SyncMediumcoreGameRuleMessage;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import toni.lib.modifiers.ModifierDefinition;

public class CommonEvents {

    private static final ModifierDefinition INITIAL_HEALTH_MODIFIER = new ModifierDefinition("mediumcore", "mediumcoreinitialhealthmod"); //Mth.createInsecureUUID(RandomSource.create(2929292911123L));
    private static final ModifierDefinition HEALTH_MODIFIER = new ModifierDefinition("mediumcore", "mediumcorehealthmod"); //Mth.createInsecureUUID(RandomSource.create(111222333441249L));


    public static void onInitialize() {

        ServerLifecycleEvents.SERVER_STARTED.register((server) -> {
            if (server.isDedicatedServer() && server instanceof DedicatedServer dedicatedServer){
                boolean propertiesSayMediumcore = ((DedicatedServerPropertiesAccessor)dedicatedServer.getProperties()).isServerMediumcore();
                if (propertiesSayMediumcore) {
                    Mediumcore.LOGGER.info("set server game rule for mediumcore because it is set to true in server.properties");
                    GameRuleRegistry.setMediumcoreMode(server.getGameRules(), true, server);
                }
            }
        });

        ServerPlayerEvents.AFTER_RESPAWN.register((ServerPlayer oldPlayer, ServerPlayer newPlayer, boolean alive) -> {
            if (!alive && !VersionUtils.level(newPlayer).isClientSide) {
                if (GameRuleRegistry.isMediumCoreMode(VersionUtils.level(newPlayer).getGameRules())) {
                    double healthModifiedBy = MediumCoreData.getPlayerData(newPlayer).healthModifiedBy;
                    updateHealth(newPlayer, healthModifiedBy);
                }
            }
        });

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) ->
        {
            boolean mediumcore = GameRuleRegistry.isMediumCoreMode(server.overworld().getGameRules());

            new SyncMediumcoreGameRuleMessage(mediumcore).send(handler.player);

            if (!mediumcore)
                return;

            double healthModifiedBy = MediumCoreData.getPlayerData(handler.player).healthModifiedBy;
            updateHealth(handler.player, healthModifiedBy);
        });

        ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) ->
        {
            if (!(entity instanceof Player player))
                return;

            if (!GameRuleRegistry.isMediumCoreMode(VersionUtils.level(player).getGameRules()))
                return;

            MediumCoreData.PlayerData data = MediumCoreData.getPlayerData(player);
            double clampedHealth = Mth.clamp(player.getMaxHealth() - Mediumcore.CONFIG.healthDecreasePerDeath.get(), Mediumcore.CONFIG.minimumPlayerHealth.get(), Mediumcore.CONFIG.maxPlayerHealth.get());
            data.healthModifiedBy += clampedHealth - player.getMaxHealth();
            updateHealth(player, data.healthModifiedBy);
        });
    }


    public static void updateHealth(Player player, double healthModifiedBy) {
        AttributeInstance attribute = player.getAttribute(Attributes.MAX_HEALTH);

        INITIAL_HEALTH_MODIFIER.removeModifier(attribute);
        INITIAL_HEALTH_MODIFIER.addPermanentModifier(attribute, Mediumcore.CONFIG.startingPlayerHealth.get() - 20F);

        HEALTH_MODIFIER.removeModifier(attribute);
        HEALTH_MODIFIER.addPermanentModifier(attribute, healthModifiedBy);

        player.setHealth(Mth.clamp(player.getHealth(), 0, player.getMaxHealth()));
    }
}
