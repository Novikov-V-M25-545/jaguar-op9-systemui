package com.android.systemui.util;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.widget.FrameLayout;
import android.widget.ImageView;
import com.android.systemui.R;

/* loaded from: classes.dex */
public class DismissCircleView extends FrameLayout {
    private final ImageView mIconView;

    public DismissCircleView(Context context) throws Resources.NotFoundException {
        super(context);
        ImageView imageView = new ImageView(getContext());
        this.mIconView = imageView;
        Resources resources = getResources();
        setBackground(resources.getDrawable(R.drawable.dismiss_circle_background));
        imageView.setImageDrawable(resources.getDrawable(R.drawable.ic_close_white));
        addView(imageView);
        setViewSizes();
    }

    @Override // android.view.View
    protected void onConfigurationChanged(Configuration configuration) throws Resources.NotFoundException {
        super.onConfigurationChanged(configuration);
        setViewSizes();
    }

    private void setViewSizes() throws Resources.NotFoundException {
        int dimensionPixelSize = getResources().getDimensionPixelSize(R.dimen.dismiss_target_x_size);
        this.mIconView.setLayoutParams(new FrameLayout.LayoutParams(dimensionPixelSize, dimensionPixelSize, 17));
    }
}
