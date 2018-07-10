package com.clumob.recyclerview.section.presenter;


import com.clumob.listitem.controller.source.ItemController;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by prashant.rathore on 20/06/18.
 */

public class ArrayPresenterSection<IN extends ItemController> extends PresenterSection<IN> {

    private List<IN> items = new ArrayList<>();

    @Override
    public IN getItem(int position) {
        return items.get(position);
    }

    @Override
    public int getItemType(int position) {
        return getItem(position).getType();
    }

    @Override
    protected int computeItemCount() {
        return items.size();
    }
}
