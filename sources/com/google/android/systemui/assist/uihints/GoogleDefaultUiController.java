package com.google.android.systemui.assist.uihints;

import android.content.Context;
import com.android.systemui.assist.AssistLogger;
import com.android.systemui.assist.ui.DefaultUiController;

/* loaded from: classes.dex */
public class GoogleDefaultUiController extends DefaultUiController {
    public GoogleDefaultUiController(Context context, AssistLogger assistLogger) {
        super(context, assistLogger);
        setGoogleAssistant(false);
    }

    public void setGoogleAssistant(boolean z) {
        ((AssistantInvocationLightsView) this.mInvocationLightsView).setGoogleAssistant(z);
    }
}
