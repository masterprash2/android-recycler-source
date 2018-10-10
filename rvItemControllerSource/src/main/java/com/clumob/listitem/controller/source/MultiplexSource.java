package com.clumob.listitem.controller.source;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.observers.DisposableObserver;

/**
 * Created by prashant.rathore on 24/06/18.
 */

public class MultiplexSource extends ItemControllerSource<ItemController> {

    private List<AdapterAsItem> adapters = new ArrayList<>();
    private boolean isAttached;

    @Override
    public void onAttached() {
        isAttached = true;
        for (AdapterAsItem item : adapters) {
            item.adapter.onAttached();
        }
    }

    @Override
    public void setViewInteractor(ViewInteractor viewInteractor) {
        super.setViewInteractor(viewInteractor);
        for(AdapterAsItem adapterAsItem : adapters) {
            adapterAsItem.adapter.setViewInteractor(viewInteractor);
        }
    }

    @Override
    public void onItemAttached(int position) {
        AdapterAsItem adapterAsItem = decodeAdapterItem(position);
        adapterAsItem.adapter.onItemAttached(position - adapterAsItem.startPosition);
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    public void addAdapter(final ItemControllerSource<? extends ItemController> adapter) {
        final AdapterAsItem item = new AdapterAsItem(adapter);
        adapter.setViewInteractor(getViewInteractor());
        processWhenSafe(new Runnable() {
            @Override
            public void run() {
                if (adapters.size() > 0) {
                    AdapterAsItem previousItem = adapters.get(adapters.size() - 1);
                    item.startPosition = previousItem.startPosition + previousItem.adapter.getItemCount();
                }
                adapters.add(item);
                if(isAttached) {
                    item.adapter.onAttached();
                }
                notifyItemsInserted(item.startPosition, item.adapter.getItemCount());
            }
        });
    }

    @Override
    protected int computeItemCount() {
        if (adapters.size() > 0) {
            AdapterAsItem item = adapters.get(adapters.size() - 1);
            return item.startPosition + item.adapter.getItemCount();
        }
        return 0;
    }

    @Override
    public ItemController getItemForPosition(int position) {
        AdapterAsItem item = decodeAdapterItem(position);
        return item.adapter.getItem(position - item.startPosition);
    }

//    @Override
//    public void onItemDetached(int position) {
//        AdapterAsItem adapterAsItem = decodeAdapterItem(position);
//        adapterAsItem.adapter.onItemDetached(position - adapterAsItem.startPosition);
//    }

    @Override
    public void onDetached() {
        for (AdapterAsItem item : adapters) {
            item.adapter.onDetached();
        }
        isAttached = false;
    }

    private AdapterAsItem decodeAdapterItem(int position) {
        AdapterAsItem previous = null;
        for (AdapterAsItem adapterAsItem : adapters) {
            if (adapterAsItem.startPosition > position) {
                return previous;
            } else {
                previous = adapterAsItem;
            }
        }
        return previous;
    }

    @Override
    public int getItemPosition(ItemController item) {
        int top = 0;
        int itemPosition = -1;
        for (AdapterAsItem adapterAsItem : adapters) {
            int foundPosition = adapterAsItem.adapter.getItemPosition(item);
            if(foundPosition >= 0) {
                itemPosition = top + foundPosition;
                break;
            }
        }
        return itemPosition;
    }

    public void removeAdapter(final int removeAdapterAtPosition) {
        processWhenSafe(new Runnable() {
            @Override
            public void run() {
                AdapterAsItem remove = adapters.remove(removeAdapterAtPosition);
                final int removePositionStart = remove.startPosition;
                int nextAdapterStartPosition = removePositionStart;
                for (int index = removeAdapterAtPosition; index < adapters.size(); index++) {
                    AdapterAsItem adapterAsItem = adapters.get(index);
                    adapterAsItem.startPosition = nextAdapterStartPosition;
                    nextAdapterStartPosition = adapterAsItem.startPosition + adapterAsItem.adapter.getItemCount();
                }
                notifyItemsRemoved(removePositionStart, remove.adapter.getItemCount());
                remove.adapter.setViewInteractor(null);
            }
        });

    }

    void updateIndexes(AdapterAsItem modifiedItem) {
        boolean continueUpdating = false;
        for(AdapterAsItem item : this.adapters) {
            if(continueUpdating) {
                item.startPosition = modifiedItem.startPosition + modifiedItem.adapter.getItemCount();
                modifiedItem = item;
            }
            else if(item == modifiedItem) {
                continueUpdating = true;
            }
        }
    }

    class AdapterAsItem {

        int startPosition = 0;

        final ItemControllerSource adapter;
        final DisposableObserver<SourceUpdateEvent> updateObserver = new DisposableObserver<SourceUpdateEvent>() {
            @Override
            public void onNext(SourceUpdateEvent sourceUpdateEvent) {
                transformUpdateEvent(sourceUpdateEvent);
            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
            }

            @Override
            public void onComplete() {

            }
        };

        void transformUpdateEvent(SourceUpdateEvent event) {
            final int actualStartPosition = startPosition + event.getPosition();
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
            updateIndexes(this);
        }


        AdapterAsItem(ItemControllerSource adapter) {
            this.adapter = adapter;
            this.adapter.observeAdapterUpdates().subscribe(updateObserver);
        }

    }
}
