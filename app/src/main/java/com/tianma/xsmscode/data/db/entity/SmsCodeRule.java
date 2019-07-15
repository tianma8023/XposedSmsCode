package com.tianma.xsmscode.data.db.entity;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.tianma.xsmscode.feature.backup.BackupConst;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Index;
import org.greenrobot.greendao.annotation.NotNull;

import java.util.Objects;

@Entity(
        indexes = {
            @Index(value = "company, codeKeyword, codeRegex", unique = true)
        }
)
public class SmsCodeRule implements Parcelable {

    /**
     * id
     */
    @Id(autoincrement = true)
    private Long id;

    /**
     * company or company or organization name
     */
    @SerializedName(BackupConst.KEY_COMPANY)
    @Expose
    private String company;

    /**
     * verification code keyword
     */
    @SerializedName(BackupConst.KEY_CODE_KEYWORD)
    @Expose
    @NotNull
    private String codeKeyword;

    /**
     * verification code regex
     */
    @SerializedName(BackupConst.KEY_CODE_REGEX)
    @Expose
    @NotNull
    private String codeRegex;

    public SmsCodeRule(String company, @NotNull String codeKeyword,
                       @NotNull String codeRegex) {
        this.company = company;
        this.codeKeyword = codeKeyword;
        this.codeRegex = codeRegex;
    }

    private SmsCodeRule(Parcel in) {
        if (in.readByte() == 0) {
            id = null;
        } else {
            id = in.readLong();
        }
        company = in.readString();
        codeKeyword = in.readString();
        codeRegex = in.readString();
    }

    @Generated(hash = 1853112924)
    public SmsCodeRule(Long id, String company, @NotNull String codeKeyword,
            @NotNull String codeRegex) {
        this.id = id;
        this.company = company;
        this.codeKeyword = codeKeyword;
        this.codeRegex = codeRegex;
    }

    @Generated(hash = 1135501737)
    public SmsCodeRule() {
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getCodeKeyword() {
        return codeKeyword;
    }

    public void setCodeKeyword(String codeKeyword) {
        this.codeKeyword = codeKeyword;
    }

    public String getCodeRegex() {
        return codeRegex;
    }

    public void setCodeRegex(String codeRegex) {
        this.codeRegex = codeRegex;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SmsCodeRule)) return false;
        SmsCodeRule that = (SmsCodeRule) o;
        return Objects.equals(company, that.company) &&
                Objects.equals(codeKeyword, that.codeKeyword) &&
                Objects.equals(codeRegex, that.codeRegex);
    }

    @Override
    public int hashCode() {
        return Objects.hash(company, codeKeyword, codeRegex);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (id == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(id);
        }
        dest.writeString(company);
        dest.writeString(codeKeyword);
        dest.writeString(codeRegex);
    }

    public static final Creator<SmsCodeRule> CREATOR = new Creator<SmsCodeRule>() {
        @Override
        public SmsCodeRule createFromParcel(Parcel in) {
            return new SmsCodeRule(in);
        }

        @Override
        public SmsCodeRule[] newArray(int size) {
            return new SmsCodeRule[size];
        }
    };

    public void copyFrom(SmsCodeRule newRule) {
        this.id = newRule.id;
        this.company = newRule.company;
        this.codeKeyword = newRule.codeKeyword;
        this.codeRegex = newRule.codeRegex;
    }

    @Override
    public String toString() {
        return "SmsCodeRule{" +
                "company='" + company + '\'' +
                ", codeKeyword='" + codeKeyword + '\'' +
                ", codeRegex='" + codeRegex + '\'' +
                '}';
    }
}