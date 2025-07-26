package net.technochronicle.technochronicleapi.storage.techtree;


import dev.ftb.mods.ftbteams.FTBTeams;
import dev.ftb.mods.ftbteams.api.FTBTeamsAPI;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.PlayerDataStorage;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

public class TechTreeStorage extends SavedData {
    private static final String FolderPath = "technochronicle/techtree/";

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag compoundTag, HolderLookup.@NotNull Provider provider) {
        return compoundTag;
    }
}