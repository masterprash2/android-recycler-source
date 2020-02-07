package com.clumob.recyclerview.adapter;

import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.clumob.listitem.controller.source.ItemController;
import com.clumob.listitem.controller.source.ItemControllerSource;
import com.clumob.listitem.controller.source.SourceUpdateEvent;

import java.util.Deque;
import java.util.LinkedList;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

import io.reactivex.observers.DisposableObserver;
import kotlin.Unit;
import kotlin.jvm.functions.Function0;

/**
 * Created by prashant.rathore on 28/05/18.
 */

public class RvAdapter extends RecyclerView.Adapter<RvViewHolder> {

    private final ItemControllerSource itemControllerSource;
    private final ViewHolderProvider viewHolderProvider;
    private final LifecycleOwner lifecycleOwner;
    private OnRecyclerItemClickListener itemClickListener;
    private RecyclerView recyclerView;
    private AdapterUpdateObserver adapterUpdateEventObserver;

    private Handler mHandler = new Handler();

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (recyclerView != null && itemClickListener != null) {
                RecyclerView.ViewHolder childViewHolder = recyclerView.getChildViewHolder(view);
                itemClickListener.onRecyclerItemClick(recyclerView, childViewHolder.getAdapterPosition());
            }
        }
    };

    public RvAdapter(ViewHolderProvider viewHolderProvider,
                     ItemControllerSource itemControllerSource,
                     LifecycleOwner lifecycleOwner) {
        this.itemControllerSource = itemControllerSource;
        this.viewHolderProvider = viewHolderProvider;
        setHasStableIds(this.itemControllerSource.hasStableIds());
        this.itemControllerSource.setViewInteractor(createViewInteractor());
        this.lifecycleOwner = lifecycleOwner;
    }


    private ItemControllerSource.ViewInteractor createViewInteractor() {
        return new ItemControllerSource.ViewInteractor() {

            Deque<Runnable> deque = new LinkedList<>();
            private boolean processingInProgress;

            @Override
            public void processWhenSafe(@NotNull Runnable runnable) {
                deque.add(runnable);
                if (!processingInProgress) {
                    processingInProgress = true;
                    processWhenQueueIdle();
                }
            }

            private void processWhenQueueIdle() {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (isComputingLayout()) {
                            mHandler.post(this);
                        } else {
                            if (deque.peekFirst() != null) {
                                Runnable runnable = deque.pollFirst();
                                runnable.run();
                                mHandler.post(this);
                            } else {
                                processingInProgress = false;
                            }
                        }
                    }
                });
            }

            @Override
            public void cancelOldProcess(Runnable runnable) {
                mHandler.removeCallbacks(runnable);
            }

        };
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        this.recyclerView = recyclerView;
        if (this.adapterUpdateEventObserver != null) {
            this.adapterUpdateEventObserver.dispose();
            this.adapterUpdateEventObserver = null;
        }
        this.adapterUpdateEventObserver = new AdapterUpdateObserver();
        itemControllerSource.observeAdapterUpdates().subscribe(this.adapterUpdateEventObserver);
        this.itemControllerSource.onAttached();
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        this.itemControllerSource.onDetached();
        this.recyclerView = null;
        if (this.adapterUpdateEventObserver != null) {
            this.adapterUpdateEventObserver.dispose();
            this.adapterUpdateEventObserver = null;
        }
        super.onDetachedFromRecyclerView(recyclerView);
    }

    boolean isComputingLayout() {
        return recyclerView != null && recyclerView.isComputingLayout();
    }

    public void setItemClickListener(OnRecyclerItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    @NonNull
    @Override
    public RvViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RvViewHolder<? extends ItemController> rvViewHolder = viewHolderProvider.provideViewHolder(parent, viewType);
        rvViewHolder.setLifecycleOwner(this.lifecycleOwner);
        return rvViewHolder;
    }


    @Override
    public void onViewAttachedToWindow(@NonNull RvViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        holder.onAttach();
//        Log.d("PAGINATEDP","LP: "+ holder.getLayoutPosition() + " AP:"+holder.getAdapterPosition() + " P:"+holder.getPosition());
        itemControllerSource.onItemAttached(holder.getAdapterPosition());
    }

    @Override
    public void onBindViewHolder(@NonNull RvViewHolder holder, int position) {
        ItemController item = itemControllerSource.getItem(position);
        holder.getItemView().setOnClickListener(onClickListener);
        holder.bind(item);
    }

    @Override
    public long getItemId(int position) {
        return itemControllerSource.getItemId(position);
    }


    @Override
    public void onViewDetachedFromWindow(@NonNull RvViewHolder holder) {
        holder.onDetach();
        super.onViewDetachedFromWindow(holder);
    }

    @Override
    public void onViewRecycled(@NonNull RvViewHolder holder) {
        holder.unBind();
        super.onViewRecycled(holder);
    }

    @Override
    public int getItemViewType(int position) {
//        Log.d("PAGINATEDIP"," "+position);
        return itemControllerSource.getItemType(position);
    }

    @Override
    public int getItemCount() {
        return itemControllerSource.getItemCount();
    }

    public static interface OnRecyclerItemClickListener {
        public void onRecyclerItemClick(RecyclerView recyclerView, int position);
    }

    class AdapterUpdateObserver extends DisposableObserver<SourceUpdateEvent> {

        @Override
        public void onNext(SourceUpdateEvent sourceUpdateEvent) {
            if (recyclerView == null) {
                return;
            }
            switch (sourceUpdateEvent.getType()) {
                case UPDATE_BEGINS:
                    break;
                case ITEMS_CHANGED:
                    notifyItemRangeChanged(sourceUpdateEvent.getPosition(), sourceUpdateEvent.getItemCount());
                    break;
                case ITEMS_REMOVED:
                    notifyItemRangeRemoved(sourceUpdateEvent.getPosition(), sourceUpdateEvent.getItemCount());
                    break;
                case ITEMS_ADDED:
                    notifyItemRangeInserted(sourceUpdateEvent.getPosition(), sourceUpdateEvent.getItemCount());
                    break;
                case ITEMS_MOVED:
                    notifyItemMoved(sourceUpdateEvent.getPosition(), sourceUpdateEvent.getItemCount());
                    break;
                case UPDATE_ENDS:
                    break;
                case HAS_STABLE_IDS:
                    setHasStableIds(itemControllerSource.hasStableIds());
                    break;
            }
        }

        @Override
        public void onError(Throwable e) {
            Log.d("RvAdapter", "Observer Error ");
            e.printStackTrace();
        }

        @Override
        public void onComplete() {
            Log.d("RvAdapter", "Observer OnComplete ");
        }
    }

}
