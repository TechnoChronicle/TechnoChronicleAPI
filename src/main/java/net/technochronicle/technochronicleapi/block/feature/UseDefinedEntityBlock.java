package net.technochronicle.technochronicleapi.block.feature;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.technochronicle.technochronicleapi.definition.EntityBlockDefinition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/// 使用{@link EntityBlockDefinition}的实体方块接口
public interface UseDefinedEntityBlock extends EntityBlock {
    EntityBlockDefinition<?> getDefinition();
    @Override
    @Nullable
    default BlockEntity newBlockEntity(@NotNull BlockPos blockPos, @NotNull BlockState blockState) {
        return getDefinition().getBlockEntityType().create(blockPos, blockState);
    }
}
