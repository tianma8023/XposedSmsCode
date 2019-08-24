package com.tianma.xsmscode.data.db.entity;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;

import java.util.Objects;

@Entity
public class AppInfo implements Parcelable {

    @Id
    @SerializedName("packageName")
    @Expose
    private String packageName;

    @SerializedName("label")
    @Expose
    private String label;

    @SerializedName("blocked")
    @Expose
    private boolean blocked;

    public AppInfo(String label, String packageName) {
        this.label = label;
        this.packageName = packageName;
    }

    @Generated(hash = 1451390451)
    public AppInfo(String packageName, String label, boolean blocked) {
        this.packageName = packageName;
        this.label = label;
        this.blocked = blocked;
    }

    @Generated(hash = 1656151854)
    public AppInfo() {
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public boolean isBlocked() {
        return blocked;
    }

    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof AppInfo))
            return false;
        AppInfo appInfo = (AppInfo) o;
        return Objects.equals(packageName, appInfo.packageName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(packageName);
    }

    @Override
    public String toString() {
        return "AppInfo{" +
                "label='" + label + '\'' +
                ", packageName='" + packageName + '\'' +
                ", blocked=" + blocked +
                '}';
    }

    public boolean getBlocked() {
        return this.blocked;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(packageName);
        dest.writeString(label);
        dest.writeByte((byte) (blocked ? 1 : 0));
    }

    protected AppInfo(Parcel in) {
        packageName = in.readString();
        label = in.readString();
        blocked = in.readByte() != 0;
    }

    public static final Creator<AppInfo> CREATOR = new Creator<AppInfo>() {
        @Override
        public AppInfo createFromParcel(Parcel in) {
            return new AppInfo(in);
        }

        @Override
        public AppInfo[] newArray(int size) {
            return new AppInfo[size];
        }
    };
}
