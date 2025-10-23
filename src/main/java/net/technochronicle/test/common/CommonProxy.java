package net.technochronicle.test.common;

import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLConstructModEvent;
import net.technochronicle.technochronicleapi.TechnoChronicleAPI;

import static net.technochronicle.technochronicleapi.registries.TCRegistrate.TCRegistry;

public class CommonProxy {

    public CommonProxy() {
        init();
        var eventBus = TechnoChronicleAPI.getModEventBus();
        TCRegistry.registerEventListeners(eventBus);
        eventBus.addListener(CommonProxy::commonSetup);
        eventBus.addListener(CommonProxy::modConstruct);
    }

    private static void commonSetup(FMLCommonSetupEvent event) {}

    private static void modConstruct(FMLConstructModEvent event) {}

    private void init() {}
}
