package com.clumob.list.presenter.source;

/**
 * Created by prashant.rathore on 02/07/18.
 */

public abstract class ViewModelPresenter<VM> implements Presenter {

    public final VM viewModel;

    public ViewModelPresenter(VM viewModel) {
        this.viewModel = viewModel;
    }

    @Override
    public void onCreate() {

    }

    @Override
    public void onAttach() {

    }

    @Override
    public void onDetach() {

    }

    @Override
    public void onDestroy() {

    }

    public abstract int getType();

    public abstract long getId();
}
