package com.tianma.xsmscode.data.db.entity;

/**
 * Apk version info
 */
public class ApkVersion implements Comparable<ApkVersion> {

    private final String mVersionName;
    private final String mVersionInfo;

    public ApkVersion(String versionName, String versionInfo) {
        mVersionName = versionName;
        mVersionInfo = versionInfo;
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
                ", mVersionInfo='" + mVersionInfo + '\'' +
                '}';
    }

    @Override
    public int compareTo(ApkVersion that) {
        if (that == null) {
            return 1;
        }

        String[] thisParts = this.getVersionName().split("\\.");
        String[] thatParts = that.getVersionName().split("\\.");

        int maxLength = Math.max(thisParts.length, thatParts.length);
        for (int i = 0; i < maxLength; i++) {
            int thisPart = i < thisParts.length ? Integer.parseInt(thisParts[i]) : 0;
            int thatPart = i < thatParts.length ? Integer.parseInt(thatParts[i]) : 0;
            if (thisPart < thatPart) {
                return -1;
            } else if (thisPart > thatPart) {
                return 1;
            }
        }
        return 0;
    }
}
