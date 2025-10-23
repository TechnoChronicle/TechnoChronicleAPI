package net.technochronicle.test.client;

import net.minecraft.client.KeyMapping;
import net.technochronicle.technochronicleapi.utils.ClientUtil;

import com.mojang.blaze3d.platform.InputConstants;

public final class KeyBind {

    public static void init() {
        dev.architectury.registry.client.keymappings.KeyMappingRegistry.register(flyingspeedKey);
    }

    private static final KeyMapping flyingspeedKey = new KeyMap("key.technochronicle.flyingspeed", InputConstants.KEY_X, 0);

    private static class KeyMap extends KeyMapping {

        boolean isDownOld;
        private final int type;

        KeyMap(String name, int keyCode, int type) {
            super(name, keyCode, "key.keybinding.technochronicle");
            this.type = type;
        }

        @Override
        public void setDown(boolean isDown) {
            super.setDown(isDown);
            if (isDownOld != isDown && isDown && ClientUtil.getPlayer() != null) {
                // ClientMessage.send("key", buf -> buf.writeVarInt(type));
            }
            isDownOld = isDown;
        }
    }
}
