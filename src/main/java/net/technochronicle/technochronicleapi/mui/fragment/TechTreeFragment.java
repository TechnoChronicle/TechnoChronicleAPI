package net.technochronicle.technochronicleapi.mui.fragment;

import dev.ftb.mods.ftbteams.data.TeamManagerImpl;
import icyllis.modernui.core.Context;
import icyllis.modernui.fragment.Fragment;
import icyllis.modernui.graphics.drawable.ImageDrawable;
import icyllis.modernui.util.DataSet;
import icyllis.modernui.view.LayoutInflater;
import icyllis.modernui.view.View;
import icyllis.modernui.view.ViewGroup;
import icyllis.modernui.widget.GridLayout;
import lombok.extern.log4j.Log4j2;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.technochronicle.technochronicleapi.helper.ImageHelper;
import net.technochronicle.technochronicleapi.techtree.TeamTechTree;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;


@Log4j2
@OnlyIn(Dist.CLIENT)
public class TechTreeFragment extends Fragment {
    private final UUID playerID;
    private UUID lastedTeamID;

    public TechTreeFragment(UUID playerID) {
        super();
        this.playerID = playerID;
        lastedTeamID = TeamManagerImpl.INSTANCE.getTeamForPlayerID(playerID).get().getTeamId();
    }

    public @Nullable TeamTechTree getLastedTree() {
        return TeamTechTree.getTree(lastedTeamID).orElse(null);
    }

    @Override
    public void onAttach(@NotNull Context context) {
        super.onAttach(context);
        log.info("onAttach");
    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container, DataSet savedInstanceState) {
        log.info("onCreateView");
        return InitCommonView(inflater, container, savedInstanceState);
    }

    protected View InitCommonView(@NotNull LayoutInflater inflater, ViewGroup container, DataSet savedInstanceState) {
        var grid = new GridLayout(getContext());
        grid.setBackground(new ImageDrawable(ImageHelper.getImage("techtree/background.png")));

        DisPlayTree(grid);
        
        return grid;
    }

    protected void DisPlayTree(View container) {
        var tree = getLastedTree();
    }
}