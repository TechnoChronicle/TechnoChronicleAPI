package net.technochronicle.technochronicleapi.misc;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.technochronicle.technochronicleapi.registrate.TCRegistrate;
import org.jetbrains.annotations.NotNull;

/// 适用 {@link CreativeModeTab.Builder#displayItems(CreativeModeTab.DisplayItemsGenerator)}的与{@link TCRegistrate}搭配的生成器
public class RegistrateDisplayItemsGenerator implements CreativeModeTab.DisplayItemsGenerator {
    public final String name;
    public final TCRegistrate registrate;

    public RegistrateDisplayItemsGenerator(String name, TCRegistrate registrate) {
        this.name = name;
        this.registrate = registrate;
    }

    @Override
    public void accept(@NotNull CreativeModeTab.ItemDisplayParameters itemDisplayParameters,
                       @NotNull CreativeModeTab.Output output) {
        var tab = registrate.get(name, Registries.CREATIVE_MODE_TAB);
        for (var entry : registrate.getAll(Registries.BLOCK)) {
            if (!registrate.isInCreativeTab(entry, tab))
                continue;
            Item item = entry.get().asItem();
            if (item == Items.AIR)
                continue;
        }
        for (var entry : registrate.getAll(Registries.ITEM)) {
            if (!registrate.isInCreativeTab(entry, tab))
                continue;
            Item item = entry.get();

            output.accept(item);
        }
    }
}