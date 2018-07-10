package com.clumob.list.pager;

import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import com.clumob.listitem.controller.source.ItemControllerSource;

/**
 * Created by prashant.rathore on 02/07/18.
 */

public class PresenterPagerAdapter extends PagerAdapter {

    private ItemControllerSource<?> itemControllerSource;

    public PresenterPagerAdapter(ItemControllerSource<?> itemControllerSource) {
        this.itemControllerSource = itemControllerSource;
    }

    @Override
    public int getCount() {
        return itemControllerSource.getItemCount();
    }

    @Override
    public void startUpdate(ViewGroup container) {
        super.startUpdate(container);
    }

    @Override
    public void finishUpdate(ViewGroup container) {
        super.finishUpdate(container);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        return super.instantiateItem(container, position);
    }

    @Override
    public void setPrimaryItem(ViewGroup container, int position, Object object) {
        super.setPrimaryItem(container, position, object);
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return false;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        super.destroyItem(container, position, object);
    }
}
