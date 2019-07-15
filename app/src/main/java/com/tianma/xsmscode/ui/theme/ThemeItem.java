package com.tianma.xsmscode.ui.theme;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.ColorRes;
import android.support.annotation.StringRes;
import android.support.annotation.StyleRes;

public class ThemeItem implements Parcelable{

    private @StringRes int colorNameRes;
    private  @ColorRes int colorValueRes;
    private  @StyleRes int themeRes;

    public ThemeItem(@StringRes int colorNameRes, @ColorRes int colorValueRes, @StyleRes int themeRes) {
        this.colorNameRes = colorNameRes;
        this.colorValueRes = colorValueRes;
        this.themeRes = themeRes;
    }

    private ThemeItem(Parcel in) {
        colorNameRes = in.readInt();
        colorValueRes = in.readInt();
        themeRes = in.readInt();
    }

    public @StringRes int getColorNameRes() {
        return colorNameRes;
    }

    public @ColorRes int getColorValueRes() {
        return colorValueRes;
    }

    public @StyleRes int getThemeRes() {
        return themeRes;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(colorNameRes);
        dest.writeInt(colorValueRes);
        dest.writeInt(themeRes);
    }

    public static final Creator<ThemeItem> CREATOR = new Creator<ThemeItem>() {
        @Override
        public ThemeItem createFromParcel(Parcel in) {
            return new ThemeItem(in);
        }

        @Override
        public ThemeItem[] newArray(int size) {
            return new ThemeItem[size];
        }
    };
}
