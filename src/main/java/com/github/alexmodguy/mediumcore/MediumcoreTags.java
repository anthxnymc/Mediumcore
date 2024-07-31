package com.github.alexmodguy.mediumcore;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import toni.lib.VersionUtils;

public class MediumcoreTags {
    public static final TagKey<Item> RESTORES_MAX_HEALTH = TagKey.create(Registries.ITEM, VersionUtils.resource("mediumcore", "restores_max_health"));

}
