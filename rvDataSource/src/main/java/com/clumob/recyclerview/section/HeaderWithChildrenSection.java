package com.clumob.recyclerview.section;

import com.clumob.listitem.controller.source.ItemController;

/**
 * Created by prashant.rathore on 19/06/18.
 */

public class HeaderWithChildrenSection extends Section {

    private final boolean isChildrenAttachedToHeader;

    private Section header;
    private Section children;

    public HeaderWithChildrenSection(boolean isChildrenAttachedToHeader) {
        this.isChildrenAttachedToHeader = isChildrenAttachedToHeader;
    }

    public void setChildren(Section children) {
        this.children = children;
    }

    public void setHeader(Section header) {
        this.header = header;
        if(this.header != null) {
            this.header.observeAdapterUpdates().subscribe();
        }
    }

    public Section getHeader() {
        return header;
    }

    public Section getChildren() {
        return children;
    }

    @Override
    public ItemController getItem(int position) {
        int headerCount = getHeaderCount();
        if (position < headerCount - 1) {
            return header.getItem(position);
        } else {
            return children.getItem(position - headerCount - 1);
        }
    }

    @Override
    public int getItemType(int position) {
        return 0;
    }

    private int getHeaderCount() {
        return header == null ? 0 : header.getItemCount();
    }

    private int getChildrenCount() {
        return children == null ? 0 : children.getItemCount();
    }

    @Override
    protected int computeItemCount() {
        if (isChildrenAttachedToHeader && (header == null || children == null)) {
            return 0;
        }
        return getHeaderCount() + getChildrenCount();
    }
}
