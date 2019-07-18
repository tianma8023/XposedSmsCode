package com.tianma.xsmscode.ui.faq;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.tianma8023.xposed.smscode.R;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class FaqItemAdapter extends RecyclerView.Adapter<FaqItemAdapter.ViewHolder> {

    private final List<FaqItem> mFaqItems;

    private final String mQuestionPrefix;
    private final String mAnswerPrefix;

    FaqItemAdapter(Context context, List<FaqItem> items) {
        mFaqItems = items;
        mQuestionPrefix = context.getString(R.string.simplified_question);
        mAnswerPrefix = context.getString(R.string.simplified_answer);
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
        holder.bindData(item);
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

        void bindData(FaqItem item) {
            mItem = item;
            String question = mQuestionPrefix + item.getQuestion();
            mQuestionView.setText(question);
            String answer = mAnswerPrefix + item.getAnswer();
            mAnswerView.setText(answer);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mAnswerView.getText() + "'";
        }
    }
}
