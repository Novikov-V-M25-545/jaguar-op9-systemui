package kotlinx.coroutines.internal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import kotlin.ExceptionsKt__ExceptionsKt;
import kotlin.TypeCastException;
import kotlin.collections.CollectionsKt__IterablesKt;
import kotlin.collections.CollectionsKt__MutableCollectionsKt;
import kotlin.collections.CollectionsKt___CollectionsKt;
import kotlin.io.CloseableKt;
import kotlin.jvm.internal.Intrinsics;
import kotlin.text.StringsKt__StringsJVMKt;
import kotlin.text.StringsKt__StringsKt;
import org.jetbrains.annotations.NotNull;

/* compiled from: FastServiceLoader.kt */
/* loaded from: classes2.dex */
public final class FastServiceLoader {
    public static final FastServiceLoader INSTANCE = new FastServiceLoader();

    private FastServiceLoader() {
    }

    @NotNull
    public final <S> List<S> load$kotlinx_coroutines_core(@NotNull Class<S> service, @NotNull ClassLoader loader) {
        Intrinsics.checkParameterIsNotNull(service, "service");
        Intrinsics.checkParameterIsNotNull(loader, "loader");
        try {
            return loadProviders$kotlinx_coroutines_core(service, loader);
        } catch (Throwable unused) {
            ServiceLoader serviceLoaderLoad = ServiceLoader.load(service, loader);
            Intrinsics.checkExpressionValueIsNotNull(serviceLoaderLoad, "ServiceLoader.load(service, loader)");
            return CollectionsKt___CollectionsKt.toList(serviceLoaderLoad);
        }
    }

    @NotNull
    public final <S> List<S> loadProviders$kotlinx_coroutines_core(@NotNull Class<S> service, @NotNull ClassLoader loader) throws IOException {
        Intrinsics.checkParameterIsNotNull(service, "service");
        Intrinsics.checkParameterIsNotNull(loader, "loader");
        Enumeration<URL> urls = loader.getResources("META-INF/services/" + service.getName());
        Intrinsics.checkExpressionValueIsNotNull(urls, "urls");
        ArrayList<URL> list = Collections.list(urls);
        Intrinsics.checkExpressionValueIsNotNull(list, "java.util.Collections.list(this)");
        ArrayList arrayList = new ArrayList();
        for (URL it : list) {
            FastServiceLoader fastServiceLoader = INSTANCE;
            Intrinsics.checkExpressionValueIsNotNull(it, "it");
            CollectionsKt__MutableCollectionsKt.addAll(arrayList, fastServiceLoader.parse(it));
        }
        Set set = CollectionsKt___CollectionsKt.toSet(arrayList);
        if (!(!set.isEmpty())) {
            throw new IllegalArgumentException("No providers were loaded with FastServiceLoader".toString());
        }
        ArrayList arrayList2 = new ArrayList(CollectionsKt__IterablesKt.collectionSizeOrDefault(set, 10));
        Iterator it2 = set.iterator();
        while (it2.hasNext()) {
            arrayList2.add(INSTANCE.getProviderInstance((String) it2.next(), loader, service));
        }
        return arrayList2;
    }

    private final <S> S getProviderInstance(String str, ClassLoader classLoader, Class<S> cls) throws ClassNotFoundException {
        Class<?> cls2 = Class.forName(str, false, classLoader);
        if (!cls.isAssignableFrom(cls2)) {
            throw new IllegalArgumentException(("Expected service of class " + cls + ", but found " + cls2).toString());
        }
        return cls.cast(cls2.getDeclaredConstructor(new Class[0]).newInstance(new Object[0]));
    }

    private final List<String> parse(URL url) throws IOException {
        BufferedReader bufferedReader;
        String string = url.toString();
        Intrinsics.checkExpressionValueIsNotNull(string, "url.toString()");
        if (StringsKt__StringsJVMKt.startsWith$default(string, "jar", false, 2, null)) {
            String strSubstringBefore$default = StringsKt__StringsKt.substringBefore$default(StringsKt__StringsKt.substringAfter$default(string, "jar:file:", null, 2, null), '!', (String) null, 2, (Object) null);
            String strSubstringAfter$default = StringsKt__StringsKt.substringAfter$default(string, "!/", null, 2, null);
            JarFile jarFile = new JarFile(strSubstringBefore$default, false);
            try {
                bufferedReader = new BufferedReader(new InputStreamReader(jarFile.getInputStream(new ZipEntry(strSubstringAfter$default)), "UTF-8"));
                try {
                    List<String> file = INSTANCE.parseFile(bufferedReader);
                    CloseableKt.closeFinally(bufferedReader, null);
                    jarFile.close();
                    return file;
                } finally {
                }
            } catch (Throwable th) {
                try {
                    throw th;
                } catch (Throwable th2) {
                    try {
                        jarFile.close();
                        throw th2;
                    } catch (Throwable th3) {
                        ExceptionsKt__ExceptionsKt.addSuppressed(th, th3);
                        throw th;
                    }
                }
            }
        } else {
            bufferedReader = new BufferedReader(new InputStreamReader(url.openStream()));
            try {
                List<String> file2 = INSTANCE.parseFile(bufferedReader);
                CloseableKt.closeFinally(bufferedReader, null);
                return file2;
            } catch (Throwable th4) {
                try {
                    throw th4;
                } finally {
                }
            }
        }
    }

    private final List<String> parseFile(BufferedReader bufferedReader) throws IOException {
        boolean z;
        LinkedHashSet linkedHashSet = new LinkedHashSet();
        while (true) {
            String line = bufferedReader.readLine();
            if (line != null) {
                String strSubstringBefore$default = StringsKt__StringsKt.substringBefore$default(line, "#", (String) null, 2, (Object) null);
                if (strSubstringBefore$default == null) {
                    throw new TypeCastException("null cannot be cast to non-null type kotlin.CharSequence");
                }
                String string = StringsKt__StringsKt.trim(strSubstringBefore$default).toString();
                int i = 0;
                while (true) {
                    if (i >= string.length()) {
                        z = true;
                        break;
                    }
                    char cCharAt = string.charAt(i);
                    if (!(cCharAt == '.' || Character.isJavaIdentifierPart(cCharAt))) {
                        z = false;
                        break;
                    }
                    i++;
                }
                if (!z) {
                    throw new IllegalArgumentException(("Illegal service provider class name: " + string).toString());
                }
                if (string.length() > 0) {
                    linkedHashSet.add(string);
                }
            } else {
                return CollectionsKt___CollectionsKt.toList(linkedHashSet);
            }
        }
    }
}
