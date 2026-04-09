package androidx.slice.compat;

import android.annotation.SuppressLint;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcelable;
import android.os.Process;
import android.os.RemoteException;
import android.os.StrictMode;
import android.util.Log;
import androidx.collection.ArraySet;
import androidx.core.graphics.drawable.IconCompat;
import androidx.core.util.Preconditions;
import androidx.slice.Slice;
import androidx.slice.SliceItemHolder;
import androidx.slice.SliceProvider;
import androidx.slice.SliceSpec;
import androidx.versionedparcelable.ParcelUtils;
import androidx.versionedparcelable.VersionedParcelable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/* loaded from: classes.dex */
public class SliceProviderCompat {
    String mCallback;
    private final Context mContext;
    private CompatPermissionManager mPermissionManager;
    private CompatPinnedList mPinnedList;
    private final SliceProvider mProvider;
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private final Runnable mAnr = new Runnable() { // from class: androidx.slice.compat.SliceProviderCompat.1
        @Override // java.lang.Runnable
        public void run() {
            Process.sendSignal(Process.myPid(), 3);
            Log.wtf("SliceProviderCompat", "Timed out while handling slice callback " + SliceProviderCompat.this.mCallback);
        }
    };

    public SliceProviderCompat(SliceProvider sliceProvider, CompatPermissionManager compatPermissionManager, Context context) {
        this.mProvider = sliceProvider;
        this.mContext = context;
        SharedPreferences sharedPreferences = context.getSharedPreferences("slice_data_all_slice_files", 0);
        Set<String> stringSet = sharedPreferences.getStringSet("slice_data_all_slice_files", Collections.emptySet());
        if (!stringSet.contains("slice_data_androidx.slice.compat.SliceProviderCompat")) {
            ArraySet arraySet = new ArraySet(stringSet);
            arraySet.add("slice_data_androidx.slice.compat.SliceProviderCompat");
            sharedPreferences.edit().putStringSet("slice_data_all_slice_files", arraySet).commit();
        }
        this.mPinnedList = new CompatPinnedList(context, "slice_data_androidx.slice.compat.SliceProviderCompat");
        this.mPermissionManager = compatPermissionManager;
    }

    private Context getContext() {
        return this.mContext;
    }

    public String getCallingPackage() {
        return this.mProvider.getCallingPackage();
    }

    public Bundle call(String str, String str2, Bundle bundle) throws SecurityException {
        if (str.equals("bind_slice")) {
            Uri uri = (Uri) bundle.getParcelable("slice_uri");
            this.mProvider.validateIncomingAuthority(uri.getAuthority());
            Slice sliceHandleBindSlice = handleBindSlice(uri, getSpecs(bundle), getCallingPackage());
            Bundle bundle2 = new Bundle();
            if ("supports_versioned_parcelable".equals(str2)) {
                synchronized (SliceItemHolder.sSerializeLock) {
                    bundle2.putParcelable("slice", sliceHandleBindSlice != null ? ParcelUtils.toParcelable(sliceHandleBindSlice) : null);
                }
            } else {
                bundle2.putParcelable("slice", sliceHandleBindSlice != null ? sliceHandleBindSlice.toBundle() : null);
            }
            return bundle2;
        }
        if (str.equals("map_slice")) {
            Uri uriOnMapIntentToUri = this.mProvider.onMapIntentToUri((Intent) bundle.getParcelable("slice_intent"));
            this.mProvider.validateIncomingAuthority(uriOnMapIntentToUri.getAuthority());
            Bundle bundle3 = new Bundle();
            Slice sliceHandleBindSlice2 = handleBindSlice(uriOnMapIntentToUri, getSpecs(bundle), getCallingPackage());
            if ("supports_versioned_parcelable".equals(str2)) {
                synchronized (SliceItemHolder.sSerializeLock) {
                    bundle3.putParcelable("slice", sliceHandleBindSlice2 != null ? ParcelUtils.toParcelable(sliceHandleBindSlice2) : null);
                }
            } else {
                bundle3.putParcelable("slice", sliceHandleBindSlice2 != null ? sliceHandleBindSlice2.toBundle() : null);
            }
            return bundle3;
        }
        if (str.equals("map_only")) {
            Uri uriOnMapIntentToUri2 = this.mProvider.onMapIntentToUri((Intent) bundle.getParcelable("slice_intent"));
            this.mProvider.validateIncomingAuthority(uriOnMapIntentToUri2.getAuthority());
            Bundle bundle4 = new Bundle();
            bundle4.putParcelable("slice", uriOnMapIntentToUri2);
            return bundle4;
        }
        if (str.equals("pin_slice")) {
            Uri uri2 = (Uri) bundle.getParcelable("slice_uri");
            this.mProvider.validateIncomingAuthority(uri2.getAuthority());
            if (this.mPinnedList.addPin(uri2, bundle.getString("pkg"), getSpecs(bundle))) {
                handleSlicePinned(uri2);
            }
            return null;
        }
        if (str.equals("unpin_slice")) {
            Uri uri3 = (Uri) bundle.getParcelable("slice_uri");
            this.mProvider.validateIncomingAuthority(uri3.getAuthority());
            if (this.mPinnedList.removePin(uri3, bundle.getString("pkg"))) {
                handleSliceUnpinned(uri3);
            }
            return null;
        }
        if (str.equals("get_specs")) {
            Uri uri4 = (Uri) bundle.getParcelable("slice_uri");
            this.mProvider.validateIncomingAuthority(uri4.getAuthority());
            Bundle bundle5 = new Bundle();
            ArraySet<SliceSpec> specs = this.mPinnedList.getSpecs(uri4);
            if (specs.size() == 0) {
                throw new IllegalStateException(uri4 + " is not pinned");
            }
            addSpecs(bundle5, specs);
            return bundle5;
        }
        if (str.equals("get_descendants")) {
            Uri uri5 = (Uri) bundle.getParcelable("slice_uri");
            this.mProvider.validateIncomingAuthority(uri5.getAuthority());
            Bundle bundle6 = new Bundle();
            bundle6.putParcelableArrayList("slice_descendants", new ArrayList<>(handleGetDescendants(uri5)));
            return bundle6;
        }
        if (str.equals("check_perms")) {
            Uri uri6 = (Uri) bundle.getParcelable("slice_uri");
            this.mProvider.validateIncomingAuthority(uri6.getAuthority());
            int i = bundle.getInt("pid");
            int i2 = bundle.getInt("uid");
            Bundle bundle7 = new Bundle();
            bundle7.putInt("result", this.mPermissionManager.checkSlicePermission(uri6, i, i2));
            return bundle7;
        }
        if (str.equals("grant_perms")) {
            Uri uri7 = (Uri) bundle.getParcelable("slice_uri");
            this.mProvider.validateIncomingAuthority(uri7.getAuthority());
            String string = bundle.getString("pkg");
            if (Binder.getCallingUid() != Process.myUid()) {
                throw new SecurityException("Only the owning process can manage slice permissions");
            }
            this.mPermissionManager.grantSlicePermission(uri7, string);
        } else if (str.equals("revoke_perms")) {
            Uri uri8 = (Uri) bundle.getParcelable("slice_uri");
            this.mProvider.validateIncomingAuthority(uri8.getAuthority());
            String string2 = bundle.getString("pkg");
            if (Binder.getCallingUid() != Process.myUid()) {
                throw new SecurityException("Only the owning process can manage slice permissions");
            }
            this.mPermissionManager.revokeSlicePermission(uri8, string2);
        }
        return null;
    }

    private Collection<Uri> handleGetDescendants(Uri uri) {
        this.mCallback = "onGetSliceDescendants";
        return this.mProvider.onGetSliceDescendants(uri);
    }

    private void handleSlicePinned(Uri uri) {
        this.mCallback = "onSlicePinned";
        this.mHandler.postDelayed(this.mAnr, 2000L);
        try {
            this.mProvider.onSlicePinned(uri);
            this.mProvider.handleSlicePinned(uri);
        } finally {
            this.mHandler.removeCallbacks(this.mAnr);
        }
    }

    private void handleSliceUnpinned(Uri uri) {
        this.mCallback = "onSliceUnpinned";
        this.mHandler.postDelayed(this.mAnr, 2000L);
        try {
            this.mProvider.onSliceUnpinned(uri);
            this.mProvider.handleSliceUnpinned(uri);
        } finally {
            this.mHandler.removeCallbacks(this.mAnr);
        }
    }

    private Slice handleBindSlice(Uri uri, Set<SliceSpec> set, String str) {
        if (str == null) {
            str = getContext().getPackageManager().getNameForUid(Binder.getCallingUid());
        }
        if (this.mPermissionManager.checkSlicePermission(uri, Binder.getCallingPid(), Binder.getCallingUid()) != 0) {
            return this.mProvider.createPermissionSlice(uri, str);
        }
        return onBindSliceStrict(uri, set);
    }

    private Slice onBindSliceStrict(Uri uri, Set<SliceSpec> set) {
        StrictMode.ThreadPolicy threadPolicy = StrictMode.getThreadPolicy();
        this.mCallback = "onBindSlice";
        this.mHandler.postDelayed(this.mAnr, 2000L);
        try {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectAll().penaltyDeath().build());
            SliceProvider.setSpecs(set);
            try {
                try {
                    Slice sliceOnBindSlice = this.mProvider.onBindSlice(uri);
                    StrictMode.setThreadPolicy(threadPolicy);
                    return sliceOnBindSlice;
                } finally {
                    SliceProvider.setSpecs(null);
                    this.mHandler.removeCallbacks(this.mAnr);
                }
            } catch (Exception e) {
                Log.wtf("SliceProviderCompat", "Slice with URI " + uri.toString() + " is invalid.", e);
                StrictMode.setThreadPolicy(threadPolicy);
                return null;
            }
        } catch (Throwable th) {
            StrictMode.setThreadPolicy(threadPolicy);
            throw th;
        }
    }

    public static Slice bindSlice(Context context, Uri uri, Set<SliceSpec> set) {
        ProviderHolder providerHolderAcquireClient = acquireClient(context.getContentResolver(), uri);
        if (providerHolderAcquireClient.mProvider == null) {
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
        try {
            Bundle bundle = new Bundle();
            bundle.putParcelable("slice_uri", uri);
            addSpecs(bundle, set);
            return parseSlice(context, providerHolderAcquireClient.mProvider.call("bind_slice", "supports_versioned_parcelable", bundle));
        } catch (RemoteException e) {
            Log.e("SliceProviderCompat", "Unable to bind slice", e);
            return null;
        } finally {
            providerHolderAcquireClient.close();
        }
    }

    public static void addSpecs(Bundle bundle, Set<SliceSpec> set) {
        ArrayList<String> arrayList = new ArrayList<>();
        ArrayList<Integer> arrayList2 = new ArrayList<>();
        for (SliceSpec sliceSpec : set) {
            arrayList.add(sliceSpec.getType());
            arrayList2.add(Integer.valueOf(sliceSpec.getRevision()));
        }
        bundle.putStringArrayList("specs", arrayList);
        bundle.putIntegerArrayList("revs", arrayList2);
    }

    public static Set<SliceSpec> getSpecs(Bundle bundle) {
        ArraySet arraySet = new ArraySet();
        ArrayList<String> stringArrayList = bundle.getStringArrayList("specs");
        ArrayList<Integer> integerArrayList = bundle.getIntegerArrayList("revs");
        if (stringArrayList != null && integerArrayList != null) {
            for (int i = 0; i < stringArrayList.size(); i++) {
                arraySet.add(new SliceSpec(stringArrayList.get(i), integerArrayList.get(i).intValue()));
            }
        }
        return arraySet;
    }

    public static Slice bindSlice(Context context, Intent intent, Set<SliceSpec> set) {
        ActivityInfo activityInfo;
        Bundle bundle;
        Preconditions.checkNotNull(intent, "intent");
        Preconditions.checkArgument((intent.getComponent() == null && intent.getPackage() == null && intent.getData() == null) ? false : true, String.format("Slice intent must be explicit %s", intent));
        ContentResolver contentResolver = context.getContentResolver();
        Uri data = intent.getData();
        if (data != null && "vnd.android.slice".equals(contentResolver.getType(data))) {
            return bindSlice(context, data, set);
        }
        Intent intent2 = new Intent(intent);
        if (!intent2.hasCategory("android.app.slice.category.SLICE")) {
            intent2.addCategory("android.app.slice.category.SLICE");
        }
        List<ResolveInfo> listQueryIntentContentProviders = context.getPackageManager().queryIntentContentProviders(intent2, 0);
        if (listQueryIntentContentProviders == null || listQueryIntentContentProviders.isEmpty()) {
            ResolveInfo resolveInfoResolveActivity = context.getPackageManager().resolveActivity(intent, 128);
            if (resolveInfoResolveActivity == null || (activityInfo = resolveInfoResolveActivity.activityInfo) == null || (bundle = activityInfo.metaData) == null || !bundle.containsKey("android.metadata.SLICE_URI")) {
                return null;
            }
            return bindSlice(context, Uri.parse(resolveInfoResolveActivity.activityInfo.metaData.getString("android.metadata.SLICE_URI")), set);
        }
        Uri uriBuild = new Uri.Builder().scheme("content").authority(listQueryIntentContentProviders.get(0).providerInfo.authority).build();
        ProviderHolder providerHolderAcquireClient = acquireClient(contentResolver, uriBuild);
        if (providerHolderAcquireClient.mProvider == null) {
            throw new IllegalArgumentException("Unknown URI " + uriBuild);
        }
        try {
            Bundle bundle2 = new Bundle();
            bundle2.putParcelable("slice_intent", intent);
            addSpecs(bundle2, set);
            return parseSlice(context, providerHolderAcquireClient.mProvider.call("map_slice", "supports_versioned_parcelable", bundle2));
        } catch (RemoteException e) {
            Log.e("SliceProviderCompat", "Unable to bind slice", e);
            return null;
        } finally {
            providerHolderAcquireClient.close();
        }
    }

    @SuppressLint({"WrongConstant"})
    private static Slice parseSlice(final Context context, Bundle bundle) {
        if (bundle == null) {
            return null;
        }
        synchronized (SliceItemHolder.sSerializeLock) {
            try {
                SliceItemHolder.sHandler = new SliceItemHolder.HolderHandler() { // from class: androidx.slice.compat.SliceProviderCompat.2
                    @Override // androidx.slice.SliceItemHolder.HolderHandler
                    public void handle(SliceItemHolder sliceItemHolder, String str) {
                        VersionedParcelable versionedParcelable = sliceItemHolder.mVersionedParcelable;
                        if (versionedParcelable instanceof IconCompat) {
                            IconCompat iconCompat = (IconCompat) versionedParcelable;
                            iconCompat.checkResource(context);
                            if (iconCompat.getType() == 2 && iconCompat.getResId() == 0) {
                                sliceItemHolder.mVersionedParcelable = null;
                            }
                        }
                    }
                };
                bundle.setClassLoader(SliceProviderCompat.class.getClassLoader());
                Parcelable parcelable = bundle.getParcelable("slice");
                if (parcelable == null) {
                    return null;
                }
                if (parcelable instanceof Bundle) {
                    return new Slice((Bundle) parcelable);
                }
                return (Slice) ParcelUtils.fromParcelable(parcelable);
            } finally {
                SliceItemHolder.sHandler = null;
            }
        }
    }

    public static void pinSlice(Context context, Uri uri, Set<SliceSpec> set) {
        ProviderHolder providerHolderAcquireClient = acquireClient(context.getContentResolver(), uri);
        if (providerHolderAcquireClient.mProvider == null) {
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
        try {
            try {
                Bundle bundle = new Bundle();
                bundle.putParcelable("slice_uri", uri);
                bundle.putString("pkg", context.getPackageName());
                addSpecs(bundle, set);
                providerHolderAcquireClient.mProvider.call("pin_slice", "supports_versioned_parcelable", bundle);
            } catch (RemoteException e) {
                Log.e("SliceProviderCompat", "Unable to pin slice", e);
            }
        } finally {
            providerHolderAcquireClient.close();
        }
    }

    public static void unpinSlice(Context context, Uri uri, Set<SliceSpec> set) {
        ProviderHolder providerHolderAcquireClient = acquireClient(context.getContentResolver(), uri);
        if (providerHolderAcquireClient.mProvider == null) {
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
        try {
            try {
                Bundle bundle = new Bundle();
                bundle.putParcelable("slice_uri", uri);
                bundle.putString("pkg", context.getPackageName());
                addSpecs(bundle, set);
                providerHolderAcquireClient.mProvider.call("unpin_slice", "supports_versioned_parcelable", bundle);
            } catch (RemoteException e) {
                Log.e("SliceProviderCompat", "Unable to unpin slice", e);
            }
        } finally {
            providerHolderAcquireClient.close();
        }
    }

    public static Set<SliceSpec> getPinnedSpecs(Context context, Uri uri) {
        ProviderHolder providerHolderAcquireClient = acquireClient(context.getContentResolver(), uri);
        if (providerHolderAcquireClient.mProvider == null) {
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
        try {
            try {
                Bundle bundle = new Bundle();
                bundle.putParcelable("slice_uri", uri);
                Bundle bundleCall = providerHolderAcquireClient.mProvider.call("get_specs", "supports_versioned_parcelable", bundle);
                if (bundleCall != null) {
                    return getSpecs(bundleCall);
                }
            } catch (RemoteException e) {
                Log.e("SliceProviderCompat", "Unable to get pinned specs", e);
            }
            providerHolderAcquireClient.close();
            return null;
        } finally {
            providerHolderAcquireClient.close();
        }
    }

    public static void grantSlicePermission(Context context, String str, String str2, Uri uri) {
        try {
            ProviderHolder providerHolderAcquireClient = acquireClient(context.getContentResolver(), uri);
            try {
                Bundle bundle = new Bundle();
                bundle.putParcelable("slice_uri", uri);
                bundle.putString("provider_pkg", str);
                bundle.putString("pkg", str2);
                providerHolderAcquireClient.mProvider.call("grant_perms", "supports_versioned_parcelable", bundle);
                providerHolderAcquireClient.close();
            } finally {
            }
        } catch (RemoteException e) {
            Log.e("SliceProviderCompat", "Unable to get slice descendants", e);
        }
    }

    public static List<Uri> getPinnedSlices(Context context) {
        ArrayList arrayList = new ArrayList();
        Iterator<String> it = context.getSharedPreferences("slice_data_all_slice_files", 0).getStringSet("slice_data_all_slice_files", Collections.emptySet()).iterator();
        while (it.hasNext()) {
            arrayList.addAll(new CompatPinnedList(context, it.next()).getPinnedSlices());
        }
        return arrayList;
    }

    private static ProviderHolder acquireClient(ContentResolver contentResolver, Uri uri) {
        ContentProviderClient contentProviderClientAcquireUnstableContentProviderClient = contentResolver.acquireUnstableContentProviderClient(uri);
        if (contentProviderClientAcquireUnstableContentProviderClient == null) {
            throw new IllegalArgumentException("No provider found for " + uri);
        }
        return new ProviderHolder(contentProviderClientAcquireUnstableContentProviderClient);
    }

    private static class ProviderHolder implements AutoCloseable {
        final ContentProviderClient mProvider;

        ProviderHolder(ContentProviderClient contentProviderClient) {
            this.mProvider = contentProviderClient;
        }

        @Override // java.lang.AutoCloseable
        public void close() {
            ContentProviderClient contentProviderClient = this.mProvider;
            if (contentProviderClient == null) {
                return;
            }
            if (Build.VERSION.SDK_INT >= 24) {
                contentProviderClient.close();
            } else {
                contentProviderClient.release();
            }
        }
    }
}
