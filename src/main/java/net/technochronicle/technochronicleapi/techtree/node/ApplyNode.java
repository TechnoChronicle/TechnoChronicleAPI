package net.technochronicle.technochronicleapi.techtree.node;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.technochronicle.technochronicleapi.config.ConfigHolder;
import net.technochronicle.technochronicleapi.techtree.TeamTechTree;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public non-sealed class ApplyNode extends BaseNode {
    public static ApplyNode create(ResourceLocation location, UUID id, JsonObject json, Collection<BaseNode> parents) {
        for (var parent : parents) {
            if (parent instanceof TheoryNode) {
                return new ApplyNode(location, id, json, parents);
            }
        }
        throw new IllegalArgumentException("ApplyNode must have a parent of type TheoryNode");
    }

    public static ApplyNode create(ResourceLocation location, UUID id, JsonObject json, BaseNode... parents) {
        for (var parent : parents) {
            if (parent instanceof TheoryNode) {
                return new ApplyNode(location, id, json, List.of(parents));
            }
        }
        throw new IllegalArgumentException("ApplyNode must have a parent of type TheoryNode");
    }

    protected ApplyNode(ResourceLocation location, UUID id, JsonObject json, Collection<BaseNode> parents) {
        super(location, id, json, parents);
    }

    @Override
    public boolean isVisible(TeamTechTree teamTechTree) {
        if (!ConfigHolder.INSTANCE.techTree.enableHiddenTree) {
            return true;
        }
        var nodeStates = teamTechTree.getNodeStates();
        for (var node : getParents()) {
            if (!nodeStates.get(node.getId()).isUnlocked())
                return false;
        }
        return true;
    }

    @Override
    protected void UpdateDepth() {
        depth = Math.max(getParents().stream().filter(p -> p instanceof ApplyNode).mapToInt(BaseNode::getDepth).max().orElse(0), 0);
        NotifyDepthChange();
    }

    @Override
    protected void NotifyDepthChange() {
        getChildren().stream().filter(c -> c instanceof ApplyNode).forEach(BaseNode::NotifyDepthChange);
    }
}