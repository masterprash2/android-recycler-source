package com.clumob.interactor.datasource;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by prashant.rathore on 24/06/18.
 */

public class ArrayInteractorAdapter<Item, Ir extends Interactor<Item>> extends InteractorAdapter<Item, Ir> {

    private List<InteractorItem<Item, Ir>> interactorItems = new ArrayList<>();

    @Override
    public void onAttached() {
        for (InteractorItem<Item, Ir> item : interactorItems) {
            item.onCreate();
        }
    }

    public void switchItems(List<InteractorItem<Item, Ir>> newItems) {
        int diff = newItems.size() - this.interactorItems.size();
        if(diff > 0) {
            notifyItemsInserted(this.interactorItems.size(),diff);
        }
        else {
            notifyItemsRemoved(newItems.size(),diff * (-1));
        }
        this.interactorItems = newItems;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public long getItemId(int position) {
        return interactorItems.get(position).getId();
    }

    @Override
    public int getItemType(int position) {
        return interactorItems.get(position).getType();
    }

    @Override
    protected int computeItemCount() {
        return interactorItems.size();
    }

    @Override
    public InteractorItem<Item, Ir> getItem(int position) {
        return interactorItems.get(position);
    }

    @Override
    public void onDetached() {
        for (InteractorItem<Item, Ir> item : interactorItems) {
            item.onDetached();
        }
    }
}
