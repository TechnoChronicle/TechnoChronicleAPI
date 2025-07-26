package net.technochronicle.technochronicleapi.techtree;

import dev.ftb.mods.ftbteams.api.event.TeamCreatedEvent;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.nbt.CompoundTag;
import net.technochronicle.technochronicleapi.techtree.node.BaseNode;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/// 队伍技术树
public class TeamTechTree {
    /// 所有队伍科技树的缓存
    @Getter
    private static final Map<UUID, TeamTechTree> techTreeMap = new HashMap<>();

    /// 获得某个队伍的科技树
    public static Optional<TeamTechTree> getTree(UUID teamId) {
        return Optional.ofNullable(techTreeMap.get(teamId));
    }

    /// 创建某个队伍的科技树
    protected static TeamTechTree createTree(UUID teamId) {
        var tree = new TeamTechTree(teamId);
        var tag = new CompoundTag();
        tree.saveTree(tag);
        TreeHelper.SaveTeamTechTree(teamId,tag);
        return tree;
    }

    /// 加载某个队伍的科技树
    protected static TeamTechTree loadTree(UUID teamId) {
        var tree = new TeamTechTree(teamId);
        tree.loadTree(TreeHelper.LoadTeamTechTree(teamId));
        return tree;
    }

    /// 构造函数
    protected TeamTechTree(UUID teamId) {
        techTreeMap.put(teamId, this);
        this.teamId = teamId;
        BaseNode.getNodes().forEach((id, node) -> nodeStates.put(id, new NodeState(id)));
    }

    @Getter
    private final UUID teamId;
    @Getter
    private final Map<UUID, NodeState> nodeStates = new HashMap<>();

    public void saveTree(CompoundTag tag) {
        var treeTag = new CompoundTag();
        nodeStates.values().forEach(nodeState -> nodeState.save(treeTag));
        tag.put(teamId.toString(), treeTag);
    }

    public void loadTree(CompoundTag tag) {
        CompoundTag treeTag = (CompoundTag) tag.get(teamId.toString());
        if (treeTag == null) return;
        nodeStates.values().forEach(nodeState -> nodeState.load(treeTag));
    }

    /// 团队创建时创建技术树
    public static void onTeamCreate(TeamCreatedEvent event) {
        var team=event.getTeam();
        if(team.isPartyTeam()) {
            TeamTechTree.createTree(team.getTeamId());
        }
    }

    /// 初始化
    public static void init() {
        TeamCreatedEvent.CREATED.register(TeamTechTree::onTeamCreate);
    }

    /// 节点状态
    public static class NodeState {
        @Getter
        private final UUID nodeId;
        /// 节点是否解锁
        @Getter
        @Setter
        private boolean isUnlocked = false;

        public NodeState(UUID nodeId) {
            this.nodeId = nodeId;
        }

        public void save(CompoundTag tag) {
            var nodeTag = new CompoundTag();
            nodeTag.putBoolean("isUnlocked", isUnlocked);
            tag.put(nodeId.toString(), nodeTag);
        }

        public void load(CompoundTag tag) {
            CompoundTag nodeTag = (CompoundTag) tag.get(nodeId.toString());
            if (nodeTag == null) return;
            isUnlocked = nodeTag.getBoolean("isUnlocked");
        }
    }
}