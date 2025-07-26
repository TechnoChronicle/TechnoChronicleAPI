package net.technochronicle.technochronicleapi.techtree.node;

import net.technochronicle.technochronicleapi.config.ConfigHolder;
import net.technochronicle.technochronicleapi.techtree.TeamTechTree;

import java.util.UUID;

public non-sealed class ApplyNode extends BaseNode {
    public static ApplyNode create(UUID id, BaseNode... parents) {
        for (var parent : parents) {
            if (parent instanceof TheoryNode) {
                return new ApplyNode(id, parents);
            }
        }
        throw new IllegalArgumentException("ApplyNode must have a parent of type TheoryNode");
    }

    public static ApplyNode create(BaseNode... parents) {
        for (var parent : parents) {
            if (parent instanceof TheoryNode) {
                return new ApplyNode(parents);
            }
        }
        throw new IllegalArgumentException("ApplyNode must have a parent of type TheoryNode");
    }

    protected ApplyNode(UUID id, BaseNode... parents) {
        super(id, parents);
    }

    protected ApplyNode(BaseNode... parent) {
        super(parent);
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