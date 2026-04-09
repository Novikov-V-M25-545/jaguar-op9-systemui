package com.google.android.systemui.assist.uihints;

import android.content.ComponentName;
import android.content.Context;
import com.android.internal.app.AssistUtils;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/* loaded from: classes.dex */
public class AssistantPresenceHandler {
    private final AssistUtils mAssistUtils;
    private final Set<AssistantPresenceChangeListener> mAssistantPresenceChangeListeners = new HashSet();
    private boolean mGoogleIsAssistant;

    public interface AssistantPresenceChangeListener {
        void onAssistantPresenceChanged(boolean z);
    }

    AssistantPresenceHandler(Context context, AssistUtils assistUtils) {
        this.mAssistUtils = assistUtils;
    }

    public void registerAssistantPresenceChangeListener(AssistantPresenceChangeListener assistantPresenceChangeListener) {
        this.mAssistantPresenceChangeListeners.add(assistantPresenceChangeListener);
    }

    public void requestAssistantPresenceUpdate() {
        updateAssistantPresence(fetchIsGoogleAssistant());
    }

    private void updateAssistantPresence(boolean z) {
        if (this.mGoogleIsAssistant != z) {
            this.mGoogleIsAssistant = z;
            Iterator<AssistantPresenceChangeListener> it = this.mAssistantPresenceChangeListeners.iterator();
            while (it.hasNext()) {
                it.next().onAssistantPresenceChanged(this.mGoogleIsAssistant);
            }
        }
    }

    private boolean fetchIsGoogleAssistant() {
        ComponentName assistComponentForUser = this.mAssistUtils.getAssistComponentForUser(-2);
        return assistComponentForUser != null && "com.google.android.googlequicksearchbox/com.google.android.voiceinteraction.GsaVoiceInteractionService".equals(assistComponentForUser.flattenToString());
    }
}
