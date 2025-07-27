package net.technochronicle.technochronicleapi.render;

import com.lowdragmc.lowdraglib.client.renderer.ATESRRendererProvider;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

@OnlyIn(Dist.CLIENT)
public class TCRendererProvider extends ATESRRendererProvider<BlockEntity> {

    private static TCRendererProvider INSTANCE;

    private TCRendererProvider(BlockEntityRendererProvider.Context context) {
        // ModelBellows.INSTANCE = new ModelBellows(context);
        // ModelHungryChest.INSTANCE = new ModelHungryChest(context);
    }

    public static TCRendererProvider getOrCreate(BlockEntityRendererProvider.Context context) {
        if (INSTANCE == null) {
            INSTANCE = new TCRendererProvider(context);
        }
        return INSTANCE;
    }

    @Nullable
    public static TCRendererProvider getInstance() {
        return INSTANCE;
    }
}