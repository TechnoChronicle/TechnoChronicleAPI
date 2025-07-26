package net.technochronicle.technochronicleapi.helper;

import dev.ftb.mods.ftbteams.api.Team;
import dev.ftb.mods.ftbteams.data.TeamManagerImpl;
import icyllis.modernui.mc.neoforge.MuiForgeApi;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.technochronicle.technochronicleapi.mui.fragment.TechTreeFragment;

import java.util.UUID;


public class TechTreeHelper {
    public static boolean shouldEnableTechTree(UUID playerID) {
        var oTeam= TeamManagerImpl.INSTANCE.getTeamForPlayerID(playerID);
        return oTeam.map(Team::isPartyTeam).orElse(false);
    }

    public static void TryOpenTechTree(ClientTickEvent.Post event) {
        var player = Minecraft.getInstance().player;
        if (shouldEnableTechTree(player.getUUID())) {
            MuiForgeApi.openScreen(new TechTreeFragment(player.getUUID()));
        } else {
            player.displayClientMessage(Component.translatable("msg.techtree.not_available"), false);
        }
    }
}