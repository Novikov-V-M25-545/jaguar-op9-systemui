package com.android.systemui.statusbar.notification.row;

import android.app.Dialog;
import android.content.Context;
import android.widget.TextView;
import com.android.systemui.R;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: ChannelEditorDialogController.kt */
/* loaded from: classes.dex */
public final class ChannelEditorDialog extends Dialog {
    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    public ChannelEditorDialog(@NotNull Context context) {
        super(context);
        Intrinsics.checkParameterIsNotNull(context, "context");
    }

    public final void updateDoneButtonText(boolean z) {
        int i;
        TextView textView = (TextView) findViewById(R.id.done_button);
        if (textView != null) {
            if (z) {
                i = R.string.inline_ok_button;
            } else {
                i = R.string.inline_done_button;
            }
            textView.setText(i);
        }
    }

    /* compiled from: ChannelEditorDialogController.kt */
    public static final class Builder {
        private Context context;

        @NotNull
        public final Builder setContext(@NotNull Context context) {
            Intrinsics.checkParameterIsNotNull(context, "context");
            this.context = context;
            return this;
        }

        @NotNull
        public final ChannelEditorDialog build() {
            Context context = this.context;
            if (context == null) {
                Intrinsics.throwUninitializedPropertyAccessException("context");
            }
            return new ChannelEditorDialog(context);
        }
    }
}
