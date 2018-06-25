package com.clumob.interactor.datasource;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;

/**
 * Created by prashant.rathore on 24/06/18.
 */

public abstract class InteractorAdapter<Item, Ir extends Interactor<Item>> {

    private PublishSubject<AdapterUpdateEvent> updateEventPublisher = PublishSubject.create();
    private int itemCount = 0;
    private int maxCount = -1;
    private boolean limitCount;

    public abstract void onAttached();

    public abstract boolean hasStableIds();

    public final Observable<AdapterUpdateEvent> observeAdapterUpdates() {
        return updateEventPublisher;
    }

    public abstract long getItemId(int position);

    public abstract int getItemType(int position);

    public int getItemCount() {
        return itemCount;
    }

    public abstract InteractorItem<Item, Ir> getItem(int position);

    public abstract void onDetached();

    protected final void notifyItemsInserted(int startPosition, int itemsInserted) {
        final int oldItemCount = this.itemCount;
        this.itemCount = computeItemCountOnItemsInserted(startPosition, itemsInserted);
        final int diff = this.itemCount - oldItemCount;
        publishUpdateEvent(startPosition, AdapterUpdateEvent.Type.ITEMS_ADDED, diff);
    }

    private int computeItemCountOnItemsInserted(int startPosition, int itemCount) {
        if (limitCount)
            return itemCountIfLimitEnabled(startPosition + itemCount);
        else
            return this.itemCount + itemCount;
    }

    protected final void notifyItemsRemoved(int startPosition, int itemsRemoved) {
        final int oldItemCount = this.itemCount;
        this.itemCount = computeItemCountOnItemsRemoved(startPosition, itemsRemoved);
        final int diff = oldItemCount - this.itemCount;
        publishUpdateEvent(startPosition, AdapterUpdateEvent.Type.ITEMS_REMOVED, diff);
    }

    private int computeItemCountOnItemsRemoved(int startPosition, int itemCount) {
        if (limitCount)
            return itemCountIfLimitEnabled(startPosition - itemCount);
        else
            return this.itemCount - itemCount;
    }

    private int itemCountIfLimitEnabled(int newItemCount) {
        return Math.min(newItemCount, maxCount);
    }

    protected abstract int computeItemCount();

    private void publishUpdateEvent(int startPosition, AdapterUpdateEvent.Type type, int itemCount) {
        updateEventPublisher.onNext(AdapterUpdateEvent
                .builder()
                .setItemCount(itemCount)
                .setPosition(startPosition)
                .setType(type)
                .build());
    }


//    InteractorAdapter<Item,Ir> getRootAdapter(int position);

}
