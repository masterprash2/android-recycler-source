package com.clumob.recyclerview.adapter;

import com.clumob.recyclerview.section.interactor.Interactor;

/**
 * Created by prashant.rathore on 24/06/18.
 */

public class InteractorItem<Item,Ir extends Interactor<Item>> {

    private Ir interactor;
    private Item item;
    private int type;
    private long id;

    public InteractorItem(Ir interactor, Item item) {
        this.interactor = interactor;
        this.item = item;
    }


    public int getType() {
        return type;
    }

    public Ir getInteractor() {
        return interactor;
    }


    public Item getItem() {
        return item;
    }

    public long getId() {
        return id;
    }

    public void onDetached() {
        interactor.onDestroy(item);
    }

    public void onCreate() {
        interactor.onCreate(item);
    }
}
