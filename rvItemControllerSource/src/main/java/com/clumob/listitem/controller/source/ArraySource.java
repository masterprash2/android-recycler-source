package com.clumob.listitem.controller.source;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by prashant.rathore on 24/06/18.
 */

public class ArraySource<Controller extends ItemController> extends ItemControllerSource<Controller> {

    private List<Controller> controller = new ArrayList<>();

    public ArraySource() {
    }

    @Override
    public void onAttached() {
        for (Controller item : controller) {
            item.onCreate();
        }
    }

    @Override
    public void onItemAttached(int position) {

    }

    public List<Controller> getItems() {
        return controller;
    }

    public void setItems(List<Controller> items) {
        this.controller = controller;
    }

    public void switchItems(final List<Controller> newItems) {
        this.controller = newItems;
        int diff = newItems.size() - this.controller.size();
        beginUpdates();
        if (diff > 0) {
            notifyItemsInserted(this.controller.size(), diff);
        } else {
            notifyItemsRemoved(newItems.size(), diff * (-1));
        }
        endUpdates();
    }

    public void replaceItem(int index, Controller item) {
        this.controller.set(index, item);
        item.onCreate();
        notifyItemsChanged(index, 1);
    }

    @Override
    public int getItemPosition(Controller item) {
        return this.controller.indexOf(item);
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    protected int computeItemCount() {
        return controller.size();
    }

    @Override
    public Controller getItemForPosition(int position) {
        return controller.get(position);
    }

//    @Override
//    public void onItemDetached(int position) {
//
//    }

    @Override
    public void onDetached() {
        for (Controller item : controller) {
            item.onDetach();
        }
    }
}
