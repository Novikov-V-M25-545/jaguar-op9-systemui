package androidx.slice.widget;

import android.R;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.text.SpannableStringBuilder;
import android.util.TypedValue;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.slice.SliceItem;
import androidx.slice.core.SliceQuery;
import androidx.slice.widget.SliceView;

/* loaded from: classes.dex */
public class MessageView extends SliceChildView {
    private TextView mDetails;
    private ImageView mIcon;

    @Override // androidx.slice.widget.SliceChildView
    public void resetView() {
    }

    public MessageView(Context context) {
        super(context);
    }

    @Override // android.view.View
    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mDetails = (TextView) findViewById(R.id.summary);
        this.mIcon = (ImageView) findViewById(R.id.icon);
    }

    @Override // androidx.slice.widget.SliceChildView
    public void setSliceItem(SliceContent sliceContent, boolean z, int i, int i2, SliceView.OnSliceActionListener onSliceActionListener) {
        Drawable drawableLoadDrawable;
        SliceItem sliceItem = sliceContent.getSliceItem();
        setSliceActionListener(onSliceActionListener);
        SliceItem sliceItemFindSubtype = SliceQuery.findSubtype(sliceItem, "image", "source");
        if (sliceItemFindSubtype != null && sliceItemFindSubtype.getIcon() != null && (drawableLoadDrawable = sliceItemFindSubtype.getIcon().loadDrawable(getContext())) != null) {
            int iApplyDimension = (int) TypedValue.applyDimension(1, 24.0f, getContext().getResources().getDisplayMetrics());
            Bitmap bitmapCreateBitmap = Bitmap.createBitmap(iApplyDimension, iApplyDimension, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmapCreateBitmap);
            drawableLoadDrawable.setBounds(0, 0, iApplyDimension, iApplyDimension);
            drawableLoadDrawable.draw(canvas);
            this.mIcon.setImageBitmap(SliceViewUtil.getCircularBitmap(bitmapCreateBitmap));
        }
        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
        for (SliceItem sliceItem2 : SliceQuery.findAll(sliceItem, "text")) {
            if (spannableStringBuilder.length() != 0) {
                spannableStringBuilder.append('\n');
            }
            spannableStringBuilder.append(sliceItem2.getSanitizedText());
        }
        this.mDetails.setText(spannableStringBuilder.toString());
    }
}
