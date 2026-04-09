package com.android.systemui.log;

import android.content.ContentResolver;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import java.util.LinkedHashMap;
import java.util.Map;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: LogcatEchoTrackerDebug.kt */
/* loaded from: classes.dex */
public final class LogcatEchoTrackerDebug implements LogcatEchoTracker {
    public static final Factory Factory = new Factory(null);
    private final Map<String, LogLevel> cachedBufferLevels;
    private final Map<String, LogLevel> cachedTagLevels;
    private final ContentResolver contentResolver;

    @NotNull
    public static final LogcatEchoTrackerDebug create(@NotNull ContentResolver contentResolver, @NotNull Looper looper) {
        return Factory.create(contentResolver, looper);
    }

    private LogcatEchoTrackerDebug(ContentResolver contentResolver) {
        this.contentResolver = contentResolver;
        this.cachedBufferLevels = new LinkedHashMap();
        this.cachedTagLevels = new LinkedHashMap();
    }

    public /* synthetic */ LogcatEchoTrackerDebug(ContentResolver contentResolver, DefaultConstructorMarker defaultConstructorMarker) {
        this(contentResolver);
    }

    /* compiled from: LogcatEchoTrackerDebug.kt */
    public static final class Factory {
        private Factory() {
        }

        public /* synthetic */ Factory(DefaultConstructorMarker defaultConstructorMarker) {
            this();
        }

        @NotNull
        public final LogcatEchoTrackerDebug create(@NotNull ContentResolver contentResolver, @NotNull Looper mainLooper) {
            Intrinsics.checkParameterIsNotNull(contentResolver, "contentResolver");
            Intrinsics.checkParameterIsNotNull(mainLooper, "mainLooper");
            LogcatEchoTrackerDebug logcatEchoTrackerDebug = new LogcatEchoTrackerDebug(contentResolver, null);
            logcatEchoTrackerDebug.attach(mainLooper);
            return logcatEchoTrackerDebug;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void attach(final Looper looper) {
        this.contentResolver.registerContentObserver(Settings.Global.getUriFor("systemui/buffer"), true, new ContentObserver(new Handler(looper)) { // from class: com.android.systemui.log.LogcatEchoTrackerDebug.attach.1
            @Override // android.database.ContentObserver
            public void onChange(boolean z, @NotNull Uri uri) {
                Intrinsics.checkParameterIsNotNull(uri, "uri");
                super.onChange(z, uri);
                LogcatEchoTrackerDebug.this.cachedBufferLevels.clear();
            }
        });
        this.contentResolver.registerContentObserver(Settings.Global.getUriFor("systemui/tag"), true, new ContentObserver(new Handler(looper)) { // from class: com.android.systemui.log.LogcatEchoTrackerDebug.attach.2
            @Override // android.database.ContentObserver
            public void onChange(boolean z, @NotNull Uri uri) {
                Intrinsics.checkParameterIsNotNull(uri, "uri");
                super.onChange(z, uri);
                LogcatEchoTrackerDebug.this.cachedTagLevels.clear();
            }
        });
    }

    @Override // com.android.systemui.log.LogcatEchoTracker
    public synchronized boolean isBufferLoggable(@NotNull String bufferName, @NotNull LogLevel level) {
        Intrinsics.checkParameterIsNotNull(bufferName, "bufferName");
        Intrinsics.checkParameterIsNotNull(level, "level");
        return level.ordinal() >= getLogLevel(bufferName, "systemui/buffer", this.cachedBufferLevels).ordinal();
    }

    @Override // com.android.systemui.log.LogcatEchoTracker
    public synchronized boolean isTagLoggable(@NotNull String tagName, @NotNull LogLevel level) {
        Intrinsics.checkParameterIsNotNull(tagName, "tagName");
        Intrinsics.checkParameterIsNotNull(level, "level");
        return level.compareTo(getLogLevel(tagName, "systemui/tag", this.cachedTagLevels)) >= 0;
    }

    private final LogLevel getLogLevel(String str, String str2, Map<String, LogLevel> map) {
        LogLevel logLevel = map.get(str);
        if (logLevel != null) {
            return logLevel;
        }
        LogLevel setting = readSetting(str2 + '/' + str);
        map.put(str, setting);
        return setting;
    }

    private final LogLevel readSetting(String str) {
        try {
            return parseProp(Settings.Global.getString(this.contentResolver, str));
        } catch (Settings.SettingNotFoundException unused) {
            return LogcatEchoTrackerDebugKt.DEFAULT_LEVEL;
        }
    }

    /* JADX WARN: Failed to restore switch over string. Please report as a decompilation issue */
    private final LogLevel parseProp(String str) {
        String lowerCase;
        if (str != null) {
            lowerCase = str.toLowerCase();
            Intrinsics.checkExpressionValueIsNotNull(lowerCase, "(this as java.lang.String).toLowerCase()");
        } else {
            lowerCase = null;
        }
        if (lowerCase != null) {
            switch (lowerCase.hashCode()) {
                case -1408208058:
                    if (lowerCase.equals("assert")) {
                        return LogLevel.WTF;
                    }
                    break;
                case 100:
                    if (lowerCase.equals("d")) {
                        return LogLevel.DEBUG;
                    }
                    break;
                case 101:
                    if (lowerCase.equals("e")) {
                        return LogLevel.ERROR;
                    }
                    break;
                case 105:
                    if (lowerCase.equals("i")) {
                        return LogLevel.INFO;
                    }
                    break;
                case 118:
                    if (lowerCase.equals("v")) {
                        return LogLevel.VERBOSE;
                    }
                    break;
                case 119:
                    if (lowerCase.equals("w")) {
                        return LogLevel.WARNING;
                    }
                    break;
                case 118057:
                    if (lowerCase.equals("wtf")) {
                        return LogLevel.WTF;
                    }
                    break;
                case 3237038:
                    if (lowerCase.equals("info")) {
                        return LogLevel.INFO;
                    }
                    break;
                case 3641990:
                    if (lowerCase.equals("warn")) {
                        return LogLevel.WARNING;
                    }
                    break;
                case 95458899:
                    if (lowerCase.equals("debug")) {
                        return LogLevel.DEBUG;
                    }
                    break;
                case 96784904:
                    if (lowerCase.equals("error")) {
                        return LogLevel.ERROR;
                    }
                    break;
                case 351107458:
                    if (lowerCase.equals("verbose")) {
                        return LogLevel.VERBOSE;
                    }
                    break;
                case 1124446108:
                    if (lowerCase.equals("warning")) {
                        return LogLevel.WARNING;
                    }
                    break;
            }
        }
        return LogcatEchoTrackerDebugKt.DEFAULT_LEVEL;
    }
}
