package net.technochronicle.technochronicleapi.registrate;

import net.minecraft.client.KeyMapping;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.technochronicle.technochronicleapi.TechnoChronicleAPI;
import net.technochronicle.technochronicleapi.definition.BaseMachineDefinition;

public class TCRegistries {
    public static final ResourceKey<Registry<BaseMachineDefinition<?>>> MACHINE_DEFINITIONS =
            ResourceKey.createRegistryKey(TechnoChronicleAPI.id("machine_definitions"));
    public static final ResourceKey<Registry<KeyMapping>>KEYMAPPING=ResourceKey.createRegistryKey(TechnoChronicleAPI.id("key_mappings"));
}
