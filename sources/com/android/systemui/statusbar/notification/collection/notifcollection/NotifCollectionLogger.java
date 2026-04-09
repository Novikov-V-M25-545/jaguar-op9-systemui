package com.android.systemui.statusbar.notification.collection.notifcollection;

import android.os.RemoteException;
import android.service.notification.NotificationListenerService;
import com.android.systemui.log.LogBuffer;
import com.android.systemui.log.LogLevel;
import com.android.systemui.log.LogMessage;
import com.android.systemui.log.LogMessageImpl;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: NotifCollectionLogger.kt */
/* loaded from: classes.dex */
public final class NotifCollectionLogger {
    private final LogBuffer buffer;

    public NotifCollectionLogger(@NotNull LogBuffer buffer) {
        Intrinsics.checkParameterIsNotNull(buffer, "buffer");
        this.buffer = buffer;
    }

    public final void logNotifPosted(@NotNull String key) {
        Intrinsics.checkParameterIsNotNull(key, "key");
        LogBuffer logBuffer = this.buffer;
        LogLevel logLevel = LogLevel.INFO;
        C01672 c01672 = new Function1<LogMessage, String>() { // from class: com.android.systemui.statusbar.notification.collection.notifcollection.NotifCollectionLogger.logNotifPosted.2
            @Override // kotlin.jvm.functions.Function1
            @NotNull
            public final String invoke(@NotNull LogMessage receiver) {
                Intrinsics.checkParameterIsNotNull(receiver, "$receiver");
                return "POSTED " + receiver.getStr1();
            }
        };
        if (logBuffer.getFrozen()) {
            return;
        }
        LogMessageImpl logMessageImplObtain = logBuffer.obtain("NotifCollection", logLevel, c01672);
        logMessageImplObtain.setStr1(key);
        logBuffer.push(logMessageImplObtain);
    }

    public final void logNotifGroupPosted(@NotNull String groupKey, int i) {
        Intrinsics.checkParameterIsNotNull(groupKey, "groupKey");
        LogBuffer logBuffer = this.buffer;
        LogLevel logLevel = LogLevel.INFO;
        C01662 c01662 = new Function1<LogMessage, String>() { // from class: com.android.systemui.statusbar.notification.collection.notifcollection.NotifCollectionLogger.logNotifGroupPosted.2
            @Override // kotlin.jvm.functions.Function1
            @NotNull
            public final String invoke(@NotNull LogMessage receiver) {
                Intrinsics.checkParameterIsNotNull(receiver, "$receiver");
                return "POSTED GROUP " + receiver.getStr1() + " (" + receiver.getInt1() + " events)";
            }
        };
        if (logBuffer.getFrozen()) {
            return;
        }
        LogMessageImpl logMessageImplObtain = logBuffer.obtain("NotifCollection", logLevel, c01662);
        logMessageImplObtain.setStr1(groupKey);
        logMessageImplObtain.setInt1(i);
        logBuffer.push(logMessageImplObtain);
    }

    public final void logNotifUpdated(@NotNull String key) {
        Intrinsics.checkParameterIsNotNull(key, "key");
        LogBuffer logBuffer = this.buffer;
        LogLevel logLevel = LogLevel.INFO;
        C01702 c01702 = new Function1<LogMessage, String>() { // from class: com.android.systemui.statusbar.notification.collection.notifcollection.NotifCollectionLogger.logNotifUpdated.2
            @Override // kotlin.jvm.functions.Function1
            @NotNull
            public final String invoke(@NotNull LogMessage receiver) {
                Intrinsics.checkParameterIsNotNull(receiver, "$receiver");
                return "UPDATED " + receiver.getStr1();
            }
        };
        if (logBuffer.getFrozen()) {
            return;
        }
        LogMessageImpl logMessageImplObtain = logBuffer.obtain("NotifCollection", logLevel, c01702);
        logMessageImplObtain.setStr1(key);
        logBuffer.push(logMessageImplObtain);
    }

    public final void logNotifRemoved(@NotNull String key, int i) {
        Intrinsics.checkParameterIsNotNull(key, "key");
        LogBuffer logBuffer = this.buffer;
        LogLevel logLevel = LogLevel.INFO;
        C01692 c01692 = new Function1<LogMessage, String>() { // from class: com.android.systemui.statusbar.notification.collection.notifcollection.NotifCollectionLogger.logNotifRemoved.2
            @Override // kotlin.jvm.functions.Function1
            @NotNull
            public final String invoke(@NotNull LogMessage receiver) {
                Intrinsics.checkParameterIsNotNull(receiver, "$receiver");
                return "REMOVED " + receiver.getStr1() + " reason=" + receiver.getInt1();
            }
        };
        if (logBuffer.getFrozen()) {
            return;
        }
        LogMessageImpl logMessageImplObtain = logBuffer.obtain("NotifCollection", logLevel, c01692);
        logMessageImplObtain.setStr1(key);
        logMessageImplObtain.setInt1(i);
        logBuffer.push(logMessageImplObtain);
    }

    public final void logNotifReleased(@NotNull String key) {
        Intrinsics.checkParameterIsNotNull(key, "key");
        LogBuffer logBuffer = this.buffer;
        LogLevel logLevel = LogLevel.INFO;
        C01682 c01682 = new Function1<LogMessage, String>() { // from class: com.android.systemui.statusbar.notification.collection.notifcollection.NotifCollectionLogger.logNotifReleased.2
            @Override // kotlin.jvm.functions.Function1
            @NotNull
            public final String invoke(@NotNull LogMessage receiver) {
                Intrinsics.checkParameterIsNotNull(receiver, "$receiver");
                return "RELEASED " + receiver.getStr1();
            }
        };
        if (logBuffer.getFrozen()) {
            return;
        }
        LogMessageImpl logMessageImplObtain = logBuffer.obtain("NotifCollection", logLevel, c01682);
        logMessageImplObtain.setStr1(key);
        logBuffer.push(logMessageImplObtain);
    }

    public final void logNotifDismissed(@NotNull String key) {
        Intrinsics.checkParameterIsNotNull(key, "key");
        LogBuffer logBuffer = this.buffer;
        LogLevel logLevel = LogLevel.INFO;
        C01642 c01642 = new Function1<LogMessage, String>() { // from class: com.android.systemui.statusbar.notification.collection.notifcollection.NotifCollectionLogger.logNotifDismissed.2
            @Override // kotlin.jvm.functions.Function1
            @NotNull
            public final String invoke(@NotNull LogMessage receiver) {
                Intrinsics.checkParameterIsNotNull(receiver, "$receiver");
                return "DISMISSED " + receiver.getStr1();
            }
        };
        if (logBuffer.getFrozen()) {
            return;
        }
        LogMessageImpl logMessageImplObtain = logBuffer.obtain("NotifCollection", logLevel, c01642);
        logMessageImplObtain.setStr1(key);
        logBuffer.push(logMessageImplObtain);
    }

    public final void logChildDismissed(@NotNull NotificationEntry entry) {
        Intrinsics.checkParameterIsNotNull(entry, "entry");
        LogBuffer logBuffer = this.buffer;
        LogLevel logLevel = LogLevel.DEBUG;
        AnonymousClass2 anonymousClass2 = new Function1<LogMessage, String>() { // from class: com.android.systemui.statusbar.notification.collection.notifcollection.NotifCollectionLogger.logChildDismissed.2
            @Override // kotlin.jvm.functions.Function1
            @NotNull
            public final String invoke(@NotNull LogMessage receiver) {
                Intrinsics.checkParameterIsNotNull(receiver, "$receiver");
                return "CHILD DISMISSED (inferred): " + receiver.getStr1();
            }
        };
        if (logBuffer.getFrozen()) {
            return;
        }
        LogMessageImpl logMessageImplObtain = logBuffer.obtain("NotifCollection", logLevel, anonymousClass2);
        logMessageImplObtain.setStr1(entry.getKey());
        logBuffer.push(logMessageImplObtain);
    }

    public final void logDismissAll(int i) {
        LogBuffer logBuffer = this.buffer;
        LogLevel logLevel = LogLevel.INFO;
        C01582 c01582 = new Function1<LogMessage, String>() { // from class: com.android.systemui.statusbar.notification.collection.notifcollection.NotifCollectionLogger.logDismissAll.2
            @Override // kotlin.jvm.functions.Function1
            @NotNull
            public final String invoke(@NotNull LogMessage receiver) {
                Intrinsics.checkParameterIsNotNull(receiver, "$receiver");
                return "DISMISS ALL notifications for user " + receiver.getInt1();
            }
        };
        if (logBuffer.getFrozen()) {
            return;
        }
        LogMessageImpl logMessageImplObtain = logBuffer.obtain("NotifCollection", logLevel, c01582);
        logMessageImplObtain.setInt1(i);
        logBuffer.push(logMessageImplObtain);
    }

    public final void logDismissOnAlreadyCanceledEntry(@NotNull NotificationEntry entry) {
        Intrinsics.checkParameterIsNotNull(entry, "entry");
        LogBuffer logBuffer = this.buffer;
        LogLevel logLevel = LogLevel.DEBUG;
        C01592 c01592 = new Function1<LogMessage, String>() { // from class: com.android.systemui.statusbar.notification.collection.notifcollection.NotifCollectionLogger.logDismissOnAlreadyCanceledEntry.2
            @Override // kotlin.jvm.functions.Function1
            @NotNull
            public final String invoke(@NotNull LogMessage receiver) {
                Intrinsics.checkParameterIsNotNull(receiver, "$receiver");
                return "Dismiss on " + receiver.getStr1() + ", which was already canceled. Trying to remove...";
            }
        };
        if (logBuffer.getFrozen()) {
            return;
        }
        LogMessageImpl logMessageImplObtain = logBuffer.obtain("NotifCollection", logLevel, c01592);
        logMessageImplObtain.setStr1(entry.getKey());
        logBuffer.push(logMessageImplObtain);
    }

    public final void logNotifDismissedIntercepted(@NotNull String key) {
        Intrinsics.checkParameterIsNotNull(key, "key");
        LogBuffer logBuffer = this.buffer;
        LogLevel logLevel = LogLevel.INFO;
        C01652 c01652 = new Function1<LogMessage, String>() { // from class: com.android.systemui.statusbar.notification.collection.notifcollection.NotifCollectionLogger.logNotifDismissedIntercepted.2
            @Override // kotlin.jvm.functions.Function1
            @NotNull
            public final String invoke(@NotNull LogMessage receiver) {
                Intrinsics.checkParameterIsNotNull(receiver, "$receiver");
                return "DISMISS INTERCEPTED " + receiver.getStr1();
            }
        };
        if (logBuffer.getFrozen()) {
            return;
        }
        LogMessageImpl logMessageImplObtain = logBuffer.obtain("NotifCollection", logLevel, c01652);
        logMessageImplObtain.setStr1(key);
        logBuffer.push(logMessageImplObtain);
    }

    public final void logNotifClearAllDismissalIntercepted(@NotNull String key) {
        Intrinsics.checkParameterIsNotNull(key, "key");
        LogBuffer logBuffer = this.buffer;
        LogLevel logLevel = LogLevel.INFO;
        C01632 c01632 = new Function1<LogMessage, String>() { // from class: com.android.systemui.statusbar.notification.collection.notifcollection.NotifCollectionLogger.logNotifClearAllDismissalIntercepted.2
            @Override // kotlin.jvm.functions.Function1
            @NotNull
            public final String invoke(@NotNull LogMessage receiver) {
                Intrinsics.checkParameterIsNotNull(receiver, "$receiver");
                return "CLEAR ALL DISMISSAL INTERCEPTED " + receiver.getStr1();
            }
        };
        if (logBuffer.getFrozen()) {
            return;
        }
        LogMessageImpl logMessageImplObtain = logBuffer.obtain("NotifCollection", logLevel, c01632);
        logMessageImplObtain.setStr1(key);
        logBuffer.push(logMessageImplObtain);
    }

    public final void logNoNotificationToRemoveWithKey(@NotNull String key) {
        Intrinsics.checkParameterIsNotNull(key, "key");
        LogBuffer logBuffer = this.buffer;
        LogLevel logLevel = LogLevel.ERROR;
        C01622 c01622 = new Function1<LogMessage, String>() { // from class: com.android.systemui.statusbar.notification.collection.notifcollection.NotifCollectionLogger.logNoNotificationToRemoveWithKey.2
            @Override // kotlin.jvm.functions.Function1
            @NotNull
            public final String invoke(@NotNull LogMessage receiver) {
                Intrinsics.checkParameterIsNotNull(receiver, "$receiver");
                return "No notification to remove with key " + receiver.getStr1();
            }
        };
        if (logBuffer.getFrozen()) {
            return;
        }
        LogMessageImpl logMessageImplObtain = logBuffer.obtain("NotifCollection", logLevel, c01622);
        logMessageImplObtain.setStr1(key);
        logBuffer.push(logMessageImplObtain);
    }

    public final void logRankingMissing(@NotNull String key, @NotNull NotificationListenerService.RankingMap rankingMap) {
        Intrinsics.checkParameterIsNotNull(key, "key");
        Intrinsics.checkParameterIsNotNull(rankingMap, "rankingMap");
        LogBuffer logBuffer = this.buffer;
        LogLevel logLevel = LogLevel.WARNING;
        C01712 c01712 = new Function1<LogMessage, String>() { // from class: com.android.systemui.statusbar.notification.collection.notifcollection.NotifCollectionLogger.logRankingMissing.2
            @Override // kotlin.jvm.functions.Function1
            @NotNull
            public final String invoke(@NotNull LogMessage receiver) {
                Intrinsics.checkParameterIsNotNull(receiver, "$receiver");
                return "Ranking update is missing ranking for " + receiver.getStr1();
            }
        };
        if (!logBuffer.getFrozen()) {
            LogMessageImpl logMessageImplObtain = logBuffer.obtain("NotifCollection", logLevel, c01712);
            logMessageImplObtain.setStr1(key);
            logBuffer.push(logMessageImplObtain);
        }
        LogBuffer logBuffer2 = this.buffer;
        LogLevel logLevel2 = LogLevel.DEBUG;
        AnonymousClass4 anonymousClass4 = new Function1<LogMessage, String>() { // from class: com.android.systemui.statusbar.notification.collection.notifcollection.NotifCollectionLogger.logRankingMissing.4
            @Override // kotlin.jvm.functions.Function1
            @NotNull
            public final String invoke(@NotNull LogMessage receiver) {
                Intrinsics.checkParameterIsNotNull(receiver, "$receiver");
                return "Ranking map contents:";
            }
        };
        if (!logBuffer2.getFrozen()) {
            logBuffer2.push(logBuffer2.obtain("NotifCollection", logLevel2, anonymousClass4));
        }
        for (String str : rankingMap.getOrderedKeys()) {
            LogBuffer logBuffer3 = this.buffer;
            LogLevel logLevel3 = LogLevel.DEBUG;
            AnonymousClass6 anonymousClass6 = new Function1<LogMessage, String>() { // from class: com.android.systemui.statusbar.notification.collection.notifcollection.NotifCollectionLogger.logRankingMissing.6
                @Override // kotlin.jvm.functions.Function1
                @NotNull
                public final String invoke(@NotNull LogMessage receiver) {
                    Intrinsics.checkParameterIsNotNull(receiver, "$receiver");
                    return "  " + receiver.getStr1();
                }
            };
            if (!logBuffer3.getFrozen()) {
                LogMessageImpl logMessageImplObtain2 = logBuffer3.obtain("NotifCollection", logLevel3, anonymousClass6);
                logMessageImplObtain2.setStr1(str);
                logBuffer3.push(logMessageImplObtain2);
            }
        }
    }

    public final void logRemoteExceptionOnNotificationClear(@NotNull String key, @NotNull RemoteException e) {
        Intrinsics.checkParameterIsNotNull(key, "key");
        Intrinsics.checkParameterIsNotNull(e, "e");
        LogBuffer logBuffer = this.buffer;
        LogLevel logLevel = LogLevel.WTF;
        C01732 c01732 = new Function1<LogMessage, String>() { // from class: com.android.systemui.statusbar.notification.collection.notifcollection.NotifCollectionLogger.logRemoteExceptionOnNotificationClear.2
            @Override // kotlin.jvm.functions.Function1
            @NotNull
            public final String invoke(@NotNull LogMessage receiver) {
                Intrinsics.checkParameterIsNotNull(receiver, "$receiver");
                return "RemoteException while attempting to clear " + receiver.getStr1() + ":\n" + receiver.getStr2();
            }
        };
        if (logBuffer.getFrozen()) {
            return;
        }
        LogMessageImpl logMessageImplObtain = logBuffer.obtain("NotifCollection", logLevel, c01732);
        logMessageImplObtain.setStr1(key);
        logMessageImplObtain.setStr2(e.toString());
        logBuffer.push(logMessageImplObtain);
    }

    public final void logRemoteExceptionOnClearAllNotifications(@NotNull RemoteException e) {
        Intrinsics.checkParameterIsNotNull(e, "e");
        LogBuffer logBuffer = this.buffer;
        LogLevel logLevel = LogLevel.WTF;
        C01722 c01722 = new Function1<LogMessage, String>() { // from class: com.android.systemui.statusbar.notification.collection.notifcollection.NotifCollectionLogger.logRemoteExceptionOnClearAllNotifications.2
            @Override // kotlin.jvm.functions.Function1
            @NotNull
            public final String invoke(@NotNull LogMessage receiver) {
                Intrinsics.checkParameterIsNotNull(receiver, "$receiver");
                return "RemoteException while attempting to clear all notifications:\n" + receiver.getStr1();
            }
        };
        if (logBuffer.getFrozen()) {
            return;
        }
        LogMessageImpl logMessageImplObtain = logBuffer.obtain("NotifCollection", logLevel, c01722);
        logMessageImplObtain.setStr1(e.toString());
        logBuffer.push(logMessageImplObtain);
    }

    public final void logLifetimeExtended(@NotNull String key, @NotNull NotifLifetimeExtender extender) {
        Intrinsics.checkParameterIsNotNull(key, "key");
        Intrinsics.checkParameterIsNotNull(extender, "extender");
        LogBuffer logBuffer = this.buffer;
        LogLevel logLevel = LogLevel.INFO;
        C01602 c01602 = new Function1<LogMessage, String>() { // from class: com.android.systemui.statusbar.notification.collection.notifcollection.NotifCollectionLogger.logLifetimeExtended.2
            @Override // kotlin.jvm.functions.Function1
            @NotNull
            public final String invoke(@NotNull LogMessage receiver) {
                Intrinsics.checkParameterIsNotNull(receiver, "$receiver");
                return "LIFETIME EXTENDED: " + receiver.getStr1() + " by " + receiver.getStr2();
            }
        };
        if (logBuffer.getFrozen()) {
            return;
        }
        LogMessageImpl logMessageImplObtain = logBuffer.obtain("NotifCollection", logLevel, c01602);
        logMessageImplObtain.setStr1(key);
        logMessageImplObtain.setStr2(extender.getName());
        logBuffer.push(logMessageImplObtain);
    }

    public final void logLifetimeExtensionEnded(@NotNull String key, @NotNull NotifLifetimeExtender extender, int i) {
        Intrinsics.checkParameterIsNotNull(key, "key");
        Intrinsics.checkParameterIsNotNull(extender, "extender");
        LogBuffer logBuffer = this.buffer;
        LogLevel logLevel = LogLevel.INFO;
        C01612 c01612 = new Function1<LogMessage, String>() { // from class: com.android.systemui.statusbar.notification.collection.notifcollection.NotifCollectionLogger.logLifetimeExtensionEnded.2
            @Override // kotlin.jvm.functions.Function1
            @NotNull
            public final String invoke(@NotNull LogMessage receiver) {
                Intrinsics.checkParameterIsNotNull(receiver, "$receiver");
                return "LIFETIME EXTENSION ENDED for " + receiver.getStr1() + " by '" + receiver.getStr2() + "'; " + receiver.getInt1() + " remaining extensions";
            }
        };
        if (logBuffer.getFrozen()) {
            return;
        }
        LogMessageImpl logMessageImplObtain = logBuffer.obtain("NotifCollection", logLevel, c01612);
        logMessageImplObtain.setStr1(key);
        logMessageImplObtain.setStr2(extender.getName());
        logMessageImplObtain.setInt1(i);
        logBuffer.push(logMessageImplObtain);
    }
}
