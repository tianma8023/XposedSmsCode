package com.tianma.xsmscode.xp.hook.code.helper;

import android.annotation.SuppressLint;
import android.hardware.input.InputManager;
import android.os.SystemClock;
import android.view.InputDevice;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;

import de.robv.android.xposed.XposedHelpers;

/**
 * Helper for InputMethod Input Characters.<br/>
 * Refer: com.android.commands.input.Input
 */
public class InputHelper {

    private InputHelper() {
    }

    /**
     * refer: com.android.commands.input.Input#sendText()
     *
     * @throws Throwable throwable throws if the caller has no android.permission.INJECT_EVENTS permission
     */
    public static void sendText(String text) throws Throwable {
        int source = InputDevice.SOURCE_KEYBOARD;

        StringBuilder sb = new StringBuilder(text);

        boolean escapeFlag = false;
        for (int i = 0; i < sb.length(); i++) {
            if (escapeFlag) {
                escapeFlag = false;
                if (sb.charAt(i) == 's') {
                    sb.setCharAt(i, ' ');
                    sb.deleteCharAt(--i);
                }
            }
            if (sb.charAt(i) == '%') {
                escapeFlag = true;
            }
        }

        char[] chars = sb.toString().toCharArray();

        KeyCharacterMap kcm = KeyCharacterMap.load(KeyCharacterMap.VIRTUAL_KEYBOARD);
        KeyEvent[] events = kcm.getEvents(chars);
        for (KeyEvent keyEvent : events) {
            if (source != keyEvent.getSource()) {
                keyEvent.setSource(source);
            }
            injectKeyEvent(keyEvent);
        }
    }

    public static void sendKeyEvent(int inputSource, int keyCode, boolean longpress) throws Throwable {
        long now = SystemClock.uptimeMillis();
        injectKeyEvent(new KeyEvent(now, now, KeyEvent.ACTION_DOWN, keyCode, 0, 0,
                KeyCharacterMap.VIRTUAL_KEYBOARD, 0, 0, inputSource));
        if (longpress) {
            injectKeyEvent(new KeyEvent(now, now, KeyEvent.ACTION_DOWN, keyCode, 1, 0,
                    KeyCharacterMap.VIRTUAL_KEYBOARD, 0, KeyEvent.FLAG_LONG_PRESS,
                    inputSource));
        }
        injectKeyEvent(new KeyEvent(now, now, KeyEvent.ACTION_UP, keyCode, 0, 0,
                KeyCharacterMap.VIRTUAL_KEYBOARD, 0, 0, inputSource));
    }

    /**
     * refer com.android.commands.input.Input#injectKeyEvent()
     */
    @SuppressLint("PrivateApi")
    private static void injectKeyEvent(KeyEvent keyEvent) throws Throwable {
        InputManager inputManager = (InputManager) XposedHelpers.callStaticMethod(InputManager.class, "getInstance");

        int INJECT_INPUT_EVENT_MODE_WAIT_FOR_FINISH =
                XposedHelpers.getStaticIntField(InputManager.class, "INJECT_INPUT_EVENT_MODE_WAIT_FOR_FINISH");

        Class<?>[] paramTypes = {KeyEvent.class, int.class,};
        Object[] args = {keyEvent, INJECT_INPUT_EVENT_MODE_WAIT_FOR_FINISH,};

        XposedHelpers.callMethod(inputManager, "injectInputEvent", paramTypes, args);
    }

}
