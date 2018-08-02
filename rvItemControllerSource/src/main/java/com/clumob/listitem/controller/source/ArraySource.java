package com.clumob.listitem.controller.source;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by prashant.rathore on 24/06/18.
 */

public class ArraySource<P extends ItemController> extends ItemControllerSource<P> {

    private List<P> presenters = new ArrayList<>();

    @Override
    public void onAttached() {
        for (P item : presenters) {
            item.onCreate();
        }
    }

    @Override
    public void onItemAttached(int position) {

    }

    public void switchItems(List<P> newItems) {
        int diff = newItems.size() - this.presenters.size();
        beginUpdates();
        if (diff > 0) {
            notifyItemsInserted(this.presenters.size(), diff);
        } else {
            notifyItemsRemoved(newItems.size(), diff * (-1));
        }
        endUpdates();
        this.presenters = newItems;
    }


    public void replaceItem(int index, P item) {
        this.presenters.set(index, item);
        item.onCreate();
        notifyItemsChanged(index, 1);
    }

    @Override
    public int getItemPosition(P item) {
        return this.presenters.indexOf(item);
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    protected int computeItemCount() {
        return presenters.size();
    }

    @Override
    public P getItemForPosition(int position) {
        return presenters.get(position);
    }

//    @Override
//    public void onItemDetached(int position) {
//
//    }

    @Override
    public void onDetached() {
        for (P item : presenters) {
            item.onDetach();
        }
    }
}
