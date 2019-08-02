package com.tianma.xsmscode.data.db.entity;

/**
 * Apk version info
 */
public class ApkVersion {

    private String mVersionName;
    private int mVersionValue;
    private String mVersionInfo;

    public ApkVersion(String versionName, String versionInfo) {
        mVersionName = versionName;
        mVersionValue = Integer.parseInt(versionName.replaceAll("\\.", ""));
        mVersionInfo = versionInfo;
    }

    public int getVersionValue() {
        return mVersionValue;
    }

    public String getVersionInfo() {
        return mVersionInfo;
    }

    public String getVersionName() {
        return mVersionName;
    }

    @Override
    public String toString() {
        return "ApkVersion{" +
                "mVersionName='" + mVersionName + '\'' +
                ", mVersionValue=" + mVersionValue +
                ", mVersionInfo='" + mVersionInfo + '\'' +
                '}';
    }
}
