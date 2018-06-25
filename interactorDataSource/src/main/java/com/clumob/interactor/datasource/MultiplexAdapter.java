package com.clumob.interactor.datasource;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.observers.DisposableObserver;

/**
 * Created by prashant.rathore on 24/06/18.
 */

public class MultiplexAdapter extends InteractorAdapter {

    private List<AdapterAsItem> adapters = new ArrayList<>();

    @Override
    public void onAttached() {
        for (AdapterAsItem item : adapters) {
            item.adapter.onAttached();
        }
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    public void addAdapter(InteractorAdapter<?, ?> adapter) {
        AdapterAsItem item = new AdapterAsItem(adapter);
        if (adapters.size() > 0) {
            AdapterAsItem previousItem = adapters.get(adapters.size() - 1);
            item.startPosition = previousItem.startPosition + previousItem.adapter.getItemCount();
        }
        adapters.add(item);
        notifyItemsInserted(item.startPosition, item.adapter.getItemCount());
    }

    @Override
    public long getItemId(int position) {
        AdapterAsItem item = decodeAdapterItem(position);
        return item.adapter.getItemId(position - item.startPosition);
    }

    @Override
    public int getItemType(int position) {
        AdapterAsItem item = decodeAdapterItem(position);
        return item.adapter.getItemType(position - item.startPosition);
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
    public InteractorItem getItem(int position) {
        AdapterAsItem item = decodeAdapterItem(position);
        return item.adapter.getItem(position - item.startPosition);
    }

    @Override
    public void onDetached() {
        for (AdapterAsItem item : adapters) {
            item.adapter.onDetached();
        }
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

    public InteractorAdapter removeAdapter(final int removeAdapterAtPosition) {
        AdapterAsItem remove = adapters.remove(removeAdapterAtPosition);
        final int removePositionStart = remove.startPosition;
        int nextAdapterStartPosition = removePositionStart;
        for(int index = removeAdapterAtPosition ; index < adapters.size() ; index++) {
            AdapterAsItem adapterAsItem = adapters.get(index);
            adapterAsItem.startPosition = nextAdapterStartPosition;
            nextAdapterStartPosition = adapterAsItem.startPosition + adapterAsItem.adapter.getItemCount();
        }
        notifyItemsRemoved(removePositionStart,remove.adapter.getItemCount());
        return remove.adapter;
    }

    class AdapterAsItem {

        int startPosition = 0;

        final InteractorAdapter adapter;
        final DisposableObserver<AdapterUpdateEvent> updateObserver = new DisposableObserver<AdapterUpdateEvent>() {
            @Override
            public void onNext(AdapterUpdateEvent adapterUpdateEvent) {
                transformUpdateEvent(adapterUpdateEvent);
            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
            }

            @Override
            public void onComplete() {

            }
        };

        void transformUpdateEvent(AdapterUpdateEvent event) {
            final int actualStartPosition = startPosition + event.getPosition();
            switch (event.getType()) {
                case ITEMS_CHANGED:
                    break;
                case ITEMS_REMOVED:
                    break;
                case ITEMS_ADDED:
                    notifyItemsInserted(actualStartPosition, event.getItemCount());
                    break;
                case ITEMS_MOVED:
                    break;
                case HAS_STABLE_IDS:
                    break;
            }
        }


        AdapterAsItem(InteractorAdapter adapter) {
            this.adapter = adapter;
            this.adapter.observeAdapterUpdates().subscribe(updateObserver);
        }

    }
}
