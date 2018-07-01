//package com.clumob.interactor.datasource;
//
//import com.clumob.recyclerview.section.SourceUpdateEvent;
//
//import io.reactivex.Observable;
//
///**
// * Created by prashant.rathore on 24/06/18.
// */
//
//public class SectionInteractorAdapter <Item,Ir extends Presenter<Item>> implements InteractorSource<Item,Ir> {
//
//    private final Presenter<InteractorSource<Item, Ir>> sectionInteractor;
//    private InteractorSource<Item,Ir> contentAdapter;
//
//    public SectionInteractorAdapter(Presenter<InteractorSource<Item,Ir>> sectionInteractor,
//                                    InteractorSource<Item, Ir> contentAdapter) {
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
//    public Observable<SourceUpdateEvent> observeAdapterUpdates() {
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
