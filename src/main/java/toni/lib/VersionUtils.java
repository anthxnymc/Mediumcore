package toni.lib;

import net.minecraft.resources.ResourceLocation;

public class VersionUtils
{
    public static ResourceLocation resource(String modid, String path) {
        #if AFTER_21
        return ResourceLocation.fromNamespaceAndPath(modid, path);
        #else
        return new ResourceLocation(modid, path);
        #endif
    }
}
