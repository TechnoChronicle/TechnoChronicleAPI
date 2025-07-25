package net.technochronicle.technochronicleapi.machine.features;

import net.technochronicle.technochronicleapi.machine.MetaMachine;

/// 机器额外能力接口
public interface IMachineFeature {

    default MetaMachine self() {
        return (MetaMachine) this;
    }
}