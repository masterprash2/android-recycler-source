package com.clumob.list.presenter.source;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import io.reactivex.observers.DisposableObserver;

/**
 * Created by prashant.rathore on 03/07/18.
 */

public class PaginatedSource extends PresenterSource<Presenter> {

    private final Presenter loadingItemPresenter;
    private List<PaginatedSourceItem> sources = new LinkedList<>();
    private final PagenatedCallbacks callbacks;
    private boolean hasMoreBottomPage = false;
    private boolean hasMoreTopPage = false;
    private boolean isAttached;

    final private int threshHold;
    final private int safeLimit = 20;

    private int lastItemAttached;

    private Runnable trimPagesRunnable;

    public PaginatedSource(Presenter loadingItemPresenter, int preloadTriggerSize, PagenatedCallbacks callbacks) {
        this.loadingItemPresenter = loadingItemPresenter;
        this.threshHold = preloadTriggerSize;
        this.callbacks = callbacks;
        int itemCount = computeItemCount();
        notifyItemsInserted(0, itemCount);
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
        loadingItemPresenter.onCreate();
        for (PaginatedSourceItem item : sources) {
            item.source.onAttached();
        }
        isAttached = true;
    }

    @Override
    public void onItemAttached(int position) {
        lastItemAttached = position;
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

    public void addPageOnTop(PresenterSource<?> page) {
        addPageOnTopWhenSafe(page);
    }

    private void addPageOnTopWhenSafe(final PresenterSource<?> page) {
        proessWhenSafe(new Runnable() {
            @Override
            public void run() {
                addPageOnTopInternal(page);
            }
        });
    }

    private void addPageOnTopInternal(PresenterSource<?> page) {
        PaginatedSourceItem item = new PaginatedSourceItem(page);
        item.source.setViewInteractor(getViewInteractor());
        int itemsInserted = item.source.getItemCount();
        int startPosition = 0;
        final boolean oldHadMoreTopPage = this.hasMoreTopPage;
        this.hasMoreTopPage = callbacks.hasMoreTopPage();
        if (this.hasMoreTopPage != oldHadMoreTopPage) {
            if (oldHadMoreTopPage) {
                startPosition = 0;
                notifyItemsRemoved(0, 1);
            } else {
                itemsInserted++;
                startPosition = 1;
            }
        } else if (oldHadMoreTopPage) {
            startPosition++;
        }

        item.startPosition = startPosition;
        sources.add(0, item);
        updateIndexes(item);
        if (isAttached) {
            item.source.onAttached();
        }

        if (hasMoreBottomPage != callbacks.hasMoreBottomPage()) {
            hasMoreBottomPage = !hasMoreBottomPage;
            if (hasMoreBottomPage) {
                itemsInserted++;
            }
        }
        notifyItemsInserted(item.startPosition, itemsInserted);
    }


    public void addPageInBottom(PresenterSource<?> page) {
        addPagInBottomWhenSafe(page);
    }

    private void addPagInBottomWhenSafe(final PresenterSource<?> page) {
        proessWhenSafe(new Runnable() {
            @Override
            public void run() {
                addPageInBottomInternal(page);
            }
        });
    }

    private void addPageInBottomInternal(PresenterSource<?> page) {
        final boolean oldHadMoreBottomPages = this.hasMoreBottomPage;
        this.hasMoreBottomPage = callbacks.hasMoreBottomPage();
        PaginatedSourceItem item = new PaginatedSourceItem(page);
        int startPosition = getItemCount() - 1;
        int itemCount = page.getItemCount();
        if (this.hasMoreBottomPage != oldHadMoreBottomPages) {
            if (oldHadMoreBottomPages) {
                startPosition--;
                notifyItemsRemoved(item.startPosition, 1);
            } else {
                itemCount++;
            }
        }
        item.startPosition = startPosition;
        item.source.setViewInteractor(getViewInteractor());
        sources.add(item);
        notifyItemsInserted(item.startPosition, itemCount);
    }


    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public int getItemPosition(Presenter item) {
        return 0;
    }


    @Override
    public Presenter getItem(int position) {
        if (position == 0 && hasMoreTopPage) {
            return this.loadingItemPresenter;
        } else if (position == getItemCount() - 1 && hasMoreBottomPage) {
            return this.loadingItemPresenter;
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

    @Override
    public void onItemDetached(int position) {
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
    }


    private void trimPagesSafely(final int position) {
        cancelOldProcess(trimPagesRunnable);
        trimPagesRunnable = new Runnable() {
            @Override
            public void run() {
                trimPages(position);
            }
        };
        proessWhenSafe(trimPagesRunnable);
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
                    removed.add(sources.remove(0));
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
        }
        return success;
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
                    removedItems.add(sources.remove(i));
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
        }
        return success;
    }

    @Override
    public void onDetached() {
        loadingItemPresenter.onDestroy();
        for (PaginatedSourceItem item : sources) {
            item.source.onDetached();
        }
        isAttached = false;
    }

    @Override
    protected int computeItemCount() {
        this.hasMoreTopPage = callbacks.hasMoreTopPage();
        int count = this.hasMoreTopPage ? 1 : 0;
        if (sources.size() > 0) {
            PaginatedSourceItem paginatedSourceItem = sources.get(sources.size() - 1);
            count += paginatedSourceItem.startPosition + paginatedSourceItem.source.getItemCount();
        }

        this.hasMoreBottomPage = hasMoreTopPage && count == 1 ? false : callbacks.hasMoreBottomPage();
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

        public void unloadingTopPage(PresenterSource<?> source);

        public void unloadingBottomPage(PresenterSource<?> source);
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
        final PresenterSource<?> source;

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


        PaginatedSourceItem(PresenterSource<?> source) {
            this.source = source;
            this.source.observeAdapterUpdates().subscribe(updateObserver);
        }
    }
}
