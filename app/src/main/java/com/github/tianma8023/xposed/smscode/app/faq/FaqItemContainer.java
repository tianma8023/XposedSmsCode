package com.github.tianma8023.xposed.smscode.app.faq;

import android.content.Context;
import android.content.res.Resources;

import com.github.tianma8023.xposed.smscode.R;

import java.util.ArrayList;
import java.util.List;


public class FaqItemContainer {

    private List<FaqItem> mFaqItems = new ArrayList<>();

    public FaqItemContainer(Context context) {
        loadItems(context);
    }

    private void loadItems(Context context) {
        Resources res = context.getResources();
        String[] questionArr = res.getStringArray(R.array.question_list);
        String[] answerArr = res.getStringArray(R.array.answer_list);
        for (int i = 0; i < questionArr.length; i++) {
            mFaqItems.add(new FaqItem(questionArr[i], answerArr[i]));
        }
    }

    public List<FaqItem> getFaqItems() {
        return mFaqItems;
    }
}
