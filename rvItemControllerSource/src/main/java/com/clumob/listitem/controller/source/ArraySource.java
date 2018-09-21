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
        this.controller = controller;
    }

    public void switchItems(List<Controller> items) {
        final List<Controller> newItems;
        if (items == null) {
            newItems = new ArrayList<>();
        } else {
            newItems = items;
        }
        proessWhenSafe(new Runnable() {
            @Override
            public void run() {
                final int oldCount = controller.size();
                final int newCount = newItems.size();
                for (Controller item : controller) {
                    item.onDetach();
                    item.onDestroy();
                }
                for (Controller item : newItems) {
                    item.onCreate(itemUpdatePublisher);
                }
                int diff = newCount - oldCount;
                controller = newItems;
                beginUpdates();
                if (diff > 0) {
                    notifyItemsInserted(oldCount, diff);
                    notifyItemsChanged(0, oldCount);
                } else if (diff < 0) {
                    notifyItemsRemoved(newCount, diff * (-1));
                    notifyItemsChanged(0, newCount);
                } else {
                    notifyItemsChanged(0, newCount);
                }
                endUpdates();
                if (isAttached) {
                    for (Controller item : newItems) {
                        item.onAttach();
                    }
                }
            }
        });
    }

    public void replaceItem(int index, Controller item) {
        this.controller.set(index, item);
        item.onCreate();
        notifyItemsChanged(index, 1);
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
            public void accept(ItemController itemController) throws Exception {
                int index = controller.indexOf(itemController);
                notifyItemsChanged(index, 0);
            }
        });
    }

    @Override
    public void onDetached() {
        compositeDisposable.dispose();
        isAttached = false;
        for (Controller item : controller) {
            item.onDetach();
        }
    }

}
