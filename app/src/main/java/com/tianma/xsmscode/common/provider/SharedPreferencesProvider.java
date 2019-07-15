//package com.tianma.xsmscode.provider;
//
//import com.crossbowffs.remotepreferences.RemotePreferenceProvider;
//import PrefConst;
//
///**
// * SharedPreferencesProvider for IPC
// */
//public class SharedPreferencesProvider extends RemotePreferenceProvider {
//
//    public SharedPreferencesProvider() {
//        super(PrefConst.REMOTE_PREF_AUTHORITY, new String[]{PrefConst.REMOTE_PREF_NAME});
//    }
//
////    @Override
////    protected boolean checkAccess(String prefName, String prefKey, boolean write) {
////        // allow write
//////        if (write)
//////            return false;
////        return super.checkAccess(prefName, prefKey, write);
////    }
//}
