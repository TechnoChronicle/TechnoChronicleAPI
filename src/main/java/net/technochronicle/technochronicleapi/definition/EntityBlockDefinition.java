package net.technochronicle.technochronicleapi.definition;

import com.tterrag.registrate.AbstractRegistrate;
import lombok.Setter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.technochronicle.technochronicleapi.machine.instance.MachineBlockEntity;

import java.util.function.Supplier;

/// 实体方块定义类
public class EntityBlockDefinition<T extends MachineBlockEntity>extends MetaDefinition<EntityBlockDefinition<T>> {
    @Setter
    private Supplier<BlockEntityType<? extends BlockEntity>> blockEntityTypeSupplier;

    public EntityBlockDefinition(AbstractRegistrate<?> owner, ResourceLocation id) {
        super(owner, id);
    }

    public BlockEntityType<? extends BlockEntity> getBlockEntityType() {
        return blockEntityTypeSupplier.get();
    }
}