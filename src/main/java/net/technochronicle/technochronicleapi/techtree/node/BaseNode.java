package net.technochronicle.technochronicleapi.techtree.node;

import lombok.Getter;
import net.technochronicle.technochronicleapi.techtree.TeamTechTree;

import java.util.*;

public sealed abstract class BaseNode permits ApplyNode, TheoryNode {
    /// 全局Node缓存
    @Getter
    private static final Map<UUID, BaseNode> nodes = new HashMap<>();
    /// 子节点映射
    @Getter
    private final Set<BaseNode> children = new HashSet<>();
    /// 父节点映射
    @Getter
    private final Set<BaseNode> parents = new HashSet<>();
    /// Node ID
    @Getter
    private final UUID id;

    /// 加载Node
    protected BaseNode(UUID id, BaseNode... parents) {
        nodes.put(id, this);
        this.id = id;
        this.parents.addAll(List.of(parents));
        for (BaseNode parent : parents) {
            parent.children.add(this);
        }
        UpdateDepth();
    }

    /// 创建Node
    public BaseNode(BaseNode... parent) {
        this(UUID.randomUUID(), parent);
    }

    /// 是否在某个队伍的科技树中可见
    public abstract boolean isVisible(TeamTechTree teamTechTree);

    /// 节点深度
    /// -1表示未计算
    /// 0表示根节点
    @Getter
    protected int depth = -1;
    
    /// 更新节点深度
    protected abstract void UpdateDepth();

    /// 通知修改节点深度
    protected abstract void NotifyDepthChange() ;
}