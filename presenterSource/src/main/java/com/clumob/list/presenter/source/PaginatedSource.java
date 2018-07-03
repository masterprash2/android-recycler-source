package com.clumob.list.presenter.source;

import java.util.LinkedList;
import java.util.List;

import io.reactivex.observers.DisposableObserver;

/**
 * Created by prashant.rathore on 03/07/18.
 */

public class PaginatedSource extends PresenterSource<Presenter> {

    private final int preloadTriggerSize;
    private final Presenter loadingItemPresenter;
    private List<PaginatedSourceItem> sources = new LinkedList<>();
    private final PagenatedCallbacks callbacks;
    private boolean hasMoreBottomPage = false;
    private boolean hasMoreTopPage = false;
    private boolean isAttached;

    final private int threshHold = 5;
    final private int safeLimit = 20;

    public PaginatedSource(Presenter loadingItemPresenter, int preloadTriggerSize, PagenatedCallbacks callbacks) {
        this.loadingItemPresenter = loadingItemPresenter;
        this.preloadTriggerSize = preloadTriggerSize;
        this.callbacks = callbacks;
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
        PaginatedSourceItem adapterAsItem = decodeAdapterItem(position);
        adapterAsItem.source.onItemAttached(position - adapterAsItem.startPosition);
        bloatPages(position);
    }

    private void bloatPages(int attachedIndex) {
        if (hasMoreTopPage && attachedIndex + threshHold > getItemCount()) {
            callbacks.loadNextTopPage();
        }
        if (hasMoreBottomPage && attachedIndex - threshHold < 0) {
            callbacks.loadNextBottomPage();
        }
    }

    public void addPageOnTop(PresenterSource<?> page) {
        PaginatedSourceItem item = new PaginatedSourceItem(page);
        if (sources.size() > 0) {
            PaginatedSourceItem previousItem = sources.get(sources.size() - 1);
            item.startPosition = previousItem.startPosition + previousItem.source.getItemCount();
        }
        sources.add(item);
        if (isAttached) {
            item.source.onAttached();
        }
        notifyItemsInserted(item.startPosition, item.source.getItemCount());
    }

    public void addPageInBottom(PresenterSource<?> page) {
        PaginatedSourceItem item = new PaginatedSourceItem(page);
        if (sources.size() > 0) {
            PaginatedSourceItem previousItem = sources.get(sources.size() - 1);
            item.startPosition = previousItem.startPosition + previousItem.source.getItemCount();
        }
        sources.add(item);
        notifyItemsInserted(item.startPosition, item.source.getItemCount());
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
            PaginatedSourceItem item = decodeAdapterItem(position + (hasMoreTopPage ? 1 : 0));
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
        PaginatedSourceItem adapterAsItem = decodeAdapterItem(position);
        adapterAsItem.source.onItemDetached(position - adapterAsItem.startPosition);
        trimPages(position);
    }


    private void trimPages(int detachedItemPosition) {
        beginUpdates();
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
            int startPosition = hasMoreTopPage ? 1 : 0;
            for (int i = 0; i < sources.size(); ) {
                PaginatedSourceItem paginatedSourceItem = sources.get(i);
                if (paginatedSourceItem.startPosition > safePosition) {
                    sources.remove(i);
                    callbacks.unloadingTopPage(paginatedSourceItem.source);
                    success = true;
                    removedItems += paginatedSourceItem.source.getItemCount();
                } else {
                    if (success) {
                        if (hasMoreTopPage != callbacks.hasMoreTopPage()) {
                            if (hasMoreTopPage) {
                                startPosition = 0;
                                removedItems++;
                            }
                        }
                        notifyItemsRemoved(0, removedItems + 1);
                        for (PaginatedSourceItem sourceItem : sources) {
                            sourceItem.startPosition = startPosition;
                            startPosition += sourceItem.source.getItemCount();
                        }
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
            for (int i = sources.size() - 1; i > 0; i--) {
                PaginatedSourceItem paginatedSourceItem = sources.get(i);
                if (paginatedSourceItem.startPosition > safePosition) {
                    sources.remove(i);
                    removedItemsCount += paginatedSourceItem.source.getItemCount();
                    callbacks.unloadingBottomPage(paginatedSourceItem.source);
                    success = true;
                    startPosition = paginatedSourceItem.startPosition;
                } else {
                    if (removedItemsCount > 0) {
                        if (didHaveMoreBottomItems != callbacks.hasMoreBottomPage()) {
                            if (didHaveMoreBottomItems) {
                                removedItemsCount++;
                            } else {
                                notifyItemsInserted(startPosition + removedItemsCount, 1);
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

        public void unloadingTopPage(PresenterSource<?> source);

        public void unloadingBottomPage(PresenterSource<?> source);
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
        }


        PaginatedSourceItem(PresenterSource<?> source) {
            this.source = source;
            this.source.observeAdapterUpdates().subscribe(updateObserver);
        }
    }
}
