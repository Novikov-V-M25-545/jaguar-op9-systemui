package com.android.systemui.screenshot;

import android.app.PendingIntent;
import android.content.Context;
import android.graphics.drawable.Icon;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.systemui.R;

/* loaded from: classes.dex */
public class ScreenshotActionChip extends FrameLayout {
    private ImageView mIcon;
    private int mIconColor;
    private TextView mText;

    public ScreenshotActionChip(Context context) {
        this(context, null);
    }

    public ScreenshotActionChip(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public ScreenshotActionChip(Context context, AttributeSet attributeSet, int i) {
        this(context, attributeSet, i, 0);
    }

    public ScreenshotActionChip(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
        this.mIconColor = context.getColor(R.color.global_screenshot_button_icon);
    }

    @Override // android.view.View
    protected void onFinishInflate() {
        this.mIcon = (ImageView) findViewById(R.id.screenshot_action_chip_icon);
        this.mText = (TextView) findViewById(R.id.screenshot_action_chip_text);
    }

    void setIcon(Icon icon, boolean z) {
        this.mIcon.setImageIcon(icon);
        if (z) {
            return;
        }
        this.mIcon.setImageTintList(null);
    }

    void setText(CharSequence charSequence) {
        this.mText.setText(charSequence);
    }

    void setPendingIntent(final PendingIntent pendingIntent, final Runnable runnable) {
        setOnClickListener(new View.OnClickListener() { // from class: com.android.systemui.screenshot.ScreenshotActionChip$$ExternalSyntheticLambda0
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) throws PendingIntent.CanceledException {
                ScreenshotActionChip.lambda$setPendingIntent$0(pendingIntent, runnable, view);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static /* synthetic */ void lambda$setPendingIntent$0(PendingIntent pendingIntent, Runnable runnable, View view) throws PendingIntent.CanceledException {
        try {
            pendingIntent.send();
            runnable.run();
        } catch (PendingIntent.CanceledException e) {
            Log.e("ScreenshotActionChip", "Intent cancelled", e);
        }
    }
}
