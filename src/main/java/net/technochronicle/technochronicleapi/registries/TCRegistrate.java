package net.technochronicle.technochronicleapi.registries;

import net.technochronicle.technochronicleapi.TechnoChronicleAPI;

import com.tterrag.registrate.AbstractRegistrate;

public class TCRegistrate extends AbstractRegistrate<TCRegistrate> {

    public static TCRegistrate TCRegistry = CreateRegistry(TechnoChronicleAPI.MOD_ID);

    /**
     * Construct a new Registrate for the given mod ID.
     *
     * @param modId The mod ID for which objects will be registered
     */
    protected TCRegistrate(String modId) {
        super(modId);
    }

    public static TCRegistrate CreateRegistry(String modId) {
        return new TCRegistrate(modId);
    }
}
