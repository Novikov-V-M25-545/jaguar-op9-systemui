package com.android.systemui.statusbar;

import android.R;
import android.app.Notification;
import android.graphics.PorterDuff;
import android.text.TextUtils;
import android.view.NotificationHeaderView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.internal.util.ContrastColorUtil;
import com.android.internal.widget.ConversationLayout;
import com.android.systemui.statusbar.notification.row.ExpandableNotificationRow;
import com.android.systemui.statusbar.notification.row.NotificationContentView;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/* loaded from: classes.dex */
public class NotificationHeaderUtil {
    private static final VisibilityApplicator sAppNameApplicator;
    private static final TextViewComparator sTextViewComparator;
    private static final VisibilityApplicator sVisibilityApplicator;
    private final ArrayList<HeaderProcessor> mComparators;
    private final HashSet<Integer> mDividers;
    private final ExpandableNotificationRow mRow;
    private static final DataExtractor sIconExtractor = new DataExtractor() { // from class: com.android.systemui.statusbar.NotificationHeaderUtil.1
        @Override // com.android.systemui.statusbar.NotificationHeaderUtil.DataExtractor
        public Object extractData(ExpandableNotificationRow expandableNotificationRow) {
            return expandableNotificationRow.getEntry().getSbn().getNotification();
        }
    };
    private static final IconComparator sIconVisibilityComparator = new IconComparator() { // from class: com.android.systemui.statusbar.NotificationHeaderUtil.2
        @Override // com.android.systemui.statusbar.NotificationHeaderUtil.ViewComparator
        public boolean compare(View view, View view2, Object obj, Object obj2) {
            return hasSameIcon(obj, obj2) && hasSameColor(obj, obj2);
        }
    };
    private static final IconComparator sGreyComparator = new IconComparator() { // from class: com.android.systemui.statusbar.NotificationHeaderUtil.3
        @Override // com.android.systemui.statusbar.NotificationHeaderUtil.ViewComparator
        public boolean compare(View view, View view2, Object obj, Object obj2) {
            return !hasSameIcon(obj, obj2) || hasSameColor(obj, obj2);
        }
    };
    private static final ResultApplicator mGreyApplicator = new ResultApplicator() { // from class: com.android.systemui.statusbar.NotificationHeaderUtil.4
        @Override // com.android.systemui.statusbar.NotificationHeaderUtil.ResultApplicator
        public void apply(View view, View view2, boolean z, boolean z2) {
            NotificationHeaderView notificationHeaderView = (NotificationHeaderView) view2;
            ImageView imageView = (ImageView) view2.findViewById(R.id.icon);
            ImageView imageView2 = (ImageView) view2.findViewById(R.id.current_scene);
            applyToChild(imageView, z, notificationHeaderView.getOriginalIconColor());
            applyToChild(imageView2, z, notificationHeaderView.getOriginalNotificationColor());
        }

        private void applyToChild(View view, boolean z, int i) {
            if (i != 1) {
                ImageView imageView = (ImageView) view;
                imageView.getDrawable().mutate();
                if (z) {
                    imageView.getDrawable().setColorFilter(ContrastColorUtil.resolveColor(view.getContext(), 0, (view.getContext().getResources().getConfiguration().uiMode & 48) == 32), PorterDuff.Mode.SRC_ATOP);
                } else {
                    imageView.getDrawable().setColorFilter(i, PorterDuff.Mode.SRC_ATOP);
                }
            }
        }
    };

    private interface DataExtractor {
        Object extractData(ExpandableNotificationRow expandableNotificationRow);
    }

    private interface ResultApplicator {
        void apply(View view, View view2, boolean z, boolean z2);
    }

    private interface ViewComparator {
        boolean compare(View view, View view2, Object obj, Object obj2);

        boolean isEmpty(View view);
    }

    static {
        sTextViewComparator = new TextViewComparator();
        sVisibilityApplicator = new VisibilityApplicator();
        sAppNameApplicator = new AppNameApplicator();
    }

    public NotificationHeaderUtil(ExpandableNotificationRow expandableNotificationRow) {
        ArrayList<HeaderProcessor> arrayList = new ArrayList<>();
        this.mComparators = arrayList;
        HashSet<Integer> hashSet = new HashSet<>();
        this.mDividers = hashSet;
        this.mRow = expandableNotificationRow;
        DataExtractor dataExtractor = sIconExtractor;
        IconComparator iconComparator = sIconVisibilityComparator;
        VisibilityApplicator visibilityApplicator = sVisibilityApplicator;
        arrayList.add(new HeaderProcessor(expandableNotificationRow, R.id.icon, dataExtractor, iconComparator, visibilityApplicator));
        arrayList.add(new HeaderProcessor(expandableNotificationRow, R.id.low_light, dataExtractor, sGreyComparator, mGreyApplicator));
        arrayList.add(new HeaderProcessor(expandableNotificationRow, R.id.notification_top_line, null, new ViewComparator() { // from class: com.android.systemui.statusbar.NotificationHeaderUtil.5
            @Override // com.android.systemui.statusbar.NotificationHeaderUtil.ViewComparator
            public boolean compare(View view, View view2, Object obj, Object obj2) {
                return view.getVisibility() != 8;
            }

            @Override // com.android.systemui.statusbar.NotificationHeaderUtil.ViewComparator
            public boolean isEmpty(View view) {
                return (view instanceof ImageView) && ((ImageView) view).getDrawable() == null;
            }
        }, visibilityApplicator));
        arrayList.add(new HeaderProcessor(expandableNotificationRow, R.id.aerr_mute, null, sTextViewComparator, sAppNameApplicator));
        arrayList.add(HeaderProcessor.forTextView(expandableNotificationRow, R.id.firstStrong));
        hashSet.add(Integer.valueOf(R.id.firstStrongLtr));
        hashSet.add(Integer.valueOf(R.id.fitCenter));
        hashSet.add(Integer.valueOf(R.id.singleInstancePerTask));
    }

    public void updateChildrenHeaderAppearance() {
        List<ExpandableNotificationRow> attachedChildren = this.mRow.getAttachedChildren();
        if (attachedChildren == null) {
            return;
        }
        for (int i = 0; i < this.mComparators.size(); i++) {
            this.mComparators.get(i).init();
        }
        for (int i2 = 0; i2 < attachedChildren.size(); i2++) {
            ExpandableNotificationRow expandableNotificationRow = attachedChildren.get(i2);
            for (int i3 = 0; i3 < this.mComparators.size(); i3++) {
                this.mComparators.get(i3).compareToHeader(expandableNotificationRow);
            }
        }
        for (int i4 = 0; i4 < attachedChildren.size(); i4++) {
            ExpandableNotificationRow expandableNotificationRow2 = attachedChildren.get(i4);
            for (int i5 = 0; i5 < this.mComparators.size(); i5++) {
                this.mComparators.get(i5).apply(expandableNotificationRow2);
            }
            sanitizeHeaderViews(expandableNotificationRow2);
        }
    }

    private void sanitizeHeaderViews(ExpandableNotificationRow expandableNotificationRow) {
        if (expandableNotificationRow.isSummaryWithChildren()) {
            sanitizeHeader(expandableNotificationRow.getNotificationHeader());
            return;
        }
        NotificationContentView privateLayout = expandableNotificationRow.getPrivateLayout();
        sanitizeChild(privateLayout.getContractedChild());
        sanitizeChild(privateLayout.getHeadsUpChild());
        sanitizeChild(privateLayout.getExpandedChild());
    }

    private void sanitizeChild(View view) {
        if (view != null) {
            sanitizeHeader((ViewGroup) view.findViewById(R.id.low_light));
        }
    }

    private void sanitizeHeader(ViewGroup viewGroup) {
        boolean z;
        View childAt;
        boolean z2;
        if (viewGroup == null) {
            return;
        }
        int childCount = viewGroup.getChildCount();
        View viewFindViewById = viewGroup.findViewById(R.id.simple);
        int i = 0;
        while (true) {
            if (i >= childCount) {
                z = false;
                break;
            }
            View childAt2 = viewGroup.getChildAt(i);
            if ((childAt2 instanceof TextView) && childAt2.getVisibility() != 8 && !this.mDividers.contains(Integer.valueOf(childAt2.getId())) && childAt2 != viewFindViewById) {
                z = true;
                break;
            }
            i++;
        }
        viewFindViewById.setVisibility((!z || this.mRow.getEntry().getSbn().getNotification().showsTime()) ? 0 : 8);
        View view = null;
        int i2 = 0;
        while (i2 < childCount) {
            View childAt3 = viewGroup.getChildAt(i2);
            if (this.mDividers.contains(Integer.valueOf(childAt3.getId()))) {
                while (true) {
                    i2++;
                    if (i2 >= childCount) {
                        break;
                    }
                    childAt = viewGroup.getChildAt(i2);
                    if (this.mDividers.contains(Integer.valueOf(childAt.getId()))) {
                        i2--;
                        break;
                    } else if (childAt.getVisibility() != 8 && (childAt instanceof TextView)) {
                        if (view != null) {
                            z2 = true;
                        }
                    }
                }
                childAt = view;
                z2 = false;
                childAt3.setVisibility(z2 ? 0 : 8);
                view = childAt;
            } else if (childAt3.getVisibility() != 8 && (childAt3 instanceof TextView)) {
                view = childAt3;
            }
            i2++;
        }
    }

    public void restoreNotificationHeader(ExpandableNotificationRow expandableNotificationRow) {
        for (int i = 0; i < this.mComparators.size(); i++) {
            this.mComparators.get(i).apply(expandableNotificationRow, true);
        }
        sanitizeHeaderViews(expandableNotificationRow);
    }

    private static class HeaderProcessor {
        private final ResultApplicator mApplicator;
        private boolean mApply;
        private ViewComparator mComparator;
        private final DataExtractor mExtractor;
        private final int mId;
        private Object mParentData;
        private final ExpandableNotificationRow mParentRow;
        private View mParentView;

        public static HeaderProcessor forTextView(ExpandableNotificationRow expandableNotificationRow, int i) {
            return new HeaderProcessor(expandableNotificationRow, i, null, NotificationHeaderUtil.sTextViewComparator, NotificationHeaderUtil.sVisibilityApplicator);
        }

        HeaderProcessor(ExpandableNotificationRow expandableNotificationRow, int i, DataExtractor dataExtractor, ViewComparator viewComparator, ResultApplicator resultApplicator) {
            this.mId = i;
            this.mExtractor = dataExtractor;
            this.mApplicator = resultApplicator;
            this.mComparator = viewComparator;
            this.mParentRow = expandableNotificationRow;
        }

        public void init() {
            this.mParentView = this.mParentRow.getNotificationHeader().findViewById(this.mId);
            DataExtractor dataExtractor = this.mExtractor;
            this.mParentData = dataExtractor == null ? null : dataExtractor.extractData(this.mParentRow);
            this.mApply = !this.mComparator.isEmpty(this.mParentView);
        }

        public void compareToHeader(ExpandableNotificationRow expandableNotificationRow) {
            View contractedChild;
            View viewFindViewById;
            if (!this.mApply || (contractedChild = expandableNotificationRow.getPrivateLayout().getContractedChild()) == null || (viewFindViewById = contractedChild.findViewById(this.mId)) == null) {
                return;
            }
            DataExtractor dataExtractor = this.mExtractor;
            this.mApply = this.mComparator.compare(this.mParentView, viewFindViewById, this.mParentData, dataExtractor == null ? null : dataExtractor.extractData(expandableNotificationRow));
        }

        public void apply(ExpandableNotificationRow expandableNotificationRow) {
            apply(expandableNotificationRow, false);
        }

        public void apply(ExpandableNotificationRow expandableNotificationRow, boolean z) {
            boolean z2 = this.mApply && !z;
            if (expandableNotificationRow.isSummaryWithChildren()) {
                applyToView(z2, z, expandableNotificationRow.getNotificationHeader());
                return;
            }
            applyToView(z2, z, expandableNotificationRow.getPrivateLayout().getContractedChild());
            applyToView(z2, z, expandableNotificationRow.getPrivateLayout().getHeadsUpChild());
            applyToView(z2, z, expandableNotificationRow.getPrivateLayout().getExpandedChild());
        }

        private void applyToView(boolean z, boolean z2, View view) {
            View viewFindViewById;
            if (view == null || (viewFindViewById = view.findViewById(this.mId)) == null || this.mComparator.isEmpty(viewFindViewById)) {
                return;
            }
            this.mApplicator.apply(view, viewFindViewById, z, z2);
        }
    }

    private static class TextViewComparator implements ViewComparator {
        private TextViewComparator() {
        }

        @Override // com.android.systemui.statusbar.NotificationHeaderUtil.ViewComparator
        public boolean compare(View view, View view2, Object obj, Object obj2) {
            return ((TextView) view).getText().equals(((TextView) view2).getText());
        }

        @Override // com.android.systemui.statusbar.NotificationHeaderUtil.ViewComparator
        public boolean isEmpty(View view) {
            return TextUtils.isEmpty(((TextView) view).getText());
        }
    }

    private static abstract class IconComparator implements ViewComparator {
        @Override // com.android.systemui.statusbar.NotificationHeaderUtil.ViewComparator
        public boolean isEmpty(View view) {
            return false;
        }

        private IconComparator() {
        }

        protected boolean hasSameIcon(Object obj, Object obj2) {
            return ((Notification) obj).getSmallIcon().sameAs(((Notification) obj2).getSmallIcon());
        }

        protected boolean hasSameColor(Object obj, Object obj2) {
            return ((Notification) obj).color == ((Notification) obj2).color;
        }
    }

    private static class VisibilityApplicator implements ResultApplicator {
        private VisibilityApplicator() {
        }

        @Override // com.android.systemui.statusbar.NotificationHeaderUtil.ResultApplicator
        public void apply(View view, View view2, boolean z, boolean z2) {
            view2.setVisibility(z ? 8 : 0);
        }
    }

    private static class AppNameApplicator extends VisibilityApplicator {
        private AppNameApplicator() {
            super();
        }

        @Override // com.android.systemui.statusbar.NotificationHeaderUtil.VisibilityApplicator, com.android.systemui.statusbar.NotificationHeaderUtil.ResultApplicator
        public void apply(View view, View view2, boolean z, boolean z2) {
            if (z2 && (view instanceof ConversationLayout)) {
                z = ((ConversationLayout) view).shouldHideAppName();
            }
            super.apply(view, view2, z, z2);
        }
    }
}
