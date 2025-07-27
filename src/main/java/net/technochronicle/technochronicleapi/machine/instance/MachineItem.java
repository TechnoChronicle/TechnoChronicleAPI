package net.technochronicle.technochronicleapi.machine.instance;

import com.lowdragmc.lowdraglib.client.renderer.IItemRendererProvider;
import com.lowdragmc.lowdraglib.client.renderer.IRenderer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.technochronicle.technochronicleapi.definition.BaseMachineDefinition;
import net.technochronicle.technochronicleapi.machine.IMachineBlock;
import org.jetbrains.annotations.Nullable;

public class MachineItem extends BlockItem implements IItemRendererProvider {
    public MachineItem(IMachineBlock block, Properties properties) {
        super(block.self(), properties);
    }

    public BaseMachineDefinition getDefinition() {
        return ((IMachineBlock) getBlock()).getDefinition();
    }

    @Nullable
    @Override
    public IRenderer getRenderer(ItemStack stack) {
        return ((IMachineBlock) getBlock()).getDefinition().getRenderer();
    }
}
