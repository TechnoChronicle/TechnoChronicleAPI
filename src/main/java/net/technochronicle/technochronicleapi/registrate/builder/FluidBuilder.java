package net.technochronicle.technochronicleapi.registrate.builder;

import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;
import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.builders.AbstractBuilder;
import com.tterrag.registrate.builders.BlockBuilder;
import com.tterrag.registrate.builders.BuilderCallback;
import com.tterrag.registrate.builders.ItemBuilder;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.providers.RegistrateLangProvider;
import com.tterrag.registrate.providers.RegistrateTagsProvider;
import com.tterrag.registrate.util.OneTimeEventReceiver;
import com.tterrag.registrate.util.RegistrateDistExecutor;
import com.tterrag.registrate.util.entry.FluidEntry;
import com.tterrag.registrate.util.entry.RegistryEntry;
import com.tterrag.registrate.util.nullness.*;
import net.minecraft.Util;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.common.util.Lazy;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;


public class FluidBuilder<T extends BaseFlowingFluid, P> extends AbstractBuilder<Fluid, T, P, FluidBuilder<T, P>> {

    public static <P> FluidBuilder<BaseFlowingFluid.Flowing, P> create(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback, 
                                                                       NonNullSupplier<IClientFluidTypeExtensions>fluidTypeExtensionsSupplier) {
        return create(owner, parent, name, callback, FluidType::new, fluidTypeExtensionsSupplier, BaseFlowingFluid.Flowing::new);
    }
    public static <P> FluidBuilder<BaseFlowingFluid.Flowing, P> create(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback, 
                                                                       NonNullFunction<FluidType.Properties,FluidType> typeFactory,
                                                                       NonNullSupplier<IClientFluidTypeExtensions>fluidTypeExtensionsSupplier) {
        return create(owner, parent, name, callback, typeFactory, fluidTypeExtensionsSupplier, BaseFlowingFluid.Flowing::new);
    }
    
    public static <T extends BaseFlowingFluid, P> FluidBuilder<T, P> create(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback, 
                                                                            NonNullSupplier<IClientFluidTypeExtensions>fluidTypeExtensionsSupplier,
                                                                                                            NonNullFunction<BaseFlowingFluid.Properties, T> fluidFactory) {
        return create(owner, parent, name, callback, FluidType::new, fluidTypeExtensionsSupplier, fluidFactory);
    }
    
    public static <T extends BaseFlowingFluid, P> FluidBuilder<T, P> create(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback,
                                                                                                            NonNullFunction<FluidType.Properties,FluidType> typeFactory,
                                                                            NonNullSupplier<IClientFluidTypeExtensions>fluidTypeExtensionsSupplier,
                                                                            NonNullFunction<BaseFlowingFluid.Properties, T> fluidFactory) {
        FluidBuilder<T, P> ret = new FluidBuilder<>(owner, parent, name, callback, typeFactory, fluidTypeExtensionsSupplier, fluidFactory)
                .defaultLang().defaultSource().defaultBlock().defaultBucket();
        return ret;
    }

    private final String sourceName, bucketName;
    private final NonNullFunction<BaseFlowingFluid.Properties, T> fluidFactory;
    @Nullable
    private final NonNullSupplier<FluidType> fluidType;
    private final NonNullSupplier<IClientFluidTypeExtensions> fluidTypeExtensionsSupplier;

    @Nullable
    private Boolean defaultSource, defaultBlock, defaultBucket;

    private NonNullConsumer<FluidType.Properties> typeProperties = $ -> {};

    private NonNullConsumer<BaseFlowingFluid.Properties> fluidProperties;

    private @Nullable Supplier<Supplier<RenderType>> layer = null;

    @Nullable
    private NonNullSupplier<? extends BaseFlowingFluid> source;
    private final List<TagKey<Fluid>> tags = new ArrayList<>();

    public FluidBuilder(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback,
                        NonNullFunction<FluidType.Properties,FluidType> typeFactory,
                        NonNullSupplier<IClientFluidTypeExtensions>fluidTypeExtensionsSupplier, 
                        NonNullFunction<BaseFlowingFluid.Properties, T> fluidFactory) {
        super(owner, parent, "flowing_" + name, callback, Registries.FLUID);
        this.sourceName = name;
        this.bucketName = name + "_bucket";
        this.fluidFactory = fluidFactory;
        this.fluidType = NonNullSupplier.lazy(() -> typeFactory.apply(makeTypeProperties()));
        this.fluidTypeExtensionsSupplier = fluidTypeExtensionsSupplier;

        String bucketName = this.bucketName;
        this.fluidProperties = p -> p.bucket(() -> owner.get(bucketName, Registries.ITEM).get())
                .block(() -> owner.<Block, LiquidBlock>get(name, Registries.BLOCK).get());
    }

    /**
     * Modify the properties of the fluid type. Modifications are done lazily, but the passed function is composed with the current one, and as such this method can be called multiple times to perform
     * different operations.
     *
     * @param cons
     *            The action to perform on the attributes
     * @return this {@link FluidBuilder}
     */
    public FluidBuilder<T, P> properties(NonNullConsumer<FluidType.Properties> cons) {
        typeProperties = typeProperties.andThen(cons);
        return this;
    }

    /**
     * Modify the properties of the flowing fluid. Modifications are done lazily, but the passed function is composed with the current one, and as such this method can be called multiple times to perform
     * different operations.
     *
     * @param cons
     *            The action to perform on the attributes
     * @return this {@link FluidBuilder}
     */
    public FluidBuilder<T, P> fluidProperties(NonNullConsumer<BaseFlowingFluid.Properties> cons) {
        fluidProperties = fluidProperties.andThen(cons);
        return this;
    }

    /**
     * Assign the default translation, as specified by {@link RegistrateLangProvider#toEnglishName(String)}. This is the default, so it is generally not necessary to call, unless for
     * undoing previous changes.
     *
     * @return this {@link FluidBuilder}
     */
    public FluidBuilder<T, P> defaultLang() {
        return lang(f -> f.getFluidType().getDescriptionId(), RegistrateLangProvider.toEnglishName(sourceName));
    }

    /**
     * Set the translation for this fluid.
     *
     * @param name
     *            A localized English name
     * @return this {@link FluidBuilder}
     */
    public FluidBuilder<T, P> lang(String name) {
        return lang(f -> f.getFluidType().getDescriptionId(), name);
    }



    @SuppressWarnings("deprecation")
    public FluidBuilder<T, P> renderType(Supplier<Supplier<RenderType>> layer) {
        RegistrateDistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            Preconditions.checkArgument(RenderType.chunkBufferLayers().contains(layer.get().get()), "Invalid render type: " + layer);
        });

        if (this.layer == null) {
            onRegister(this::registerRenderType);
        }
        this.layer = layer;
        return this;
    }

    @SuppressWarnings("deprecation")
    protected void registerRenderType(T entry) {
        RegistrateDistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            OneTimeEventReceiver.addModListener(getOwner(), FMLClientSetupEvent.class, $ -> {
                if (this.layer != null) {
                    RenderType layer = this.layer.get().get();
                    ItemBlockRenderTypes.setRenderLayer(entry, layer);
                    ItemBlockRenderTypes.setRenderLayer(getSource(), layer);
                }
            });
        });
    }

    /**
     * Create a standard {@link BaseFlowingFluid.Source} for this fluid which will be built and registered along with this fluid.
     *
     * @return this {@link FluidBuilder}
     * @see #source(NonNullFunction)
     * @throws IllegalStateException
     *             If {@link #source(NonNullFunction)} has been called before this method
     */
    public FluidBuilder<T, P> defaultSource() {
        if (this.defaultSource != null) {
            throw new IllegalStateException("Cannot set a default source after a custom source has been created");
        }
        this.defaultSource = true;
        return this;
    }

    /**
     * Create a {@link BaseFlowingFluid} for this fluid, which is created by the given factory, and which will be built and registered along with this fluid.
     *
     * @param factory
     *            A factory for the fluid, which accepts the properties and returns a new fluid
     * @return this {@link FluidBuilder}
     */
    public FluidBuilder<T, P> source(NonNullFunction<BaseFlowingFluid.Properties, ? extends BaseFlowingFluid> factory) {
        this.defaultSource = false;
        this.source = NonNullSupplier.lazy(() -> factory.apply(makeProperties()));
        return this;
    }

    /**
     * Create a standard {@link LiquidBlock} for this fluid, building it immediately, and not allowing for further configuration.
     *
     * @return this {@link FluidBuilder}
     * @see #block()
     * @throws IllegalStateException
     *             If {@link #block()} or {@link #block(NonNullBiFunction)} has been called before this method
     */
    public FluidBuilder<T, P> defaultBlock() {
        if (this.defaultBlock != null) {
            throw new IllegalStateException("Cannot set a default block after a custom block has been created");
        }
        this.defaultBlock = true;
        return this;
    }

    /**
     * Create a standard {@link LiquidBlock} for this fluid, and return the builder for it so that further customization can be done.
     *
     * @return the {@link BlockBuilder} for the {@link LiquidBlock}
     */
    public BlockBuilder<LiquidBlock, FluidBuilder<T, P>> block() {
        return block(LiquidBlock::new);
    }

    /**
     * Create a {@link LiquidBlock} for this fluid, which is created by the given factory, and return the builder for it so that further customization can be done.
     *
     * @param <B>
     *            The type of the block
     * @param factory
     *            A factory for the block, which accepts the block object and properties and returns a new block
     * @return the {@link BlockBuilder} for the {@link LiquidBlock}
     */
    public <B extends LiquidBlock> BlockBuilder<B, FluidBuilder<T, P>> block(NonNullBiFunction<T, BlockBehaviour.Properties, ? extends B> factory) {
        if (this.defaultBlock == Boolean.FALSE) {
            throw new IllegalStateException("Only one call to block/noBlock per builder allowed");
        }
        this.defaultBlock = false;
        final NonNullSupplier<T> supplier = asSupplier();
        final var lightLevel = Lazy.of(() -> fluidType.get().getLightLevel());
        final ToIntFunction<BlockState> lightLevelInt = $ -> lightLevel.get();
        return getOwner().<B, FluidBuilder<T, P>>block(this, sourceName, p -> factory.apply(supplier.get(), p))
                .properties(p -> BlockBehaviour.Properties.ofFullCopy(Blocks.WATER).noLootTable())
                .properties(p -> p.lightLevel(lightLevelInt))
                .blockstate((ctx, prov) -> prov.simpleBlock(ctx.getEntry(), prov.models().getBuilder(sourceName)
                        .texture("particle", fluidTypeExtensionsSupplier.get().getStillTexture())));
    }

    @Beta
    public FluidBuilder<T, P> noBlock() {
        if (this.defaultBlock == Boolean.FALSE) {
            throw new IllegalStateException("Only one call to block/noBlock per builder allowed");
        }
        this.defaultBlock = false;
        return this;
    }

    /**
     * Create a standard {@link BucketItem} for this fluid, building it immediately, and not allowing for further configuration.
     *
     * @return this {@link FluidBuilder}
     * @see #bucket()
     * @throws IllegalStateException
     *             If {@link #bucket()} or {@link #bucket(NonNullBiFunction)} has been called before this method
     */
    public FluidBuilder<T, P> defaultBucket() {
        if (this.defaultBucket != null) {
            throw new IllegalStateException("Cannot set a default bucket after a custom bucket has been created");
        }
        defaultBucket = true;
        return this;
    }

    /**
     * Create a standard {@link BucketItem} for this fluid, and return the builder for it so that further customization can be done.
     *
     * @return the {@link ItemBuilder} for the {@link BucketItem}
     */
    public ItemBuilder<BucketItem, FluidBuilder<T, P>> bucket() {
        return bucket(BucketItem::new);
    }

    /**
     * Create a {@link BucketItem} for this fluid, which is created by the given factory, and return the builder for it so that further customization can be done.
     *
     * @param <I>
     *            The type of the bucket item
     * @param factory
     *            A factory for the bucket item, which accepts the fluid object supplier and properties and returns a new item
     * @return the {@link ItemBuilder} for the {@link BucketItem}
     */
    public <I extends BucketItem> ItemBuilder<I, FluidBuilder<T, P>> bucket(NonNullBiFunction<BaseFlowingFluid, Item.Properties, ? extends I> factory) {
        if (this.defaultBucket == Boolean.FALSE) {
            throw new IllegalStateException("Only one call to bucket/noBucket per builder allowed");
        }
        this.defaultBucket = false;
        NonNullSupplier<? extends BaseFlowingFluid> source = this.source;
        // TODO: Can we find a way to circumvent this limitation?
        if (source == null) {
            throw new IllegalStateException("Cannot create a bucket before creating a source block");
        }
        return getOwner().<I, FluidBuilder<T, P>>item(this, bucketName, p -> factory.apply(source.get(), p))
                .properties(p -> p.craftRemainder(Items.BUCKET).stacksTo(1))
                .model((ctx, prov) -> prov.generated(ctx::getEntry, ResourceLocation.fromNamespaceAndPath(getOwner().getModid(), "item/" + bucketName)));
    }

    @Beta
    public FluidBuilder<T, P> noBucket() {
        if (this.defaultBucket == Boolean.FALSE) {
            throw new IllegalStateException("Only one call to bucket/noBucket per builder allowed");
        }
        this.defaultBucket = false;
        return this;
    }

    /**
     * Assign {@link TagKey}{@code s} to this fluid and its source fluid. Multiple calls will add additional tags.
     *
     * @param tags
     *            The tags to assign
     * @return this {@link FluidBuilder}
     */
    @SafeVarargs
    public final FluidBuilder<T, P> tag(TagKey<Fluid>... tags) {
        FluidBuilder<T, P> ret = this.tag(ProviderType.FLUID_TAGS, tags);
        if (this.tags.isEmpty()) {
            ret.getOwner().<RegistrateTagsProvider<Fluid>, Fluid>setDataGenerator(ret.sourceName, getRegistryKey(), ProviderType.FLUID_TAGS,
                    prov -> this.tags.stream().map(prov::addTag).forEach(p -> p.add(getSource().builtInRegistryHolder().key())));
        }
        this.tags.addAll(Arrays.asList(tags));
        return ret;
    }

    /**
     * Remove {@link TagKey}{@code s} from this fluid and its source fluid. Multiple calls will remove additional tags.
     *
     * @param tags
     *            The tags to remove
     * @return this {@link FluidBuilder}
     */
    @SafeVarargs
    public final FluidBuilder<T, P> removeTag(TagKey<Fluid>... tags) {
        this.tags.removeAll(Arrays.asList(tags));
        return this.removeTag(ProviderType.FLUID_TAGS, tags);
    }

    private BaseFlowingFluid getSource() {
        NonNullSupplier<? extends BaseFlowingFluid> source = this.source;
        Preconditions.checkNotNull(source, "Fluid has no source block: " + sourceName);
        return source.get();
    }

    private BaseFlowingFluid.Properties makeProperties() {
        NonNullSupplier<? extends BaseFlowingFluid> source = this.source;
        BaseFlowingFluid.Properties ret = new BaseFlowingFluid.Properties(fluidType, source == null ? null : source::get, asSupplier());
        fluidProperties.accept(ret);
        return ret;
    }

    private FluidType.Properties makeTypeProperties() {
        FluidType.Properties properties = FluidType.Properties.create();
        Optional<RegistryEntry<Block, Block>> block = getOwner().getOptional(sourceName, Registries.BLOCK);
        this.typeProperties.accept(properties);

        // Force the translation key after the user callback runs
        // This is done because we need to remove the lang data generator if using the block key,
        // and if it was possible to undo this change, it might result in the user translation getting
        // silently lost, as there's no good way to check whether the translation key was changed.
        // TODO improve this?
        if (block.isPresent() && block.get().isBound()) {
            properties.descriptionId(block.get().get().getDescriptionId());
            setData(ProviderType.LANG, NonNullBiConsumer.noop());
        } else {
            properties.descriptionId(Util.makeDescriptionId("fluid", ResourceLocation.fromNamespaceAndPath(getOwner().getModid(), sourceName)));
        }

        return properties;
    }

    @Override
    protected @NotNull T createEntry() {
        return fluidFactory.apply(makeProperties());
    }

    /**
     * {@inheritDoc}
     * <p>
     * Additionally registers the source fluid and the fluid type (if constructed).
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public @NotNull FluidEntry<T> register() {
        // Check the fluid has a type.
        if (this.fluidType != null) {
            // Register the type.
            var type= getOwner().simple(this, this.sourceName, NeoForgeRegistries.Keys.FLUID_TYPES, this.fluidType);
            getOwner().getModEventBus().addListener(RegisterClientExtensionsEvent.class,event-> {
                event.registerFluidType(fluidTypeExtensionsSupplier.get(),type);
            });
        } else {
            throw new IllegalStateException("Fluid must have a type: " + getName());
        }

        if (defaultSource == Boolean.TRUE) {
            source(BaseFlowingFluid.Source::new);
        }
        if (defaultBlock == Boolean.TRUE) {
            block().register();
        }
        if (defaultBucket == Boolean.TRUE) {
            bucket().register();
        }

        NonNullSupplier<? extends BaseFlowingFluid> source = this.source;
        if (source != null) {
            getCallback().accept(sourceName, Registries.FLUID, (FluidBuilder) this, source::get);
        } else {
            throw new IllegalStateException("Fluid must have a source version: " + getName());
        }

        return (FluidEntry<T>) super.register();
    }

    @Override
    protected @NotNull RegistryEntry<Fluid, T> createEntryWrapper(@NotNull DeferredHolder<Fluid, T> delegate) {
        return new FluidEntry<>(getOwner(), delegate);
    }
}