package com.clumob.interactor.datasource;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.observers.DisposableObserver;

/**
 * Created by prashant.rathore on 24/06/18.
 */

public class MultiplexSource extends InteractorSource {

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

    public void addAdapter(InteractorSource<?, ?> adapter) {
        AdapterAsItem item = new AdapterAsItem(adapter);
        if (adapters.size() > 0) {
            AdapterAsItem previousItem = adapters.get(adapters.size() - 1);
            item.startPosition = previousItem.startPosition + previousItem.adapter.getItemCount();
        }
        adapters.add(item);
        notifyItemsInserted(item.startPosition, item.adapter.getItemCount());
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

    public InteractorSource removeAdapter(final int removeAdapterAtPosition) {
        AdapterAsItem remove = adapters.remove(removeAdapterAtPosition);
        final int removePositionStart = remove.startPosition;
        int nextAdapterStartPosition = removePositionStart;
        for (int index = removeAdapterAtPosition; index < adapters.size(); index++) {
            AdapterAsItem adapterAsItem = adapters.get(index);
            adapterAsItem.startPosition = nextAdapterStartPosition;
            nextAdapterStartPosition = adapterAsItem.startPosition + adapterAsItem.adapter.getItemCount();
        }
        notifyItemsRemoved(removePositionStart, remove.adapter.getItemCount());
        return remove.adapter;
    }

    class AdapterAsItem {

        int startPosition = 0;

        final InteractorSource adapter;
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
                case HAS_STABLE_IDS:
                    break;
            }
        }


        AdapterAsItem(InteractorSource adapter) {
            this.adapter = adapter;
            this.adapter.observeAdapterUpdates().subscribe(updateObserver);
        }

    }
}
