package net.technochronicle.technochronicleapi.techtree;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.technochronicle.technochronicleapi.helper.StorageFileHelper;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.UUID;

public class TreeHelper {
    public static final String TreeFolder = "techTree/teams";
    private static MinecraftServer server;

    private static String getTreeFolder() {
        var root = StorageFileHelper.getWorldDirectory(server);
        return root.resolve(TreeFolder).toString();
    }

    public static void SaveTeamTechTree(UUID teamId, CompoundTag tag) {
        StorageFileHelper.saveDataToFolder(server, getTreeFolder(), teamId.toString(), tag, 1);
    }

    public static CompoundTag LoadTeamTechTree(UUID teamId) {
        return StorageFileHelper.loadDataFromFolder(server, getTreeFolder(), teamId.toString());
    }

    private static void onServerStart(ServerStartedEvent event) {
        server = event.getServer();
        var folder = new File(getTreeFolder());
        var files = folder.listFiles();
        if(files!= null){
            Arrays.stream(files).filter(File::isFile)
                    .map(file->UUID.fromString(file.getName().substring(0,file.getName().lastIndexOf('.')))).
                    forEach(teamID-> TeamTechTree.loadTree(teamID));
        }
    }

    private static void onServerStop(ServerStoppingEvent event) {
        TeamTechTree.getTechTreeMap().values().forEach(tree -> {
            var tag = new CompoundTag();
            tree.saveTree(tag);
            SaveTeamTechTree(tree.getTeamId(), tag);
        });
    }

    public static void init() {
        NeoForge.EVENT_BUS.addListener(TreeHelper::onServerStart);
        NeoForge.EVENT_BUS.addListener(TreeHelper::onServerStop);
    }
}