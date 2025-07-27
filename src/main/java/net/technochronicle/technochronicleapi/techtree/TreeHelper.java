package net.technochronicle.technochronicleapi.techtree;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;
import net.technochronicle.technochronicleapi.TechnoChronicleAPI;
import net.technochronicle.technochronicleapi.helper.StorageFileHelper;
import net.technochronicle.technochronicleapi.techtree.node.BaseNode;

import java.io.File;
import java.util.Arrays;
import java.util.UUID;

public class TreeHelper {
    public static final String TreeFolder = "techTree/teams";

    private static String getTreeFolder() {
        var root = StorageFileHelper.getWorldDirectory(TechnoChronicleAPI.getMinecraftServer());
        return root.resolve(TreeFolder).toString();
    }

    public static void SaveTeamTechTree(UUID teamId, CompoundTag tag) {
        StorageFileHelper.saveDataToFolder(TechnoChronicleAPI.getMinecraftServer(), getTreeFolder(), teamId.toString(), tag, 1);
    }

    public static CompoundTag LoadTeamTechTree(UUID teamId) {
        return StorageFileHelper.loadDataFromFolder(TechnoChronicleAPI.getMinecraftServer(), getTreeFolder(), teamId.toString());
    }

    private static void onServerStart(ServerStartedEvent event) {
        var folder = new File(getTreeFolder());
        var files = folder.listFiles();
        if (files != null) {
            Arrays.stream(files).filter(File::isFile)
                    .map(file -> UUID.fromString(file.getName().substring(0, file.getName().lastIndexOf('.')))).
                    forEach(TeamTechTree::loadTree);
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
        NeoForge.EVENT_BUS.addListener(GlobalTechTree::onAddReloadListeners);
    }

    public static class GlobalTechTree {
        public static void onAddReloadListeners(AddReloadListenerEvent event) {
            event.addListener(new TechTreeDataLoader());
        }
    }
}