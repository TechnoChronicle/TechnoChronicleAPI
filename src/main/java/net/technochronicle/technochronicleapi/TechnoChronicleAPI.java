package net.technochronicle.technochronicleapi;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(TechnoChronicleAPI.MOD_ID)
public class TechnoChronicleAPI {
    public static final String MOD_ID = "technochronicleapi";

    public static final Logger LOGGER = LogManager.getLogger(TechnoChronicleAPI.MOD_ID);
    public TechnoChronicleAPI(IEventBus modEventBus, ModContainer modContainer) {
    }
}