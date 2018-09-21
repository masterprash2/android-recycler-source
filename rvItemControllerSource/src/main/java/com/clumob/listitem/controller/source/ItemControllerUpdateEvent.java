package com.clumob.listitem.controller.source;

import com.google.auto.value.AutoValue;

/**
 * Created by prashant.rathore on 20/09/18.
 */
@AutoValue
public abstract class ItemControllerUpdateEvent {

    public abstract ItemController getItemController();
    public abstract SourceUpdateEvent getUpdateEvent();

    public static ItemControllerUpdateEvent create(ItemController itemController, SourceUpdateEvent sourceUpdateEvent) {
        return new AutoValue_ItemControllerUpdateEvent(itemController,sourceUpdateEvent);
    }


}
