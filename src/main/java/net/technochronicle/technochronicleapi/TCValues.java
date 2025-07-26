package net.technochronicle.technochronicleapi;

import net.minecraft.util.RandomSource;

import java.util.UUID;

public class TCValues {
    public static final String 
            MODID_JEI = "jei",
            MODID_REI = "roughlyenoughitems",
            MODID_EMI = "emi",
            MODID_FTB_TEAMS = "ftbteams";
    public static final RandomSource RNG = RandomSource.createThreadSafe();

    public static long CLIENT_TIME = 0;
    public static UUID EMPTY_UUID = UUID.fromString("00000000-0000-000000000000");
}
