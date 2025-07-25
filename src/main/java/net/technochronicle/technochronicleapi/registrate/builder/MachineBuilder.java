package net.technochronicle.technochronicleapi.registrate.builder;

import com.lowdragmc.lowdraglib.client.renderer.IRenderer;
import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.builders.AbstractBuilder;
import com.tterrag.registrate.builders.BlockBuilder;
import com.tterrag.registrate.builders.BuilderCallback;
import com.tterrag.registrate.builders.ItemBuilder;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.util.entry.RegistryEntry;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;
import com.tterrag.registrate.util.nullness.NonNullConsumer;
import com.tterrag.registrate.util.nullness.NonNullUnaryOperator;
import com.tterrag.registrate.util.nullness.NonnullType;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.technochronicle.technochronicleapi.TechnoChronicleAPI;
import net.technochronicle.technochronicleapi.block.feature.RotationState;
import net.technochronicle.technochronicleapi.config.ConfigHolder;
import net.technochronicle.technochronicleapi.definition.BaseMachineDefinition;
import net.technochronicle.technochronicleapi.machine.IMachineBlock;
import net.technochronicle.technochronicleapi.machine.IMachineBlockEntity;
import net.technochronicle.technochronicleapi.machine.MetaMachine;
import net.technochronicle.technochronicleapi.machine.instance.MachineBlockEntity;
import net.technochronicle.technochronicleapi.machine.instance.MachineItem;
import net.technochronicle.technochronicleapi.registrate.TCRegistries;
import net.technochronicle.technochronicleapi.registrate.entry.MachineEntry;
import net.technochronicle.technochronicleapi.render.TCRendererProvider;
import org.apache.commons.lang3.function.TriFunction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.*;

@Accessors(chain = true)
public class MachineBuilder<DEFINITION extends BaseMachineDefinition<?>,P> extends AbstractBuilder<BaseMachineDefinition<?>,DEFINITION,P,MachineBuilder<DEFINITION,P>> {
    public static <DEFINITION extends BaseMachineDefinition<?>, P>
    MachineBuilder<DEFINITION, P> create(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback,
                                         BiFunction<AbstractRegistrate<?>, ResourceLocation, DEFINITION> definitionFactory,
                                         Function<IMachineBlockEntity, MetaMachine> machineFactory,
                                         BiFunction<BlockBehaviour.Properties, DEFINITION, IMachineBlock> blockFactory,
                                         TriFunction<BlockEntityType<?>, BlockPos, BlockState, IMachineBlockEntity> blockEntityFactory,
                                         BiFunction<IMachineBlock, Item.Properties, MachineItem> itemFactory) {
        return new MachineBuilder<>(owner, parent, name, callback, definitionFactory, machineFactory, blockFactory, blockEntityFactory, itemFactory);
    }

    public MachineBuilder(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback,
                          BiFunction<AbstractRegistrate<?>, ResourceLocation, DEFINITION> definitionFactory,
                          Function<IMachineBlockEntity, MetaMachine> machineFactory,
                          BiFunction<BlockBehaviour.Properties, DEFINITION, IMachineBlock> blockFactory,
                          TriFunction<BlockEntityType<?>, BlockPos, BlockState, IMachineBlockEntity> blockEntityFactory,
                          BiFunction<IMachineBlock, Item.Properties, MachineItem> itemFactory) {
        super(owner, parent, name, callback, TCRegistries.MACHINE_DEFINITIONS);
        this.definitionFactory = definitionFactory;
        this.machineFactory = machineFactory;
        this.blockFactory = blockFactory;
        this.blockEntityFactory = blockEntityFactory;
        this.itemFactory = itemFactory;
    }

    /// 机器定义数据工厂
    protected final BiFunction<AbstractRegistrate<?>, ResourceLocation, DEFINITION> definitionFactory;
    /// 机器元数据
    protected final Function<IMachineBlockEntity, MetaMachine> machineFactory;
    /// 机器方块工厂
    protected final BiFunction<BlockBehaviour.Properties, DEFINITION, IMachineBlock> blockFactory;
    /// 机器方块工厂
    protected final BiFunction<IMachineBlock, Item.Properties, MachineItem> itemFactory;
    /// 机器实体工厂
    protected final TriFunction<BlockEntityType<?>, BlockPos, BlockState, IMachineBlockEntity> blockEntityFactory;

    //TODO: MISC
    /// 渲染器工厂
    @Nullable
    @Setter
    private Supplier<IRenderer> renderer;

    /// 设置以Model为源的渲染器
    public MachineBuilder<DEFINITION, P> modelRenderer(Supplier<ResourceLocation> model) {
        this.renderer = () -> IRenderer.EMPTY;//new MachineRenderer(model.get());
        return this;
    }

    /// 设置为默认渲染器
    public MachineBuilder<DEFINITION, P> defaultModelRenderer() {
        return modelRenderer(() -> ResourceLocation.fromNamespaceAndPath(getOwner().getModid(), "block/" + getName()));
    }

    /// 渲染模型
    @Setter
    private VoxelShape shape = Shapes.block();
    /// 朝向逻辑
    @Setter
    private RotationState rotationState = RotationState.NONE;
    /// 是否允许额外旋转
    @Setter
    boolean enableExtraRotation = false;
    /// 是否使用自定义渲染
    @Setter
    private boolean hasTESR;
    /// 是否渲染多方快的世界预览
    @Setter
    private boolean renderMultiblockWorldPreview = true;
    /// 是否渲染多方快的JEI(EMI,NEI)预览
    @Setter
    private boolean renderMultiblockXEIPreview = true;

    /// 方块属性
    @Setter
    private NonNullUnaryOperator<BlockBehaviour.Properties> blockProp = p -> p;
    /// 物品属性
    @Setter
    private NonNullUnaryOperator<Item.Properties> itemProp = p -> p;
    /// 方块工厂
    @Setter
    @Nullable
    private Consumer<BlockBuilder<? extends Block, ?>> blockBuilder;
    /// 物品工厂
    @Setter
    @Nullable
    private Consumer<ItemBuilder<? extends MachineItem, ?>> itemBuilder;
    /// 方块书体注册完成回调
    @Setter
    private NonNullConsumer<BlockEntityType<BlockEntity>> onBlockEntityRegister = MachineBlockEntity::onBlockEntityRegister;
    /// 默认绘制颜色
    @Setter
    private int paintingColor = Long.decode(ConfigHolder.INSTANCE.client.defaultPaintingColor).intValue();
    /// 提示栏信息
    private final List<Component> tooltips = new ArrayList<>();
    /// 提示栏构建器
    @Setter
    @Nullable
    private BiConsumer<ItemStack, List<Component>> tooltipBuilder;
    @Setter
    @Nullable
    private Supplier<BlockState> appearance;
    /// 机器Lang名称
    @Setter
    @Nullable
    private String langValue = null;


    /// 机器外观逻辑
    public MachineBuilder<DEFINITION, P> appearanceBlock(Supplier<? extends Block> block) {
        appearance = () -> block.get().defaultBlockState();
        return this;
    }

    /// 机器提示词逻辑
    public MachineBuilder<DEFINITION, P> tooltips(Component... components) {
        tooltips.addAll(Arrays.stream(components).toList());
        return this;
    }

    /// 机器提示词逻辑（一定条件下）
    public MachineBuilder<DEFINITION, P> conditionalTooltip(Component component, Supplier<Boolean> condition) {
        return conditionalTooltip(component, condition.get());
    }

    /// 机器提示词逻辑（一定条件下）
    public MachineBuilder<DEFINITION, P> conditionalTooltip(Component component, boolean condition) {
        if (condition)
            tooltips.add(component);
        return this;
    }

    /// 是否渲染多方快
    public MachineBuilder<DEFINITION, P> multiblockPreviewRenderer(boolean multiBlockWorldPreview,
                                                                   boolean multiBlockXEIPreview) {
        this.renderMultiblockWorldPreview = multiBlockWorldPreview;
        this.renderMultiblockXEIPreview = multiBlockXEIPreview;
        return this;
    }

    /// Builder处理
    public MachineBuilder<DEFINITION, P> transform(Consumer<MachineBuilder<DEFINITION, P>> config) {
        config.accept(this);
        return this;
    }

    //TODO: Register
    private DEFINITION definition;

    @Override
    protected @NonnullType @NotNull DEFINITION createEntry() {
        if (definition == null) {
            definition = definitionFactory.apply(getOwner(), ResourceLocation.fromNamespaceAndPath(getOwner().getModid(), getName()));
        }
        return definition;
    }

    @Override
    public @NotNull MachineEntry<DEFINITION> register() {
        var definition = createEntry();

        var blockBuilder = BlockBuilderWrapper.makeBlockBuilder(this, definition);
        if (this.langValue != null) {
            blockBuilder.lang(langValue);
        }
        if (this.blockBuilder != null) {
            this.blockBuilder.accept(blockBuilder);
        }
        var block = blockBuilder.register();

        var itemBuilder = ItemBuilderWrapper.makeItemBuilder(this, block);
        if (this.itemBuilder != null) {
            this.itemBuilder.accept(itemBuilder);
        }
        var item = itemBuilder.register();

        var blockEntityBuilder = getOwner()
                .blockEntity(getName(), (type, pos, state) -> blockEntityFactory.apply(type, pos, state).self())
                .onRegister(onBlockEntityRegister)
                .validBlock(block);
        if (hasTESR) {
            blockEntityBuilder = blockEntityBuilder.renderer(() -> TCRendererProvider::getOrCreate);
        }
        var blockEntity = blockEntityBuilder.register();
        definition.setBlockSupplier(block);
        definition.setItemSupplier(item);
        definition.setBlockEntityTypeSupplier(blockEntity::get);
        definition.setMachineSupplier(machineFactory);

        definition.setTooltipBuilder((itemStack, components) -> {
            components.addAll(tooltips);
            if (tooltipBuilder != null) tooltipBuilder.accept(itemStack, components);
        });

        if (renderer == null) {
            renderer = () -> IRenderer.EMPTY;//new MachineRenderer(ResourceLocation.fromNamespaceAndPath(owner.getModid(), "block/machine/" + name));
        }
        if (appearance == null) {
            appearance = block::getDefaultState;
        }
        definition.setAppearance(appearance);
        definition.setEnableExtraRotation(enableExtraRotation);
        definition.setRenderer(TechnoChronicleAPI.isClientSide() ? renderer.get() : IRenderer.EMPTY);
        definition.setShape(shape);
        definition.setDefaultPaintingColor(paintingColor);
        definition.setRenderXEIPreview(renderMultiblockXEIPreview);
        definition.setRenderWorldPreview(renderMultiblockWorldPreview);
        
        return (MachineEntry<DEFINITION>) super.register();
    }

    static class BlockBuilderWrapper {

        @SuppressWarnings("removal")
        public static <D extends BaseMachineDefinition<?>, P> BlockBuilder<Block, MachineBuilder<D, P>>
        makeBlockBuilder(MachineBuilder<D, P> builder, D definition) {
            return builder.getOwner().block(builder, builder.getName(), prop -> {
                        RotationState.setPreState(builder.rotationState);
                        BaseMachineDefinition.setBuilt(definition);
                        var b = builder.blockFactory.apply(prop, definition);
                        RotationState.clearPreState();
                        BaseMachineDefinition.clearBuilt();
                        return b.self();
                    })
                    .initialProperties(() -> Blocks.DISPENSER)
                    .properties(BlockBehaviour.Properties::noLootTable)
                    .addLayer(() -> RenderType::cutoutMipped)
                    .blockstate(NonNullBiConsumer.noop())
                    .properties(builder.blockProp);
        }
    }

    static class ItemBuilderWrapper {

        public static <D extends BaseMachineDefinition<?>, P> ItemBuilder<MachineItem, MachineBuilder<D, P>>
        makeItemBuilder(MachineBuilder<D, P> builder, BlockEntry<Block> block) {
            return builder.getOwner().item(builder, builder.getName(), prop ->
                            builder.itemFactory.apply((IMachineBlock) block.get(), prop))
                    .setData(ProviderType.LANG, NonNullBiConsumer.noop()) // do not gen any lang keys
                    .model(NonNullBiConsumer.noop())
                    .properties(builder.itemProp);
        }
    }

    @Override
    protected @NotNull RegistryEntry<BaseMachineDefinition<?>, DEFINITION> createEntryWrapper(DeferredHolder<BaseMachineDefinition<?>, DEFINITION> delegate) {
        return new MachineEntry<>(getOwner(), delegate);
    }
}