package net.technochronicle.technochronicleapi.test;

import com.tterrag.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.util.entry.FluidEntry;
import com.tterrag.registrate.util.entry.ItemEntry;
import com.tterrag.registrate.util.entry.RegistryEntry;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.technochronicle.technochronicleapi.TechnoChronicleAPI;
import net.technochronicle.technochronicleapi.misc.FluidTypeExtensions;
import net.technochronicle.technochronicleapi.misc.RegistrateDisplayItemsGenerator;
import net.technochronicle.technochronicleapi.registrate.TCRegistrate;
import org.jetbrains.annotations.Nullable;

public class TestInit {
    public static final TCRegistrate REGISTRATE = TCRegistrate.create(TechnoChronicleAPI.MOD_ID);

    static {
        REGISTRATE.defaultCreativeTab((ResourceKey<CreativeModeTab>) null);
    }

    public static RegistryEntry<CreativeModeTab, CreativeModeTab> Test_Tab = REGISTRATE.defaultCreativeTab("test_tab",
                    builder -> builder
                            .displayItems(new RegistrateDisplayItemsGenerator("test_tab", REGISTRATE))
                            .icon(Items.IRON_INGOT::getDefaultInstance)
                            .title(REGISTRATE.addLang("itemGroup",
                                    ResourceLocation.fromNamespaceAndPath(TechnoChronicleAPI.MOD_ID, "test_tab"),
                                    "Test Tab"))
                            .build())
            .register();

    static {
        REGISTRATE.creativeModeTab(() -> Test_Tab);
    }

    public static ItemEntry<Item> Test_Item = REGISTRATE.item("test_item", Item::new)
            .lang("Test Item")
            .register();

    public static BlockEntry<Block> Test_Block = REGISTRATE.block("test_block", Block::new)
            .simpleItem()
            .lang("Test Block")
            .register();

    public static FluidEntry<BaseFlowingFluid.Flowing> Test_Fluid = REGISTRATE.tcFluid(REGISTRATE, "test_fluid",
                    BaseFlowingFluid.Flowing::new, FluidType::new,
                    () -> FluidTypeExtensions.create()
                            .LiquidBase()
                            .setTintColor(0xffff00FF)
                            .build())
            .renderType(() -> () -> RenderType.TRANSLUCENT)
            .register();
    public static FluidEntry<BaseFlowingFluid.Flowing> Test_Fluid2 = REGISTRATE.tcFluid(REGISTRATE, "test_fluid2",
                    BaseFlowingFluid.Flowing::new, FluidType::new,
                    () -> FluidTypeExtensions.EMPTY)
            .renderType(() -> () -> RenderType.TRANSLUCENT)
            .register();

    public static void init() {
    }
}