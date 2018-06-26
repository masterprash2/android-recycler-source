package com.clumob.interactor.datasource;

/**
 * Created by prashant.rathore on 24/06/18.
 */

public class InteractorItem<Item,Ir extends Interactor<Item>> {

    private Ir interactor;
    private Item item;
    private int type;
    private long id;
    private Item up;

    public InteractorItem(Ir interactor, Item item) {
        this.interactor = interactor;
        this.item = item;
    }

    public InteractorItem(Ir interactor) {
        this.interactor = interactor;
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

    public InteractorItem<Item,Ir> mutate(Item item) {
        this.item = item;
        return this;
    }
}
