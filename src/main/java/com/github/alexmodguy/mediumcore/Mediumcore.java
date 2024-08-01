package com.github.alexmodguy.mediumcore;

import com.github.alexmodguy.mediumcore.event.CommonEvents;
import com.github.alexmodguy.mediumcore.misc.MediumCoreData;
import com.github.alexmodguy.mediumcore.packet.SyncMediumcoreGameRuleMessage;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import toni.lib.VersionUtils;

#if AFTER_21
import net.neoforged.fml.config.ModConfig;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import fuzs.forgeconfigapiport.fabric.api.forge.v4.ForgeConfigRegistry;
#elif BEFORE_20_1
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.api.ModLoadingContext;
#elif BEFORE_21
import net.minecraftforge.fml.config.ModConfig;
import fuzs.forgeconfigapiport.api.config.v2.ForgeConfigRegistry;
#endif

import static net.minecraft.commands.Commands.*;

public class Mediumcore implements ModInitializer, ClientModInitializer
{
    public static final Logger LOGGER = LogManager.getLogger("mediumcore");
    public static final ResourceLocation MEDIUMCORE_HEARTS_TEXTURE = VersionUtils.resource("mediumcore", "textures/gui/icons.png");

    public static final String MOD_ID = "mediumcore";

    public static final ForgeConfigSpec CONFIG_SPEC;
    public static final MediumcoreConfig CONFIG;

    public static boolean clientActive;

    static
    {
        {
            final Pair<MediumcoreConfig, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(MediumcoreConfig::new);
            CONFIG = specPair.getLeft();
            CONFIG_SPEC = specPair.getRight();
        }
    }

    public void onInitializeClient()
    {
        SyncMediumcoreGameRuleMessage.registerClient();

        ItemTooltipCallback.EVENT.register((stack, tooltipContext, #if AFTER_21 tooltipType, #endif lines) ->
        {
            if (stack.is(MediumcoreTags.RESTORES_MAX_HEALTH) && clientActive) {
                lines.add(Component.literal("❤ Restores Max Health").withStyle(ChatFormatting.RED));
            }
        });
    }

    public void onInitialize()
    {
        #if BEFORE_20_1
        ModLoadingContext.registerConfig(MOD_ID, ModConfig.Type.COMMON, CONFIG_SPEC);
        #else
        ForgeConfigRegistry.INSTANCE.register(Mediumcore.MOD_ID, ModConfig.Type.COMMON, CONFIG_SPEC);
        #endif
        GameRuleRegistry.setup();

        SyncMediumcoreGameRuleMessage.register();
        CommonEvents.onInitialize();

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(literal("mediumcore")
            .requires(source -> source.hasPermission(2))
            .then(literal("removehearts")
                .then(argument("player", EntityArgument.player())
                    .then(argument("value", IntegerArgumentType.integer())
                        .executes(context -> {
                            var player = EntityArgument.getPlayer(context, "player");
                            var value = IntegerArgumentType.getInteger(context, "value");
                            context.getSource().sendSuccess(#if AFTER_20_1 () -> #endif Component.literal("Removed " + value + " Hearts"), false);
                            return addHearts(player, -value);
                        })
                    )
                )
            )
            .then(literal("addhearts")
                .then(argument("player", EntityArgument.player())
                    .then(argument("value", IntegerArgumentType.integer())
                        .executes(context -> {
                            var player = EntityArgument.getPlayer(context, "player");
                            var value = IntegerArgumentType.getInteger(context, "value");
                            context.getSource().sendSuccess(#if AFTER_20_1 () -> #endif Component.literal("Added " + value + " Hearts"), false);
                            return addHearts(player, value);
                        })
                    )
                )
            )
        ));
    }

    public static int addHearts(ServerPlayer player, int amount) {
        if (player == null || !GameRuleRegistry.isMediumCoreMode(VersionUtils.level(player).getGameRules()))
            return 0;

        MediumCoreData.PlayerData data = MediumCoreData.getPlayerData(player);
        double clampedHealth = Mth.clamp(player.getMaxHealth() + amount, Mediumcore.CONFIG.minimumPlayerHealth.get(), Mediumcore.CONFIG.maxPlayerHealth.get());
        data.healthModifiedBy += clampedHealth - player.getMaxHealth();
        CommonEvents.updateHealth(player, data.healthModifiedBy);
        return 1;
    }


}
