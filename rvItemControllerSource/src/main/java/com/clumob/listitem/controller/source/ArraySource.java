package com.clumob.listitem.controller.source;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

/**
 * Created by prashant.rathore on 24/06/18.
 */

public class ArraySource<Controller extends ItemController> extends ItemControllerSource<Controller> {

    private List<Controller> controller = new ArrayList<>();
    private boolean isAttached;
    private ItemUpdatePublisher itemUpdatePublisher = new ItemUpdatePublisher();
    private CompositeDisposable compositeDisposable;

    public ArraySource() {
    }

    @Override
    public void onAttached() {
        isAttached = true;
        compositeDisposable = new CompositeDisposable();
        compositeDisposable.add(observeItemUpdates());
        for (Controller item : controller) {
            item.onCreate(itemUpdatePublisher);
        }
    }


    @Override
    public void onItemAttached(int position) {

    }

    public List<Controller> getItems() {
        return controller;
    }

    public void setItems(List<Controller> items) {
        switchItems(items);
    }

    private void switchItems(List<Controller> items, final boolean useDiffProcess) {
        final List<Controller> newItems;
        if (items == null) {
            newItems = new ArrayList<>();
        } else {
            newItems = items;
        }
        processWhenSafe(new Runnable() {
            @Override
            public void run() {
                final int oldCount = controller.size();
                final int newCount = newItems.size();
                DiffUtil.DiffResult diffResult = diffResults(controller,newItems);
                controller = newItems;
                beginUpdates();
                if(useDiffProcess) {
                    diffResult.dispatchUpdatesTo(ArraySource.this);
                }
                else {
                    int diff = newCount - oldCount;
                    if (diff > 0) {
                        notifyItemsInserted(oldCount, diff);
                        notifyItemsChanged(0, oldCount);
                    } else if (diff < 0) {
                        notifyItemsRemoved(newCount, diff * (-1));
                        notifyItemsChanged(0, newCount);
                    } else {
                        notifyItemsChanged(0, newCount);
                    }
                }
                endUpdates();
                if (isAttached) {
                    for (Controller item : newItems) {
                        item.onCreate(itemUpdatePublisher);
                    }
                }
            }
        });
    }

    public void switchItems(List<Controller> items) {
        switchItems(items,false);
    }

    public void switchItemsWithDiffRemovalAndInsertions(List<Controller> items) {
        switchItems(items,true);
    }

    private DiffUtil.DiffResult diffResults(final List<Controller> oldItems, final List<Controller> newItems) {
        return DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            public int getOldListSize() {
                return oldItems.size();
            }

            @Override
            public int getNewListSize() {
                return newItems.size();
            }

            @Override
            public boolean areItemsTheSame(int oldPosition, int newPosition) {
                Controller itemOld = oldItems.get(oldPosition);
                Controller itemNew = newItems.get(newPosition);
                boolean equals = itemOld == itemNew || itemOld.hashCode() == itemNew.hashCode() && itemOld.equals(itemNew);
                if(equals) {
                    newItems.set(newPosition, itemOld);
                }
                return equals;
            }

            @Override
            public boolean areContentsTheSame(int oldPosition, int newPosition) {
                return areItemsTheSame(oldPosition,newPosition);
            }
        }, false);
    }


    public void replaceItem(final int index, final Controller item) {
        processWhenSafe(new Runnable() {
            @Override
            public void run() {
                replaceItemWhenSafe(index,item);
            }
        });
    }

    private void replaceItemWhenSafe(int index, Controller item) {
        Controller set = this.controller.set(index, item);
        set.onDestroy();
        notifyItemsChanged(index, 1);
        if (isAttached) {
            item.onCreate(itemUpdatePublisher);
        }
    }

    @Override
    public int getItemPosition(Controller item) {
        return this.controller.indexOf(item);
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    protected int computeItemCount() {
        return controller.size();
    }

    @Override
    public Controller getItemForPosition(int position) {
        return controller.get(position);
    }

//    @Override
//    public void onItemDetached(int position) {
//
//    }

    private Disposable observeItemUpdates() {
        return itemUpdatePublisher.observeEvents().subscribe(new Consumer<ItemController>() {
            @Override
            public void accept(final ItemController itemController) throws Exception {
                processWhenSafe(new Runnable() {
                    @Override
                    public void run() {
                        int index = controller.indexOf(itemController);
                        if(index >= 0)
                            notifyItemsChanged(index, 1);
                    }
                });
            }
        });
    }

    @Override
    public void onDetached() {
        compositeDisposable.dispose();
        isAttached = false;
        for (Controller item : controller) {
            item.onDetach(null);
        }
    }

}
