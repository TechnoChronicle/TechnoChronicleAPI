package net.technochronicle.technochronicleapi.block;

import lombok.Getter;
import net.minecraft.world.level.block.Block;
import net.technochronicle.technochronicleapi.block.feature.UseDefinedEntityBlock;
import net.technochronicle.technochronicleapi.definition.EntityBlockDefinition;

/// 基础实体方块类
public class MetaEntityBlock extends Block implements UseDefinedEntityBlock {
    @Getter
    private final EntityBlockDefinition<?> definition;

    public MetaEntityBlock(Properties properties, EntityBlockDefinition<?> definition) {
        super(properties);
        this.definition = definition;
    }
}