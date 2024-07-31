package com.github.alexmodguy.mediumcore.mixins;

import com.github.alexmodguy.mediumcore.GameRuleRegistry;
import com.github.alexmodguy.mediumcore.Mediumcore;
import com.github.alexmodguy.mediumcore.MediumcoreTags;
import com.github.alexmodguy.mediumcore.event.CommonEvents;
import com.github.alexmodguy.mediumcore.misc.MediumCoreData;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(Item.class)
public class ItemMixin
{
    @Inject(at = @At("HEAD"), method = "finishUsingItem")
    public void onUseItem(ItemStack stack, Level level, LivingEntity livingEntity, CallbackInfoReturnable<ItemStack> cir)
    {
        if (!(livingEntity instanceof Player player) || level.isClientSide())
            return;

        if (stack.is(MediumcoreTags.RESTORES_MAX_HEALTH) && GameRuleRegistry.isMediumCoreMode(player.level().getGameRules())) {
            MediumCoreData.PlayerData data = MediumCoreData.getPlayerData(player);
            double clampedHealth = Mth.clamp(player.getMaxHealth() + Mediumcore.CONFIG.healthIncreasePerHeal.get(), Mediumcore.CONFIG.minimumPlayerHealth.get(), Mediumcore.CONFIG.maxPlayerHealth.get());
            data.healthModifiedBy += (clampedHealth - player.getMaxHealth());
            CommonEvents.updateHealth(player, data.healthModifiedBy);
        }
    }
}