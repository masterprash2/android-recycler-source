package com.clumob.recyclerview.adaptersource;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.clumob.interactor.datasource.ArraySource;
import com.clumob.interactor.datasource.Interactor;
import com.clumob.interactor.datasource.InteractorSource;
import com.clumob.interactor.datasource.InteractorItem;
import com.clumob.interactor.datasource.MultiplexSource;
import com.clumob.recyclerview.adapter.RvAdapter;
import com.clumob.recyclerview.adapter.RvViewHolder;
import com.clumob.recyclerview.adapter.ViewHolderProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(createRecyclerViewAdapter());
    }


    @Override
    protected void onDestroy() {
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setAdapter(null);
        super.onDestroy();
    }

    private RecyclerView.Adapter createRecyclerViewAdapter() {
        return new RvAdapter(createViewHolderProvider(), createMultiplexAdapterSample());
    }

    private boolean removeAdapter;

    private MultiplexSource createMultiplexAdapterSample() {
        final MultiplexSource multiplexSource = new MultiplexSource();
        multiplexSource.addAdapter(createInteractorAdapter("a"));
        multiplexSource.addAdapter(createInteractorAdapter("b"));
        multiplexSource.addAdapter(createInteractorAdapter("c"));
        final Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                if(removeAdapter) {
                    multiplexSource.removeAdapter(1);
                }
                else {
                    String s = String.valueOf(System.currentTimeMillis());
                    InteractorSource interactorSource = createInteractorAdapter(String.valueOf(s.charAt(s.length() - 1)));
                    int limit = new Random().nextInt(20);
                    Log.d("LIMIT",s+"-"+limit);
                    interactorSource.setMaxLimit(limit);
                    multiplexSource.addAdapter(interactorSource);
                }

                removeAdapter = !removeAdapter;
                handler.postDelayed(this, 2000);
            }
        });
        return multiplexSource;
    }

    private InteractorSource createInteractorAdapter(String d) {
        ArraySource<Object, Interactor<Object>> arraySource = new ArraySource();
        List<InteractorItem<Object, Interactor<Object>>> items = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            items.add(new InteractorItem<Object, Interactor<Object>>(createInteractor(), d + " = Object " + i));
        }
        arraySource.switchItems(items);
        return arraySource;
    }

    private Interactor<Object> createInteractor() {
        return new Interactor<Object>() {
            @Override
            public void onCreate(Object o) {
                Log.d("OnCreate", o.toString());
            }

            @Override
            public void onAttach(Object o) {
                Log.d("OnAttach", o.toString());
            }

            @Override
            public void onDetach(Object o) {
                Log.d("OnDetach", o.toString());
            }

            @Override
            public void onDestroy(Object o) {
                Log.d("OnDestroy", o.toString());
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
                        TextView tv = getItemView().findViewById(R.id.text);
                        tv.setText(getItem().toString());
                    }

                    @Override
                    protected void unBindView() {
                    }
                };
            }
        };
    }

//    private RvAdapter createSectionInteractorAdapterSample() {
//        return new RvAdapter(createViewHolderProvider(), createSectionInteractorAdapter());
//    }
//
//    private InteractorSource createSectionInteractorAdapter() {
//        return new SectionInteractorAdapter(createSectionInteractor(), createSectionContentAdapter());
//    }

    private Interactor<InteractorSource> createSectionInteractor() {
        return null;
    }

    private InteractorSource createSectionContentAdapter() {
        return null;
    }
}
