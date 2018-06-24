package com.clumob.recyclerview.adapter;

import com.clumob.recyclerview.section.AdapterUpdateEvent;
import com.clumob.recyclerview.section.interactor.Interactor;

import io.reactivex.Observable;

/**
 * Created by prashant.rathore on 24/06/18.
 */

public interface InteractorAdapter<Item,Ir extends Interactor<Item>> {

    void onAttached();

    boolean hasStableIds();

    Observable<AdapterUpdateEvent> observeAdapterUpdates();

    long getItemId(int position);

    int getItemType(int position);

    int getItemCount();

    InteractorItem<Item,Ir> getItem(int position);

    void onDetached();

}
