//package com.clumob.interactor.datasource;
//
//import java.util.ArrayList;
//import java.util.List;
//
///**
// * Created by prashant.rathore on 24/06/18.
// */
//
//public class BasicItemArraySource<P extends ItemController> extends PresenterSource<P> {
//
//    private List<Item> contentItems = new ArrayList<>();
//    private InteractorItem<Item, ItemController<Item>> reusableItem = createReusableInteractor();
//
//
//    @Override
//    public void onAttached() {
//    }
//
//    public void switchContentItems(List<Item> newItems) {
//        int diff = newItems.size() - this.contentItems.size();
//        if (diff > 0) {
//            notifyItemsInserted(this.contentItems.size(), diff);
//        } else {
//            notifyItemsRemoved(newItems.size(), diff * (-1));
//        }
//        this.contentItems = newItems;
//    }
//
//    public void replaceContentItem(int index, Item item) {
//        this.contentItems.set(index, item);
//        notifyItemsChanged(index, 1);
//    }
//
//    public Item getContentItem(int index) {
//        return this.contentItems.get(index);
//    }
//
//
//    @Override
//    public boolean hasStableIds() {
//        return false;
//    }
//
//    @Override
//    protected int computeItemCount() {
//        return contentItems.size();
//    }
//
//    @Override
//    public InteractorItem<Item, ItemController<Item>> getItemForPosition(int position) {
//        return reusableItem.mutate(contentItems.get(position));
//    }
//
//    @Override
//    public void onDetached() {
//    }
//
//
//    private InteractorItem<Item, ItemController<Item>> createReusableInteractor() {
//        return new InteractorItem<Item, ItemController<Item>>(new ItemController<Item>() {
//            @Override
//            public void onCreate(Item item) {
//
//            }
//
//            @Override
//            public void onAttach(Item item) {
//
//            }
//
//            @Override
//            public void onDetach(Item item) {
//
//            }
//
//            @Override
//            public void onDestroy(Item item) {
//
//            }
//        });
//    }
//
//}
