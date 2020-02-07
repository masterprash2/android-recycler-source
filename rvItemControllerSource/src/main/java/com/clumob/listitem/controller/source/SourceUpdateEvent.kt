package com.clumob.listitem.controller.source

import com.google.auto.value.AutoValue

/**
 * Created by prashant.rathore on 28/05/18.
 */
@AutoValue
abstract class SourceUpdateEvent {
    enum class Type {
        UPDATE_BEGINS, ITEMS_CHANGED, ITEMS_REMOVED, ITEMS_ADDED, ITEMS_MOVED, UPDATE_ENDS, HAS_STABLE_IDS
    }

    abstract val type: Type
    abstract val position: Int
    abstract val itemCount: Int
    abstract fun toBuilder(): Builder

    @AutoValue.Builder
    abstract class Builder {
        abstract fun setType(type: Type): Builder
        abstract fun setPosition(position: Int): Builder
        abstract fun setItemCount(itemCount: Int): Builder
        abstract fun build(): SourceUpdateEvent
    }

    companion object {
        @JvmStatic
        fun builder(): Builder {
            return AutoValue_SourceUpdateEvent.Builder().setItemCount(-1)
        }
    }
}