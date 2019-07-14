package com.tianma.xsmscode.app.record;


import com.tianma.xsmscode.entity.SmsMsg;

import java.util.Objects;

public class RecordItem {

    private SmsMsg smsMsg;
    private boolean selected;

    RecordItem(SmsMsg smsMsg) {
        this.smsMsg = smsMsg;
    }

    public SmsMsg getSmsMsg() {
        return smsMsg;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    @Override
    public String toString() {
        return "RecordItem{" +
                "smsMsg=" + smsMsg +
                ", selected=" + selected +
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
