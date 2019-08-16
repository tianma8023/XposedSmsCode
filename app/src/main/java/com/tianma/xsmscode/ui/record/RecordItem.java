package com.tianma.xsmscode.ui.record;


import com.tianma.xsmscode.data.db.entity.SmsMsg;

import java.util.Objects;

public class RecordItem {

    private SmsMsg smsMsg;
    private boolean mSelected;

    RecordItem(SmsMsg smsMsg) {
        this.smsMsg = smsMsg;
    }

    public SmsMsg getSmsMsg() {
        return smsMsg;
    }

    public boolean isSelected() {
        return mSelected;
    }

    public void setSelected(boolean selected) {
        this.mSelected = selected;
    }

    @Override
    public String toString() {
        return "RecordItem{" +
                "smsMsg=" + smsMsg +
                ", selected=" + mSelected +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RecordItem)) return false;
        RecordItem item = (RecordItem) o;
        return Objects.equals(smsMsg, item.smsMsg);
    }

    @Override
    public int hashCode() {
        return Objects.hash(smsMsg);
    }
}
