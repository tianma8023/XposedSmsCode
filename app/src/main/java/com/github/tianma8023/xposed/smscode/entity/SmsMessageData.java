package com.github.tianma8023.xposed.smscode.entity;

import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;
import android.telephony.SmsMessage;

import com.github.tianma8023.xposed.smscode.utils.SmsMessageUtils;

import java.text.Normalizer;

public class SmsMessageData implements Parcelable {

    // Sender
    private String mSender;
    // Message content
    private String mBody;

    public static SmsMessageData fromIntent(Intent intent) {
        SmsMessage[] smsMessageParts = SmsMessageUtils.fromIntent(intent);
        String sender = smsMessageParts[0].getDisplayOriginatingAddress();
        String body = SmsMessageUtils.getMessageBody(smsMessageParts);

        sender = Normalizer.normalize(sender, Normalizer.Form.NFC);
        body = Normalizer.normalize(body, Normalizer.Form.NFC);

        SmsMessageData message = new SmsMessageData();
        message.setSender(sender).setBody(body);
        return message;
    }

    public SmsMessageData setSender(String sender) {
        mSender = sender;
        return this;
    }

    public SmsMessageData setBody(String body) {
        mBody = body;
        return this;
    }

    public String getSender() {
        return mSender;
    }

    public String getBody() {
        return mBody;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    private SmsMessageData(Parcel source) {
        mSender = source.readString();
        mBody = source.readString();
    }

    public SmsMessageData() {
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mSender);
        dest.writeString(mBody);
    }

    public static final Parcelable.Creator<SmsMessageData> CREATOR = new Parcelable.Creator<SmsMessageData>() {

        @Override
        public SmsMessageData createFromParcel(Parcel source) {
            return new SmsMessageData(source);
        }

        @Override
        public SmsMessageData[] newArray(int size) {
            return new SmsMessageData[size];
        }
    };
}
