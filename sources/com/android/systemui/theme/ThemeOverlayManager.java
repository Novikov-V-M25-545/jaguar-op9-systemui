package com.android.systemui.theme;

import android.content.om.OverlayInfo;
import android.content.om.OverlayManager;
import android.os.UserHandle;
import android.util.ArrayMap;
import android.util.Log;
import com.google.android.collect.Lists;
import com.google.android.collect.Sets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/* loaded from: classes.dex */
class ThemeOverlayManager {
    static final String ANDROID_PACKAGE = "android";
    static final String SETTINGS_PACKAGE = "com.android.settings";
    static final String SYSUI_PACKAGE = "com.android.systemui";
    private final Map<String, String> mCategoryToTargetPackage;
    private final Executor mExecutor;
    private final String mLauncherPackage;
    private final OverlayManager mOverlayManager;
    private final Map<String, Set<String>> mTargetPackageToCategories;
    private final String mThemePickerPackage;
    static final String OVERLAY_CATEGORY_ICON_LAUNCHER = "android.theme.customization.icon_pack.launcher";
    static final String OVERLAY_CATEGORY_SHAPE = "android.theme.customization.adaptive_icon_shape";
    static final String OVERLAY_CATEGORY_FONT = "android.theme.customization.font";
    static final String OVERLAY_CATEGORY_COLOR = "android.theme.customization.accent_color";
    static final String OVERLAY_CATEGORY_ICON_ANDROID = "android.theme.customization.icon_pack.android";
    static final String OVERLAY_CATEGORY_ICON_SYSUI = "android.theme.customization.icon_pack.systemui";
    static final String OVERLAY_CATEGORY_ICON_SETTINGS = "android.theme.customization.icon_pack.settings";
    static final String OVERLAY_CATEGORY_ICON_SIGNAL = "android.theme.customization.signal_icon";
    static final String OVERLAY_CATEGORY_ICON_WIFI = "android.theme.customization.wifi_icon";
    static final String OVERLAY_CATEGORY_NAVBAR = "android.theme.customization.navbar";
    static final String OVERLAY_CATEGORY_ICON_THEME_PICKER = "android.theme.customization.icon_pack.themepicker";
    static final List<String> THEME_CATEGORIES = Lists.newArrayList(new String[]{OVERLAY_CATEGORY_ICON_LAUNCHER, OVERLAY_CATEGORY_SHAPE, OVERLAY_CATEGORY_FONT, OVERLAY_CATEGORY_COLOR, OVERLAY_CATEGORY_ICON_ANDROID, OVERLAY_CATEGORY_ICON_SYSUI, OVERLAY_CATEGORY_ICON_SETTINGS, OVERLAY_CATEGORY_ICON_SIGNAL, OVERLAY_CATEGORY_ICON_WIFI, OVERLAY_CATEGORY_NAVBAR, OVERLAY_CATEGORY_ICON_THEME_PICKER});
    static final Set<String> SYSTEM_USER_CATEGORIES = Sets.newHashSet(new String[]{OVERLAY_CATEGORY_COLOR, OVERLAY_CATEGORY_FONT, OVERLAY_CATEGORY_SHAPE, OVERLAY_CATEGORY_ICON_ANDROID, OVERLAY_CATEGORY_NAVBAR, OVERLAY_CATEGORY_ICON_SYSUI});

    ThemeOverlayManager(OverlayManager overlayManager, Executor executor, String str, String str2) {
        ArrayMap arrayMap = new ArrayMap();
        this.mTargetPackageToCategories = arrayMap;
        ArrayMap arrayMap2 = new ArrayMap();
        this.mCategoryToTargetPackage = arrayMap2;
        this.mOverlayManager = overlayManager;
        this.mExecutor = executor;
        this.mLauncherPackage = str;
        this.mThemePickerPackage = str2;
        arrayMap.put(ANDROID_PACKAGE, Sets.newHashSet(new String[]{OVERLAY_CATEGORY_COLOR, OVERLAY_CATEGORY_FONT, OVERLAY_CATEGORY_SHAPE, OVERLAY_CATEGORY_ICON_ANDROID}));
        arrayMap.put(SYSUI_PACKAGE, Sets.newHashSet(new String[]{OVERLAY_CATEGORY_ICON_SYSUI}));
        arrayMap.put(SETTINGS_PACKAGE, Sets.newHashSet(new String[]{OVERLAY_CATEGORY_ICON_SETTINGS}));
        arrayMap.put(str, Sets.newHashSet(new String[]{OVERLAY_CATEGORY_ICON_LAUNCHER}));
        arrayMap.put(str2, Sets.newHashSet(new String[]{OVERLAY_CATEGORY_ICON_THEME_PICKER}));
        arrayMap2.put(OVERLAY_CATEGORY_COLOR, ANDROID_PACKAGE);
        arrayMap2.put(OVERLAY_CATEGORY_FONT, ANDROID_PACKAGE);
        arrayMap2.put(OVERLAY_CATEGORY_SHAPE, ANDROID_PACKAGE);
        arrayMap2.put(OVERLAY_CATEGORY_ICON_ANDROID, ANDROID_PACKAGE);
        arrayMap2.put(OVERLAY_CATEGORY_ICON_SYSUI, SYSUI_PACKAGE);
        arrayMap2.put(OVERLAY_CATEGORY_ICON_SETTINGS, SETTINGS_PACKAGE);
        arrayMap2.put(OVERLAY_CATEGORY_ICON_LAUNCHER, str);
        arrayMap2.put(OVERLAY_CATEGORY_ICON_THEME_PICKER, str2);
        arrayMap2.put(OVERLAY_CATEGORY_ICON_SIGNAL, SYSUI_PACKAGE);
        arrayMap2.put(OVERLAY_CATEGORY_ICON_WIFI, SYSUI_PACKAGE);
        arrayMap2.put(OVERLAY_CATEGORY_NAVBAR, SYSUI_PACKAGE);
    }

    void applyCurrentUserOverlays(Map<String, String> map, Set<UserHandle> set) {
        List<String> list = THEME_CATEGORIES;
        final HashSet hashSet = new HashSet(list);
        hashSet.removeAll(map.keySet());
        Set set2 = (Set) hashSet.stream().map(new Function() { // from class: com.android.systemui.theme.ThemeOverlayManager$$ExternalSyntheticLambda2
            @Override // java.util.function.Function
            public final Object apply(Object obj) {
                return this.f$0.lambda$applyCurrentUserOverlays$0((String) obj);
            }
        }).collect(Collectors.toSet());
        final ArrayList arrayList = new ArrayList();
        set2.forEach(new Consumer() { // from class: com.android.systemui.theme.ThemeOverlayManager$$ExternalSyntheticLambda1
            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                this.f$0.lambda$applyCurrentUserOverlays$1(arrayList, (String) obj);
            }
        });
        Map map2 = (Map) arrayList.stream().filter(new Predicate() { // from class: com.android.systemui.theme.ThemeOverlayManager$$ExternalSyntheticLambda5
            @Override // java.util.function.Predicate
            public final boolean test(Object obj) {
                return this.f$0.lambda$applyCurrentUserOverlays$2((OverlayInfo) obj);
            }
        }).filter(new Predicate() { // from class: com.android.systemui.theme.ThemeOverlayManager$$ExternalSyntheticLambda6
            @Override // java.util.function.Predicate
            public final boolean test(Object obj) {
                return ThemeOverlayManager.lambda$applyCurrentUserOverlays$3(hashSet, (OverlayInfo) obj);
            }
        }).filter(new Predicate() { // from class: com.android.systemui.theme.ThemeOverlayManager$$ExternalSyntheticLambda7
            @Override // java.util.function.Predicate
            public final boolean test(Object obj) {
                return ((OverlayInfo) obj).isEnabled();
            }
        }).collect(Collectors.toMap(new Function() { // from class: com.android.systemui.theme.ThemeOverlayManager$$ExternalSyntheticLambda4
            @Override // java.util.function.Function
            public final Object apply(Object obj) {
                return ((OverlayInfo) obj).category;
            }
        }, new Function() { // from class: com.android.systemui.theme.ThemeOverlayManager$$ExternalSyntheticLambda3
            @Override // java.util.function.Function
            public final Object apply(Object obj) {
                return ((OverlayInfo) obj).packageName;
            }
        }));
        for (String str : list) {
            if (map.containsKey(str)) {
                setEnabled(map.get(str), str, set, true);
            } else if (map2.containsKey(str)) {
                setEnabled((String) map2.get(str), str, set, false);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ String lambda$applyCurrentUserOverlays$0(String str) {
        return this.mCategoryToTargetPackage.get(str);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$applyCurrentUserOverlays$1(List list, String str) {
        list.addAll(this.mOverlayManager.getOverlayInfosForTarget(str, UserHandle.SYSTEM));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ boolean lambda$applyCurrentUserOverlays$2(OverlayInfo overlayInfo) {
        return this.mTargetPackageToCategories.get(overlayInfo.targetPackageName).contains(overlayInfo.category);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static /* synthetic */ boolean lambda$applyCurrentUserOverlays$3(Set set, OverlayInfo overlayInfo) {
        return set.contains(overlayInfo.category);
    }

    private void setEnabled(String str, String str2, Set<UserHandle> set, boolean z) {
        Iterator<UserHandle> it = set.iterator();
        while (it.hasNext()) {
            setEnabledAsync(str, it.next(), z);
        }
        if (set.contains(UserHandle.SYSTEM) || !SYSTEM_USER_CATEGORIES.contains(str2)) {
            return;
        }
        setEnabledAsync(str, UserHandle.SYSTEM, z);
    }

    private void setEnabledAsync(final String str, final UserHandle userHandle, final boolean z) {
        this.mExecutor.execute(new Runnable() { // from class: com.android.systemui.theme.ThemeOverlayManager$$ExternalSyntheticLambda0
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.lambda$setEnabledAsync$7(str, userHandle, z);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$setEnabledAsync$7(String str, UserHandle userHandle, boolean z) {
        try {
            if (z) {
                this.mOverlayManager.setEnabledExclusiveInCategory(str, userHandle);
            } else {
                this.mOverlayManager.setEnabled(str, false, userHandle);
            }
        } catch (IllegalStateException | SecurityException e) {
            Log.e("ThemeOverlayManager", String.format("setEnabled failed: %s %s %b", str, userHandle, Boolean.valueOf(z)), e);
        }
    }
}
