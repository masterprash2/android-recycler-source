package com.clumob.recyclerview.adapter;

import com.clumob.recyclerview.section.AdapterUpdateEvent;
import com.clumob.recyclerview.section.interactor.Interactor;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;

/**
 * Created by prashant.rathore on 24/06/18.
 */

public class ArrayInteractorAdapter<Item,Ir extends Interactor<Item>> implements InteractorAdapter<Item,Ir> {

    private List<InteractorItem<Item,Ir>> interactorItems = new ArrayList<>();

    @Override
    public void onAttached() {
        for(InteractorItem<Item,Ir> item : interactorItems) {
            item.onCreate();
        }
    }

    public void switchItems(List<InteractorItem<Item,Ir>> newItems) {
        this.interactorItems = newItems;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public Observable<AdapterUpdateEvent> observeAdapterUpdates() {
        return Observable.never();
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
    public int getItemCount() {
        return interactorItems.size();
    }

    @Override
    public InteractorItem<Item,Ir> getItem(int position) {
        return interactorItems.get(position);
    }

    @Override
    public void onDetached() {
        for(InteractorItem<Item, Ir> item : interactorItems) {
            item.onDetached();
        }
    }
}
