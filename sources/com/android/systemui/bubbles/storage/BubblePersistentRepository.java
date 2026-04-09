package com.android.systemui.bubbles.storage;

import android.content.Context;
import android.util.AtomicFile;
import android.util.Log;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import kotlin.Unit;
import kotlin.collections.CollectionsKt__CollectionsKt;
import kotlin.io.CloseableKt;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: BubblePersistentRepository.kt */
/* loaded from: classes.dex */
public final class BubblePersistentRepository {
    private final AtomicFile bubbleFile;

    public BubblePersistentRepository(@NotNull Context context) {
        Intrinsics.checkParameterIsNotNull(context, "context");
        this.bubbleFile = new AtomicFile(new File(context.getFilesDir(), "overflow_bubbles.xml"), "overflow-bubbles");
    }

    public final boolean persistsToDisk(@NotNull List<BubbleEntity> bubbles) {
        Intrinsics.checkParameterIsNotNull(bubbles, "bubbles");
        synchronized (this.bubbleFile) {
            try {
                FileOutputStream fileOutputStreamStartWrite = this.bubbleFile.startWrite();
                Intrinsics.checkExpressionValueIsNotNull(fileOutputStreamStartWrite, "bubbleFile.startWrite()");
                try {
                    BubbleXmlHelperKt.writeXml(fileOutputStreamStartWrite, bubbles);
                    this.bubbleFile.finishWrite(fileOutputStreamStartWrite);
                } catch (Exception e) {
                    Log.e("BubblePersistentRepository", "Failed to save bubble file, restoring backup", e);
                    this.bubbleFile.failWrite(fileOutputStreamStartWrite);
                    Unit unit = Unit.INSTANCE;
                    return false;
                }
            } catch (IOException e2) {
                Log.e("BubblePersistentRepository", "Failed to save bubble file", e2);
                return false;
            }
        }
        return true;
    }

    @NotNull
    public final List<BubbleEntity> readFromDisk() {
        synchronized (this.bubbleFile) {
            if (!this.bubbleFile.exists()) {
                return CollectionsKt__CollectionsKt.emptyList();
            }
            try {
                FileInputStream fileInputStreamOpenRead = this.bubbleFile.openRead();
                try {
                    List<BubbleEntity> xml = BubbleXmlHelperKt.readXml(fileInputStreamOpenRead);
                    CloseableKt.closeFinally(fileInputStreamOpenRead, null);
                    return xml;
                } finally {
                }
            } catch (Throwable th) {
                Log.e("BubblePersistentRepository", "Failed to open bubble file", th);
                return CollectionsKt__CollectionsKt.emptyList();
            }
        }
    }
}
