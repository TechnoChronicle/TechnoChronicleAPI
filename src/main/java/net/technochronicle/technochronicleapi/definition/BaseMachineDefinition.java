package net.technochronicle.technochronicleapi.definition;

import com.lowdragmc.lowdraglib.client.renderer.IRenderer;
import com.lowdragmc.lowdraglib.utils.ShapeUtils;
import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.technochronicle.technochronicleapi.machine.*;
import net.technochronicle.technochronicleapi.machine.instance.MachineBlockEntity;
import net.technochronicle.technochronicleapi.machine.instance.MachineItem;
import org.jetbrains.annotations.NotNull;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

/// 基础机器定义类
public class BaseMachineDefinition<T extends MachineBlockEntity> extends EntityBlockDefinition<T>implements NonNullSupplier<IMachineBlock> {
    public BaseMachineDefinition(AbstractRegistrate<?> owner, ResourceLocation id) {
        super(owner, id);
    }
    //TODO: {@link BaseMachineDefinition} 的基础内容
    //region Basic Data
    @Setter
    private Supplier<? extends Block> blockSupplier;
    @Setter
    private Supplier<? extends MachineItem> itemSupplier;
    @Setter
    private Function<IMachineBlockEntity, MetaMachine> machineSupplier;

    public Block getBlock() {
        return blockSupplier.get();
    }

    public MachineItem getItem() {
        return itemSupplier.get();
    }

    public MetaMachine createMetaMachine(IMachineBlockEntity blockEntity) {
        return machineSupplier.apply(blockEntity);
    }

    public ItemStack asStack() {
        return new ItemStack(getItem());
    }

    public ItemStack asStack(int count) {
        return new ItemStack(getItem(), count);
    }

    public String getDescriptionId() {
        return getBlock().getDescriptionId();
    }

    public BlockState defaultBlockState() {
        return getBlock().defaultBlockState();
    }

    @Override
    public @NotNull IMachineBlock get() {
        return (IMachineBlock) blockSupplier.get();
    }

    public String getName() {
        return getId().getPath();
    }

    @Override
    public String toString() {
        return "[Definition: %s]".formatted(getId());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        var that = (BaseMachineDefinition) o;

        return getId().equals(that.getId());
    }
    @Override
    public int hashCode() {
        return getId().hashCode();
    }
    //endregion
    //TODO: {@link BaseMachineDefinition} 的额外内容
    /// 默认绘制颜色
    @Getter
    @Setter
    private int defaultPaintingColor;
    /// 机器方块渲染器
    @Getter
    @Setter
    private IRenderer renderer;
    /// 机器渲染模型
    @Setter
    private VoxelShape shape;
    /// 获取机器渲染模型
    public VoxelShape getShape(Direction direction) {
        if (shape.isEmpty() || shape == Shapes.block() || direction == Direction.NORTH) return shape;
        return this.cache.computeIfAbsent(direction, dir -> ShapeUtils.rotate(shape, dir));
    }
    /// 是否渲染世界预览
    @Getter
    @Setter
    private boolean renderWorldPreview;
    /// 是否渲染JEI(EMI,NEI)预览
    @Getter
    @Setter
    private boolean renderXEIPreview;
    /// 机器渲染缓存
    private final Map<Direction, VoxelShape> cache = new EnumMap<>(Direction.class);
    /// 机器提示信息构建器
    @Getter
    @Setter
    private BiConsumer<ItemStack, List<Component>> tooltipBuilder;
    /// 机器外观工厂
    @Getter
    @Setter
    private Supplier<BlockState> appearance;
    /// 是否允许额外旋转
    @Getter
    @Setter
    private boolean enableExtraRotation = true;
    
    private static ThreadLocal<BaseMachineDefinition> Built = new ThreadLocal<>();

    public static void setBuilt(BaseMachineDefinition definition) {
        Built.set(definition);
    }

    public static BaseMachineDefinition getBuilt() {
        return Built.get();
    }

    public static void clearBuilt() {
        Built.remove();
    }
}
