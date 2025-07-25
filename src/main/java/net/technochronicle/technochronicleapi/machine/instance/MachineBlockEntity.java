package net.technochronicle.technochronicleapi.machine.instance;

import com.lowdragmc.lowdraglib.syncdata.managed.MultiManagedStorage;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.technochronicle.technochronicleapi.TCValues;
import net.technochronicle.technochronicleapi.blockentity.MetaBlockEntity;
import net.technochronicle.technochronicleapi.machine.IMachineBlockEntity;
import net.technochronicle.technochronicleapi.machine.MetaMachine;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MachineBlockEntity extends MetaBlockEntity implements IMachineBlockEntity {
    /// 机器存储区
    public final MultiManagedStorage managedStorage = new MultiManagedStorage();
    /// 机器元数据
    @Getter
    public final MetaMachine metaMachine;
    /// 随机时刻偏移
    @Getter
    private final long offset = TCValues.RNG.nextInt(20);

    public MachineBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
        this.metaMachine = getDefinition().createMetaMachine(this);
    }

    /// 工厂函数
    public static MachineBlockEntity createBlockEntity(BlockEntityType<?> type, BlockPos pos,
                                                       BlockState blockState) {
        return new MachineBlockEntity(type, pos, blockState);
    }

    /// 方块实体注册回调
    public static void onBlockEntityRegister(BlockEntityType<BlockEntity> type) {
    }

    /// 获取机器存储区
    @Override
    public @NotNull MultiManagedStorage getRootStorage() {
        return managedStorage;
    }

    @Override
    protected void applyImplicitComponents(BlockEntity.@NotNull DataComponentInput componentInput) {
        super.applyImplicitComponents(componentInput);
        metaMachine.applyImplicitComponents(new ExDataComponentInput() {
            @Override
            public @Nullable <T> T get(@NotNull DataComponentType<T> component) {
                return componentInput.get(component);
            }

            @Override
            public <T> @NotNull T getOrDefault(@NotNull DataComponentType<? extends T> component, @NotNull T defaultValue) {
                return componentInput.getOrDefault(component, defaultValue);
            }
        });
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.@NotNull Builder components) {
        super.collectImplicitComponents(components);
        metaMachine.collectImplicitComponents(components);
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        metaMachine.onUnload();
    }

    @Override
    public void clearRemoved() {
        super.clearRemoved();
        metaMachine.onLoad();
    }

    @Override
    public void setChanged() {
        if (getLevel() != null) {
            getLevel().blockEntityChanged(getBlockPos());
        }
    }

    /**
     * Extending interface to make {@link BlockEntity.DataComponentInput} public as it's protected by default.
     */
    public interface ExDataComponentInput extends BlockEntity.DataComponentInput {
    }
}