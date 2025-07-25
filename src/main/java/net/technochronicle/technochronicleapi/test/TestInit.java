package net.technochronicle.technochronicleapi.test;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.technochronicle.technochronicleapi.TechnoChronicleAPI;
import net.technochronicle.technochronicleapi.registrate.TCRegistrate;

public class TestInit {
    public static TCRegistrate REGISTRATE = TCRegistrate.create(TechnoChronicleAPI.MOD_ID);

    static {
        REGISTRATE.defaultCreativeTab((ResourceKey<CreativeModeTab>) null);
    }

    public static void init() {
    }
}