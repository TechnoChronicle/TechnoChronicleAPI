package net.technochronicle.technochronicleapi.registrate.builder;

import com.mojang.blaze3d.platform.InputConstants;
import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.builders.AbstractBuilder;
import com.tterrag.registrate.builders.BuilderCallback;
import com.tterrag.registrate.util.entry.RegistryEntry;
import com.tterrag.registrate.util.nullness.NonNullConsumer;
import com.tterrag.registrate.util.nullness.NonnullType;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.IKeyConflictContext;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.technochronicle.technochronicleapi.registrate.TCRegistries;
import net.technochronicle.technochronicleapi.registrate.entry.KeyMappingEntry;
import org.jetbrains.annotations.NotNull;

@Accessors(chain = true)
public class KeyMappingBuilder<P> extends AbstractBuilder<KeyMapping,KeyMapping,P,KeyMappingBuilder<P>> {
    public KeyMappingBuilder(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback,
                             int keyCode, ResourceLocation keyGroup, IKeyConflictContext keyConflictContext, InputConstants.Type inputType) {
        super(owner, parent, name, callback, TCRegistries.KEYMAPPING);
        var group = keyGroup.getPath();
        var namespace = keyGroup.getNamespace();
        description = "key." + owner.getModid() + "." + name;
        category = "key.category." + namespace + "." + group;
        this.keyConflictContext = keyConflictContext;
        this.inputType = inputType;
        this.keyCode = keyCode;
    }

    private final String description;
    private final IKeyConflictContext keyConflictContext;
    private final InputConstants.Type inputType;
    private final int keyCode;
    private final String category;

    private KeyMapping entry;

    boolean isCharging = false;
    @Setter
    public NonNullConsumer<ClientTickEvent.Post> onKeyDown = null;

    @Setter
    public NonNullConsumer<ClientTickEvent.Post> onKeyUp = null;

    @Override
    protected @NonnullType @NotNull KeyMapping createEntry() {
        if (entry == null)
            entry = new KeyMapping(description, keyConflictContext, inputType, keyCode, category);
        return entry;
    }

    @Override
    public @NotNull KeyMappingEntry register() {
        var mapping = createEntry();
        getOwner().getModEventBus().addListener(RegisterKeyMappingsEvent.class, event -> {
            event.register(mapping);
        });
        if (FMLEnvironment.dist.isClient()) {
            NeoForge.EVENT_BUS.addListener(ClientTickEvent.Post.class, event -> {
                if (mapping.isDown()) {
                    if (!isCharging) {
                        isCharging = true;
                        if (onKeyDown != null)
                            onKeyDown.accept(event);
                    }
                } else if (isCharging) {
                    isCharging = false;
                    if (onKeyUp != null)
                        onKeyUp.accept(event);
                }
            });
        }
        return (KeyMappingEntry) super.register();
    }

    @Override
    protected @NotNull RegistryEntry<KeyMapping, KeyMapping> createEntryWrapper(@NotNull DeferredHolder<KeyMapping, KeyMapping> delegate) {
        return new KeyMappingEntry(getOwner(), delegate);
    }
}