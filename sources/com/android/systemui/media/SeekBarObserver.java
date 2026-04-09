package com.android.systemui.media;

import android.content.Context;
import android.text.format.DateUtils;
import android.widget.SeekBar;
import androidx.lifecycle.Observer;
import com.android.systemui.R;
import com.android.systemui.media.SeekBarViewModel;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: SeekBarObserver.kt */
/* loaded from: classes.dex */
public final class SeekBarObserver implements Observer<SeekBarViewModel.Progress> {
    private final PlayerViewHolder holder;
    private final int seekBarDisabledHeight;
    private final int seekBarDisabledVerticalPadding;
    private final int seekBarEnabledMaxHeight;
    private final int seekBarEnabledVerticalPadding;

    public SeekBarObserver(@NotNull PlayerViewHolder holder) {
        Intrinsics.checkParameterIsNotNull(holder, "holder");
        this.holder = holder;
        SeekBar seekBar = holder.getSeekBar();
        Intrinsics.checkExpressionValueIsNotNull(seekBar, "holder.seekBar");
        Context context = seekBar.getContext();
        Intrinsics.checkExpressionValueIsNotNull(context, "holder.seekBar.context");
        this.seekBarEnabledMaxHeight = context.getResources().getDimensionPixelSize(R.dimen.qs_media_enabled_seekbar_height);
        SeekBar seekBar2 = holder.getSeekBar();
        Intrinsics.checkExpressionValueIsNotNull(seekBar2, "holder.seekBar");
        Context context2 = seekBar2.getContext();
        Intrinsics.checkExpressionValueIsNotNull(context2, "holder.seekBar.context");
        this.seekBarDisabledHeight = context2.getResources().getDimensionPixelSize(R.dimen.qs_media_disabled_seekbar_height);
        SeekBar seekBar3 = holder.getSeekBar();
        Intrinsics.checkExpressionValueIsNotNull(seekBar3, "holder.seekBar");
        Context context3 = seekBar3.getContext();
        Intrinsics.checkExpressionValueIsNotNull(context3, "holder.seekBar.context");
        this.seekBarEnabledVerticalPadding = context3.getResources().getDimensionPixelSize(R.dimen.qs_media_enabled_seekbar_vertical_padding);
        SeekBar seekBar4 = holder.getSeekBar();
        Intrinsics.checkExpressionValueIsNotNull(seekBar4, "holder.seekBar");
        Context context4 = seekBar4.getContext();
        Intrinsics.checkExpressionValueIsNotNull(context4, "holder.seekBar.context");
        this.seekBarDisabledVerticalPadding = context4.getResources().getDimensionPixelSize(R.dimen.qs_media_disabled_seekbar_vertical_padding);
    }

    @Override // androidx.lifecycle.Observer
    public void onChanged(@NotNull SeekBarViewModel.Progress data) {
        Intrinsics.checkParameterIsNotNull(data, "data");
        if (!data.getEnabled()) {
            SeekBar seekBar = this.holder.getSeekBar();
            Intrinsics.checkExpressionValueIsNotNull(seekBar, "holder.seekBar");
            if (seekBar.getMaxHeight() != this.seekBarDisabledHeight) {
                SeekBar seekBar2 = this.holder.getSeekBar();
                Intrinsics.checkExpressionValueIsNotNull(seekBar2, "holder.seekBar");
                seekBar2.setMaxHeight(this.seekBarDisabledHeight);
                setVerticalPadding(this.seekBarDisabledVerticalPadding);
            }
            this.holder.getSeekBar().setEnabled(false);
            this.holder.getSeekBar().getThumb().setAlpha(0);
            this.holder.getSeekBar().setProgress(0);
            this.holder.getElapsedTimeView().setText("");
            this.holder.getTotalTimeView().setText("");
            return;
        }
        this.holder.getSeekBar().getThumb().setAlpha(data.getSeekAvailable() ? 255 : 0);
        this.holder.getSeekBar().setEnabled(data.getSeekAvailable());
        SeekBar seekBar3 = this.holder.getSeekBar();
        Intrinsics.checkExpressionValueIsNotNull(seekBar3, "holder.seekBar");
        if (seekBar3.getMaxHeight() != this.seekBarEnabledMaxHeight) {
            SeekBar seekBar4 = this.holder.getSeekBar();
            Intrinsics.checkExpressionValueIsNotNull(seekBar4, "holder.seekBar");
            seekBar4.setMaxHeight(this.seekBarEnabledMaxHeight);
            setVerticalPadding(this.seekBarEnabledVerticalPadding);
        }
        int duration = data.getDuration();
        this.holder.getSeekBar().setMax(duration);
        this.holder.getTotalTimeView().setText(DateUtils.formatElapsedTime(duration / 1000));
        Integer elapsedTime = data.getElapsedTime();
        if (elapsedTime != null) {
            int iIntValue = elapsedTime.intValue();
            this.holder.getSeekBar().setProgress(iIntValue);
            this.holder.getElapsedTimeView().setText(DateUtils.formatElapsedTime(iIntValue / 1000));
        }
    }

    public final void setVerticalPadding(int i) {
        SeekBar seekBar = this.holder.getSeekBar();
        Intrinsics.checkExpressionValueIsNotNull(seekBar, "holder.seekBar");
        int paddingLeft = seekBar.getPaddingLeft();
        SeekBar seekBar2 = this.holder.getSeekBar();
        Intrinsics.checkExpressionValueIsNotNull(seekBar2, "holder.seekBar");
        this.holder.getSeekBar().setPadding(paddingLeft, i, seekBar2.getPaddingRight(), i);
    }
}
