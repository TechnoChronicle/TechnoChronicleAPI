package net.technochronicle.technochronicleapi.techtree;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.extern.log4j.Log4j2;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.technochronicle.technochronicleapi.techtree.node.ApplyNode;
import net.technochronicle.technochronicleapi.techtree.node.BaseNode;
import net.technochronicle.technochronicleapi.techtree.node.TheoryNode;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@Log4j2
public class TechTreeDataLoader extends SimpleJsonResourceReloadListener {
    private static final String directory = "tech_tree";

    public TechTreeDataLoader() {
        super(new GsonBuilder().create(), directory);
    }

    @Override
    protected void apply(@NotNull Map<ResourceLocation, JsonElement> resourceLocationJsonElementMap,
                         @NotNull ResourceManager resourceManager,
                         @NotNull ProfilerFiller profilerFiller) {
        Map<ResourceLocation, JsonObject> data = new HashMap<>();
        for (var item : resourceLocationJsonElementMap.entrySet()) {
            ResourceLocation id = item.getKey();
            JsonElement json = item.getValue();
            if (json instanceof JsonObject jObj) {
                data.put(id, jObj);
            }
        }
        resolveDependencies(data);
    }

    private void resolveDependencies(Map<ResourceLocation, JsonObject> data) {
        // 实现数据间的依赖解析
        final Map<UUID, TJNode> tmpNodes = new HashMap<>();
        data.forEach((id, json) -> {
            var uuid = UUID.fromString(json.get("uuid").getAsString());
            tmpNodes.put(uuid, new TJNode(uuid, id, json));
        });
        tmpNodes.values().forEach(node -> StepLoadNode(tmpNodes, node));
    }

    private void StepLoadNode(final Map<UUID, TJNode> mapping, TJNode node) {
        if (BaseNode.getNodes().containsKey(node.uuid))
            return;
        var parents = node.getParents();
        for (var parent : parents) {
            if (!BaseNode.getNodes().containsKey(parent)) {
                StepLoadNode(mapping, mapping.get(parent));
            }
        }
        if (node.isTheory()) {
            new TheoryNode(node.resourceLocation, node.uuid, node.json,
                    parents.stream().map(uuid -> (TheoryNode) BaseNode.getNodes().get(uuid)).toList());
        } else if (node.isApply()) {
            ApplyNode.create(node.resourceLocation, node.uuid, node.json,
                    parents.stream().map(BaseNode.getNodes()::get).toList());
        } else {
            log.error("Unknown node type: " + node.json.get("type").getAsString());
        }
    }

    private record TJNode(UUID uuid, ResourceLocation resourceLocation, JsonObject json) {
        public Collection<UUID> getParents() {
            var parents = new HashSet<UUID>();
            for (var parent : json.get("parents").getAsJsonArray()) {
                parents.add(UUID.fromString(parent.getAsString()));
            }
            return parents;
        }

        public boolean isTheory() {
            return Objects.equals(json.get("type").getAsString(), "TheoryNode");
        }

        public boolean isApply() {
            return Objects.equals(json.get("type").getAsString(), "ApplyNode");
        }
    }
}