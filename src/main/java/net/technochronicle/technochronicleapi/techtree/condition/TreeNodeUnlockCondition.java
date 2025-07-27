package net.technochronicle.technochronicleapi.techtree.condition;

import lombok.Getter;

public abstract class TreeNodeUnlockCondition {
    @Getter
    private final ConditionType<? extends TreeNodeUnlockCondition> type;

    protected TreeNodeUnlockCondition(ConditionType<? extends TreeNodeUnlockCondition> type) {
        this.type = type;
    }
}
