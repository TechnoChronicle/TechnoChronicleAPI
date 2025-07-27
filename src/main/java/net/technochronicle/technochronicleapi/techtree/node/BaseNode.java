package net.technochronicle.technochronicleapi.techtree.node;

import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.resources.ResourceLocation;
import net.technochronicle.technochronicleapi.techtree.TeamTechTree;
import net.technochronicle.technochronicleapi.techtree.condition.ConditionType;
import net.technochronicle.technochronicleapi.techtree.condition.TreeNodeUnlockCondition;

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
    @Getter
    private final ResourceLocation location;
    @Getter
    @Setter
    private String title;
    @Getter
    @Setter
    private String description;
    @Getter
    private final Set<TreeNodeUnlockCondition> unlockConditions = new HashSet<>();

    /// 加载Node
    protected BaseNode(ResourceLocation location, UUID id, JsonObject json, Collection<? extends BaseNode> parents) {
        nodes.put(id, this);
        this.location = location;
        this.id = id;
        this.parents.addAll(parents);
        for (BaseNode parent : parents) {
            parent.children.add(this);
        }
        UpdateDepth();
        LoadData(json);
    }
    protected void LoadData(JsonObject json) {
        title = json.get("title").getAsString();
        description = json.get("description").getAsString();
        if (json.has("unlock_conditions")) {
            for (var cJson : json.get("unlock_conditions").getAsJsonArray()) {
                if (cJson instanceof JsonObject cJObj) {
                    var cTypeLocation = ResourceLocation.parse(cJObj.get("type").getAsString());
                    
                }
            }
        }
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
    protected abstract void NotifyDepthChange();
}