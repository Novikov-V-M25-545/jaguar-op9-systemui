package com.android.systemui.backup;

import android.app.job.JobScheduler;
import android.content.Context;
import android.os.Environment;
import com.android.systemui.controls.controller.AuxiliaryPersistenceWrapper;
import java.io.File;
import kotlin.Unit;
import kotlin.io.FilesKt__UtilsKt;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.internal.Intrinsics;

/* compiled from: BackupHelper.kt */
/* loaded from: classes.dex */
public final class BackupHelperKt {
    /* JADX INFO: Access modifiers changed from: private */
    public static final Function0<Unit> getPPControlsFile(final Context context) {
        return new Function0<Unit>() { // from class: com.android.systemui.backup.BackupHelperKt.getPPControlsFile.1
            /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
            {
                super(0);
            }

            @Override // kotlin.jvm.functions.Function0
            public /* bridge */ /* synthetic */ Unit invoke() {
                invoke2();
                return Unit.INSTANCE;
            }

            /* renamed from: invoke, reason: avoid collision after fix types in other method */
            public final void invoke2() {
                File filesDir = context.getFilesDir();
                File file = Environment.buildPath(filesDir, new String[]{"controls_favorites.xml"});
                if (file.exists()) {
                    File dest = Environment.buildPath(filesDir, new String[]{"aux_controls_favorites.xml"});
                    Intrinsics.checkExpressionValueIsNotNull(file, "file");
                    Intrinsics.checkExpressionValueIsNotNull(dest, "dest");
                    FilesKt__UtilsKt.copyTo$default(file, dest, false, 0, 6, null);
                    JobScheduler jobScheduler = (JobScheduler) context.getSystemService(JobScheduler.class);
                    if (jobScheduler != null) {
                        jobScheduler.schedule(AuxiliaryPersistenceWrapper.DeletionJobService.Companion.getJobForContext(context));
                    }
                }
            }
        };
    }
}
