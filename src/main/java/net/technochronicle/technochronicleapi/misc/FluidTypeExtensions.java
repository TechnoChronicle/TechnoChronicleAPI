package net.technochronicle.technochronicleapi.misc;

import com.mojang.blaze3d.vertex.PoseStack;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ScreenEffectRenderer;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.technochronicle.technochronicleapi.TechnoChronicleAPI;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Getter
@Setter
public class FluidTypeExtensions implements IClientFluidTypeExtensions {
    public static final ResourceLocation EMPTY_TEXTURE = TechnoChronicleAPI.id("block/fluid/empty");
    public static final ResourceLocation UNDERWATER_LOCATION = ResourceLocation.withDefaultNamespace("textures/misc/underwater.png");
    public static final ResourceLocation LIQUID_STILL = ResourceLocation.withDefaultNamespace("block/water_still");
    public static final ResourceLocation LIQUID_FLOWING = ResourceLocation.withDefaultNamespace("block/water_flow");
    public static final ResourceLocation LIQUID_OVERLAY = ResourceLocation.withDefaultNamespace("block/water_overlay");
    public static final ResourceLocation MELT_STILL = TechnoChronicleAPI.id("block/fluid/melt_still");
    public static final ResourceLocation MELT_FLOWING = TechnoChronicleAPI.id("block/fluid/melt_flow");
    public static final FluidTypeExtensions EMPTY = new FluidTypeExtensions(-1, EMPTY_TEXTURE, EMPTY_TEXTURE);

    protected FluidTypeExtensions(int tintColor,
                                  ResourceLocation stillTexture,
                                  ResourceLocation flowingTexture,
                                  ResourceLocation overlayTexture,
                                  ResourceLocation renderOverlayTexture) {
        this.tintColor = tintColor;
        this.stillTexture = stillTexture;
        this.flowingTexture = flowingTexture;
        this.overlayTexture = overlayTexture;
        this.renderOverlayTexture = renderOverlayTexture;
    }

    public FluidTypeExtensions(int tintColor, ResourceLocation stillTexture, ResourceLocation flowingTexture) {
        this(tintColor, stillTexture, flowingTexture, null, null);
    }

    private int tintColor;
    private ResourceLocation stillTexture;
    private ResourceLocation flowingTexture;
    private ResourceLocation overlayTexture;
    private ResourceLocation renderOverlayTexture;

    @Override
    public @Nullable ResourceLocation getRenderOverlayTexture(@NotNull Minecraft mc) {
        return getRenderOverlayTexture();
    }

    public static Builder create() {
        return new Builder();
    }

    @Accessors(chain = true)
    @Setter
    public static class Builder {
        private int tintColor = -1;
        private ResourceLocation stillTexture;
        private ResourceLocation flowingTexture;
        private ResourceLocation overlayTexture = null;
        private ResourceLocation renderOverlayTexture = null;

        public FluidTypeExtensions build() {
            return new FluidTypeExtensions(tintColor, stillTexture, flowingTexture, overlayTexture, renderOverlayTexture);
        }

        public Builder MeltBase() {
            return this.setStillTexture(MELT_STILL)
                    .setFlowingTexture(MELT_FLOWING);
        }

        public Builder LiquidBase() {
            return this.setStillTexture(LIQUID_STILL)
                    .setFlowingTexture(LIQUID_FLOWING)
                    .setOverlayTexture(LIQUID_OVERLAY)
                    .setRenderOverlayTexture(UNDERWATER_LOCATION);
        }
    }
}