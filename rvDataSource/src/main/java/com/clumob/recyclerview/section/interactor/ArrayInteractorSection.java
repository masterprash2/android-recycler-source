package com.clumob.recyclerview.section.interactor;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by prashant.rathore on 20/06/18.
 */

public class ArrayInteractorSection<VM, IN extends Interactor> extends InteractorSection<VM,IN> {

    private List<InteractorSectionItem<VM,IN>> items = new ArrayList<>();

    @Override
    public InteractorSectionItem<VM, IN> getItem(int position) {
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
