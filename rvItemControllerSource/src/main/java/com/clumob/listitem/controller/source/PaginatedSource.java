package com.clumob.listitem.controller.source;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.observers.DisposableObserver;

/**
 * Created by prashant.rathore on 03/07/18.
 */

public class PaginatedSource<T extends ItemController> extends ItemControllerSource<T> {

    private final T loadingItemItemController;
    private List<PaginatedSourceItem> sources = new LinkedList<>();
    private final PagenatedCallbacks callbacks;
    private boolean hasMoreBottomPage = false;
    private boolean hasMoreTopPage = false;
    private boolean isAttached;

    final private int threshHold;
    final private int safeLimit = 20;

    private int lastItemAttached = -1;
    private int cachedLastItemAttached = -1;

    private Runnable trimPagesRunnable;
    private ItemUpdatePublisher itemUpdatePublisher = new ItemUpdatePublisher();

    public PaginatedSource(T loadingItemItemController, int preloadTriggerSize, PagenatedCallbacks callbacks) {
        this.loadingItemItemController = loadingItemItemController;
        this.threshHold = preloadTriggerSize;
        this.callbacks = callbacks;
        int itemCount = computeItemCount();
        notifyItemsInserted(0, itemCount);
        bloatPagesOnContentChange();

    }

    private void bloatPagesOnContentChange() {
        Disposable subscribe = observeAdapterUpdates().subscribe(new Consumer<SourceUpdateEvent>() {

            int lastIndex;

            @Override
            public void accept(SourceUpdateEvent sourceUpdateEvent) throws Exception {
                switch (sourceUpdateEvent.getType()) {
                    case UPDATE_BEGINS:
                        lastIndex = cachedLastItemAttached;
                        break;
                    case ITEMS_ADDED:
                        if(sourceUpdateEvent.getPosition()<= lastIndex)
                            lastIndex += sourceUpdateEvent.getItemCount();
                        break;
                    case ITEMS_REMOVED:
                        if(sourceUpdateEvent.getPosition() <= lastIndex) {
                            lastIndex -= sourceUpdateEvent.getItemCount();
                        }
                        break;
                    case UPDATE_ENDS:
                        if(lastIndex >= 0) {
                            bloatPages(this.lastIndex);
                            lastIndex = -1;
                        }
                        break;
                }
            }
        });
    }


    @Override
    public void setViewInteractor(ViewInteractor viewInteractor) {
        super.setViewInteractor(viewInteractor);
        for (PaginatedSourceItem item : sources) {
            item.source.setViewInteractor(viewInteractor);
        }
    }

    @Override
    public void onAttached() {
        loadingItemItemController.onCreate(itemUpdatePublisher);
        for (PaginatedSourceItem item : sources) {
            item.source.onAttached();
        }
        isAttached = true;
    }

    @Override
    public void onItemAttached(int position) {
        cachedLastItemAttached = lastItemAttached = position;
        trimPagesSafely(position);
        bloatPages(position);
        if (position == getItemCount() - 1 && hasMoreBottomPage) {
            return;
        } else if (hasMoreTopPage) {
            if (position == 0) {
                return;
            }
        }
        PaginatedSourceItem adapterAsItem = decodeAdapterItem(position);
        if (adapterAsItem != null) {
            adapterAsItem.source.onItemAttached(position - adapterAsItem.startPosition);
        }

    }

    private void bloatPages(int attachedIndex) {
        if (hasMoreBottomPage && attachedIndex + threshHold > getItemCount()) {
            callbacks.loadNextBottomPage();
        }
        if (hasMoreTopPage && attachedIndex - threshHold < 0) {
            callbacks.loadNextTopPage();
        }
    }

    public void addPageOnTop(ItemControllerSource<T> page) {
        addPageOnTopWhenSafe(page);
    }

    private void addPageOnTopWhenSafe(final ItemControllerSource<T> page) {
        processWhenSafe(new Runnable() {
            @Override
            public void run() {
                addPageOnTopInternal(page);
            }
        });
    }

    private void addPageOnTopInternal(ItemControllerSource<T> page) {
        PaginatedSourceItem item = new PaginatedSourceItem(page);
        item.source.setViewInteractor(getViewInteractor());
        final boolean oldHadMoreTopPage = this.hasMoreTopPage;
        int startPosition = oldHadMoreTopPage ? 1 : 0;
        this.hasMoreTopPage = callbacks.hasMoreTopPage();

        if (this.hasMoreTopPage != oldHadMoreTopPage) {
            if (oldHadMoreTopPage) {
                startPosition = 0;
            } else {
                startPosition = 1;
            }
        }

        item.startPosition = startPosition;
        sources.add(0, item);
        updateIndexes(item);
        if (isAttached) {
            item.attach();
        }

        beginUpdates();
        if (this.hasMoreTopPage != oldHadMoreTopPage) {
            if (oldHadMoreTopPage) {
                if(page.getItemCount() > 0) {
                    notifyItemsChanged(0, 1);
                    notifyItemsInserted(1, page.getItemCount() - 1);
                }
                else {
                    notifyItemsRemoved(0,1);
                }
            } else {
                notifyItemsInserted(0, page.getItemCount() + 1);
            }
        }
        else if(hasMoreTopPage) {
            notifyItemsChanged(0,1);
            notifyItemsInserted(1,page.getItemCount() - 1);
            notifyItemsInserted(0,1);
        }
        else if(oldHadMoreTopPage) {
            notifyItemsChanged(0,1);
            notifyItemsInserted(1,page.getItemCount() - 1);
        }
        else {
            notifyItemsInserted(startPosition,page.getItemCount());
        }
        endUpdates();
    }


    public void addPageInBottom(ItemControllerSource<T> page) {
        addPagInBottomWhenSafe(page);
    }

    private void addPagInBottomWhenSafe(final ItemControllerSource<T> page) {
        processWhenSafe(new Runnable() {
            @Override
            public void run() {
                addPageInBottomInternal(page);
            }
        });
    }

    private void addPageInBottomInternal(ItemControllerSource<T> page) {
        final boolean oldHadMoreBottomPages = this.hasMoreBottomPage;
        this.hasMoreBottomPage = callbacks.hasMoreBottomPage();
        PaginatedSourceItem item = new PaginatedSourceItem(page);
        int startPosition = getItemCount() - (oldHadMoreBottomPages ? 1 : 0);

        item.startPosition = startPosition;
        item.source.setViewInteractor(getViewInteractor());

        sources.add(item);
        if (isAttached) {
            item.attach();
        }
        beginUpdates();
        if (this.hasMoreBottomPage != oldHadMoreBottomPages) {
            if (oldHadMoreBottomPages) {
                if(page.getItemCount() > 0) {
                    notifyItemsChanged(startPosition, 1);
                    notifyItemsInserted(startPosition, page.getItemCount() - 1);
                }
                else {
                    notifyItemsRemoved(startPosition, 1);
                }
            } else {
                notifyItemsInserted(startPosition,page.getItemCount()  + 1);
            }
        }
        else {
            notifyItemsInserted(startPosition,page.getItemCount());
        }
        endUpdates();
    }


    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public int getItemPosition(ItemController item) {
        if(this.loadingItemItemController == item) {
            if(hasMoreTopPage) {
                return 0;
            }
            else {
                return getItemCount() - 1;
            }
        }
        else {
            for (PaginatedSourceItem source : this.sources) {
                ItemControllerSource source1 = source.source;
                int itemPosition = source1.getItemPosition(item);
                if(itemPosition >= 0) {
                    return itemPosition + source.startPosition;
                }
            }
        }
        return -1;
    }


    @Override
    public T getItemForPosition(int position) {
        if (position == 0 && hasMoreTopPage) {
            return this.loadingItemItemController;
        } else if (position == getItemCount() - 1 && hasMoreBottomPage) {
            return this.loadingItemItemController;
        } else {
            PaginatedSourceItem item = decodeAdapterItem(position);
            return item.source.getItem(position - item.startPosition);
        }
    }

    private PaginatedSourceItem decodeAdapterItem(int position) {
        PaginatedSourceItem previous = null;
        for (PaginatedSourceItem adapterAsItem : sources) {
            if (adapterAsItem.startPosition > position) {
                return previous;
            } else {
                previous = adapterAsItem;
            }
        }
        return previous;
    }

//    @Override
//    public void onItemDetached(int position) {
//        trimPagesSafely(position);
//        bloatPages(position);
//        if (position == getItemCount() - 1 && hasMoreBottomPage) {
//            return;
//        } else if (hasMoreTopPage) {
//            if (position == 0) {
//                return;
//            }
//        }
//        PaginatedSourceItem adapterAsItem = decodeAdapterItem(position);
//        if(adapterAsItem == null) {
//            System.out.print("");
//        }
//        adapterAsItem.source.onItemDetached(position);
//    }


    private void trimPagesSafely(final int position) {
        cancelOldProcess(trimPagesRunnable);
        trimPagesRunnable = new Runnable() {
            @Override
            public void run() {
                trimPages(position);
            }
        };
        processWhenSafe(trimPagesRunnable);
    }

    private void trimPages(int detachedItemPosition) {
        if (lastItemAttached < 0) {
            return;
        }
        beginUpdates();
        detachedItemPosition = lastItemAttached;
        lastItemAttached = -1;
        final boolean didTripBottomPages = trimBottomPage(detachedItemPosition);
        final boolean didTrimTopPages = trimTopPage(detachedItemPosition);
        if (didTrimTopPages || didTripBottomPages)
            endUpdates();
    }

    private boolean trimTopPage(int detachedItemPosition) {
        boolean success = false;
        if (detachedItemPosition - safeLimit > 0) {
            int safePosition = detachedItemPosition - safeLimit;
            int removedItems = 0;
            List<PaginatedSourceItem> removed = new ArrayList<>();
            while (0 < sources.size()) {
                PaginatedSourceItem paginatedSourceItem = sources.get(0);
                if (paginatedSourceItem.startPosition + paginatedSourceItem.source.getItemCount() < safePosition) {
                    PaginatedSourceItem remove = sources.remove(0);
                    removed.add(remove);
                    callbacks.unloadingTopPage(paginatedSourceItem.source);
                    success = true;
                    removedItems += paginatedSourceItem.source.getItemCount();
                } else {
                    if (success) {
                        int startPosition = 0;
                        if (hasMoreTopPage != callbacks.hasMoreTopPage()) {
                            hasMoreTopPage = !hasMoreTopPage;
                            if (!hasMoreTopPage) {
                                startPosition = 0;
                                removedItems++;
                            } else {
                                startPosition = 1;
                                removedItems--;
                                notifyItemsChanged(0, 1);
                            }
                        } else if (hasMoreTopPage) {
                            startPosition = 1;
                        }
                        resetIndexes(startPosition);
                        notifyItemsRemoved(startPosition, removedItems);
                    }
                    break;
                }
            }
            for(PaginatedSourceItem item : removed) {
                item.detach();
            }
        }
        return success;
    }

    public void refreshTopPageAvailability() {
        getViewInteractor().processWhenSafe(new Runnable() {
            @Override
            public void run() {
                refreshTopPageAvailabilityInternal();
            }
        });
    }

    private void refreshTopPageAvailabilityInternal() {
        if(this.hasMoreTopPage != this.callbacks.hasMoreTopPage()) {
            this.hasMoreTopPage = this.callbacks.hasMoreTopPage();
            int startPosition;
            beginUpdates();
            if(this.hasMoreTopPage) {
                notifyItemsInserted(0,1);
                startPosition = 1;
            }
            else {
                notifyItemsRemoved(0,1);
                startPosition = 0;
            }
            resetIndexes(startPosition);
            endUpdates();
        }
    }


    public void refreshBottomPageAvailability() {
        getViewInteractor().processWhenSafe(new Runnable() {
            @Override
            public void run() {
                refreshBottomPageAvailabilityInternal();
            }
        });
    }

    private void refreshBottomPageAvailabilityInternal() {
        boolean hasMoreBottomPage = this.callbacks.hasMoreBottomPage();
        if(this.hasMoreBottomPage != hasMoreBottomPage) {
            int count = getItemCount();
            this.hasMoreBottomPage = hasMoreBottomPage;
            beginUpdates();
            if(this.hasMoreBottomPage) {
                notifyItemsInserted(count,1);
            }
            else {
                notifyItemsRemoved(count-1,1);
            }
            endUpdates();
        }
    }

    private boolean trimBottomPage(int detachedItemPosition) {
        boolean success = false;
        if (detachedItemPosition + safeLimit < getItemCount()) {
            final int safePosition = detachedItemPosition + safeLimit;
            int removedItemsCount = 0;
            int startPosition = 0;
            final boolean didHaveMoreBottomItems = this.hasMoreBottomPage;
            List<PaginatedSourceItem> removedItems = new ArrayList<>();
            for (int i = sources.size() - 1; i >= 0; i--) {
                PaginatedSourceItem paginatedSourceItem = sources.get(i);
                if (paginatedSourceItem.startPosition > safePosition) {
                    PaginatedSourceItem remove = sources.remove(i);
                    removedItems.add(remove);
                    removedItemsCount += paginatedSourceItem.source.getItemCount();
                    callbacks.unloadingBottomPage(paginatedSourceItem.source);
                    success = true;
                    startPosition = paginatedSourceItem.startPosition;
                } else {
                    if (removedItemsCount > 0) {
                        if (didHaveMoreBottomItems != callbacks.hasMoreBottomPage()) {
                            hasMoreBottomPage = !hasMoreBottomPage;
                            if (didHaveMoreBottomItems) {
                                removedItemsCount++;
                            } else {
                                removedItemsCount--;
                                notifyItemsChanged(startPosition + removedItemsCount, 1);
                            }
                        }
                        notifyItemsRemoved(startPosition, removedItemsCount);
                    }
                    break;
                }
            }
            for(PaginatedSourceItem item : removedItems) {
                item.detach();
            }
        }
        return success;
    }

    @Override
    public void onDetached() {
        loadingItemItemController.onDestroy();
        for (PaginatedSourceItem item : sources) {
            item.detach();
        }
        isAttached = false;
    }

    @Override
    protected int computeItemCount() {
        this.hasMoreTopPage = callbacks.hasMoreTopPage();
        int count = this.hasMoreTopPage ? 1 : 0;
        resetIndexes(count);
        if (sources.size() > 0) {
            PaginatedSourceItem paginatedSourceItem = sources.get(sources.size() - 1);
            count += paginatedSourceItem.startPosition + paginatedSourceItem.source.getItemCount();
        }
        this.hasMoreBottomPage = callbacks.hasMoreBottomPage();
        if (this.hasMoreBottomPage) {
            count++;
        }
        return count;
    }

    public static interface PagenatedCallbacks {

        public boolean hasMoreBottomPage();

        public boolean hasMoreTopPage();

        public void loadNextBottomPage();

        public void loadNextTopPage();

        public void unloadingTopPage(ItemControllerSource<?> source);

        public void unloadingBottomPage(ItemControllerSource<?> source);
    }

    private void resetIndexes(int startIndex) {
        if (sources.size() > 0) {
            PaginatedSourceItem item = sources.get(0);
            item.startPosition = startIndex;
            updateIndexes(item);
        }
    }

    void updateIndexes(PaginatedSourceItem modifiedItem) {
        boolean continueUpdating = false;
        for (PaginatedSourceItem item : this.sources) {
            if (continueUpdating) {
                item.startPosition = modifiedItem.startPosition + modifiedItem.source.getItemCount();
                modifiedItem = item;
            } else if (item == modifiedItem) {
                continueUpdating = true;
            }
        }
    }

    class PaginatedSourceItem {

        int startPosition;
        final ItemControllerSource<T> source;
        boolean isAttached;

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


        PaginatedSourceItem(ItemControllerSource<T> source) {
            this.source = source;
            this.source.observeAdapterUpdates().subscribe(updateObserver);
        }

        public void attach() {
            if(!this.isAttached) {
                this.isAttached = true;
                this.source.onAttached();
            }
        }

        public void detach() {
            if(this.isAttached) {
                this.source.onDetached();
                this.isAttached = false;
            }
        }
    }
}
