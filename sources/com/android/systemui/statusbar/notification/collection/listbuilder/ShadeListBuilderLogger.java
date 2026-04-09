package com.android.systemui.statusbar.notification.collection.listbuilder;

import com.android.systemui.log.LogBuffer;
import com.android.systemui.log.LogLevel;
import com.android.systemui.log.LogMessage;
import com.android.systemui.log.LogMessageImpl;
import com.android.systemui.statusbar.notification.collection.GroupEntry;
import com.android.systemui.statusbar.notification.collection.ListEntry;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.notification.collection.listbuilder.pluggable.NotifFilter;
import com.android.systemui.statusbar.notification.collection.listbuilder.pluggable.NotifPromoter;
import com.android.systemui.statusbar.notification.collection.listbuilder.pluggable.NotifSection;
import java.util.List;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: ShadeListBuilderLogger.kt */
/* loaded from: classes.dex */
public final class ShadeListBuilderLogger {
    private final LogBuffer buffer;

    public ShadeListBuilderLogger(@NotNull LogBuffer buffer) {
        Intrinsics.checkParameterIsNotNull(buffer, "buffer");
        this.buffer = buffer;
    }

    public final void logOnBuildList() {
        LogBuffer logBuffer = this.buffer;
        LogLevel logLevel = LogLevel.INFO;
        C01522 c01522 = new Function1<LogMessage, String>() { // from class: com.android.systemui.statusbar.notification.collection.listbuilder.ShadeListBuilderLogger.logOnBuildList.2
            @Override // kotlin.jvm.functions.Function1
            @NotNull
            public final String invoke(@NotNull LogMessage receiver) {
                Intrinsics.checkParameterIsNotNull(receiver, "$receiver");
                return "Request received from NotifCollection";
            }
        };
        if (logBuffer.getFrozen()) {
            return;
        }
        logBuffer.push(logBuffer.obtain("ShadeListBuilder", logLevel, c01522));
    }

    public final void logEndBuildList(int i, int i2, int i3) {
        LogBuffer logBuffer = this.buffer;
        LogLevel logLevel = LogLevel.INFO;
        C01462 c01462 = new Function1<LogMessage, String>() { // from class: com.android.systemui.statusbar.notification.collection.listbuilder.ShadeListBuilderLogger.logEndBuildList.2
            @Override // kotlin.jvm.functions.Function1
            @NotNull
            public final String invoke(@NotNull LogMessage receiver) {
                Intrinsics.checkParameterIsNotNull(receiver, "$receiver");
                return "(Build " + receiver.getLong1() + ") Build complete (" + receiver.getInt1() + " top-level entries, " + receiver.getInt2() + " children)";
            }
        };
        if (logBuffer.getFrozen()) {
            return;
        }
        LogMessageImpl logMessageImplObtain = logBuffer.obtain("ShadeListBuilder", logLevel, c01462);
        logMessageImplObtain.setLong1(i);
        logMessageImplObtain.setInt1(i2);
        logMessageImplObtain.setInt2(i3);
        logBuffer.push(logMessageImplObtain);
    }

    public final void logPreGroupFilterInvalidated(@NotNull String filterName, int i) {
        Intrinsics.checkParameterIsNotNull(filterName, "filterName");
        LogBuffer logBuffer = this.buffer;
        LogLevel logLevel = LogLevel.DEBUG;
        C01542 c01542 = new Function1<LogMessage, String>() { // from class: com.android.systemui.statusbar.notification.collection.listbuilder.ShadeListBuilderLogger.logPreGroupFilterInvalidated.2
            @Override // kotlin.jvm.functions.Function1
            @NotNull
            public final String invoke(@NotNull LogMessage receiver) {
                Intrinsics.checkParameterIsNotNull(receiver, "$receiver");
                return "Pre-group NotifFilter \"" + receiver.getStr1() + "\" invalidated; pipeline state is " + receiver.getInt1();
            }
        };
        if (logBuffer.getFrozen()) {
            return;
        }
        LogMessageImpl logMessageImplObtain = logBuffer.obtain("ShadeListBuilder", logLevel, c01542);
        logMessageImplObtain.setStr1(filterName);
        logMessageImplObtain.setInt1(i);
        logBuffer.push(logMessageImplObtain);
    }

    public final void logPromoterInvalidated(@NotNull String name, int i) {
        Intrinsics.checkParameterIsNotNull(name, "name");
        LogBuffer logBuffer = this.buffer;
        LogLevel logLevel = LogLevel.DEBUG;
        C01562 c01562 = new Function1<LogMessage, String>() { // from class: com.android.systemui.statusbar.notification.collection.listbuilder.ShadeListBuilderLogger.logPromoterInvalidated.2
            @Override // kotlin.jvm.functions.Function1
            @NotNull
            public final String invoke(@NotNull LogMessage receiver) {
                Intrinsics.checkParameterIsNotNull(receiver, "$receiver");
                return "NotifPromoter \"" + receiver.getStr1() + "\" invalidated; pipeline state is " + receiver.getInt1();
            }
        };
        if (logBuffer.getFrozen()) {
            return;
        }
        LogMessageImpl logMessageImplObtain = logBuffer.obtain("ShadeListBuilder", logLevel, c01562);
        logMessageImplObtain.setStr1(name);
        logMessageImplObtain.setInt1(i);
        logBuffer.push(logMessageImplObtain);
    }

    public final void logNotifSectionInvalidated(@NotNull String name, int i) {
        Intrinsics.checkParameterIsNotNull(name, "name");
        LogBuffer logBuffer = this.buffer;
        LogLevel logLevel = LogLevel.DEBUG;
        C01512 c01512 = new Function1<LogMessage, String>() { // from class: com.android.systemui.statusbar.notification.collection.listbuilder.ShadeListBuilderLogger.logNotifSectionInvalidated.2
            @Override // kotlin.jvm.functions.Function1
            @NotNull
            public final String invoke(@NotNull LogMessage receiver) {
                Intrinsics.checkParameterIsNotNull(receiver, "$receiver");
                return "NotifSection \"" + receiver.getStr1() + "\" invalidated; pipeline state is " + receiver.getInt1();
            }
        };
        if (logBuffer.getFrozen()) {
            return;
        }
        LogMessageImpl logMessageImplObtain = logBuffer.obtain("ShadeListBuilder", logLevel, c01512);
        logMessageImplObtain.setStr1(name);
        logMessageImplObtain.setInt1(i);
        logBuffer.push(logMessageImplObtain);
    }

    public final void logFinalizeFilterInvalidated(@NotNull String name, int i) {
        Intrinsics.checkParameterIsNotNull(name, "name");
        LogBuffer logBuffer = this.buffer;
        LogLevel logLevel = LogLevel.DEBUG;
        C01502 c01502 = new Function1<LogMessage, String>() { // from class: com.android.systemui.statusbar.notification.collection.listbuilder.ShadeListBuilderLogger.logFinalizeFilterInvalidated.2
            @Override // kotlin.jvm.functions.Function1
            @NotNull
            public final String invoke(@NotNull LogMessage receiver) {
                Intrinsics.checkParameterIsNotNull(receiver, "$receiver");
                return "Finalize NotifFilter \"" + receiver.getStr1() + "\" invalidated; pipeline state is " + receiver.getInt1();
            }
        };
        if (logBuffer.getFrozen()) {
            return;
        }
        LogMessageImpl logMessageImplObtain = logBuffer.obtain("ShadeListBuilder", logLevel, c01502);
        logMessageImplObtain.setStr1(name);
        logMessageImplObtain.setInt1(i);
        logBuffer.push(logMessageImplObtain);
    }

    public final void logDuplicateSummary(int i, @NotNull String groupKey, @NotNull String existingKey, @NotNull String newKey) {
        Intrinsics.checkParameterIsNotNull(groupKey, "groupKey");
        Intrinsics.checkParameterIsNotNull(existingKey, "existingKey");
        Intrinsics.checkParameterIsNotNull(newKey, "newKey");
        LogBuffer logBuffer = this.buffer;
        LogLevel logLevel = LogLevel.WARNING;
        AnonymousClass2 anonymousClass2 = new Function1<LogMessage, String>() { // from class: com.android.systemui.statusbar.notification.collection.listbuilder.ShadeListBuilderLogger.logDuplicateSummary.2
            @Override // kotlin.jvm.functions.Function1
            @NotNull
            public final String invoke(@NotNull LogMessage receiver) {
                Intrinsics.checkParameterIsNotNull(receiver, "$receiver");
                return "(Build " + receiver.getInt1() + ") Duplicate summary for group \"" + receiver.getStr1() + "\": \"" + receiver.getStr2() + "\" vs. \"" + receiver.getStr3() + '\"';
            }
        };
        if (logBuffer.getFrozen()) {
            return;
        }
        LogMessageImpl logMessageImplObtain = logBuffer.obtain("ShadeListBuilder", logLevel, anonymousClass2);
        logMessageImplObtain.setInt1(i);
        logMessageImplObtain.setStr1(groupKey);
        logMessageImplObtain.setStr2(existingKey);
        logMessageImplObtain.setStr3(newKey);
        logBuffer.push(logMessageImplObtain);
    }

    public final void logDuplicateTopLevelKey(int i, @NotNull String topLevelKey) {
        Intrinsics.checkParameterIsNotNull(topLevelKey, "topLevelKey");
        LogBuffer logBuffer = this.buffer;
        LogLevel logLevel = LogLevel.WARNING;
        C01452 c01452 = new Function1<LogMessage, String>() { // from class: com.android.systemui.statusbar.notification.collection.listbuilder.ShadeListBuilderLogger.logDuplicateTopLevelKey.2
            @Override // kotlin.jvm.functions.Function1
            @NotNull
            public final String invoke(@NotNull LogMessage receiver) {
                Intrinsics.checkParameterIsNotNull(receiver, "$receiver");
                return "(Build " + receiver.getInt1() + ") Duplicate top-level key: " + receiver.getStr1();
            }
        };
        if (logBuffer.getFrozen()) {
            return;
        }
        LogMessageImpl logMessageImplObtain = logBuffer.obtain("ShadeListBuilder", logLevel, c01452);
        logMessageImplObtain.setInt1(i);
        logMessageImplObtain.setStr1(topLevelKey);
        logBuffer.push(logMessageImplObtain);
    }

    public final void logEntryAttachStateChanged(int i, @NotNull String key, @Nullable GroupEntry groupEntry, @Nullable GroupEntry groupEntry2) {
        Intrinsics.checkParameterIsNotNull(key, "key");
        LogBuffer logBuffer = this.buffer;
        LogLevel logLevel = LogLevel.INFO;
        C01472 c01472 = new Function1<LogMessage, String>() { // from class: com.android.systemui.statusbar.notification.collection.listbuilder.ShadeListBuilderLogger.logEntryAttachStateChanged.2
            @Override // kotlin.jvm.functions.Function1
            @NotNull
            public final String invoke(@NotNull LogMessage receiver) {
                Intrinsics.checkParameterIsNotNull(receiver, "$receiver");
                if (receiver.getStr2() == null && receiver.getStr3() != null) {
                    return "(Build " + receiver.getInt1() + ") ATTACHED {" + receiver.getStr1() + '}';
                }
                if (receiver.getStr2() != null && receiver.getStr3() == null) {
                    return "(Build " + receiver.getInt1() + ") DETACHED {" + receiver.getStr1() + '}';
                }
                return "(Build " + receiver.getInt1() + ") MODIFIED {" + receiver.getStr1() + '}';
            }
        };
        if (logBuffer.getFrozen()) {
            return;
        }
        LogMessageImpl logMessageImplObtain = logBuffer.obtain("ShadeListBuilder", logLevel, c01472);
        logMessageImplObtain.setInt1(i);
        logMessageImplObtain.setStr1(key);
        logMessageImplObtain.setStr2(groupEntry != null ? groupEntry.getKey() : null);
        logMessageImplObtain.setStr3(groupEntry2 != null ? groupEntry2.getKey() : null);
        logBuffer.push(logMessageImplObtain);
    }

    public final void logParentChanged(int i, @Nullable GroupEntry groupEntry, @Nullable GroupEntry groupEntry2) {
        LogBuffer logBuffer = this.buffer;
        LogLevel logLevel = LogLevel.INFO;
        C01532 c01532 = new Function1<LogMessage, String>() { // from class: com.android.systemui.statusbar.notification.collection.listbuilder.ShadeListBuilderLogger.logParentChanged.2
            @Override // kotlin.jvm.functions.Function1
            @NotNull
            public final String invoke(@NotNull LogMessage receiver) {
                Intrinsics.checkParameterIsNotNull(receiver, "$receiver");
                if (receiver.getStr1() == null && receiver.getStr2() != null) {
                    return "(Build " + receiver.getInt1() + ")     Parent is {" + receiver.getStr2() + '}';
                }
                if (receiver.getStr1() != null && receiver.getStr2() == null) {
                    return "(Build " + receiver.getInt1() + ")     Parent was {" + receiver.getStr1() + '}';
                }
                return "(Build " + receiver.getInt1() + ")     Reparent: {" + receiver.getStr2() + "} -> {" + receiver.getStr3() + '}';
            }
        };
        if (logBuffer.getFrozen()) {
            return;
        }
        LogMessageImpl logMessageImplObtain = logBuffer.obtain("ShadeListBuilder", logLevel, c01532);
        logMessageImplObtain.setInt1(i);
        logMessageImplObtain.setStr1(groupEntry != null ? groupEntry.getKey() : null);
        logMessageImplObtain.setStr2(groupEntry2 != null ? groupEntry2.getKey() : null);
        logBuffer.push(logMessageImplObtain);
    }

    public final void logFilterChanged(int i, @Nullable NotifFilter notifFilter, @Nullable NotifFilter notifFilter2) {
        LogBuffer logBuffer = this.buffer;
        LogLevel logLevel = LogLevel.INFO;
        C01482 c01482 = new Function1<LogMessage, String>() { // from class: com.android.systemui.statusbar.notification.collection.listbuilder.ShadeListBuilderLogger.logFilterChanged.2
            @Override // kotlin.jvm.functions.Function1
            @NotNull
            public final String invoke(@NotNull LogMessage receiver) {
                Intrinsics.checkParameterIsNotNull(receiver, "$receiver");
                return "(Build " + receiver.getInt1() + ")     Filter changed: " + receiver.getStr1() + " -> " + receiver.getStr2();
            }
        };
        if (logBuffer.getFrozen()) {
            return;
        }
        LogMessageImpl logMessageImplObtain = logBuffer.obtain("ShadeListBuilder", logLevel, c01482);
        logMessageImplObtain.setInt1(i);
        logMessageImplObtain.setStr1(notifFilter != null ? notifFilter.getName() : null);
        logMessageImplObtain.setStr2(notifFilter2 != null ? notifFilter2.getName() : null);
        logBuffer.push(logMessageImplObtain);
    }

    public final void logPromoterChanged(int i, @Nullable NotifPromoter notifPromoter, @Nullable NotifPromoter notifPromoter2) {
        LogBuffer logBuffer = this.buffer;
        LogLevel logLevel = LogLevel.INFO;
        C01552 c01552 = new Function1<LogMessage, String>() { // from class: com.android.systemui.statusbar.notification.collection.listbuilder.ShadeListBuilderLogger.logPromoterChanged.2
            @Override // kotlin.jvm.functions.Function1
            @NotNull
            public final String invoke(@NotNull LogMessage receiver) {
                Intrinsics.checkParameterIsNotNull(receiver, "$receiver");
                return "(Build " + receiver.getInt1() + ")     Promoter changed: " + receiver.getStr1() + " -> " + receiver.getStr2();
            }
        };
        if (logBuffer.getFrozen()) {
            return;
        }
        LogMessageImpl logMessageImplObtain = logBuffer.obtain("ShadeListBuilder", logLevel, c01552);
        logMessageImplObtain.setInt1(i);
        logMessageImplObtain.setStr1(notifPromoter != null ? notifPromoter.getName() : null);
        logMessageImplObtain.setStr2(notifPromoter2 != null ? notifPromoter2.getName() : null);
        logBuffer.push(logMessageImplObtain);
    }

    public final void logSectionChanged(int i, @Nullable NotifSection notifSection, int i2, @Nullable NotifSection notifSection2, int i3) {
        LogBuffer logBuffer = this.buffer;
        LogLevel logLevel = LogLevel.INFO;
        C01572 c01572 = new Function1<LogMessage, String>() { // from class: com.android.systemui.statusbar.notification.collection.listbuilder.ShadeListBuilderLogger.logSectionChanged.2
            @Override // kotlin.jvm.functions.Function1
            @NotNull
            public final String invoke(@NotNull LogMessage receiver) {
                Intrinsics.checkParameterIsNotNull(receiver, "$receiver");
                if (receiver.getStr1() == null) {
                    return "(Build " + receiver.getLong1() + ")     Section assigned: '" + receiver.getStr2() + "' (#" + receiver.getInt2() + ')';
                }
                return "(Build " + receiver.getLong1() + ")     Section changed: '" + receiver.getStr1() + "' (#" + receiver.getInt1() + ") -> '" + receiver.getStr2() + "' (#" + receiver.getInt2() + ')';
            }
        };
        if (logBuffer.getFrozen()) {
            return;
        }
        LogMessageImpl logMessageImplObtain = logBuffer.obtain("ShadeListBuilder", logLevel, c01572);
        logMessageImplObtain.setLong1(i);
        logMessageImplObtain.setStr1(notifSection != null ? notifSection.getName() : null);
        logMessageImplObtain.setInt1(i2);
        logMessageImplObtain.setStr2(notifSection2 != null ? notifSection2.getName() : null);
        logMessageImplObtain.setInt2(i3);
        logBuffer.push(logMessageImplObtain);
    }

    public final void logFinalList(@NotNull List<? extends ListEntry> entries) {
        Intrinsics.checkParameterIsNotNull(entries, "entries");
        if (entries.isEmpty()) {
            LogBuffer logBuffer = this.buffer;
            LogLevel logLevel = LogLevel.DEBUG;
            C01492 c01492 = new Function1<LogMessage, String>() { // from class: com.android.systemui.statusbar.notification.collection.listbuilder.ShadeListBuilderLogger.logFinalList.2
                @Override // kotlin.jvm.functions.Function1
                @NotNull
                public final String invoke(@NotNull LogMessage receiver) {
                    Intrinsics.checkParameterIsNotNull(receiver, "$receiver");
                    return "(empty list)";
                }
            };
            if (!logBuffer.getFrozen()) {
                logBuffer.push(logBuffer.obtain("ShadeListBuilder", logLevel, c01492));
            }
        }
        int size = entries.size();
        for (int i = 0; i < size; i++) {
            ListEntry listEntry = entries.get(i);
            LogBuffer logBuffer2 = this.buffer;
            LogLevel logLevel2 = LogLevel.DEBUG;
            AnonymousClass4 anonymousClass4 = new Function1<LogMessage, String>() { // from class: com.android.systemui.statusbar.notification.collection.listbuilder.ShadeListBuilderLogger.logFinalList.4
                @Override // kotlin.jvm.functions.Function1
                @NotNull
                public final String invoke(@NotNull LogMessage receiver) {
                    Intrinsics.checkParameterIsNotNull(receiver, "$receiver");
                    return '[' + receiver.getInt1() + "] " + receiver.getStr1();
                }
            };
            if (!logBuffer2.getFrozen()) {
                LogMessageImpl logMessageImplObtain = logBuffer2.obtain("ShadeListBuilder", logLevel2, anonymousClass4);
                logMessageImplObtain.setInt1(i);
                logMessageImplObtain.setStr1(listEntry.getKey());
                logBuffer2.push(logMessageImplObtain);
            }
            if (listEntry instanceof GroupEntry) {
                GroupEntry groupEntry = (GroupEntry) listEntry;
                NotificationEntry it = groupEntry.getSummary();
                if (it != null) {
                    LogBuffer logBuffer3 = this.buffer;
                    ShadeListBuilderLogger$logFinalList$5$2 shadeListBuilderLogger$logFinalList$5$2 = new Function1<LogMessage, String>() { // from class: com.android.systemui.statusbar.notification.collection.listbuilder.ShadeListBuilderLogger$logFinalList$5$2
                        @Override // kotlin.jvm.functions.Function1
                        @NotNull
                        public final String invoke(@NotNull LogMessage receiver) {
                            Intrinsics.checkParameterIsNotNull(receiver, "$receiver");
                            return "  [*] " + receiver.getStr1() + " (summary)";
                        }
                    };
                    if (!logBuffer3.getFrozen()) {
                        LogMessageImpl logMessageImplObtain2 = logBuffer3.obtain("ShadeListBuilder", logLevel2, shadeListBuilderLogger$logFinalList$5$2);
                        Intrinsics.checkExpressionValueIsNotNull(it, "it");
                        logMessageImplObtain2.setStr1(it.getKey());
                        logBuffer3.push(logMessageImplObtain2);
                    }
                }
                List<NotificationEntry> children = groupEntry.getChildren();
                Intrinsics.checkExpressionValueIsNotNull(children, "entry.children");
                int size2 = children.size();
                for (int i2 = 0; i2 < size2; i2++) {
                    NotificationEntry child = groupEntry.getChildren().get(i2);
                    LogBuffer logBuffer4 = this.buffer;
                    LogLevel logLevel3 = LogLevel.DEBUG;
                    AnonymousClass7 anonymousClass7 = new Function1<LogMessage, String>() { // from class: com.android.systemui.statusbar.notification.collection.listbuilder.ShadeListBuilderLogger.logFinalList.7
                        @Override // kotlin.jvm.functions.Function1
                        @NotNull
                        public final String invoke(@NotNull LogMessage receiver) {
                            Intrinsics.checkParameterIsNotNull(receiver, "$receiver");
                            return "  [" + receiver.getInt1() + "] " + receiver.getStr1();
                        }
                    };
                    if (!logBuffer4.getFrozen()) {
                        LogMessageImpl logMessageImplObtain3 = logBuffer4.obtain("ShadeListBuilder", logLevel3, anonymousClass7);
                        logMessageImplObtain3.setInt1(i2);
                        Intrinsics.checkExpressionValueIsNotNull(child, "child");
                        logMessageImplObtain3.setStr1(child.getKey());
                        logBuffer4.push(logMessageImplObtain3);
                    }
                }
            }
        }
    }
}
