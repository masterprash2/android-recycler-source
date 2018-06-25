//package com.clumob.interactor.datasource;
//
//import com.clumob.recyclerview.section.AdapterUpdateEvent;
//
//import io.reactivex.Observable;
//
///**
// * Created by prashant.rathore on 24/06/18.
// */
//
//public class SectionInteractorAdapter <Item,Ir extends Interactor<Item>> implements InteractorAdapter<Item,Ir> {
//
//    private final Interactor<InteractorAdapter<Item, Ir>> sectionInteractor;
//    private InteractorAdapter<Item,Ir> contentAdapter;
//
//    public SectionInteractorAdapter(Interactor<InteractorAdapter<Item,Ir>> sectionInteractor,
//                                    InteractorAdapter<Item, Ir> contentAdapter) {
//        this.contentAdapter = contentAdapter;
//        this.sectionInteractor = sectionInteractor;
//    }
//
//    @Override
//    public void onAttached() {
//        sectionInteractor.onCreate(contentAdapter);
//        sectionInteractor.onAttach(contentAdapter);
//        contentAdapter.onAttached();
//    }
//
//    @Override
//    public boolean hasStableIds() {
//        return contentAdapter.hasStableIds();
//    }
//
//    @Override
//    public Observable<AdapterUpdateEvent> observeAdapterUpdates() {
//        return contentAdapter.observeAdapterUpdates();
//    }
//
//    @Override
//    public long getItemId(int position) {
//        return contentAdapter.getItemId(position);
//    }
//
//    @Override
//    public int getItemType(int position) {
//        return contentAdapter.getItemType(position);
//    }
//
//    @Override
//    public int getItemCount() {
//        return contentAdapter.getItemCount();
//    }
//
//    @Override
//    public InteractorItem<Item, Ir> getItem(int position) {
//        return contentAdapter.getItem(position);
//    }
//
//    @Override
//    public void onDetached() {
//        contentAdapter.onDetached();
//        sectionInteractor.onDetach(contentAdapter);
//        sectionInteractor.onDestroy(contentAdapter);
//
//    }
//}
