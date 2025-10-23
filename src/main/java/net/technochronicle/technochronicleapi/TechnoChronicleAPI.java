package net.technochronicle.technochronicleapi;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;

import lombok.Getter;

@Mod(TechnoChronicleAPI.MOD_ID)
public class TechnoChronicleAPI {

    public static final String MOD_ID = "technochronicleapi";
    @Getter
    private static ModContainer ModContainer;
    @Getter
    private static IEventBus ModEventBus;
    @Getter
    private static final IEventBus GameEventBus = NeoForge.EVENT_BUS;

    public TechnoChronicleAPI(ModContainer container, IEventBus modEventBus) {
        ModContainer = container;
        ModEventBus = modEventBus;
    }
}
