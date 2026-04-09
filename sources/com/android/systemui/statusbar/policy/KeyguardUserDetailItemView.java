package com.android.systemui.statusbar.policy;

import android.content.Context;
import android.util.AttributeSet;
import com.android.systemui.R;
import com.android.systemui.qs.tiles.UserDetailItemView;

/* loaded from: classes.dex */
public class KeyguardUserDetailItemView extends UserDetailItemView {
    public KeyguardUserDetailItemView(Context context) {
        this(context, null);
    }

    public KeyguardUserDetailItemView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public KeyguardUserDetailItemView(Context context, AttributeSet attributeSet, int i) {
        this(context, attributeSet, i, 0);
    }

    public KeyguardUserDetailItemView(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
    }

    @Override // com.android.systemui.qs.tiles.UserDetailItemView
    protected int getFontSizeDimen() {
        return R.dimen.kg_user_switcher_text_size;
    }
}
