package net.technochronicle.technochronicleapi.registrate;

import com.mojang.blaze3d.platform.InputConstants;
import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.builders.Builder;
import com.tterrag.registrate.builders.NoConfigBuilder;
import com.tterrag.registrate.util.entry.ItemEntry;
import com.tterrag.registrate.util.entry.RegistryEntry;
import com.tterrag.registrate.util.nullness.NonNullFunction;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.Setter;
import net.minecraft.client.KeyMapping;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.settings.IKeyConflictContext;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.technochronicle.technochronicleapi.TechnoChronicleAPI;
import net.technochronicle.technochronicleapi.definition.BaseMachineDefinition;
import net.technochronicle.technochronicleapi.machine.IMachineBlock;
import net.technochronicle.technochronicleapi.machine.IMachineBlockEntity;
import net.technochronicle.technochronicleapi.machine.MetaMachine;
import net.technochronicle.technochronicleapi.machine.instance.MachineBlock;
import net.technochronicle.technochronicleapi.machine.instance.MachineBlockEntity;
import net.technochronicle.technochronicleapi.machine.instance.MachineItem;
import net.technochronicle.technochronicleapi.misc.FluidTypeExtensions;
import net.technochronicle.technochronicleapi.registrate.builder.FluidBuilder;
import net.technochronicle.technochronicleapi.registrate.builder.KeyMappingBuilder;
import net.technochronicle.technochronicleapi.registrate.builder.MachineBuilder;
import org.apache.commons.lang3.function.TriFunction;
import org.apache.http.annotation.Obsolete;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.openjdk.nashorn.api.tree.ParenthesizedTree;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * 管理模组的所有注册和数据生成器。
 * <p>
 * 通常<em>不是</em>线程安全的，因为它会以状态方式保存当前正在构建对象的名称，并使用非并发集合。
 * <p>
 * 通过{@link #object(String)}开始一个新对象。此名称将用于所有后续条目，直到下一次调用{@link #object(String)}。或者，可以使用接受名称参数的方法（例如{@link #block(String, NonNullFunction)}）。这些方法不会影响当前名称状态。
 * <p>
 * 简单用法示例如下：
 * <pre>
 * {@code
 * public static final TCRegistrate REGISTRATE = TCRegistrate.create("my_modId");
 * 
 * static {
 *     REGISTRATE.defaultCreativeTab((ResourceKey<CreativeModeTab>) null);
 * }
 * 
 * public static RegistryEntry<CreativeModeTab, CreativeModeTab> Test_Tab = REGISTRATE.defaultCreativeTab("test_tab",
 *             builder -> builder
 *                     .displayItems(new RegistrateDisplayItemsGenerator("test_tab", REGISTRATE))
 *                     .build())
 *         .register();
 *
 * static {
 *     REGISTRATE.creativeModeTab(() -> Test_Tab);
 * }
 *
 * public static final RegistryEntry<Block,MyBlock> MY_BLOCK = REGISTRATE.block(MyBlock::new)
 *         .defaultItem()
 *         .register();
 * }
 * </pre>
 * <p>
 * 上述代码将以"my_modId"的namespace注册一个新的Registrate</br>
 * 并在Registrate中注册一个名为"test_tab"的创造模式物品栏</br>
 * 并自动向其添加在{@link TCRegistrate#defaultCreativeTab(ResourceKey)}后注册的Item到物品栏中，直到再次调用{@link TCRegistrate#defaultCreativeTab(ResourceKey)}。
 */
public class TCRegistrate extends AbstractRegistrate<TCRegistrate> {
    private static final Map<String, TCRegistrate> EXISTING_REGISTRATES = new Object2ObjectOpenHashMap<>();

    private final AtomicBoolean registered = new AtomicBoolean(false);

    /**
     * Construct a new Registrate for the given mod ID.
     *
     * @param modid The mod ID for which objects will be registered
     */
    protected TCRegistrate(String modid) {
        super(modid);
    }

    public static @NotNull TCRegistrate create(@NotNull String modid) {
        return innerCreate(modid, true);
    }

    @ApiStatus.Internal
    public static TCRegistrate createIgnoringListenerErrors(String modId) {
        return innerCreate(modId, false);
    }

    private static TCRegistrate innerCreate(String modId, boolean strict) {
        if (EXISTING_REGISTRATES.containsKey(modId)) {
            return EXISTING_REGISTRATES.get(modId);
        }
        var registrate = new TCRegistrate(modId);
        Optional<IEventBus> modEventBus = ModList.get().getModContainerById(modId).map(ModContainer::getEventBus);
        if (strict) {
            modEventBus.ifPresentOrElse(registrate::registerEventListeners, () -> {
                String message = "# [TCRegistrate] Failed to register eventListeners for mod " + modId + ", This should be reported to this mod's dev #";
                String hashtags = "#".repeat(message.length());
                TechnoChronicleAPI.LOGGER.fatal(hashtags);
                TechnoChronicleAPI.LOGGER.fatal(message);
                TechnoChronicleAPI.LOGGER.fatal(hashtags);
            });
        } else {
            registrate.registerEventListeners(modEventBus.orElse(TechnoChronicleAPI.tcModBus));
        }
        EXISTING_REGISTRATES.put(modId, registrate);
        return registrate;
    }

    @Override
    public @NotNull TCRegistrate registerEventListeners(@NotNull IEventBus bus) {
        if (!registered.getAndSet(true)) {
            return (TCRegistrate) super.registerEventListeners(bus);
        }
        return this;
    }

    //TODO:add some misc methods to TCRegistrate
    //region KeyMappings
    @Setter
    private ResourceLocation defaultKeyGroup = ResourceLocation.fromNamespaceAndPath(getModid(), "default");
    public KeyMappingBuilder<TCRegistrate> keyMapping(String name, int keyCode) {
        return keyMapping(self(), name, keyCode);
    }
    public<P> KeyMappingBuilder<P> keyMapping(P parent,String name, int keyCode) {
        return keyMapping(parent, name, keyCode, defaultKeyGroup);
    }
    public KeyMappingBuilder<TCRegistrate> keyMapping(String name, int keyCode, IKeyConflictContext keyConflictContext, InputConstants.Type inputType) {
        return keyMapping(self(), name, keyCode, keyConflictContext, inputType);
    }
    public<P> KeyMappingBuilder<P> keyMapping(P parent, String name, int keyCode, IKeyConflictContext keyConflictContext, InputConstants.Type inputType) {
        return keyMapping(parent, name, keyCode, defaultKeyGroup, keyConflictContext, inputType);
    }
    public KeyMappingBuilder<TCRegistrate> keyMapping(String name, int keyCode, ResourceLocation keyGroup) {
        return keyMapping(self(), name, keyCode, keyGroup);
    }
    public<P> KeyMappingBuilder<P> keyMapping(P parent,String name, int keyCode, ResourceLocation keyGroup) {
        return keyMapping(parent, name, keyCode, keyGroup, KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM);
    }
    public KeyMappingBuilder<TCRegistrate> keyMapping(String name, int keyCode, ResourceLocation keyGroup,IKeyConflictContext keyConflictContext, InputConstants.Type inputType) {
        return keyMapping(self(), name, keyCode, keyGroup, keyConflictContext, inputType);
    }
    public<P> KeyMappingBuilder<P> keyMapping(P parent,String name, int keyCode,ResourceLocation keyGroup, IKeyConflictContext keyConflictContext, InputConstants.Type inputType) {
        return entry(name, callback -> new KeyMappingBuilder<>(this, parent, name, callback, keyCode, keyGroup, keyConflictContext, inputType));
    }
    //endregion

    //TODO:add extra methods to TCRegistrate
    @Nullable
    private RegistryEntry<CreativeModeTab, ? extends CreativeModeTab> currentTab;
    private static final Map<RegistryEntry<?, ?>, @Nullable RegistryEntry<CreativeModeTab, ? extends CreativeModeTab>> TAB_LOOKUP = new IdentityHashMap<>();

    public void creativeModeTab(Supplier<RegistryEntry<CreativeModeTab, ? extends CreativeModeTab>> currentTab) {
        this.currentTab = currentTab.get();
    }

    public void creativeModeTab(RegistryEntry<CreativeModeTab, ? extends CreativeModeTab> currentTab) {
        this.currentTab = currentTab;
    }

    public boolean isInCreativeTab(RegistryEntry<?, ?> entry, RegistryEntry<CreativeModeTab, ? extends CreativeModeTab> tab) {
        return TAB_LOOKUP.get(entry) == tab;
    }

    public void setCreativeTab(RegistryEntry<?, ?> entry, @Nullable RegistryEntry<CreativeModeTab, ? extends CreativeModeTab> tab) {
        TAB_LOOKUP.put(entry, tab);
    }

    protected <R, T extends R> @NotNull RegistryEntry<R, T> accept(@NotNull String name, @NotNull ResourceKey<? extends Registry<R>> type,
                                                                   @NotNull Builder<R, T, ?, ?> builder, @NotNull NonNullSupplier<? extends T> creator,
                                                                   @NotNull NonNullFunction<DeferredHolder<R, T>, ? extends RegistryEntry<R, T>> entryFactory) {
        RegistryEntry<R, T> entry = super.accept(name, type, builder, creator, entryFactory);

        if (this.currentTab != null) {
            TAB_LOOKUP.put(entry, this.currentTab);
        }

        return entry;
    }

    public <P> @NotNull NoConfigBuilder<CreativeModeTab, CreativeModeTab, P> defaultCreativeTab(@NotNull P parent, @NotNull String name,
                                                                                                @NotNull Consumer<CreativeModeTab.Builder> config) {
        return createCreativeModeTab(parent, name, config);
    }

    protected <P> NoConfigBuilder<CreativeModeTab, CreativeModeTab, P> createCreativeModeTab(P parent, String name,
                                                                                             Consumer<CreativeModeTab.Builder> config) {
        return this.generic(parent, name, Registries.CREATIVE_MODE_TAB, () -> {
            var builder = CreativeModeTab.builder()
                    .icon(() -> getAll(Registries.ITEM).stream().findFirst().map(ItemEntry::cast)
                            .map(ItemEntry::asStack).orElse(new ItemStack(Items.AIR)));
            config.accept(builder);
            return builder.build();
        });
    }

    //TODO: override fluid methods
    //region Fluids

    /// 使用该方法应保证在 textures/block/fluid/下有对应的 {@code name}_still 和 {@code name}_flow 的文件
    @Deprecated(since = "1.0.0", forRemoval = false)
    public FluidBuilder<BaseFlowingFluid.Flowing, TCRegistrate> tcFluid(String name) {
        return tcFluid(self(), name);
    }

    /// 使用该方法应保证在 textures/block/fluid/下有对应的 {@code name}_still 和 {@code name}_flow 的文件
    @Deprecated(since = "1.0.0", forRemoval = false)
    public <P> FluidBuilder<BaseFlowingFluid.Flowing, P> tcFluid(P parent, String name) {
        return tcFluid(parent, name, BaseFlowingFluid.Flowing::new);
    }

    /// 使用该方法应保证在 textures/block/fluid/下有对应的 {@code name}_still 和 {@code name}_flow 的文件
    @Deprecated(since = "1.0.0", forRemoval = false)
    public <T extends BaseFlowingFluid> FluidBuilder<T, TCRegistrate> tcFluid(String name, NonNullFunction<BaseFlowingFluid.Properties, T> fluidFactory) {
        return tcFluid(self(), name, fluidFactory);
    }

    /// 使用该方法应保证在 textures/block/fluid/下有对应的 {@code name}_still 和 {@code name}_flow 的文件
    @Deprecated(since = "1.0.0", forRemoval = false)
    public <T extends BaseFlowingFluid, P> FluidBuilder<T, P> tcFluid(P parent, String name, NonNullFunction<BaseFlowingFluid.Properties, T> fluidFactory) {
        return entry(name, callback -> FluidBuilder.create(this, parent, name, callback,
                () -> new FluidTypeExtensions(-1,
                        ResourceLocation.fromNamespaceAndPath(getModid(), "block/fluid/" + name + "_still"),
                        ResourceLocation.fromNamespaceAndPath(getModid(), "block/fluid/" + name + "_flow")),
                fluidFactory));
    }

    public FluidBuilder<BaseFlowingFluid.Flowing, TCRegistrate> tcFluid(String name, NonNullSupplier<IClientFluidTypeExtensions> extensionsSupplier) {
        return tcFluid(self(), name, extensionsSupplier);
    }

    public <P> FluidBuilder<BaseFlowingFluid.Flowing, P> tcFluid(P parent, String name, NonNullSupplier<IClientFluidTypeExtensions> extensionsSupplier) {
        return entry(name, callback -> FluidBuilder.create(this, parent, name, callback, extensionsSupplier));
    }

    public FluidBuilder<BaseFlowingFluid.Flowing, TCRegistrate> tcFluid(String name, NonNullFunction<FluidType.Properties, FluidType> typeFactory, NonNullSupplier<IClientFluidTypeExtensions> extensionsSupplier) {
        return tcFluid(self(), name, typeFactory, extensionsSupplier);
    }

    public <P> FluidBuilder<BaseFlowingFluid.Flowing, P> tcFluid(P parent, String name, NonNullFunction<FluidType.Properties, FluidType> typeFactory, NonNullSupplier<IClientFluidTypeExtensions> extensionsSupplier) {
        return entry(name, callback -> FluidBuilder.create(this, parent, name, callback, typeFactory, extensionsSupplier));
    }

    public <T extends BaseFlowingFluid> FluidBuilder<T, TCRegistrate> tcFluid(String name, NonNullFunction<BaseFlowingFluid.Properties, T> fluidFactory, NonNullFunction<FluidType.Properties, FluidType> typeFactory, NonNullSupplier<IClientFluidTypeExtensions> extensionsSupplier) {
        return tcFluid(self(), name, fluidFactory, typeFactory, extensionsSupplier);
    }

    public <T extends BaseFlowingFluid, P> FluidBuilder<T, P> tcFluid(P parent, String name, NonNullFunction<BaseFlowingFluid.Properties, T> fluidFactory, NonNullFunction<FluidType.Properties, FluidType> typeFactory, NonNullSupplier<IClientFluidTypeExtensions> extensionsSupplier) {
        return entry(name, callback -> FluidBuilder.create(this, parent, name, callback, typeFactory, extensionsSupplier, fluidFactory));
    }

    //endregion
    //TODO: add machine registry
    //region Machine registry
    public MachineBuilder<BaseMachineDefinition<?>, TCRegistrate> machine(String name) {
        return machine(name, MetaMachine::new);
    }

    public MachineBuilder<BaseMachineDefinition<?>, TCRegistrate> machine(String name,
                                                                          Function<IMachineBlockEntity, MetaMachine> machineFactory) {
        return machine(name, BaseMachineDefinition::new, machineFactory, MachineBlock::new, MachineBlockEntity::new);
    }

    public <DEFINITION extends BaseMachineDefinition<?>>
    MachineBuilder<DEFINITION, TCRegistrate> machine(String name,
                                                     BiFunction<AbstractRegistrate<?>, ResourceLocation, DEFINITION> definitionFactory) {
        return machine(name, definitionFactory, MetaMachine::new, MachineBlock::new, MachineBlockEntity::new);
    }

    public <DEFINITION extends BaseMachineDefinition<?>>
    MachineBuilder<DEFINITION, TCRegistrate> machine(String name,
                                                     BiFunction<AbstractRegistrate<?>, ResourceLocation, DEFINITION> definitionFactory,
                                                     Function<IMachineBlockEntity, MetaMachine> machineFactory,
                                                     BiFunction<BlockBehaviour.Properties, DEFINITION, IMachineBlock> blockFactory,
                                                     TriFunction<BlockEntityType<?>, BlockPos, BlockState, IMachineBlockEntity> blockEntityFactory) {
        return machine(name, definitionFactory, machineFactory, blockFactory, blockEntityFactory, MachineItem::new);
    }

    public <DEFINITION extends BaseMachineDefinition<?>>
    MachineBuilder<DEFINITION, TCRegistrate> machine(String name,
                                                     BiFunction<AbstractRegistrate<?>, ResourceLocation, DEFINITION> definitionFactory,
                                                     Function<IMachineBlockEntity, MetaMachine> machineFactory,
                                                     BiFunction<BlockBehaviour.Properties, DEFINITION, IMachineBlock> blockFactory,
                                                     TriFunction<BlockEntityType<?>, BlockPos, BlockState, IMachineBlockEntity> blockEntityFactory,
                                                     BiFunction<IMachineBlock, Item.Properties, MachineItem> itemFactory) {
        return machine(self(), name, definitionFactory, machineFactory, blockFactory, blockEntityFactory, itemFactory);
    }

    public <DEFINITION extends BaseMachineDefinition<?>, P>
    MachineBuilder<DEFINITION, P> machine(P parent, String name,
                                          BiFunction<AbstractRegistrate<?>, ResourceLocation, DEFINITION> definitionFactory,
                                          Function<IMachineBlockEntity, MetaMachine> machineFactory,
                                          BiFunction<BlockBehaviour.Properties, DEFINITION, IMachineBlock> blockFactory,
                                          TriFunction<BlockEntityType<?>, BlockPos, BlockState, IMachineBlockEntity> blockEntityFactory,
                                          BiFunction<IMachineBlock, Item.Properties, MachineItem> itemFactory) {
        return entry(name, callback -> MachineBuilder.create(this, parent, name, callback,
                definitionFactory, machineFactory, blockFactory, blockEntityFactory, itemFactory));
    }
    //endregion
    //region MultiBlock Machine registry
    //endregion

}