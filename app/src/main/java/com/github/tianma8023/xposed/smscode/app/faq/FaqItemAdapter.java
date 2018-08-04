package com.github.tianma8023.xposed.smscode.app.faq;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.tianma8023.xposed.smscode.R;

import java.util.List;

public class FaqItemAdapter extends RecyclerView.Adapter<FaqItemAdapter.ViewHolder> {

    private final List<FaqItem> mFaqItems;

    public FaqItemAdapter(List<FaqItem> items) {
        mFaqItems = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.faq_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        FaqItem item = mFaqItems.get(position);
        holder.mItem = item;

        holder.mQuestionView.setText(item.getQuestion());
        holder.mAnswerView.setText(item.getAnswer());
    }

    @Override
    public int getItemCount() {
        return mFaqItems.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        final View mView;
        final TextView mQuestionView;
        final TextView mAnswerView;
        FaqItem mItem;

        ViewHolder(View view) {
            super(view);
            mView = view;
            mQuestionView = view.findViewById(R.id.item_question);
            mAnswerView = view.findViewById(R.id.item_answer);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mAnswerView.getText() + "'";
        }
    }
}
