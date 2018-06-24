package com.clumob.recyclerview.adaptersource;

import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.TextView;

import com.clumob.recyclerview.adapter.ArrayInteractorAdapter;
import com.clumob.recyclerview.adapter.InteractorAdapter;
import com.clumob.recyclerview.adapter.InteractorItem;
import com.clumob.recyclerview.adapter.RvAdapter;
import com.clumob.recyclerview.adapter.RvViewHolder;
import com.clumob.recyclerview.adapter.ViewHolderProvider;
import com.clumob.recyclerview.section.interactor.Interactor;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false));
        recyclerView.setAdapter(createRecyclerViewAdapter());
    }


    @Override
    protected void onDestroy() {
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setAdapter(null);
        super.onDestroy();
    }

    private RecyclerView.Adapter createRecyclerViewAdapter() {
        return new RvAdapter(createViewHolderProvider(),createInteractorAdapter());
    }

    private InteractorAdapter createInteractorAdapter() {
        ArrayInteractorAdapter<Object,Interactor<Object>> arrayInteractorAdapter = new ArrayInteractorAdapter();
        List<InteractorItem<Object,Interactor<Object>>> items = new ArrayList<>();
        for(int i = 0 ; i < 1000 ; i++) {
            items.add(new InteractorItem<Object, Interactor<Object>>(createInteractor(),"Object " + i));
        }
        arrayInteractorAdapter.switchItems(items);
        return arrayInteractorAdapter;
    }

    private Interactor<Object> createInteractor() {
        return new Interactor<Object>() {
            @Override
            public void onCreate(Object o) {
                Log.d("OnCreate",o.toString());
            }

            @Override
            public void onAttach(Object o) {
                Log.d("OnAttach",o.toString());
            }

            @Override
            public void onDetach(Object o) {
                Log.d("OnDetach",o.toString());
            }

            @Override
            public void onDestroy(Object o) {
                Log.d("OnDestroy",o.toString());
            }
        };
    }


    private ViewHolderProvider createViewHolderProvider() {
        return new ViewHolderProvider() {
            @Override
            public RvViewHolder<?, ? extends Interactor<?>> provideViewHolder(ViewGroup parent, int type) {
                View inflate = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
                return new RvViewHolder<Object, Interactor<Object>>(inflate) {
                    @Override
                    protected void bindView() {
                    }

                    @Override
                    protected void unBindView() {
                    }
                };
            }
        };
    }
}
