package com.clumob.listitem.controller.source;

import io.reactivex.observers.DisposableObserver;

/**
 * Created by prashant.rathore on 24/06/18.
 */

public class WrapperSource extends ItemControllerSource<ItemController> {

    private ItemControllerSource<ItemController> source = new EmptyItemSource();
    private boolean isAttached;
    private DisposableObserver<SourceUpdateEvent> updateObserver;


    @Override
    public void onAttached() {
        isAttached = true;
        observeSourceUpdates();
        source.onAttached();
    }

    @Override
    public void setViewInteractor(ViewInteractor viewInteractor) {
        super.setViewInteractor(viewInteractor);
        source.setViewInteractor(viewInteractor);
    }

    @Override
    public void onItemAttached(int position) {
        this.source.onItemAttached(position);
    }

    @Override
    public boolean hasStableIds() {
        return this.source.hasStableIds();
    }

    public void setSource(ItemControllerSource<ItemController> inSource) {
        if(inSource == null) {
            inSource = new EmptyItemSource();
        }
        final ItemControllerSource<ItemController> newSource = inSource;
        newSource.setViewInteractor(getViewInteractor());
        processWhenSafe(new Runnable() {
            @Override
            public void run() {
                final ItemControllerSource<ItemController> oldSource = WrapperSource.this.source;
                final int oldCount = oldSource.getItemCount();
                final int newCount = newSource.getItemCount();
                source = newSource;
                if (isAttached) {
                    oldSource.onDetached();
                    observeSourceUpdates();
                    newSource.onAttached();
                }

                oldSource.setViewInteractor(null);

                if (oldCount == newCount) {
                    if (oldCount != 0)
                        notifyItemsChanged(0, newCount);
                } else if (oldCount > newCount) {
                    int diff = oldCount - newCount;
                    notifyItemsChanged(0, diff);
                    notifyItemsRemoved(newCount, diff);
                } else {
                    int diff = newCount - oldCount;
                    notifyItemsChanged(0, diff);
                    notifyItemsInserted(oldCount, diff);
                }
            }
        });
    }

    @Override
    protected int computeItemCount() {
        return source.computeItemCount();
    }

    @Override
    public ItemController getItemForPosition(int position) {
        return source.getItem(position);
    }

//    @Override
//    public void onItemDetached(int position) {
//        AdapterAsItem adapterAsItem = decodeAdapterItem(position);
//        adapterAsItem.adapter.onItemDetached(position - adapterAsItem.startPosition);
//    }

    @Override
    public void onDetached() {
        source.onDetached();
        isAttached = false;
        if (updateObserver != null) {
            updateObserver.dispose();
            updateObserver = null;
        }
    }

    @Override
    public int getItemPosition(ItemController item) {
        return source.getItemPosition(item);
    }

    private void observeSourceUpdates() {
        if (updateObserver != null) {
            updateObserver.dispose();
            updateObserver = null;
        }
        updateObserver = new DisposableObserver<SourceUpdateEvent>() {
            @Override
            public void onNext(SourceUpdateEvent event) {
                final int actualStartPosition = event.getPosition();
                switch (event.getType()) {
                    case UPDATE_BEGINS:
                        beginUpdates();
                        break;
                    case ITEMS_CHANGED:
                        notifyItemsChanged(actualStartPosition, event.getItemCount());
                        break;
                    case ITEMS_REMOVED:
                        notifyItemsRemoved(actualStartPosition, event.getItemCount());
                        break;
                    case ITEMS_ADDED:
                        notifyItemsInserted(actualStartPosition, event.getItemCount());
                        break;
                    case ITEMS_MOVED:
                        break;
                    case UPDATE_ENDS:
                        endUpdates();
                        break;
                    case HAS_STABLE_IDS:
                        break;
                }
            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
            }

            @Override
            public void onComplete() {

            }
        };
        this.source.observeAdapterUpdates().subscribe(updateObserver);
    }

}
