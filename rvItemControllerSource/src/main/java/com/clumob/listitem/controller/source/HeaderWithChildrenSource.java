package com.clumob.listitem.controller.source;

import io.reactivex.observers.DisposableObserver;

/**
 * Created by prashant.rathore on 24/06/18.
 */

public class HeaderWithChildrenSource extends ItemControllerSource<ItemController> {

    private AdapterAsItem headerItemSource;
    private AdapterAsItem childrenItemSource;

    private boolean isAttached;

    @Override
    public void onAttached() {
        isAttached = true;
        if (headerItemSource != null)
            headerItemSource.adapter.onAttached();
        if (childrenItemSource != null) {
            childrenItemSource.adapter.onAttached();
        }
    }

    @Override
    public void setViewInteractor(ViewInteractor viewInteractor) {
        if (headerItemSource != null)
            headerItemSource.adapter.setViewInteractor(viewInteractor);
        if (childrenItemSource != null) {
            childrenItemSource.adapter.setViewInteractor(viewInteractor);
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

//    public void addAdapter(ItemControllerSource<? extends ItemController> adapter) {
//        AdapterAsItem item = new AdapterAsItem(adapter);
//        if (adapters.size() > 0) {
//            AdapterAsItem previousItem = adapters.get(adapters.size() - 1);
//            item.startPosition = previousItem.startPosition + previousItem.adapter.getItemCount();
//        }
//        adapters.add(item);
//        if(isAttached) {
//            item.adapter.onAttached();
//        }
//        notifyItemsInserted(item.startPosition, item.adapter.getItemCount());
//    }

    @Override
    protected int computeItemCount() {
        return getHeaderCount() + getChildrentCount();
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
        if (headerItemSource != null)
            headerItemSource.adapter.onDetached();
        if (childrenItemSource != null) {
            childrenItemSource.adapter.onDetached();
        }
        isAttached = false;
    }

    private AdapterAsItem decodeAdapterItem(int position) {
        if (childrenItemSource != null && childrenItemSource.startPosition < position) {
            return childrenItemSource;
        } else {
            return headerItemSource;
        }
    }

    @Override
    public int getItemPosition(ItemController item) {
        int top = 0;
        int itemPosition = getItemPositionHeader(item);
        if (itemPosition < 0) {
            itemPosition = getItemPositionChildren(item);
        }
        return itemPosition;
    }

    private int getItemPositionHeader(ItemController item) {
        if (headerItemSource == null) {
            return -1;
        } else {
            return headerItemSource.adapter.getItemPosition(item);
        }
    }

    public int getItemPositionChildren(ItemController item) {
        if (childrenItemSource == null) {
            return -1;
        } else {
            return childrenItemSource.adapter.getItemPosition(item) - childrenItemSource.startPosition;
        }
    }

    void updateIndexes(AdapterAsItem modifiedItem) {
        if (modifiedItem == headerItemSource && childrenItemSource != null) {
            childrenItemSource.startPosition = getHeaderCount();
        }
    }

    private int getChildrentCount() {
        return childrenItemSource != null ? childrenItemSource.adapter.getItemCount() : 0;
    }

    private int getHeaderCount() {
        return headerItemSource != null ? headerItemSource.adapter.getItemCount() : 0;
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
