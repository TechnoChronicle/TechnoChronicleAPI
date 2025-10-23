package net.technochronicle.test.client;

import net.technochronicle.technochronicleapi.TechnoChronicleAPI;
import net.technochronicle.test.common.CommonProxy;

public class ClientProxy extends CommonProxy {

    public ClientProxy() {
        super();
        init();
        var eventBus = TechnoChronicleAPI.getModEventBus();
    }

    private void init() {
        KeyBind.init();
    }
}
