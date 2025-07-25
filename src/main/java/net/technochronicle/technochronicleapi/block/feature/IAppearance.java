package net.technochronicle.technochronicleapi.block.feature;


import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.extensions.IBlockExtension;
import org.jetbrains.annotations.Nullable;

/// 方块外观接口
public interface IAppearance {

    /**
     * @see IBlockExtension#getAppearance(BlockState, BlockAndTintGetter, BlockPos, Direction, BlockState, BlockPos)
     *      IBlockExtension#getAppearance
     */
    @Nullable
    default BlockState getBlockAppearance(BlockState state, BlockAndTintGetter level, BlockPos pos, Direction side,
                                          @Nullable BlockState sourceState, @Nullable BlockPos sourcePos) {
        return state;
    }
}
