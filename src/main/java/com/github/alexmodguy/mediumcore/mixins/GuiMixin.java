package com.github.alexmodguy.mediumcore.mixins;

import com.github.alexmodguy.mediumcore.Mediumcore;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Gui;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

#if AFTER_20_1
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
#else
import net.minecraft.client.gui.GuiComponent;
#endif

#if AFTER_21
@Mixin(Gui.HeartType.class)
#else
@Mixin(Gui.class)
#endif
public class GuiMixin
{
    #if AFTER_21
    @Unique private final ResourceLocation mediumcore$Full = ResourceLocation.withDefaultNamespace("hud/heart/mediumcore_full");
    @Unique private final ResourceLocation mediumcore$Half = ResourceLocation.withDefaultNamespace("hud/heart/mediumcore_half");
    @Unique private final ResourceLocation mediumcore$FullBlinking = ResourceLocation.withDefaultNamespace("hud/heart/mediumcore_full_blinking");
    @Unique private final ResourceLocation mediumcore$HalfBlinking = ResourceLocation.withDefaultNamespace("hud/heart/mediumcore_half_blinking") ;

    @Inject(at = @At("HEAD"), method = "getSprite", cancellable = true)
    public void getSpriteMediumcore(boolean hardcore, boolean halfHeart, boolean blinking, CallbackInfoReturnable<ResourceLocation> cir)
    {
        if (!Mediumcore.clientActive || (Gui.HeartType) (Object) this != Gui.HeartType.NORMAL)
            return;

        if (halfHeart) {
            cir.setReturnValue(blinking ? this.mediumcore$HalfBlinking : mediumcore$Half);
        } else {
            cir.setReturnValue(blinking ? this.mediumcore$FullBlinking : mediumcore$Full);
        }
    }
    #elif AFTER_20_1
    @Inject(at = @At("HEAD"), method = "renderHeart", cancellable = true)
    public void getSpriteMediumcore(GuiGraphics guiGraphics, Gui.HeartType heartType, int i, int j, int k, boolean bl, boolean bl2, CallbackInfo ci)
    {
        if (!Mediumcore.clientActive || heartType != Gui.HeartType.NORMAL)
            return;

        guiGraphics.blit(Mediumcore.MEDIUMCORE_HEARTS_TEXTURE, i, j, heartType.getX(bl2, bl), k, 9, 9);
        ci.cancel();
    }

    #else
    @Inject(at = @At("HEAD"), method = "renderHeart", cancellable = true)
    public void getSpriteMediumcore(PoseStack poseStack, Gui.HeartType heartType, int i, int j, int k, boolean bl, boolean bl2, CallbackInfo ci)
    {
        if (!Mediumcore.clientActive || heartType != Gui.HeartType.NORMAL)
            return;
        RenderSystem.setShaderTexture(0, Mediumcore.MEDIUMCORE_HEARTS_TEXTURE);
        ((Gui)(Object) this).blit(poseStack, i, j, heartType.getX(bl2, bl), k, 9, 9);
        RenderSystem.setShaderTexture(0, GuiComponent.GUI_ICONS_LOCATION);

        ci.cancel();
    }
    #endif


}