package net.technochronicle.technochronicleapi.machine;

import com.lowdragmc.lowdraglib.syncdata.IEnhancedManaged;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.annotation.RequireRerender;
import com.lowdragmc.lowdraglib.syncdata.field.FieldManagedStorage;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import com.lowdragmc.lowdraglib.utils.DummyWorld;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.technochronicle.technochronicleapi.TechnoChronicleAPI;
import net.technochronicle.technochronicleapi.block.feature.ExtraRotate;
import net.technochronicle.technochronicleapi.block.feature.IAppearance;
import net.technochronicle.technochronicleapi.block.feature.RotationState;
import net.technochronicle.technochronicleapi.definition.BaseMachineDefinition;
import net.technochronicle.technochronicleapi.block.feature.IPaintable;
import net.technochronicle.technochronicleapi.machine.features.ITickSubscription;
import net.technochronicle.technochronicleapi.machine.instance.MachineBlock;
import net.technochronicle.technochronicleapi.machine.instance.MachineBlockEntity;
import net.technochronicle.technochronicleapi.machine.misc.TickableSubscription;
import net.technochronicle.technochronicleapi.machine.trait.MachineTrait;
import net.technochronicle.technochronicleapi.misc.owner.MachineOwner;
import net.technochronicle.technochronicleapi.misc.owner.PlayerOwner;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MetaMachine implements IEnhancedManaged, ITickSubscription, IAppearance, IPaintable {
    /// 机器数据管理器
    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(MetaMachine.class);
    /// 机器异步存储
    @Getter
    private final FieldManagedStorage syncStorage = new FieldManagedStorage(this);
    /// 所有者UUID
    @Setter
    @Getter
    @Persisted
    @DescSynced
    @Nullable
    private UUID ownerUUID;
    /// 机器对应方块实体
    @Getter
    public final IMachineBlockEntity holder;
    /// 机器绘制颜色
    @Getter
    @Setter
    @Persisted
    @DescSynced
    @RequireRerender
    private int paintingColor = -1;
    /// 机器NBT属性列表
    @Getter
    protected final List<MachineTrait> traits;
    /// 当前Tick事件列表
    private final List<TickableSubscription> serverTicks;
    /// 预备Tick事件列表
    private final List<TickableSubscription> waitingToAdd;

    public MetaMachine(IMachineBlockEntity holder) {
        this.holder = holder;
        this.traits = new ArrayList<>();
        this.serverTicks = new ArrayList<>();
        this.waitingToAdd = new ArrayList<>();
        // 绑定异步存储容器
        this.holder.getRootStorage().attach(getSyncStorage());
    }

    //TODO: Initialization
    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    public void onChanged() {
        var level = getLevel();
        if (level != null && !level.isClientSide && level.getServer() != null) {
            level.getServer().execute(this::markDirty);
        }
    }

    /// 获取方块实体所在维度
    public @Nullable Level getLevel() {
        return holder.level();
    }

    /// 获取方块实体坐标
    public BlockPos getPos() {
        return holder.pos();
    }

    /// 获取方块实体对应BlockState
    public BlockState getBlockState() {
        return holder.getSelf().getBlockState();
    }

    /// 方块是否被移除
    public boolean isRemote() {
        return getLevel() == null ? TechnoChronicleAPI.isClientThread() : getLevel().isClientSide;
    }

    /// 通知方块更新
    public void notifyBlockUpdate() {
        holder.notifyBlockUpdate();
    }

    public void scheduleRenderUpdate() {
        holder.scheduleRenderUpdate();
    }

    public void scheduleNeighborShapeUpdate() {
        Level level = getLevel();
        BlockPos pos = getPos();

        if (level == null || pos == null)
            return;

        level.getBlockState(pos).updateNeighbourShapes(level, pos, Block.UPDATE_ALL);
    }

    public long getOffsetTimer() {
        return holder.getOffsetTimer();
    }

    /// 标识方块数据被修改
    public void markDirty() {
        holder.getSelf().setChanged();
    }

    /// 方块实体是否被删除
    public boolean isInValid() {
        return holder.getSelf().isRemoved();
    }

    /// {@link BlockEntity#setRemoved()}
    public void onUnload() {
        traits.forEach(MachineTrait::onMachineUnLoad);
        for (TickableSubscription serverTick : serverTicks) {
            serverTick.unsubscribe();
        }
        serverTicks.clear();
    }

    /// {@link BlockEntity#clearRemoved()}
    public void onLoad() {
        traits.forEach(MachineTrait::onMachineLoad);
    }

    /**
     * Use for data not able to be saved with the SyncData system, like optional mod compatiblity in internal machines.
     *
     * @param tag     the CompoundTag to load data from
     * @param forDrop if the save is done for dropping the machine as an item.
     */
    public void saveCustomPersistedData(@NotNull CompoundTag tag, boolean forDrop) {
        for (MachineTrait trait : this.getTraits()) {
            trait.saveCustomPersistedData(tag, forDrop);
        }
    }

    public void loadCustomPersistedData(@NotNull CompoundTag tag) {
        for (MachineTrait trait : this.getTraits()) {
            trait.loadCustomPersistedData(tag);
        }
    }

    public void applyImplicitComponents(MachineBlockEntity.ExDataComponentInput componentInput) {
    }

    public void collectImplicitComponents(DataComponentMap.Builder components) {
    }
    //TODO: Tickable Manager

    /**
     * For initialization. To get level and property fields after auto sync, you can subscribe it in {@link #onLoad()}
     * event.
     */
    @Nullable
    public TickableSubscription subscribeServerTick(Runnable runnable) {
        if (!isRemote()) {
            var subscription = new TickableSubscription(runnable);
            waitingToAdd.add(subscription);
            var blockState = getBlockState();
            if (!blockState.getValue(IMachineBlock.SERVER_TICK)) {
                if (getLevel() instanceof ServerLevel serverLevel) {
                    blockState = blockState.setValue(IMachineBlock.SERVER_TICK, true);
                    holder.getSelf().setBlockState(blockState);
                    serverLevel.getServer().tell(new TickTask(0, () -> {
                        if (!isInValid()) {
                            serverLevel.setBlockAndUpdate(getPos(),
                                    getBlockState().setValue(IMachineBlock.SERVER_TICK, true));
                        }
                    }));
                }
            }
            return subscription;
        } else if (getLevel() instanceof DummyWorld) {
            var subscription = new TickableSubscription(runnable);
            waitingToAdd.add(subscription);
            return subscription;
        }
        return null;
    }

    public void unsubscribe(@Nullable TickableSubscription current) {
        if (current != null) {
            current.unsubscribe();
        }
    }

    /// 服务端Tick逻辑
    public final void serverTick() {
        executeTick();
        if (serverTicks.isEmpty() && waitingToAdd.isEmpty() && !isInValid()) {
            getLevel().setBlockAndUpdate(getPos(), getBlockState().setValue(IMachineBlock.SERVER_TICK, false));
        }
    }

    public boolean isFirstDummyWorldTick = true;

    /// 客户端Tick逻辑
    @OnlyIn(Dist.CLIENT)
    public void clientTick() {
        if (getLevel() instanceof DummyWorld) {
            if (isFirstDummyWorldTick) {
                isFirstDummyWorldTick = false;
                onLoad();
            }
            executeTick();
        }
    }

    /// 执行Tick逻辑
    private void executeTick() {
        if (!waitingToAdd.isEmpty()) {
            serverTicks.addAll(waitingToAdd);
            waitingToAdd.clear();
        }
        var iter = serverTicks.iterator();
        while (iter.hasNext()) {
            var tickable = iter.next();
            if (tickable.isStillSubscribed()) {
                tickable.run();
            }
            if (isInValid()) break;
            if (!tickable.isStillSubscribed()) {
                iter.remove();
            }
        }
    }

    //TODO: MISC
    @Nullable
    public static MetaMachine getMachine(BlockGetter level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof IMachineBlockEntity machineBlockEntity) {
            return machineBlockEntity.getMetaMachine();
        }
        return null;
    }

    /**
     * All traits should be initialized while MetaMachine is creating. you cannot add them on the fly.
     */
    public void attachTraits(MachineTrait trait) {
        traits.add(trait);
    }

    /// 清除仓储数据
    public void clearInventory(IItemHandlerModifiable inventory) {
        for (int i = 0; i < inventory.getSlots(); i++) {
            ItemStack stackInSlot = inventory.getStackInSlot(i);
            if (!stackInSlot.isEmpty()) {
                inventory.setStackInSlot(i, ItemStack.EMPTY);
                Block.popResource(getLevel(), getPos(), stackInSlot);
            }
        }
    }

    public BaseMachineDefinition<?> getDefinition() {
        return holder.getDefinition();
    }

    /**
     * 添加CollisionBoundingBox
     */
    public void addCollisionBoundingBox(List<VoxelShape> collisionList) {
        collisionList.add(Shapes.block());
    }

    public boolean canSetIoOnSide(@Nullable Direction direction) {
        return !hasFrontFacing() || getFrontFacing() != direction;
    }

    public static @NotNull Direction getFrontFacing(@Nullable MetaMachine machine) {
        return machine == null ? Direction.NORTH : machine.getFrontFacing();
    }

    public Direction getFrontFacing() {
        var blockState = getBlockState();
        if (blockState.getBlock() instanceof MachineBlock machineBlock) {
            return machineBlock.getFrontFacing(blockState);
        }
        return Direction.NORTH;
    }

    public final boolean hasFrontFacing() {
        var blockState = getBlockState();
        if (blockState.getBlock() instanceof MachineBlock machineBlock) {
            return machineBlock.getRotationState() != RotationState.NONE;
        }
        return false;
    }

    public boolean isFacingValid(Direction facing) {
        if (enableExtraRotation()) {
            return true;
        }
        if (hasFrontFacing() && facing == getFrontFacing()) return false;

        var blockState = getBlockState();
        if (blockState.getBlock() instanceof MachineBlock metaMachineBlock) {
            return metaMachineBlock.rotationState.test(facing);
        }
        return false;
    }

    public static @NotNull ExtraRotate getUpwardFacing(@Nullable MetaMachine machine) {
        return machine == null || !machine.enableExtraRotation() ? ExtraRotate.ORIGINAL :
                machine.getBlockState().getValue(IMachineBlock.EXTRA_ROTATE);
    }

    public ExtraRotate getUpwardsFacing() {
        return this.enableExtraRotation() ? this.getBlockState().getValue(IMachineBlock.EXTRA_ROTATE) : ExtraRotate.ORIGINAL;
    }

    public void setUpwardsFacing(@NotNull ExtraRotate upwardsFacing) {
        if (!getDefinition().isEnableExtraRotation()) {
            return;
        }
        var blockState = getBlockState();
        if (blockState.getBlock() instanceof MachineBlock &&
                blockState.getValue(IMachineBlock.EXTRA_ROTATE) != upwardsFacing) {
            getLevel().setBlockAndUpdate(getPos(),
                    blockState.setValue(IMachineBlock.EXTRA_ROTATE, upwardsFacing));
            if (getLevel() != null && !getLevel().isClientSide) {
                notifyBlockUpdate();
                markDirty();
            }
        }
    }

    /// 方块旋转事件钩子
    public void onRotated(Direction oldFacing, Direction newFacing) {
    }

    public boolean enableExtraRotation() {
        return getDefinition().isEnableExtraRotation();
    }

    /// 着色颜色
    public int tintColor(int index) {
        // index < -100 => emission if shimmer is installed.
        if (index == 1 || index == -111) {
            return getRealColor();
        }
        return -1;
    }

    /// 周围方块更新事件钩子
    public void onNeighborChanged(Block block, BlockPos fromPos, boolean isMoving) {
    }

    /// 处理Tick逻辑
    public void animateTick(RandomSource random) {
    }

    /// 获取机器外观逻辑
    @Override
    @NotNull
    public BlockState getBlockAppearance(BlockState state, BlockAndTintGetter level, BlockPos pos, Direction side,
                                         @Nullable BlockState sourceState, BlockPos sourcePos) {
        return (BlockState) getDefinition().getAppearance().get();
    }

    //TODO: Ownership

    /// 获取机器所有者
    public @Nullable MachineOwner getOwner() {
        return MachineOwner.getOwner(ownerUUID);
    }

    /// 获取机器所有玩家
    public @Nullable PlayerOwner getPlayerOwner() {
        return MachineOwner.getPlayerOwner(ownerUUID);
    }

    @Override
    public int getDefaultPaintingColor() {
        return getDefinition().getDefaultPaintingColor();
    }
}