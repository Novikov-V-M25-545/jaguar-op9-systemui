package com.android.systemui.backup;

import android.app.backup.BackupAgentHelper;
import android.app.backup.BackupDataInputStream;
import android.app.backup.BackupDataOutput;
import android.app.backup.FileBackupHelper;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.os.UserHandle;
import android.util.Log;
import java.util.Arrays;
import java.util.Map;
import kotlin.TuplesKt;
import kotlin.TypeCastException;
import kotlin.Unit;
import kotlin.collections.MapsKt__MapsJVMKt;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: BackupHelper.kt */
/* loaded from: classes.dex */
public final class BackupHelper extends BackupAgentHelper {
    public static final Companion Companion = new Companion(null);

    @NotNull
    private static final Object controlsDataLock = new Object();

    /* compiled from: BackupHelper.kt */
    public static final class Companion {
        private Companion() {
        }

        public /* synthetic */ Companion(DefaultConstructorMarker defaultConstructorMarker) {
            this();
        }

        @NotNull
        public final Object getControlsDataLock() {
            return BackupHelper.controlsDataLock;
        }
    }

    @Override // android.app.backup.BackupAgent
    public void onCreate() {
        super.onCreate();
        addHelper("systemui.files_no_overwrite", new NoOverwriteFileBackupHelper(controlsDataLock, this, MapsKt__MapsJVMKt.mapOf(TuplesKt.to("controls_favorites.xml", BackupHelperKt.getPPControlsFile(this)))));
    }

    @Override // android.app.backup.BackupAgent
    public void onRestoreFinished() {
        super.onRestoreFinished();
        Intent intent = new Intent("com.android.systemui.backup.RESTORE_FINISHED");
        intent.setPackage(getPackageName());
        intent.putExtra("android.intent.extra.USER_ID", getUserId());
        intent.setFlags(1073741824);
        sendBroadcastAsUser(intent, UserHandle.SYSTEM, "com.android.systemui.permission.SELF");
    }

    /* compiled from: BackupHelper.kt */
    private static final class NoOverwriteFileBackupHelper extends FileBackupHelper {

        @NotNull
        private final Context context;

        @NotNull
        private final Map<String, Function0<Unit>> fileNamesAndPostProcess;

        @NotNull
        private final Object lock;

        /* JADX WARN: Illegal instructions before constructor call */
        /* JADX WARN: Multi-variable type inference failed */
        public NoOverwriteFileBackupHelper(@NotNull Object lock, @NotNull Context context, @NotNull Map<String, ? extends Function0<Unit>> fileNamesAndPostProcess) {
            Intrinsics.checkParameterIsNotNull(lock, "lock");
            Intrinsics.checkParameterIsNotNull(context, "context");
            Intrinsics.checkParameterIsNotNull(fileNamesAndPostProcess, "fileNamesAndPostProcess");
            Object[] array = fileNamesAndPostProcess.keySet().toArray(new String[0]);
            if (array == null) {
                throw new TypeCastException("null cannot be cast to non-null type kotlin.Array<T>");
            }
            String[] strArr = (String[]) array;
            super(context, (String[]) Arrays.copyOf(strArr, strArr.length));
            this.lock = lock;
            this.context = context;
            this.fileNamesAndPostProcess = fileNamesAndPostProcess;
        }

        @Override // android.app.backup.FileBackupHelper, android.app.backup.BackupHelper
        public void restoreEntity(@NotNull BackupDataInputStream data) {
            Intrinsics.checkParameterIsNotNull(data, "data");
            if (Environment.buildPath(this.context.getFilesDir(), new String[]{data.getKey()}).exists()) {
                Log.w("BackupHelper", "File " + data.getKey() + " already exists. Skipping restore.");
                return;
            }
            synchronized (this.lock) {
                super.restoreEntity(data);
                Function0<Unit> function0 = this.fileNamesAndPostProcess.get(data.getKey());
                if (function0 != null) {
                    function0.invoke();
                }
            }
        }

        @Override // android.app.backup.FileBackupHelper, android.app.backup.BackupHelper
        public void performBackup(@Nullable ParcelFileDescriptor parcelFileDescriptor, @Nullable BackupDataOutput backupDataOutput, @Nullable ParcelFileDescriptor parcelFileDescriptor2) {
            synchronized (this.lock) {
                super.performBackup(parcelFileDescriptor, backupDataOutput, parcelFileDescriptor2);
                Unit unit = Unit.INSTANCE;
            }
        }
    }
}
