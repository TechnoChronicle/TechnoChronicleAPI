package net.technochronicle.technochronicleapi.helper;

import lombok.extern.log4j.Log4j2;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@Log4j2
public class StorageFileHelper {
    public static Path getWorldDirectory(MinecraftServer server) {
        return server.getWorldPath(LevelResource.ROOT).toAbsolutePath();
    }

    public static Path getOrCreateFolder(MinecraftServer server, String folderName) {
        Path worldDir = getWorldDirectory(server);
        Path folder = worldDir.resolve(folderName);
        try {
            if (!Files.exists(folder)) {
                Files.createDirectories(folder);
            }
        } catch (IOException io) {
            log.error("Failed to create folder " + folder, io);
        }
        return folder;
    }

    public static void saveDataToFolder(
            MinecraftServer server,
            String folderName,
            String fileName,
            CompoundTag data,
            int version
    ) {
        data.putInt("version", version);

        Path folder = getOrCreateFolder(server, folderName);
        Path filePath = folder.resolve(fileName + ".dat");
        Path tempPath = folder.resolve(fileName + ".tmp");
        try {
            // 1. 写入临时文件
            NbtIo.write(data, tempPath);

            // 2. 原子操作替换原文件
            Files.move(
                    tempPath,
                    filePath,
                    StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE
            );
        } catch (IOException e) {
            log.error("Failed to save data to {}", filePath, e);
        } finally {
            // 3. 清理临时文件
            try {
                Files.deleteIfExists(tempPath);
            } catch (IOException ignored) {
            }
        }
    }

    public static CompoundTag loadDataFromFolder(
            MinecraftServer server,
            String folderName,
            String fileName
    ) {
        Path folder = getOrCreateFolder(server, folderName);
        Path filePath = folder.resolve(fileName + ".dat");

        if (!Files.exists(filePath)) {
            return new CompoundTag();
        }

        try {
            return NbtIo.read(filePath);
        } catch (IOException e) {
            log.error("Failed to load data from {}", filePath, e);
            return new CompoundTag();
        }
    }
    public static void migrateDataFolder(
            MinecraftServer server,
            String oldFolder,
            String newFolder
    ) {
        Path worldDir = getWorldDirectory(server);
        Path oldPath = worldDir.resolve(oldFolder);
        Path newPath = worldDir.resolve(newFolder);

        if (Files.exists(oldPath) && !Files.exists(newPath)) {
            try {
                Files.move(oldPath, newPath, StandardCopyOption.REPLACE_EXISTING);
                log.info("Migrated data from " + oldFolder + " to " + newFolder);
            } catch (IOException e) {
                log.error("Failed to migrate data folder", e);
            }
        }
    }
}