package com.android.systemui.bubbles;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.android.internal.util.ContrastColorUtil;
import com.android.systemui.R;
import com.android.systemui.bubbles.BadgedImageView;
import java.util.List;
import java.util.function.Consumer;

/* compiled from: BubbleOverflowActivity.java */
/* loaded from: classes.dex */
class BubbleOverflowAdapter extends RecyclerView.Adapter<ViewHolder> {
    private List<Bubble> mBubbles;
    private Context mContext;
    private int mHeight;
    private Consumer<Bubble> mPromoteBubbleFromOverflow;
    private int mWidth;

    public BubbleOverflowAdapter(Context context, List<Bubble> list, Consumer<Bubble> consumer, int i, int i2) {
        this.mContext = context;
        this.mBubbles = list;
        this.mPromoteBubbleFromOverflow = consumer;
        this.mWidth = i;
        this.mHeight = i2;
    }

    @Override // androidx.recyclerview.widget.RecyclerView.Adapter
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        LinearLayout linearLayout = (LinearLayout) LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.bubble_overflow_view, viewGroup, false);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-2, -2);
        layoutParams.width = this.mWidth;
        layoutParams.height = this.mHeight;
        linearLayout.setLayoutParams(layoutParams);
        TypedArray typedArrayObtainStyledAttributes = this.mContext.obtainStyledAttributes(new int[]{android.R.attr.colorBackgroundFloating, android.R.attr.textColorPrimary});
        int iEnsureTextContrast = ContrastColorUtil.ensureTextContrast(typedArrayObtainStyledAttributes.getColor(1, -16777216), typedArrayObtainStyledAttributes.getColor(0, -1), true);
        typedArrayObtainStyledAttributes.recycle();
        ((TextView) linearLayout.findViewById(R.id.bubble_view_name)).setTextColor(iEnsureTextContrast);
        return new ViewHolder(linearLayout);
    }

    @Override // androidx.recyclerview.widget.RecyclerView.Adapter
    public void onBindViewHolder(ViewHolder viewHolder, int i) throws Resources.NotFoundException {
        CharSequence appName;
        final Bubble bubble = this.mBubbles.get(i);
        viewHolder.iconView.setRenderedBubble(bubble);
        viewHolder.iconView.removeDotSuppressionFlag(BadgedImageView.SuppressionFlag.FLYOUT_VISIBLE);
        viewHolder.iconView.setOnClickListener(new View.OnClickListener() { // from class: com.android.systemui.bubbles.BubbleOverflowAdapter$$ExternalSyntheticLambda0
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                this.f$0.lambda$onBindViewHolder$0(bubble, view);
            }
        });
        String title = bubble.getTitle();
        if (title == null) {
            title = this.mContext.getResources().getString(R.string.notification_bubble_title);
        }
        viewHolder.iconView.setContentDescription(this.mContext.getResources().getString(R.string.bubble_content_description_single, title, bubble.getAppName()));
        viewHolder.iconView.setAccessibilityDelegate(new View.AccessibilityDelegate() { // from class: com.android.systemui.bubbles.BubbleOverflowAdapter.1
            @Override // android.view.View.AccessibilityDelegate
            public void onInitializeAccessibilityNodeInfo(View view, AccessibilityNodeInfo accessibilityNodeInfo) {
                super.onInitializeAccessibilityNodeInfo(view, accessibilityNodeInfo);
                accessibilityNodeInfo.addAction(new AccessibilityNodeInfo.AccessibilityAction(16, BubbleOverflowAdapter.this.mContext.getResources().getString(R.string.bubble_accessibility_action_add_back)));
            }
        });
        if (bubble.getShortcutInfo() != null) {
            appName = bubble.getShortcutInfo().getLabel();
        } else {
            appName = bubble.getAppName();
        }
        viewHolder.textView.setText(appName);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$onBindViewHolder$0(Bubble bubble, View view) {
        this.mBubbles.remove(bubble);
        notifyDataSetChanged();
        this.mPromoteBubbleFromOverflow.accept(bubble);
    }

    @Override // androidx.recyclerview.widget.RecyclerView.Adapter
    public int getItemCount() {
        return this.mBubbles.size();
    }

    /* compiled from: BubbleOverflowActivity.java */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public BadgedImageView iconView;
        public TextView textView;

        public ViewHolder(LinearLayout linearLayout) {
            super(linearLayout);
            this.iconView = (BadgedImageView) linearLayout.findViewById(R.id.bubble_view);
            this.textView = (TextView) linearLayout.findViewById(R.id.bubble_view_name);
        }
    }
}
