package net.technochronicle.technochronicleapi.helper;

import icyllis.modernui.graphics.Image;
import net.minecraft.resources.ResourceLocation;
import net.technochronicle.technochronicleapi.TechnoChronicleAPI;
import net.technochronicle.technochronicleapi.cache.TCCaches;

public class ImageHelper {
    public static Image getImage(String path) {
        return getImage(TechnoChronicleAPI.MOD_ID, path);
    }

    public static Image getImage(String namespace, String key) {
        var path = ResourceLocation.fromNamespaceAndPath(namespace, "gui/" + key);
        return TCCaches.ImageCache.getOrSetDefault(path, () -> Image.create(path.getNamespace(), path.getPath()));
    }
}
