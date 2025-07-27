package net.technochronicle.technochronicleapi.cache;

import icyllis.modernui.graphics.Image;
import net.minecraft.resources.ResourceLocation;
import net.technochronicle.technochronicleapi.cache.manager.LruCache;

public class TCCaches {
    public static final LruCache<ResourceLocation, Image> ImageCache = new LruCache<>(256);
}
