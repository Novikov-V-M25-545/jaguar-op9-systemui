package com.android.systemui;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.hardware.camera2.CameraManager;
import android.util.PathParser;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;
import kotlin.TypeCastException;
import kotlin.collections.CollectionsKt___CollectionsKt;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import kotlin.math.MathKt__MathJVMKt;
import kotlin.text.StringsKt__StringsKt;
import org.jetbrains.annotations.NotNull;

/* compiled from: CameraAvailabilityListener.kt */
/* loaded from: classes.dex */
public final class CameraAvailabilityListener {
    public static final Factory Factory = new Factory(null);
    private final CameraManager.AvailabilityCallback availabilityCallback;
    private final CameraManager cameraManager;
    private Rect cutoutBounds;
    private final Path cutoutProtectionPath;
    private final Set<String> excludedPackageIds;
    private final Executor executor;
    private final List<CameraTransitionCallback> listeners;
    private final String targetCameraId;

    /* compiled from: CameraAvailabilityListener.kt */
    public interface CameraTransitionCallback {
        void onApplyCameraProtection(@NotNull Path path, @NotNull Rect rect);

        void onHideCameraProtection();
    }

    public CameraAvailabilityListener(@NotNull CameraManager cameraManager, @NotNull Path cutoutProtectionPath, @NotNull String targetCameraId, @NotNull String excludedPackages, @NotNull Executor executor) {
        Intrinsics.checkParameterIsNotNull(cameraManager, "cameraManager");
        Intrinsics.checkParameterIsNotNull(cutoutProtectionPath, "cutoutProtectionPath");
        Intrinsics.checkParameterIsNotNull(targetCameraId, "targetCameraId");
        Intrinsics.checkParameterIsNotNull(excludedPackages, "excludedPackages");
        Intrinsics.checkParameterIsNotNull(executor, "executor");
        this.cameraManager = cameraManager;
        this.cutoutProtectionPath = cutoutProtectionPath;
        this.targetCameraId = targetCameraId;
        this.executor = executor;
        this.cutoutBounds = new Rect();
        this.listeners = new ArrayList();
        this.availabilityCallback = new CameraManager.AvailabilityCallback() { // from class: com.android.systemui.CameraAvailabilityListener$availabilityCallback$1
            public void onCameraClosed(@NotNull String cameraId) {
                Intrinsics.checkParameterIsNotNull(cameraId, "cameraId");
                if (Intrinsics.areEqual(this.this$0.targetCameraId, cameraId)) {
                    this.this$0.notifyCameraInactive();
                }
            }

            public void onCameraOpened(@NotNull String cameraId, @NotNull String packageId) {
                Intrinsics.checkParameterIsNotNull(cameraId, "cameraId");
                Intrinsics.checkParameterIsNotNull(packageId, "packageId");
                if (!Intrinsics.areEqual(this.this$0.targetCameraId, cameraId) || this.this$0.isExcluded(packageId)) {
                    return;
                }
                this.this$0.notifyCameraActive();
            }
        };
        RectF rectF = new RectF();
        cutoutProtectionPath.computeBounds(rectF, false);
        this.cutoutBounds.set(MathKt__MathJVMKt.roundToInt(rectF.left), MathKt__MathJVMKt.roundToInt(rectF.top), MathKt__MathJVMKt.roundToInt(rectF.right), MathKt__MathJVMKt.roundToInt(rectF.bottom));
        this.excludedPackageIds = CollectionsKt___CollectionsKt.toSet(StringsKt__StringsKt.split$default(excludedPackages, new String[]{","}, false, 0, 6, null));
    }

    public final void startListening() {
        registerCameraListener();
    }

    public final void addTransitionCallback(@NotNull CameraTransitionCallback callback) {
        Intrinsics.checkParameterIsNotNull(callback, "callback");
        this.listeners.add(callback);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final boolean isExcluded(String str) {
        return this.excludedPackageIds.contains(str);
    }

    private final void registerCameraListener() {
        this.cameraManager.registerAvailabilityCallback(this.executor, this.availabilityCallback);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void notifyCameraActive() {
        Iterator<T> it = this.listeners.iterator();
        while (it.hasNext()) {
            ((CameraTransitionCallback) it.next()).onApplyCameraProtection(this.cutoutProtectionPath, this.cutoutBounds);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void notifyCameraInactive() {
        Iterator<T> it = this.listeners.iterator();
        while (it.hasNext()) {
            ((CameraTransitionCallback) it.next()).onHideCameraProtection();
        }
    }

    /* compiled from: CameraAvailabilityListener.kt */
    public static final class Factory {
        private Factory() {
        }

        public /* synthetic */ Factory(DefaultConstructorMarker defaultConstructorMarker) {
            this();
        }

        @NotNull
        public final CameraAvailabilityListener build(@NotNull Context context, @NotNull Executor executor) {
            Intrinsics.checkParameterIsNotNull(context, "context");
            Intrinsics.checkParameterIsNotNull(executor, "executor");
            Object systemService = context.getSystemService("camera");
            if (systemService == null) {
                throw new TypeCastException("null cannot be cast to non-null type android.hardware.camera2.CameraManager");
            }
            CameraManager cameraManager = (CameraManager) systemService;
            Resources resources = context.getResources();
            String pathString = resources.getString(R.string.config_frontBuiltInDisplayCutoutProtection);
            String cameraId = resources.getString(R.string.config_protectedCameraId);
            String excluded = resources.getString(R.string.config_cameraProtectionExcludedPackages);
            Intrinsics.checkExpressionValueIsNotNull(pathString, "pathString");
            Path pathPathFromString = pathFromString(pathString);
            Intrinsics.checkExpressionValueIsNotNull(cameraId, "cameraId");
            Intrinsics.checkExpressionValueIsNotNull(excluded, "excluded");
            return new CameraAvailabilityListener(cameraManager, pathPathFromString, cameraId, excluded, executor);
        }

        private final Path pathFromString(String str) {
            if (str == null) {
                throw new TypeCastException("null cannot be cast to non-null type kotlin.CharSequence");
            }
            try {
                Path pathCreatePathFromPathData = PathParser.createPathFromPathData(StringsKt__StringsKt.trim(str).toString());
                Intrinsics.checkExpressionValueIsNotNull(pathCreatePathFromPathData, "PathParser.createPathFromPathData(spec)");
                return pathCreatePathFromPathData;
            } catch (Throwable th) {
                throw new IllegalArgumentException("Invalid protection path", th);
            }
        }
    }
}
