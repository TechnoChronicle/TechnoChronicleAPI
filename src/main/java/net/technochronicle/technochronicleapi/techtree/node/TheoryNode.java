package net.technochronicle.technochronicleapi.techtree.node;

import net.technochronicle.technochronicleapi.config.ConfigHolder;
import net.technochronicle.technochronicleapi.techtree.TeamTechTree;

import java.util.UUID;

public non-sealed class TheoryNode extends BaseNode {
    public TheoryNode(UUID id, TheoryNode... parents) {
        super(id, parents);
    }

    public TheoryNode(TheoryNode... parent) {
        super(parent);
    }

    @Override
    public boolean isVisible(TeamTechTree teamTechTree) {
        if (!ConfigHolder.INSTANCE.techTree.enableHiddenTree) {
            return true;
        }
        var nodeState = teamTechTree.getNodeStates();
        for(var node : getParents()){
            if(nodeState.get(node.getId()).isUnlocked())
                return true;
        }
        return false;
    }

    @Override
    protected void UpdateDepth() {
        if(getParents().isEmpty()){
            depth = 0;
        }else{
            depth = Math.max(getParents().stream().mapToInt(BaseNode::getDepth).max().orElse(0), 0) + 1;
        }
        NotifyDepthChange();
    }

    @Override
    protected void NotifyDepthChange() {
        getChildren().forEach(BaseNode::UpdateDepth);
    }
}