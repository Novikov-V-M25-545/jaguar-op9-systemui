package com.android.systemui.statusbar;

import android.app.ActivityManager;
import android.content.res.Resources;
import android.os.SystemProperties;
import android.util.MathUtils;
import android.view.SurfaceControl;
import android.view.ViewRootImpl;
import com.android.internal.util.IndentingPrintWriter;
import com.android.systemui.Dumpable;
import com.android.systemui.R;
import com.android.systemui.dump.DumpManager;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import kotlin.Unit;
import kotlin.io.CloseableKt;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: BlurUtils.kt */
/* loaded from: classes.dex */
public class BlurUtils implements Dumpable {
    private final boolean blurDisabledSysProp;
    private final boolean blurSupportedSysProp;
    private final int maxBlurRadius;
    private final int minBlurRadius;
    private final Resources resources;

    public BlurUtils(@NotNull Resources resources, @NotNull DumpManager dumpManager) {
        Intrinsics.checkParameterIsNotNull(resources, "resources");
        Intrinsics.checkParameterIsNotNull(dumpManager, "dumpManager");
        this.resources = resources;
        this.minBlurRadius = resources.getDimensionPixelSize(R.dimen.min_window_blur_radius);
        this.maxBlurRadius = resources.getDimensionPixelSize(R.dimen.max_window_blur_radius);
        this.blurSupportedSysProp = SystemProperties.getBoolean("ro.surface_flinger.supports_background_blur", false);
        this.blurDisabledSysProp = SystemProperties.getBoolean("persist.sys.sf.disable_blurs", false);
        String name = BlurUtils.class.getName();
        Intrinsics.checkExpressionValueIsNotNull(name, "javaClass.name");
        dumpManager.registerDumpable(name, this);
    }

    public final int getMinBlurRadius() {
        return this.minBlurRadius;
    }

    public final int getMaxBlurRadius() {
        return this.maxBlurRadius;
    }

    public final int blurRadiusOfRatio(float f) {
        if (f == 0.0f) {
            return 0;
        }
        return (int) MathUtils.lerp(this.minBlurRadius, this.maxBlurRadius, f);
    }

    public final float ratioOfBlurRadius(int i) {
        if (i == 0) {
            return 0.0f;
        }
        return MathUtils.map(this.minBlurRadius, this.maxBlurRadius, 0.0f, 1.0f, i);
    }

    public final void applyBlur(@Nullable ViewRootImpl viewRootImpl, int i) {
        if (viewRootImpl != null) {
            SurfaceControl surfaceControl = viewRootImpl.getSurfaceControl();
            Intrinsics.checkExpressionValueIsNotNull(surfaceControl, "viewRootImpl.surfaceControl");
            if (surfaceControl.isValid() && supportsBlursOnWindows()) {
                SurfaceControl.Transaction transactionCreateTransaction = createTransaction();
                try {
                    transactionCreateTransaction.setBackgroundBlurRadius(viewRootImpl.getSurfaceControl(), i);
                    transactionCreateTransaction.apply();
                    Unit unit = Unit.INSTANCE;
                    CloseableKt.closeFinally(transactionCreateTransaction, null);
                } catch (Throwable th) {
                    try {
                        throw th;
                    } catch (Throwable th2) {
                        CloseableKt.closeFinally(transactionCreateTransaction, th);
                        throw th2;
                    }
                }
            }
        }
    }

    @NotNull
    public SurfaceControl.Transaction createTransaction() {
        return new SurfaceControl.Transaction();
    }

    public boolean supportsBlursOnWindows() {
        return this.blurSupportedSysProp && !this.blurDisabledSysProp && ActivityManager.isHighEndGfx();
    }

    @Override // com.android.systemui.Dumpable
    public void dump(@NotNull FileDescriptor fd, @NotNull PrintWriter pw, @NotNull String[] args) {
        Intrinsics.checkParameterIsNotNull(fd, "fd");
        Intrinsics.checkParameterIsNotNull(pw, "pw");
        Intrinsics.checkParameterIsNotNull(args, "args");
        IndentingPrintWriter indentingPrintWriter = new IndentingPrintWriter(pw, "  ");
        indentingPrintWriter.println("BlurUtils:");
        indentingPrintWriter.increaseIndent();
        indentingPrintWriter.println("minBlurRadius: " + this.minBlurRadius);
        indentingPrintWriter.println("maxBlurRadius: " + this.maxBlurRadius);
        indentingPrintWriter.println("blurSupportedSysProp: " + this.blurSupportedSysProp);
        indentingPrintWriter.println("blurDisabledSysProp: " + this.blurDisabledSysProp);
        indentingPrintWriter.println("supportsBlursOnWindows: " + supportsBlursOnWindows());
    }
}
