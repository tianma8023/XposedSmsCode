package com.tianma.xsmscode.ui.faq;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.tianma8023.xposed.smscode.R;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * FAQ fragment
 */
public class FaqFragment extends Fragment {

    private Context mContext;
    
    public FaqFragment() {
    }

    public static FaqFragment newInstance() {
        return new FaqFragment();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_faq, container, false);

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
            List<FaqItem> items = new FaqItemContainer(context).getFaqItems();
            recyclerView.setAdapter(new FaqItemAdapter(mContext, items));
        }
        return view;
    }
}
