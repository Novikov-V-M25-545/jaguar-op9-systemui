package com.android.systemui.assist;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.SystemClock;
import androidx.slice.Clock;
import com.android.internal.app.AssistUtils;
import com.android.systemui.assist.AssistHandleBehaviorController;
import com.android.systemui.statusbar.NavigationBarController;
import java.util.EnumMap;
import java.util.Map;

/* loaded from: classes.dex */
public abstract class AssistModule {
    static Handler provideBackgroundHandler() {
        HandlerThread handlerThread = new HandlerThread("AssistHandleThread");
        handlerThread.start();
        return handlerThread.getThreadHandler();
    }

    static Map<AssistHandleBehavior, AssistHandleBehaviorController.BehaviorController> provideAssistHandleBehaviorControllerMap(AssistHandleOffBehavior assistHandleOffBehavior, AssistHandleLikeHomeBehavior assistHandleLikeHomeBehavior, AssistHandleReminderExpBehavior assistHandleReminderExpBehavior) {
        EnumMap enumMap = new EnumMap(AssistHandleBehavior.class);
        enumMap.put((EnumMap) AssistHandleBehavior.OFF, (AssistHandleBehavior) assistHandleOffBehavior);
        enumMap.put((EnumMap) AssistHandleBehavior.LIKE_HOME, (AssistHandleBehavior) assistHandleLikeHomeBehavior);
        enumMap.put((EnumMap) AssistHandleBehavior.REMINDER_EXP, (AssistHandleBehavior) assistHandleReminderExpBehavior);
        return enumMap;
    }

    static AssistHandleViewController provideAssistHandleViewController(NavigationBarController navigationBarController) {
        return navigationBarController.getAssistHandlerViewController();
    }

    static AssistUtils provideAssistUtils(Context context) {
        return new AssistUtils(context);
    }

    static Clock provideSystemClock() {
        return new Clock() { // from class: com.android.systemui.assist.AssistModule$$ExternalSyntheticLambda0
            @Override // androidx.slice.Clock
            public final long currentTimeMillis() {
                return SystemClock.uptimeMillis();
            }
        };
    }
}
