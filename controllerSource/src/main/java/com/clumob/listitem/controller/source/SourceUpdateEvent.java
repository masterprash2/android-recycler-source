package com.clumob.listitem.controller.source;

import com.google.auto.value.AutoValue;

/**
 * Created by prashant.rathore on 28/05/18.
 */

@AutoValue
public abstract class SourceUpdateEvent {

    public enum Type {
        UPDATE_BEGINS,
        ITEMS_CHANGED,
        ITEMS_REMOVED,
        ITEMS_ADDED,
        ITEMS_MOVED,
        UPDATE_ENDS,
        HAS_STABLE_IDS
    }

    public abstract Type getType();
    public abstract int getPosition();
    public abstract int getItemCount();
    public abstract Builder toBuilder();

    public static Builder builder() {
        return new AutoValue_SourceUpdateEvent.Builder().setItemCount(-1);
    }

    @AutoValue.Builder
    public static abstract class Builder {
        public abstract Builder setType(Type type);
        public abstract Builder setPosition(int position);
        public abstract Builder setItemCount(int itemCount);
        public abstract SourceUpdateEvent build();
    }
}
