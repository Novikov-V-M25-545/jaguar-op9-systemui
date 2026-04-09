package com.android.systemui.screenrecord;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.Switch;
import com.android.systemui.R;
import com.android.systemui.settings.CurrentUserContextTracker;
import java.util.ArrayList;
import java.util.List;

/* loaded from: classes.dex */
public class ScreenRecordDialog extends Activity {
    private Switch mAudioSwitch;
    private final RecordingController mController;
    private final CurrentUserContextTracker mCurrentUserContextTracker;
    private List<ScreenRecordingAudioSource> mModes;
    private Spinner mOptions;
    private Switch mTapsSwitch;

    public ScreenRecordDialog(RecordingController recordingController, CurrentUserContextTracker currentUserContextTracker) {
        this.mController = recordingController;
        this.mCurrentUserContextTracker = currentUserContextTracker;
    }

    @Override // android.app.Activity
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        Window window = getWindow();
        window.getDecorView();
        window.setLayout(-1, -2);
        window.addPrivateFlags(16);
        window.setGravity(80);
        setTitle(R.string.screenrecord_name);
        setContentView(R.layout.screen_record_dialog);
        ((Button) findViewById(R.id.button_cancel)).setOnClickListener(new View.OnClickListener() { // from class: com.android.systemui.screenrecord.ScreenRecordDialog$$ExternalSyntheticLambda0
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                this.f$0.lambda$onCreate$0(view);
            }
        });
        ((Button) findViewById(R.id.button_start)).setOnClickListener(new View.OnClickListener() { // from class: com.android.systemui.screenrecord.ScreenRecordDialog$$ExternalSyntheticLambda1
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                this.f$0.lambda$onCreate$1(view);
            }
        });
        ArrayList arrayList = new ArrayList();
        this.mModes = arrayList;
        arrayList.add(ScreenRecordingAudioSource.MIC);
        this.mModes.add(ScreenRecordingAudioSource.INTERNAL);
        this.mModes.add(ScreenRecordingAudioSource.MIC_AND_INTERNAL);
        this.mAudioSwitch = (Switch) findViewById(R.id.screenrecord_audio_switch);
        this.mTapsSwitch = (Switch) findViewById(R.id.screenrecord_taps_switch);
        this.mOptions = (Spinner) findViewById(R.id.screen_recording_options);
        ScreenRecordingAdapter screenRecordingAdapter = new ScreenRecordingAdapter(getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, this.mModes);
        screenRecordingAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        this.mOptions.setAdapter((SpinnerAdapter) screenRecordingAdapter);
        this.mOptions.setOnItemClickListenerInt(new AdapterView.OnItemClickListener() { // from class: com.android.systemui.screenrecord.ScreenRecordDialog$$ExternalSyntheticLambda2
            @Override // android.widget.AdapterView.OnItemClickListener
            public final void onItemClick(AdapterView adapterView, View view, int i, long j) {
                this.f$0.lambda$onCreate$2(adapterView, view, i, j);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$onCreate$0(View view) {
        finish();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$onCreate$1(View view) {
        requestScreenCapture();
        finish();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$onCreate$2(AdapterView adapterView, View view, int i, long j) {
        this.mAudioSwitch.setChecked(true);
    }

    private void requestScreenCapture() {
        ScreenRecordingAudioSource screenRecordingAudioSource;
        Context currentUserContext = this.mCurrentUserContextTracker.getCurrentUserContext();
        boolean zIsChecked = this.mTapsSwitch.isChecked();
        if (this.mAudioSwitch.isChecked()) {
            screenRecordingAudioSource = (ScreenRecordingAudioSource) this.mOptions.getSelectedItem();
        } else {
            screenRecordingAudioSource = ScreenRecordingAudioSource.NONE;
        }
        this.mController.startCountdown(3000L, 1000L, PendingIntent.getForegroundService(currentUserContext, 2, RecordingService.getStartIntent(currentUserContext, -1, screenRecordingAudioSource.ordinal(), zIsChecked), 201326592), PendingIntent.getService(currentUserContext, 2, RecordingService.getStopIntent(currentUserContext), 201326592));
    }
}
