package net.technochronicle.technochronicleapi.registrate.entry;

import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.util.entry.RegistryEntry;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.technochronicle.technochronicleapi.definition.BaseMachineDefinition;

public class MachineEntry<DEFINITION extends BaseMachineDefinition<?>>extends RegistryEntry<BaseMachineDefinition<?>, DEFINITION> {
    public MachineEntry(AbstractRegistrate<?> owner, DeferredHolder<BaseMachineDefinition<?>, DEFINITION> key) {
        super(owner, key);
    }
}
