package com.github.tianma8023.xposed.smscode.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;

import com.github.tianma8023.xposed.smscode.aidl.DaoMaster;
import com.github.tianma8023.xposed.smscode.aidl.DaoSession;
import com.github.tianma8023.xposed.smscode.aidl.SmsMsg;
import com.github.tianma8023.xposed.smscode.aidl.SmsMsgDao;
import com.github.tianma8023.xposed.smscode.entity.SmsCodeRule;
import com.github.tianma8023.xposed.smscode.entity.SmsCodeRuleDao;

import org.greenrobot.greendao.AbstractDao;

import java.util.List;

/**
 * Database Manager for GreenDao
 */
public class DBManager {

    private static final String DB_NAME = "sms-code.db";

    private static DBManager sInstance;

    private DaoSession mDaoSession;

    private DBManager(Context context) {
        TSQLiteOpenHelper dbOpenHelper =
                new TSQLiteOpenHelper(context.getApplicationContext(), DB_NAME);
        SQLiteDatabase database = dbOpenHelper.getWritableDatabase();
        mDaoSession = new DaoMaster(database).newSession();
    }

    public static DBManager get(@NonNull Context context) {
        if (sInstance == null) {
            synchronized (DBManager.class) {
                if (sInstance == null) {
                    sInstance = new DBManager(context);
                }
            }
        }
        return sInstance;
    }

    private <T> AbstractDao getAbstractDao(Class<T> entityClass) {
        return mDaoSession.getDao(entityClass);
    }

    @SuppressWarnings("unchecked")
    private <T> long addEntity(Class<T> entityClass, T entity) {
        AbstractDao abstractDao = getAbstractDao(entityClass);
        return abstractDao.insertOrReplace(entity);
    }

    @SuppressWarnings("unchecked")
    private <T> void addEntities(Class<T> entityClass, List<T> entities) {
        AbstractDao abstractDao = getAbstractDao(entityClass);
        abstractDao.insertOrReplaceInTx(entities);
    }

    @SuppressWarnings("unchecked")
    private <T> void updateEntity(Class<T> entityClass, T entity) {
        AbstractDao abstractDao = getAbstractDao(entityClass);
        abstractDao.update(entity);
    }

    @SuppressWarnings("unchecked")
    private <T> void removeEntity(Class<T> entityClass, T entity) {
        AbstractDao abstractDao = getAbstractDao(entityClass);
        abstractDao.delete(entity);
    }

    @SuppressWarnings("unchecked")
    private <T> void removeEntities(Class<T> entityClass, List<T> entities) {
        AbstractDao abstractDao = getAbstractDao(entityClass);
        abstractDao.deleteInTx(entities);
    }

    private <T> void removeAll(Class<T> entityClass) {
        AbstractDao abstractDao = getAbstractDao(entityClass);
        abstractDao.deleteAll();
    }

    public long addSmsCodeRule(SmsCodeRule smsCodeRule) {
        return addEntity(SmsCodeRule.class, smsCodeRule);
    }

    public void addSmsCodeRules(List<SmsCodeRule> smsCodeRules) {
        addEntities(SmsCodeRule.class, smsCodeRules);
    }

    public void updateSmsCodeRule(SmsCodeRule smsCodeRule) {
        updateEntity(SmsCodeRule.class, smsCodeRule);
    }

    public List<SmsCodeRule> queryAllSmsCodeRules() {
        return mDaoSession.queryBuilder(SmsCodeRule.class).list();
    }

    public List<SmsCodeRule> querySmsCodeRules(SmsCodeRule criteria) {
        SmsCodeRuleDao dao = mDaoSession.getSmsCodeRuleDao();
        return dao.queryBuilder().
                where(
                        SmsCodeRuleDao.Properties.Company.eq(criteria.getCompany()),
                        SmsCodeRuleDao.Properties.CodeKeyword.eq(criteria.getCodeKeyword()),
                        SmsCodeRuleDao.Properties.CodeRegex.eq(criteria.getCodeRegex())
                ).list();
    }

    public boolean isExist(SmsCodeRule codeRule) {
        return !querySmsCodeRules(codeRule).isEmpty();
    }

    public void removeSmsCodeRule(SmsCodeRule smsCodeRule) {
        removeEntity(SmsCodeRule.class, smsCodeRule);
    }

    public void removeAllSmsCodeRules() {
        removeAll(SmsCodeRule.class);
    }

    public void addSmsMsg(SmsMsg smsMsg) {
        addEntity(SmsMsg.class, smsMsg);
    }

    public void addSmsMsgList(List<SmsMsg> smsMsgList) {
        addEntities(SmsMsg.class, smsMsgList);
    }

    public List<SmsMsg> queryAllSmsMsg() {
        return mDaoSession.queryBuilder(SmsMsg.class)
                .orderDesc(SmsMsgDao.Properties.Date)
                .list();
    }

    public void removeSmsMsgList(List<SmsMsg> smsMsgList) {
        removeEntities(SmsMsg.class, smsMsgList);
    }
}
