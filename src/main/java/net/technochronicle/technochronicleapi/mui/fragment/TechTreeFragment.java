package net.technochronicle.technochronicleapi.mui.fragment;

import icyllis.modernui.core.Context;
import icyllis.modernui.fragment.Fragment;
import icyllis.modernui.graphics.drawable.ImageDrawable;
import icyllis.modernui.util.DataSet;
import icyllis.modernui.view.LayoutInflater;
import icyllis.modernui.view.View;
import icyllis.modernui.view.ViewGroup;
import icyllis.modernui.widget.GridLayout;
import lombok.extern.log4j.Log4j2;
import net.technochronicle.technochronicleapi.helper.ImageHelper;
import org.jetbrains.annotations.NotNull;


@Log4j2
public class TechTreeFragment extends Fragment {
    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container, DataSet savedInstanceState) {
        log.info("onCreateView");
        return InitCommonView(inflater, container, savedInstanceState);
    }

    protected View InitCommonView(@NotNull LayoutInflater inflater, ViewGroup container, DataSet savedInstanceState) {
        var grid = new GridLayout(getContext());
        grid.setBackground(new ImageDrawable(ImageHelper.getImage("/techtree/background.png")));
        
        return grid;
    }
}