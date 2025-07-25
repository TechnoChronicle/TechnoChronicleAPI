package net.technochronicle.technochronicleapi.block.feature;

import net.minecraft.world.level.block.state.properties.EnumProperty;

/// 额外旋转实现接口
public interface IExtraRotatableBlock extends IRotatableBlock{
    EnumProperty<ExtraRotate> EXTRA_ROTATE = EnumProperty.create("extra_rotate", ExtraRotate.class);
}
