package com.tianma.xsmscode.data.eventbus;

import org.greenrobot.eventbus.EventBus;

/**
 * Event bus utils
 */
public class XEventBus {

    private XEventBus() {
    }

    private static EventBus get() {
        return EventBus.getDefault();
    }

    public static void post(Object event) {
        get().post(event);
    }

    public static void register(Object subscriber) {
        get().register(subscriber);
    }

    public static void unregister(Object subscriber) {
        get().unregister(subscriber);
    }

}
