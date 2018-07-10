package com.clumob.listitem.controller.source;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;

/**
 * Created by prashant.rathore on 24/06/18.
 */

public abstract class ItemControllerSource<Controller extends ItemController> {

    private PublishSubject<SourceUpdateEvent> updateEventPublisher = PublishSubject.create();
    private int itemCount = 0;
    private int maxCount = -1;
    private boolean hasMaxLimit;
    private ViewInteractor viewInteractor;

    public void setViewInteractor(ViewInteractor viewInteractor) {
        this.viewInteractor = viewInteractor;
    }

    protected ViewInteractor getViewInteractor() {
        return viewInteractor;
    }

    public abstract void onAttached();

    public abstract void onItemAttached(int position);

    public abstract boolean hasStableIds();

    public final Observable<SourceUpdateEvent> observeAdapterUpdates() {
        return updateEventPublisher;
    }

    final public long getItemId(int position) {
        return getItem(position).getId();
    }

    final public int getItemType(int position) {
        return getItem(position).getType();
    }

    public int getItemCount() {
        return itemCount;
    }

    public void setMaxLimit(int limit)  {
        if(limit < 0) {
            throw new IllegalArgumentException("Max Limit cannot be < 0");
        }
        this.hasMaxLimit = true;
        this.maxCount = limit;
        if(maxCount < this.itemCount) {
            notifyItemsRemoved(maxCount, this.itemCount - maxCount);
        }
    }

    public void removeMaxLimit() {
        this.hasMaxLimit = false;
        final int oldItemCount = this.itemCount;
        final int newItemCount = computeItemCount();
        if(oldItemCount < newItemCount) {
            final int diff = newItemCount - oldItemCount;
            notifyItemsInserted(oldItemCount,diff);
        }
    }

    public abstract int getItemPosition(Controller item);

    public abstract Controller getItem(int position);

    public abstract void onItemDetached(int position);

    public abstract void onDetached();

    protected final void notifyItemsInserted(int startPosition, int itemsInserted) {
        final int oldItemCount = this.itemCount;
        this.itemCount = computeItemCountOnItemsInserted(startPosition, itemsInserted);
        final int diff = this.itemCount - oldItemCount;
        publishUpdateEvent(startPosition, SourceUpdateEvent.Type.ITEMS_ADDED, diff);
    }

    private int computeItemCountOnItemsInserted(int startPosition, int itemCount) {
        if (hasMaxLimit)
            return itemCountIfLimitEnabled(startPosition + itemCount);
        else
            return this.itemCount + itemCount;
    }

    protected final void notifyItemsRemoved(int startPosition, int itemsRemoved) {
        final int oldItemCount = this.itemCount;
        this.itemCount = computeItemCountOnItemsRemoved(oldItemCount, itemsRemoved);
        final int diff = oldItemCount - this.itemCount;
        publishUpdateEvent(startPosition, SourceUpdateEvent.Type.ITEMS_REMOVED, diff);
    }

    private int computeItemCountOnItemsRemoved(int oldItemCount, int itemCount) {
        if (hasMaxLimit)
            return itemCountIfLimitEnabled(oldItemCount - itemCount);
        else
            return oldItemCount - itemCount;
    }

    private int itemCountIfLimitEnabled(int newItemCount) {
        return Math.min(newItemCount, maxCount);
    }

    protected abstract int computeItemCount();

    private void publishUpdateEvent(int startPosition, SourceUpdateEvent.Type type, int itemCount) {
        updateEventPublisher.onNext(SourceUpdateEvent
                .builder()
                .setItemCount(itemCount)
                .setPosition(startPosition)
                .setType(type)
                .build());
    }

    public void notifyItemsChanged(int startIndex, int itemCount) {
        if(this.itemCount > startIndex) {
            publishUpdateEvent(startIndex, SourceUpdateEvent.Type.ITEMS_CHANGED, Math.min(this.itemCount - startIndex, itemCount));
        }
    }

    public void endUpdates() {
        publishUpdateEvent(0, SourceUpdateEvent.Type.UPDATE_ENDS,0);
    }

    public void beginUpdates() {
        publishUpdateEvent(0, SourceUpdateEvent.Type.UPDATE_BEGINS,0);
    }

    protected void proessWhenSafe(Runnable runnable) {
        if(viewInteractor == null) {
            runnable.run();
        }
        else
            viewInteractor.processWhenSafe(runnable);
    }

    protected void cancelOldProcess(Runnable runnable) {
        if(viewInteractor != null) {
            viewInteractor.cancelOldProcess(runnable);
        }
    }

    public interface ViewInteractor {
        public void processWhenSafe(Runnable runnable);
        public void cancelOldProcess(Runnable runnable);
    }



//    ItemControllerSource<Item,Controller> getRootAdapter(int position);

}
