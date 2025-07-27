package net.technochronicle.technochronicleapi.techtree.condition;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.technochronicle.technochronicleapi.TechnoChronicleAPI;

import java.util.function.Supplier;

public class ConditionType<T extends TreeNodeUnlockCondition> {
    public static final ResourceKey<Registry<ConditionType<?>>> TREE_UNLOCK_COND =
            ResourceKey.createRegistryKey(TechnoChronicleAPI.id("tree_node_unlock_condition"));

    public static final DeferredRegister<ConditionType<?>> REGISTRY =
            DeferredRegister.create(TREE_UNLOCK_COND, TechnoChronicleAPI.MOD_ID);

    public static DeferredHolder<ConditionType<?>, ConditionType<AchievementCond>> ACHIEVEMENT_COND =
            register("achievment_cond", AchievementCond::new);
    public static DeferredHolder<ConditionType<?>, ConditionType<HandItemCond>> HAND_ITEM_COND =
            register("hand_item_cond", HandItemCond::new);

    public static <T extends TreeNodeUnlockCondition>
    DeferredHolder<ConditionType<?>, ConditionType<T>> register(String name, Supplier<T> factory) {
        return REGISTRY.register(name, () -> new ConditionType<>(factory));
    }

    public ConditionType(Supplier<? extends T> factory) {
        this.factory = factory;
    }

    private final Supplier<? extends T> factory;

    public T createCond() {
        return factory.get();
    }
}