package com.android.systemui.glwallpaper;

import android.content.Context;
import android.content.res.Resources;
import android.opengl.GLES20;
import android.util.Log;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/* loaded from: classes.dex */
class ImageGLProgram {
    private static final String TAG = "ImageGLProgram";
    private Context mContext;
    private int mProgramHandle;

    ImageGLProgram(Context context) {
        this.mContext = context.getApplicationContext();
    }

    private int loadShaderProgram(int i, int i2) throws IOException {
        return getProgramHandle(getShaderHandle(35633, getShaderResource(i)), getShaderHandle(35632, getShaderResource(i2)));
    }

    private String getShaderResource(int i) throws IOException {
        Resources resources = this.mContext.getResources();
        StringBuilder sb = new StringBuilder();
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(resources.openRawResource(i)));
            while (true) {
                try {
                    String line = bufferedReader.readLine();
                    if (line == null) {
                        break;
                    }
                    sb.append(line);
                    sb.append("\n");
                } catch (Throwable th) {
                    try {
                        bufferedReader.close();
                    } catch (Throwable th2) {
                        th.addSuppressed(th2);
                    }
                    throw th;
                }
            }
            bufferedReader.close();
        } catch (Resources.NotFoundException | IOException e) {
            Log.d(TAG, "Can not read the shader source", e);
            sb = null;
        }
        return sb == null ? "" : sb.toString();
    }

    private int getShaderHandle(int i, String str) {
        int iGlCreateShader = GLES20.glCreateShader(i);
        if (iGlCreateShader == 0) {
            Log.d(TAG, "Create shader failed, type=" + i);
            return 0;
        }
        GLES20.glShaderSource(iGlCreateShader, str);
        GLES20.glCompileShader(iGlCreateShader);
        return iGlCreateShader;
    }

    private int getProgramHandle(int i, int i2) {
        int iGlCreateProgram = GLES20.glCreateProgram();
        if (iGlCreateProgram == 0) {
            Log.d(TAG, "Can not create OpenGL ES program");
            return 0;
        }
        GLES20.glAttachShader(iGlCreateProgram, i);
        GLES20.glAttachShader(iGlCreateProgram, i2);
        GLES20.glLinkProgram(iGlCreateProgram);
        return iGlCreateProgram;
    }

    boolean useGLProgram(int i, int i2) throws IOException {
        int iLoadShaderProgram = loadShaderProgram(i, i2);
        this.mProgramHandle = iLoadShaderProgram;
        GLES20.glUseProgram(iLoadShaderProgram);
        return true;
    }

    int getAttributeHandle(String str) {
        return GLES20.glGetAttribLocation(this.mProgramHandle, str);
    }

    int getUniformHandle(String str) {
        return GLES20.glGetUniformLocation(this.mProgramHandle, str);
    }
}
