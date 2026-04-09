package com.android.systemui.statusbar.notification.row;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import com.android.systemui.R;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: ChannelEditorListView.kt */
/* loaded from: classes.dex */
public final class AppControlView extends LinearLayout {

    @NotNull
    public TextView channelName;

    @NotNull
    public ImageView iconView;

    /* renamed from: switch, reason: not valid java name */
    @NotNull
    public Switch f0switch;

    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    public AppControlView(@NotNull Context c, @NotNull AttributeSet attrs) {
        super(c, attrs);
        Intrinsics.checkParameterIsNotNull(c, "c");
        Intrinsics.checkParameterIsNotNull(attrs, "attrs");
    }

    @NotNull
    public final ImageView getIconView() {
        ImageView imageView = this.iconView;
        if (imageView == null) {
            Intrinsics.throwUninitializedPropertyAccessException("iconView");
        }
        return imageView;
    }

    @NotNull
    public final TextView getChannelName() {
        TextView textView = this.channelName;
        if (textView == null) {
            Intrinsics.throwUninitializedPropertyAccessException("channelName");
        }
        return textView;
    }

    @NotNull
    public final Switch getSwitch() {
        Switch r1 = this.f0switch;
        if (r1 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("switch");
        }
        return r1;
    }

    @Override // android.view.View
    protected void onFinishInflate() {
        View viewFindViewById = findViewById(R.id.icon);
        Intrinsics.checkExpressionValueIsNotNull(viewFindViewById, "findViewById(R.id.icon)");
        this.iconView = (ImageView) viewFindViewById;
        View viewFindViewById2 = findViewById(R.id.app_name);
        Intrinsics.checkExpressionValueIsNotNull(viewFindViewById2, "findViewById(R.id.app_name)");
        this.channelName = (TextView) viewFindViewById2;
        View viewFindViewById3 = findViewById(R.id.toggle);
        Intrinsics.checkExpressionValueIsNotNull(viewFindViewById3, "findViewById(R.id.toggle)");
        this.f0switch = (Switch) viewFindViewById3;
        setOnClickListener(new View.OnClickListener() { // from class: com.android.systemui.statusbar.notification.row.AppControlView.onFinishInflate.1
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                AppControlView.this.getSwitch().toggle();
            }
        });
    }
}
