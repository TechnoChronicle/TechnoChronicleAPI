package net.technochronicle.technochronicleapi.registrate.entry;

import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.util.entry.RegistryEntry;
import net.minecraft.client.KeyMapping;
import net.neoforged.neoforge.registries.DeferredHolder;

public class KeyMappingEntry extends RegistryEntry<KeyMapping,KeyMapping> {
    public KeyMappingEntry(AbstractRegistrate<?> owner, DeferredHolder<KeyMapping, KeyMapping> key) {
        super(owner, key);
    }
}