package net.technochronicle.technochronicleapi;

import icyllis.modernui.ModernUI;
import icyllis.modernui.audio.AudioManager;
import net.technochronicle.technochronicleapi.mui.fragment.TechTreeFragment;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;

import java.util.UUID;

public class MuiTest {
    public static void main(String[] args) {
        System.setProperty("java.awt.headless", "true");
        Configurator.setRootLevel(Level.DEBUG);

        try (ModernUI app = new ModernUI()) {
            app.run(new TechTreeFragment(UUID.fromString("380df991-f603-344c-a090-369bad2a924a")));
        }

        AudioManager.getInstance().close();
        System.gc();
    }
}