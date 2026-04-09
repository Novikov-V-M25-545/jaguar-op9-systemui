package com.android.systemui.statusbar;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;
import com.android.systemui.Dependency;
import com.android.systemui.plugins.DarkIconDispatcher;
import java.util.ArrayList;
import java.util.Iterator;
import org.lineageos.internal.statusbar.LineageStatusBarItem$DarkReceiver;
import org.lineageos.internal.statusbar.LineageStatusBarItem$VisibilityReceiver;

/* loaded from: classes.dex */
public class LineageStatusBarItemHolder extends RelativeLayout {
    private Context mContext;
    private DarkIconDispatcher.DarkReceiver mDarkReceiver;
    private ArrayList<LineageStatusBarItem$DarkReceiver> mDarkReceivers;
    private boolean mItemHolderIsVisible;
    private Rect mLastArea;
    private float mLastDarkIntensity;
    private int mLastTint;
    private View.OnSystemUiVisibilityChangeListener mSystemUiVisibilityChangeListener;
    private ArrayList<LineageStatusBarItem$VisibilityReceiver> mVisibilityReceivers;

    public LineageStatusBarItemHolder(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public LineageStatusBarItemHolder(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mDarkReceivers = new ArrayList<>();
        this.mVisibilityReceivers = new ArrayList<>();
        this.mDarkReceiver = new DarkIconDispatcher.DarkReceiver() { // from class: com.android.systemui.statusbar.LineageStatusBarItemHolder.1
            @Override // com.android.systemui.plugins.DarkIconDispatcher.DarkReceiver
            public void onDarkChanged(Rect rect, float f, int i2) {
                LineageStatusBarItemHolder.this.mLastArea = rect;
                LineageStatusBarItemHolder.this.mLastDarkIntensity = f;
                LineageStatusBarItemHolder.this.mLastTint = i2;
                Iterator it = LineageStatusBarItemHolder.this.mDarkReceivers.iterator();
                while (it.hasNext()) {
                    ((LineageStatusBarItem$DarkReceiver) it.next()).onDarkChanged(rect, f, i2);
                }
            }
        };
        this.mSystemUiVisibilityChangeListener = new View.OnSystemUiVisibilityChangeListener() { // from class: com.android.systemui.statusbar.LineageStatusBarItemHolder.2
            @Override // android.view.View.OnSystemUiVisibilityChangeListener
            public void onSystemUiVisibilityChange(int i2) {
                LineageStatusBarItemHolder.this.updateStatusBarVisibility(i2);
            }
        };
        this.mContext = context;
        this.mItemHolderIsVisible = false;
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        setOnSystemUiVisibilityChangeListener(this.mSystemUiVisibilityChangeListener);
        updateStatusBarVisibility(getSystemUiVisibility());
        ((DarkIconDispatcher) Dependency.get(DarkIconDispatcher.class)).addDarkReceiver(this.mDarkReceiver);
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        setOnSystemUiVisibilityChangeListener(null);
        ((DarkIconDispatcher) Dependency.get(DarkIconDispatcher.class)).removeDarkReceiver(this.mDarkReceiver);
    }

    @Override // android.view.View
    public void onVisibilityAggregated(boolean z) {
        super.onVisibilityAggregated(z);
        updateVisibilityReceivers(z);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateStatusBarVisibility(int i) {
        boolean z = true;
        if ((i & 4) != 0 && (i & 1) == 0) {
            z = false;
        }
        updateVisibilityReceivers(z);
    }

    private void updateVisibilityReceivers(boolean z) {
        if (z == this.mItemHolderIsVisible) {
            return;
        }
        this.mItemHolderIsVisible = z;
        Iterator<LineageStatusBarItem$VisibilityReceiver> it = this.mVisibilityReceivers.iterator();
        while (it.hasNext()) {
            it.next().onVisibilityChanged(this.mItemHolderIsVisible);
        }
    }
}
