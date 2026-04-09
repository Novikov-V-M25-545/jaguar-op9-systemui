package com.android.keyguard;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.PowerManager;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.android.internal.widget.LockPatternUtils;
import com.android.systemui.R;
import lineageos.providers.LineageSettings;

/* loaded from: classes.dex */
public class NumPadKey extends ViewGroup {
    private int mDigit;
    private final TextView mDigitText;
    private View.OnClickListener mListener;
    private final LockPatternUtils mLockPatternUtils;
    private final PowerManager mPM;
    private PasswordTextView mTextView;
    private int mTextViewResId;

    @Override // android.view.View
    public boolean hasOverlappingRendering() {
        return false;
    }

    public void userActivity() {
        this.mPM.userActivity(SystemClock.uptimeMillis(), false);
    }

    public NumPadKey(Context context) {
        this(context, null);
    }

    public NumPadKey(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public NumPadKey(Context context, AttributeSet attributeSet, int i) {
        this(context, attributeSet, i, R.layout.keyguard_num_pad_key);
    }

    protected NumPadKey(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i);
        this.mDigit = -1;
        this.mListener = new View.OnClickListener() { // from class: com.android.keyguard.NumPadKey.1
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                View viewFindViewById;
                if (NumPadKey.this.mTextView == null && NumPadKey.this.mTextViewResId > 0 && (viewFindViewById = NumPadKey.this.getRootView().findViewById(NumPadKey.this.mTextViewResId)) != null && (viewFindViewById instanceof PasswordTextView)) {
                    NumPadKey.this.mTextView = (PasswordTextView) viewFindViewById;
                }
                if (NumPadKey.this.mTextView != null && NumPadKey.this.mTextView.isEnabled()) {
                    NumPadKey.this.mTextView.append(Character.forDigit(NumPadKey.this.mDigit, 10));
                }
                NumPadKey.this.userActivity();
            }
        };
        setFocusable(true);
        TypedArray typedArrayObtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R.styleable.NumPadKey);
        try {
            this.mDigit = typedArrayObtainStyledAttributes.getInt(R.styleable.NumPadKey_digit, this.mDigit);
            this.mTextViewResId = typedArrayObtainStyledAttributes.getResourceId(R.styleable.NumPadKey_textView, 0);
            typedArrayObtainStyledAttributes.recycle();
            setOnClickListener(this.mListener);
            setOnHoverListener(new LiftToActivateListener(context));
            this.mLockPatternUtils = new LockPatternUtils(context);
            this.mPM = (PowerManager) ((ViewGroup) this).mContext.getSystemService("power");
            ((LayoutInflater) getContext().getSystemService("layout_inflater")).inflate(i2, (ViewGroup) this, true);
            TextView textView = (TextView) findViewById(R.id.digit_text);
            this.mDigitText = textView;
            textView.setText(Integer.toString(this.mDigit));
            updateText();
            setBackground(((ViewGroup) this).mContext.getDrawable(R.drawable.ripple_drawable));
            setContentDescription(textView.getText().toString());
        } catch (Throwable th) {
            typedArrayObtainStyledAttributes.recycle();
            throw th;
        }
    }

    public void setDigit(int i) {
        this.mDigit = i;
        updateText();
    }

    private void updateText() {
        LineageSettings.System.getIntForUser(getContext().getContentResolver(), "lockscreen_scramble_pin_layout", 0, -2);
        int i = this.mDigit;
        if (i >= 0) {
            this.mDigitText.setText(Integer.toString(i));
        }
    }

    @Override // android.view.View
    public boolean onTouchEvent(MotionEvent motionEvent) {
        if (motionEvent.getActionMasked() == 0) {
            doHapticKeyClick();
        }
        return super.onTouchEvent(motionEvent);
    }

    @Override // android.view.View
    protected void onMeasure(int i, int i2) {
        super.onMeasure(i, i2);
        measureChildren(i, i2);
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onLayout(boolean z, int i, int i2, int i3, int i4) {
        int measuredHeight = this.mDigitText.getMeasuredHeight();
        int height = (getHeight() / 2) - (measuredHeight / 2);
        int width = (getWidth() / 2) - (this.mDigitText.getMeasuredWidth() / 2);
        TextView textView = this.mDigitText;
        textView.layout(width, height, textView.getMeasuredWidth() + width, measuredHeight + height);
    }

    public void doHapticKeyClick() {
        if (this.mLockPatternUtils.isTactileFeedbackEnabled()) {
            performHapticFeedback(1, 3);
        }
    }
}
