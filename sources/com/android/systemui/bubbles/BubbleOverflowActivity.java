package com.android.systemui.bubbles;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.android.internal.util.ContrastColorUtil;
import com.android.systemui.R;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/* loaded from: classes.dex */
public class BubbleOverflowActivity extends Activity {
    private BubbleOverflowAdapter mAdapter;
    private BubbleController mBubbleController;
    private LinearLayout mEmptyState;
    private ImageView mEmptyStateImage;
    private TextView mEmptyStateSubtitle;
    private TextView mEmptyStateTitle;
    private List<Bubble> mOverflowBubbles = new ArrayList();
    private RecyclerView mRecyclerView;

    private class NoScrollGridLayoutManager extends GridLayoutManager {
        NoScrollGridLayoutManager(Context context, int i) {
            super(context, i);
        }

        @Override // androidx.recyclerview.widget.LinearLayoutManager, androidx.recyclerview.widget.RecyclerView.LayoutManager
        public boolean canScrollVertically() {
            if (BubbleOverflowActivity.this.mBubbleController.inLandscape()) {
                return super.canScrollVertically();
            }
            return false;
        }

        @Override // androidx.recyclerview.widget.GridLayoutManager, androidx.recyclerview.widget.RecyclerView.LayoutManager
        public int getColumnCountForAccessibility(RecyclerView.Recycler recycler, RecyclerView.State state) {
            int itemCount = state.getItemCount();
            int columnCountForAccessibility = super.getColumnCountForAccessibility(recycler, state);
            return itemCount < columnCountForAccessibility ? itemCount : columnCountForAccessibility;
        }
    }

    public BubbleOverflowActivity(BubbleController bubbleController) {
        this.mBubbleController = bubbleController;
    }

    @Override // android.app.Activity
    public void onCreate(Bundle bundle) throws Resources.NotFoundException {
        super.onCreate(bundle);
        setContentView(R.layout.bubble_overflow_activity);
        this.mRecyclerView = (RecyclerView) findViewById(R.id.bubble_overflow_recycler);
        this.mEmptyState = (LinearLayout) findViewById(R.id.bubble_overflow_empty_state);
        this.mEmptyStateTitle = (TextView) findViewById(R.id.bubble_overflow_empty_title);
        this.mEmptyStateSubtitle = (TextView) findViewById(R.id.bubble_overflow_empty_subtitle);
        this.mEmptyStateImage = (ImageView) findViewById(R.id.bubble_overflow_empty_state_image);
        updateDimensions();
        onDataChanged(this.mBubbleController.getOverflowBubbles());
        this.mBubbleController.setOverflowCallback(new Runnable() { // from class: com.android.systemui.bubbles.BubbleOverflowActivity$$ExternalSyntheticLambda0
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.lambda$onCreate$0();
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$onCreate$0() {
        onDataChanged(this.mBubbleController.getOverflowBubbles());
    }

    void updateDimensions() throws Resources.NotFoundException {
        Resources resources = getResources();
        int integer = resources.getInteger(R.integer.bubbles_overflow_columns);
        this.mRecyclerView.setLayoutManager(new NoScrollGridLayoutManager(getApplicationContext(), integer));
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int i = R.dimen.bubble_overflow_padding;
        int dimensionPixelSize = (displayMetrics.widthPixels - (resources.getDimensionPixelSize(i) * 2)) / integer;
        int dimensionPixelSize2 = (resources.getDimensionPixelSize(R.dimen.bubble_overflow_height) - resources.getDimensionPixelSize(i)) / ((int) Math.ceil(resources.getInteger(R.integer.bubbles_max_overflow) / integer));
        Context applicationContext = getApplicationContext();
        List<Bubble> list = this.mOverflowBubbles;
        final BubbleController bubbleController = this.mBubbleController;
        Objects.requireNonNull(bubbleController);
        BubbleOverflowAdapter bubbleOverflowAdapter = new BubbleOverflowAdapter(applicationContext, list, new Consumer() { // from class: com.android.systemui.bubbles.BubbleOverflowActivity$$ExternalSyntheticLambda1
            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                bubbleController.promoteBubbleFromOverflow((Bubble) obj);
            }
        }, dimensionPixelSize, dimensionPixelSize2);
        this.mAdapter = bubbleOverflowAdapter;
        this.mRecyclerView.setAdapter(bubbleOverflowAdapter);
    }

    void updateTheme() throws Resources.NotFoundException {
        Drawable drawable;
        int color;
        Resources resources = getResources();
        boolean z = (resources.getConfiguration().uiMode & 48) == 32;
        ImageView imageView = this.mEmptyStateImage;
        if (z) {
            drawable = resources.getDrawable(R.drawable.ic_empty_bubble_overflow_dark);
        } else {
            drawable = resources.getDrawable(R.drawable.ic_empty_bubble_overflow_light);
        }
        imageView.setImageDrawable(drawable);
        View viewFindViewById = findViewById(android.R.id.content);
        if (z) {
            color = resources.getColor(R.color.bubbles_dark);
        } else {
            color = resources.getColor(R.color.bubbles_light);
        }
        viewFindViewById.setBackgroundColor(color);
        TypedArray typedArrayObtainStyledAttributes = getApplicationContext().obtainStyledAttributes(new int[]{android.R.attr.colorBackgroundFloating, android.R.attr.textColorSecondary});
        int iEnsureTextContrast = ContrastColorUtil.ensureTextContrast(typedArrayObtainStyledAttributes.getColor(1, z ? -1 : -16777216), typedArrayObtainStyledAttributes.getColor(0, z ? -16777216 : -1), z);
        typedArrayObtainStyledAttributes.recycle();
        this.mEmptyStateTitle.setTextColor(iEnsureTextContrast);
        this.mEmptyStateSubtitle.setTextColor(iEnsureTextContrast);
    }

    void onDataChanged(List<Bubble> list) {
        this.mOverflowBubbles.clear();
        this.mOverflowBubbles.addAll(list);
        this.mAdapter.notifyDataSetChanged();
        if (this.mOverflowBubbles.isEmpty()) {
            this.mEmptyState.setVisibility(0);
        } else {
            this.mEmptyState.setVisibility(8);
        }
    }

    @Override // android.app.Activity
    public void onStart() {
        super.onStart();
    }

    @Override // android.app.Activity
    public void onRestart() {
        super.onRestart();
    }

    @Override // android.app.Activity
    public void onResume() throws Resources.NotFoundException {
        super.onResume();
        updateDimensions();
        updateTheme();
    }

    @Override // android.app.Activity
    public void onPause() {
        super.onPause();
    }

    @Override // android.app.Activity
    public void onStop() {
        super.onStop();
    }

    @Override // android.app.Activity
    public void onDestroy() {
        super.onDestroy();
    }
}
