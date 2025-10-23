package net.technochronicle.test;

import net.neoforged.fml.common.Mod;
import net.technochronicle.technochronicleapi.TechnoChronicleAPI;
import net.technochronicle.technochronicleapi.utils.DistExecutor;
import net.technochronicle.test.client.ClientProxy;
import net.technochronicle.test.common.CommonProxy;

@Mod(TechnoChronicleAPI.MOD_ID)
public class TCTest {

    public TCTest() {
        DistExecutor.unsafeRunForDist(() -> ClientProxy::new, () -> CommonProxy::new);
    }
}
