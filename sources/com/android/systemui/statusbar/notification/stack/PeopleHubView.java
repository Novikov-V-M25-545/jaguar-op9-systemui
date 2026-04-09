package com.android.systemui.statusbar.notification.stack;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.systemui.R;
import com.android.systemui.plugins.statusbar.NotificationMenuRowPlugin;
import com.android.systemui.statusbar.notification.people.DataListener;
import com.android.systemui.statusbar.notification.people.PersonViewModel;
import com.android.systemui.statusbar.notification.row.StackScrollerDecorView;
import kotlin.Unit;
import kotlin.collections.CollectionsKt___CollectionsKt;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import kotlin.ranges.RangesKt___RangesKt;
import kotlin.sequences.Sequence;
import kotlin.sequences.SequencesKt___SequencesKt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: PeopleHubView.kt */
/* loaded from: classes.dex */
public final class PeopleHubView extends StackScrollerDecorView implements SwipeableView {
    private boolean canSwipe;
    private ViewGroup contents;
    private TextView label;

    @NotNull
    private Sequence<? extends DataListener<? super PersonViewModel>> personViewAdapters;

    @Override // com.android.systemui.statusbar.notification.stack.SwipeableView
    @Nullable
    public NotificationMenuRowPlugin createMenu() {
        return null;
    }

    @Override // com.android.systemui.statusbar.notification.row.StackScrollerDecorView
    @Nullable
    protected View findSecondaryView() {
        return null;
    }

    @Override // com.android.systemui.statusbar.notification.stack.SwipeableView
    public boolean hasFinishedInitialization() {
        return true;
    }

    @Override // com.android.systemui.statusbar.notification.row.StackScrollerDecorView, com.android.systemui.statusbar.notification.row.ExpandableView
    public boolean needsClippingToShelf() {
        return true;
    }

    public static final /* synthetic */ ViewGroup access$getContents$p(PeopleHubView peopleHubView) {
        ViewGroup viewGroup = peopleHubView.contents;
        if (viewGroup == null) {
            Intrinsics.throwUninitializedPropertyAccessException("contents");
        }
        return viewGroup;
    }

    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    public PeopleHubView(@NotNull Context context, @NotNull AttributeSet attrs) {
        super(context, attrs);
        Intrinsics.checkParameterIsNotNull(context, "context");
        Intrinsics.checkParameterIsNotNull(attrs, "attrs");
    }

    @Override // com.android.systemui.statusbar.notification.row.StackScrollerDecorView, android.view.View
    protected void onFinishInflate() {
        View viewRequireViewById = requireViewById(R.id.people_list);
        Intrinsics.checkExpressionValueIsNotNull(viewRequireViewById, "requireViewById(R.id.people_list)");
        this.contents = (ViewGroup) viewRequireViewById;
        View viewRequireViewById2 = requireViewById(R.id.header_label);
        Intrinsics.checkExpressionValueIsNotNull(viewRequireViewById2, "requireViewById(R.id.header_label)");
        this.label = (TextView) viewRequireViewById2;
        ViewGroup viewGroup = this.contents;
        if (viewGroup == null) {
            Intrinsics.throwUninitializedPropertyAccessException("contents");
        }
        this.personViewAdapters = CollectionsKt___CollectionsKt.asSequence(SequencesKt___SequencesKt.toList(SequencesKt___SequencesKt.mapNotNull(CollectionsKt___CollectionsKt.asSequence(RangesKt___RangesKt.until(0, viewGroup.getChildCount())), new Function1<Integer, PersonDataListenerImpl>() { // from class: com.android.systemui.statusbar.notification.stack.PeopleHubView.onFinishInflate.1
            {
                super(1);
            }

            @Override // kotlin.jvm.functions.Function1
            public /* bridge */ /* synthetic */ PersonDataListenerImpl invoke(Integer num) {
                return invoke(num.intValue());
            }

            @Nullable
            public final PersonDataListenerImpl invoke(int i) {
                View childAt = PeopleHubView.access$getContents$p(PeopleHubView.this).getChildAt(i);
                if (!(childAt instanceof ImageView)) {
                    childAt = null;
                }
                ImageView imageView = (ImageView) childAt;
                if (imageView != null) {
                    return new PersonDataListenerImpl(PeopleHubView.this, imageView);
                }
                return null;
            }
        })));
        super.onFinishInflate();
        setVisible(true, false);
    }

    public final void setTextColor(int i) {
        TextView textView = this.label;
        if (textView == null) {
            Intrinsics.throwUninitializedPropertyAccessException("label");
        }
        textView.setTextColor(i);
    }

    @Override // com.android.systemui.statusbar.notification.row.StackScrollerDecorView
    @NotNull
    protected View findContentView() {
        ViewGroup viewGroup = this.contents;
        if (viewGroup == null) {
            Intrinsics.throwUninitializedPropertyAccessException("contents");
        }
        return viewGroup;
    }

    @Override // com.android.systemui.statusbar.notification.stack.SwipeableView
    public void resetTranslation() {
        setTranslationX(0.0f);
    }

    @Override // com.android.systemui.statusbar.notification.row.ExpandableView, com.android.systemui.statusbar.notification.stack.SwipeableView
    public void setTranslation(float f) {
        if (this.canSwipe) {
            super.setTranslation(f);
        }
    }

    public final boolean getCanSwipe() {
        return this.canSwipe;
    }

    public final void setCanSwipe(boolean z) {
        boolean z2 = this.canSwipe;
        if (z2 != z) {
            if (z2) {
                resetTranslation();
            }
            this.canSwipe = z;
        }
    }

    @Override // com.android.systemui.statusbar.notification.row.ExpandableView
    protected void applyContentTransformation(float f, float f2) {
        super.applyContentTransformation(f, f2);
        ViewGroup viewGroup = this.contents;
        if (viewGroup == null) {
            Intrinsics.throwUninitializedPropertyAccessException("contents");
        }
        int childCount = viewGroup.getChildCount();
        for (int i = 0; i < childCount; i++) {
            ViewGroup viewGroup2 = this.contents;
            if (viewGroup2 == null) {
                Intrinsics.throwUninitializedPropertyAccessException("contents");
            }
            View view = viewGroup2.getChildAt(i);
            Intrinsics.checkExpressionValueIsNotNull(view, "view");
            view.setAlpha(f);
            view.setTranslationY(f2);
        }
    }

    public final void setOnHeaderClickListener(@NotNull View.OnClickListener listener) {
        Intrinsics.checkParameterIsNotNull(listener, "listener");
        TextView textView = this.label;
        if (textView == null) {
            Intrinsics.throwUninitializedPropertyAccessException("label");
        }
        textView.setOnClickListener(listener);
    }

    /* compiled from: PeopleHubView.kt */
    private final class PersonDataListenerImpl implements DataListener<PersonViewModel> {

        @NotNull
        private final ImageView avatarView;
        final /* synthetic */ PeopleHubView this$0;

        public PersonDataListenerImpl(@NotNull PeopleHubView peopleHubView, ImageView avatarView) {
            Intrinsics.checkParameterIsNotNull(avatarView, "avatarView");
            this.this$0 = peopleHubView;
            this.avatarView = avatarView;
        }

        @Override // com.android.systemui.statusbar.notification.people.DataListener
        public void onDataChanged(@Nullable final PersonViewModel personViewModel) {
            this.avatarView.setVisibility(personViewModel != null ? 0 : 8);
            this.avatarView.setImageDrawable(personViewModel != null ? personViewModel.getIcon() : null);
            this.avatarView.setOnClickListener(new View.OnClickListener() { // from class: com.android.systemui.statusbar.notification.stack.PeopleHubView$PersonDataListenerImpl$onDataChanged$2
                @Override // android.view.View.OnClickListener
                public final void onClick(View view) {
                    Function0<Unit> onClick;
                    PersonViewModel personViewModel2 = personViewModel;
                    if (personViewModel2 == null || (onClick = personViewModel2.getOnClick()) == null) {
                        return;
                    }
                    onClick.invoke();
                }
            });
        }
    }
}
