package com.android.systemui.glwallpaper;

import android.opengl.EGL14;
import android.opengl.EGLConfig;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLSurface;
import android.opengl.GLUtils;
import android.text.TextUtils;
import android.util.Log;
import android.view.SurfaceHolder;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/* loaded from: classes.dex */
public class EglHelper {
    private static final String TAG = "EglHelper";
    private EGLConfig mEglConfig;
    private EGLContext mEglContext;
    private EGLDisplay mEglDisplay;
    private boolean mEglReady;
    private EGLSurface mEglSurface;
    private final int[] mEglVersion = new int[2];
    private final Set<String> mExts = new HashSet();

    public EglHelper() {
        connectDisplay();
    }

    public boolean init(SurfaceHolder surfaceHolder, boolean z) {
        if (!hasEglDisplay() && !connectDisplay()) {
            Log.w(TAG, "Can not connect display, abort!");
            return false;
        }
        EGLDisplay eGLDisplay = this.mEglDisplay;
        int[] iArr = this.mEglVersion;
        if (!EGL14.eglInitialize(eGLDisplay, iArr, 0, iArr, 1)) {
            Log.w(TAG, "eglInitialize failed: " + GLUtils.getEGLErrorString(EGL14.eglGetError()));
            return false;
        }
        EGLConfig eGLConfigChooseEglConfig = chooseEglConfig();
        this.mEglConfig = eGLConfigChooseEglConfig;
        if (eGLConfigChooseEglConfig == null) {
            Log.w(TAG, "eglConfig not initialized!");
            return false;
        }
        if (!createEglContext()) {
            Log.w(TAG, "Can't create EGLContext!");
            return false;
        }
        if (!createEglSurface(surfaceHolder, z)) {
            Log.w(TAG, "Can't create EGLSurface!");
            return false;
        }
        this.mEglReady = true;
        return true;
    }

    private boolean connectDisplay() {
        this.mExts.clear();
        this.mEglDisplay = EGL14.eglGetDisplay(0);
        if (!hasEglDisplay()) {
            Log.w(TAG, "eglGetDisplay failed: " + GLUtils.getEGLErrorString(EGL14.eglGetError()));
            return false;
        }
        String strEglQueryString = EGL14.eglQueryString(this.mEglDisplay, 12373);
        if (TextUtils.isEmpty(strEglQueryString)) {
            return true;
        }
        Collections.addAll(this.mExts, strEglQueryString.split(" "));
        return true;
    }

    boolean checkExtensionCapability(String str) {
        return this.mExts.contains(str);
    }

    int getWcgCapability() {
        return checkExtensionCapability("EGL_EXT_gl_colorspace_display_p3_passthrough") ? 13456 : 0;
    }

    private EGLConfig chooseEglConfig() {
        int[] iArr = new int[1];
        EGLConfig[] eGLConfigArr = new EGLConfig[1];
        if (!EGL14.eglChooseConfig(this.mEglDisplay, getConfig(), 0, eGLConfigArr, 0, 1, iArr, 0)) {
            Log.w(TAG, "eglChooseConfig failed: " + GLUtils.getEGLErrorString(EGL14.eglGetError()));
            return null;
        }
        if (iArr[0] <= 0) {
            Log.w(TAG, "eglChooseConfig failed, invalid configs count: " + iArr[0]);
            return null;
        }
        return eGLConfigArr[0];
    }

    private int[] getConfig() {
        return new int[]{12324, 8, 12323, 8, 12322, 8, 12321, 0, 12325, 0, 12326, 0, 12352, 4, 12327, 12344, 12344};
    }

    public boolean createEglSurface(SurfaceHolder surfaceHolder, boolean z) {
        String str = TAG;
        Log.d(str, "createEglSurface start");
        if (hasEglDisplay() && surfaceHolder.getSurface().isValid()) {
            int[] iArr = null;
            int wcgCapability = getWcgCapability();
            if (z && checkExtensionCapability("EGL_KHR_gl_colorspace") && wcgCapability > 0) {
                iArr = new int[]{12445, wcgCapability, 12344};
            }
            this.mEglSurface = askCreatingEglWindowSurface(surfaceHolder, iArr, 0);
            if (!hasEglSurface()) {
                Log.w(str, "createWindowSurface failed: " + GLUtils.getEGLErrorString(EGL14.eglGetError()));
                return false;
            }
            EGLDisplay eGLDisplay = this.mEglDisplay;
            EGLSurface eGLSurface = this.mEglSurface;
            if (!EGL14.eglMakeCurrent(eGLDisplay, eGLSurface, eGLSurface, this.mEglContext)) {
                Log.w(str, "eglMakeCurrent failed: " + GLUtils.getEGLErrorString(EGL14.eglGetError()));
                return false;
            }
            Log.d(str, "createEglSurface done");
            return true;
        }
        Log.w(str, "Create EglSurface failed: hasEglDisplay=" + hasEglDisplay() + ", has valid surface=" + surfaceHolder.getSurface().isValid());
        return false;
    }

    EGLSurface askCreatingEglWindowSurface(SurfaceHolder surfaceHolder, int[] iArr, int i) {
        return EGL14.eglCreateWindowSurface(this.mEglDisplay, this.mEglConfig, surfaceHolder, iArr, i);
    }

    public void destroyEglSurface() {
        if (hasEglSurface()) {
            EGLDisplay eGLDisplay = this.mEglDisplay;
            EGLSurface eGLSurface = EGL14.EGL_NO_SURFACE;
            EGL14.eglMakeCurrent(eGLDisplay, eGLSurface, eGLSurface, EGL14.EGL_NO_CONTEXT);
            EGL14.eglDestroySurface(this.mEglDisplay, this.mEglSurface);
            this.mEglSurface = EGL14.EGL_NO_SURFACE;
        }
    }

    public boolean hasEglSurface() {
        EGLSurface eGLSurface = this.mEglSurface;
        return (eGLSurface == null || eGLSurface == EGL14.EGL_NO_SURFACE) ? false : true;
    }

    public boolean createEglContext() {
        String str = TAG;
        Log.d(str, "createEglContext start");
        int[] iArr = new int[5];
        iArr[0] = 12440;
        char c = 2;
        iArr[1] = 2;
        if (checkExtensionCapability("EGL_IMG_context_priority")) {
            iArr[2] = 12544;
            c = 4;
            iArr[3] = 12547;
        }
        iArr[c] = 12344;
        if (hasEglDisplay()) {
            this.mEglContext = EGL14.eglCreateContext(this.mEglDisplay, this.mEglConfig, EGL14.EGL_NO_CONTEXT, iArr, 0);
            if (!hasEglContext()) {
                Log.w(str, "eglCreateContext failed: " + GLUtils.getEGLErrorString(EGL14.eglGetError()));
                return false;
            }
            Log.d(str, "createEglContext done");
            return true;
        }
        Log.w(str, "mEglDisplay is null");
        return false;
    }

    public void destroyEglContext() {
        if (hasEglContext()) {
            EGL14.eglDestroyContext(this.mEglDisplay, this.mEglContext);
            this.mEglContext = EGL14.EGL_NO_CONTEXT;
        }
    }

    public boolean hasEglContext() {
        EGLContext eGLContext = this.mEglContext;
        return (eGLContext == null || eGLContext == EGL14.EGL_NO_CONTEXT) ? false : true;
    }

    public boolean hasEglDisplay() {
        EGLDisplay eGLDisplay = this.mEglDisplay;
        return (eGLDisplay == null || eGLDisplay == EGL14.EGL_NO_DISPLAY) ? false : true;
    }

    public boolean swapBuffer() {
        boolean zEglSwapBuffers = EGL14.eglSwapBuffers(this.mEglDisplay, this.mEglSurface);
        int iEglGetError = EGL14.eglGetError();
        if (iEglGetError != 12288) {
            Log.w(TAG, "eglSwapBuffers failed: " + GLUtils.getEGLErrorString(iEglGetError));
        }
        return zEglSwapBuffers;
    }

    public void finish() {
        if (hasEglSurface()) {
            destroyEglSurface();
        }
        if (hasEglContext()) {
            destroyEglContext();
        }
        if (hasEglDisplay()) {
            terminateEglDisplay();
        }
        this.mEglReady = false;
    }

    void terminateEglDisplay() {
        EGL14.eglTerminate(this.mEglDisplay);
        this.mEglDisplay = EGL14.EGL_NO_DISPLAY;
    }

    public void dump(String str, FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        StringBuilder sb = new StringBuilder();
        sb.append(this.mEglVersion[0]);
        sb.append(".");
        sb.append(this.mEglVersion[1]);
        String string = sb.toString();
        printWriter.print(str);
        printWriter.print("EGL version=");
        printWriter.print(string);
        printWriter.print(", ");
        printWriter.print("EGL ready=");
        printWriter.print(this.mEglReady);
        printWriter.print(", ");
        printWriter.print("has EglContext=");
        printWriter.print(hasEglContext());
        printWriter.print(", ");
        printWriter.print("has EglSurface=");
        printWriter.println(hasEglSurface());
        int[] config = getConfig();
        StringBuilder sb2 = new StringBuilder();
        sb2.append('{');
        for (int i : config) {
            sb2.append("0x");
            sb2.append(Integer.toHexString(i));
            sb2.append(",");
        }
        sb2.setCharAt(sb2.length() - 1, '}');
        printWriter.print(str);
        printWriter.print("EglConfig=");
        printWriter.println(sb2.toString());
    }
}
