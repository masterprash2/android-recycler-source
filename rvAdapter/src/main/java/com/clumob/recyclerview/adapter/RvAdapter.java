package com.clumob.recyclerview.adapter;

import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.clumob.list.presenter.source.Presenter;
import com.clumob.list.presenter.source.PresenterSource;
import com.clumob.list.presenter.source.SourceUpdateEvent;

import java.util.Deque;
import java.util.LinkedList;

import io.reactivex.observers.DisposableObserver;

/**
 * Created by prashant.rathore on 28/05/18.
 */

public class RvAdapter extends RecyclerView.Adapter<RvViewHolder> {

    private final PresenterSource presenterSource;
    private final ViewHolderProvider viewHolderProvider;
    private OnRecyclerItemClickListener itemClickListener;
    private RecyclerView recyclerView;
    private AdapterUpdateObserver adapterUpdateEventObserver;

    private boolean isComputingLayout;
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
                     PresenterSource presenterSource) {
        this.presenterSource = presenterSource;
        this.viewHolderProvider = viewHolderProvider;
        setHasStableIds(this.presenterSource.hasStableIds());
        this.presenterSource.setViewInteractor(createViewInteractor());
    }

    private PresenterSource.ViewInteractor createViewInteractor() {
        return new PresenterSource.ViewInteractor() {

            Deque<Runnable> deque = new LinkedList<>();
            private boolean processingInProgress;

            @Override
            public void processWhenSafe(Runnable runnable) {
                deque.add(runnable);
                if (!processingInProgress) {
                    processingInProgress = true;
                    processWhenQueueIdle();
                }
            }

            private void processWhenQueueIdle() {
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (isComputingLayout()) {
                            mHandler.postDelayed(this, 50);
                        } else {
                            while (deque.peekFirst() != null) {
                                Runnable runnable = deque.pollFirst();
                                runnable.run();
                            }
                            processingInProgress = false;
                        }
                    }
                }, 50);
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
        presenterSource.observeAdapterUpdates().subscribe(this.adapterUpdateEventObserver);
        this.presenterSource.onAttached();
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        this.presenterSource.onDetached();
        this.recyclerView = null;
        if (this.adapterUpdateEventObserver != null) {
            this.adapterUpdateEventObserver.dispose();
            this.adapterUpdateEventObserver = null;
        }
        super.onDetachedFromRecyclerView(recyclerView);
    }

    boolean isComputingLayout() {
        return recyclerView == null ? false : recyclerView.isComputingLayout();
    }

    public void setItemClickListener(OnRecyclerItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    @NonNull
    @Override
    public RvViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return viewHolderProvider.provideViewHolder(parent, viewType);
    }


    @Override
    public void onViewAttachedToWindow(@NonNull RvViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        holder.onAttach();
        Log.d("PAGINATEDP","LP: "+ holder.getLayoutPosition() + " AP:"+holder.getAdapterPosition() + " P:"+holder.getPosition());
        presenterSource.onItemAttached(holder.getAdapterPosition());
    }

    @Override
    public void onBindViewHolder(@NonNull RvViewHolder holder, int position) {
        Presenter item = presenterSource.getItem(position);
        holder.getItemView().setOnClickListener(onClickListener);
        holder.bind(item);
    }

    @Override
    public long getItemId(int position) {
        return presenterSource.getItemId(position);
    }


    @Override
    public void onViewDetachedFromWindow(@NonNull RvViewHolder holder) {
        presenterSource.onItemDetached(holder.getAdapterPosition());
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
        Log.d("PAGINATEDIP"," "+position);
        return presenterSource.getItemType(position);
    }

    @Override
    public int getItemCount() {
        return presenterSource.getItemCount();
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
                    if (getItemCount() < 0) {
                        Log.d("PAGINATEDR", "Removed " + sourceUpdateEvent.getItemCount() + " ItemCount:" + getItemCount());
                    }
                    Log.d("PAGINATEDR", "Removed " + sourceUpdateEvent.getItemCount() + " ItemCount:" + getItemCount());
                    notifyItemRangeRemoved(sourceUpdateEvent.getPosition(), sourceUpdateEvent.getItemCount());
                    break;
                case ITEMS_ADDED:
                    Log.d("PAGINATEDR", "Added " + sourceUpdateEvent.getItemCount() + " ItemCount:" + getItemCount());
                    notifyItemRangeInserted(sourceUpdateEvent.getPosition(), sourceUpdateEvent.getItemCount());
                    break;
                case ITEMS_MOVED:
                    notifyItemMoved(sourceUpdateEvent.getPosition(), sourceUpdateEvent.getItemCount());
                    break;
                case UPDATE_ENDS:
                    break;
                case HAS_STABLE_IDS:
                    setHasStableIds(presenterSource.hasStableIds());
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
