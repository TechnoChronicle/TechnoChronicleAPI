package net.technochronicle.technochronicleapi.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemModelShaper;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import com.lowdragmc.lowdraglib.client.renderer.IItemRendererProvider;
import com.mojang.blaze3d.vertex.PoseStack;

import java.util.UUID;

public final class ClientUtil {

    public static boolean isClientPlayer(Entity entity) {
        return entity instanceof AbstractClientPlayer;
    }

    public static UUID getUUID() {
        return Minecraft.getInstance().player != null ? Minecraft.getInstance().player.getUUID() : null;
    }

    public static LocalPlayer getPlayer() {
        return Minecraft.getInstance().player;
    }

    public static ItemRenderer getItemRenderer() {
        return Minecraft.getInstance().getItemRenderer();
    }

    public static ModelBlockRenderer modelRenderer() {
        return Minecraft.getInstance().getBlockRenderer().getModelRenderer();
    }

    public static BakedModel getVanillaModel(ItemStack itemStack, ClientLevel clientLevel, LivingEntity livingEntity) {
        ItemModelShaper itemModelShaper = getItemRenderer().getItemModelShaper();
        BakedModel itemModel = itemModelShaper.getItemModel(itemStack.getItem());
        if (itemModel != null) {
            BakedModel resolve = itemModel.getOverrides().resolve(itemModel, itemStack, clientLevel, livingEntity, 0);
            if (resolve != null) {
                return resolve;
            }
        }

        return itemModelShaper.getModelManager().getMissingModel();
    }

    public static void vanillaRender(ItemStack itemStack, ItemDisplayContext displayContext, boolean leftHand, PoseStack poseStack, MultiBufferSource bufferSource, int combinedLight, int combinedOverlay, BakedModel p_model) {
        IItemRendererProvider.disabled.set(true);
        getItemRenderer().render(itemStack, displayContext, leftHand, poseStack, bufferSource, combinedLight, combinedOverlay, p_model);
        IItemRendererProvider.disabled.set(false);
    }
}
