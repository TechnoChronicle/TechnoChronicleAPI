package net.technochronicle.technochronicleapi.machine.features;

import com.lowdragmc.lowdraglib.gui.modular.IUIHolder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import net.technochronicle.technochronicleapi.TechnoChronicleAPI;

/// 带UI支持的机器接口
public interface IUIMachine extends IUIHolder, IMachineFeature {

    default boolean shouldOpenUI(Player player, InteractionHand hand, BlockHitResult hit) {
        return true;
    }

    default ItemInteractionResult tryToOpenUI(Player player, InteractionHand hand, BlockHitResult hit) {
        if (this.shouldOpenUI(player, hand, hit)) {
            if (player instanceof ServerPlayer serverPlayer) {
                //MachineUIFactory.INSTANCE.openUI(self(), serverPlayer);
                //TODO: 显示UI
            }
        } else {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        return ItemInteractionResult.sidedSuccess(player.level().isClientSide);
    }

    @Override
    default boolean isInvalid() {
        return self().isInValid();
    }

    @Override
    default boolean isRemote() {
        var level = self().getLevel();
        return level == null ? TechnoChronicleAPI.isClientThread() : level.isClientSide;
    }

    @Override
    default void markAsDirty() {
        self().markDirty();
    }
}