package com.android.systemui.controls.controller;

import android.app.backup.BackupManager;
import android.content.ComponentName;
import android.util.AtomicFile;
import android.util.Log;
import android.util.Xml;
import com.android.systemui.backup.BackupHelper;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import kotlin.collections.CollectionsKt__CollectionsKt;
import kotlin.collections.CollectionsKt___CollectionsKt;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.Ref$IntRef;
import libcore.io.IoUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

/* compiled from: ControlsFavoritePersistenceWrapper.kt */
/* loaded from: classes.dex */
public final class ControlsFavoritePersistenceWrapper {
    public static final Companion Companion = new Companion(null);
    private BackupManager backupManager;
    private final Executor executor;
    private File file;

    public ControlsFavoritePersistenceWrapper(@NotNull File file, @NotNull Executor executor, @Nullable BackupManager backupManager) {
        Intrinsics.checkParameterIsNotNull(file, "file");
        Intrinsics.checkParameterIsNotNull(executor, "executor");
        this.file = file;
        this.executor = executor;
        this.backupManager = backupManager;
    }

    public /* synthetic */ ControlsFavoritePersistenceWrapper(File file, Executor executor, BackupManager backupManager, int i, DefaultConstructorMarker defaultConstructorMarker) {
        this(file, executor, (i & 4) != 0 ? null : backupManager);
    }

    /* compiled from: ControlsFavoritePersistenceWrapper.kt */
    public static final class Companion {
        private Companion() {
        }

        public /* synthetic */ Companion(DefaultConstructorMarker defaultConstructorMarker) {
            this();
        }
    }

    public final void changeFileAndBackupManager(@NotNull File fileName, @Nullable BackupManager backupManager) {
        Intrinsics.checkParameterIsNotNull(fileName, "fileName");
        this.file = fileName;
        this.backupManager = backupManager;
    }

    public final boolean getFileExists() {
        return this.file.exists();
    }

    public final void deleteFile() {
        this.file.delete();
    }

    public final void storeFavorites(@NotNull final List<StructureInfo> structures) {
        Intrinsics.checkParameterIsNotNull(structures, "structures");
        if (!structures.isEmpty() || this.file.exists()) {
            this.executor.execute(new Runnable() { // from class: com.android.systemui.controls.controller.ControlsFavoritePersistenceWrapper.storeFavorites.1
                @Override // java.lang.Runnable
                public final void run() {
                    boolean z;
                    BackupManager backupManager;
                    Log.d("ControlsFavoritePersistenceWrapper", "Saving data to file: " + ControlsFavoritePersistenceWrapper.this.file);
                    AtomicFile atomicFile = new AtomicFile(ControlsFavoritePersistenceWrapper.this.file);
                    synchronized (BackupHelper.Companion.getControlsDataLock()) {
                        try {
                            try {
                                FileOutputStream fileOutputStreamStartWrite = atomicFile.startWrite();
                                z = false;
                                try {
                                    try {
                                        XmlSerializer xmlSerializerNewSerializer = Xml.newSerializer();
                                        xmlSerializerNewSerializer.setOutput(fileOutputStreamStartWrite, "utf-8");
                                        xmlSerializerNewSerializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", false);
                                        xmlSerializerNewSerializer.startDocument(null, Boolean.TRUE);
                                        xmlSerializerNewSerializer.startTag(null, "version");
                                        xmlSerializerNewSerializer.text("1");
                                        xmlSerializerNewSerializer.endTag(null, "version");
                                        xmlSerializerNewSerializer.startTag(null, "structures");
                                        for (StructureInfo structureInfo : structures) {
                                            xmlSerializerNewSerializer.startTag(null, "structure");
                                            xmlSerializerNewSerializer.attribute(null, "component", structureInfo.getComponentName().flattenToString());
                                            xmlSerializerNewSerializer.attribute(null, "structure", structureInfo.getStructure().toString());
                                            xmlSerializerNewSerializer.startTag(null, "controls");
                                            for (ControlInfo controlInfo : structureInfo.getControls()) {
                                                xmlSerializerNewSerializer.startTag(null, "control");
                                                xmlSerializerNewSerializer.attribute(null, "id", controlInfo.getControlId());
                                                xmlSerializerNewSerializer.attribute(null, "title", controlInfo.getControlTitle().toString());
                                                xmlSerializerNewSerializer.attribute(null, "subtitle", controlInfo.getControlSubtitle().toString());
                                                xmlSerializerNewSerializer.attribute(null, "type", String.valueOf(controlInfo.getDeviceType()));
                                                xmlSerializerNewSerializer.endTag(null, "control");
                                            }
                                            xmlSerializerNewSerializer.endTag(null, "controls");
                                            xmlSerializerNewSerializer.endTag(null, "structure");
                                        }
                                        xmlSerializerNewSerializer.endTag(null, "structures");
                                        xmlSerializerNewSerializer.endDocument();
                                        atomicFile.finishWrite(fileOutputStreamStartWrite);
                                        z = true;
                                    } catch (Throwable unused) {
                                        Log.e("ControlsFavoritePersistenceWrapper", "Failed to write file, reverting to previous version");
                                        atomicFile.failWrite(fileOutputStreamStartWrite);
                                    }
                                } finally {
                                    IoUtils.closeQuietly(fileOutputStreamStartWrite);
                                }
                            } catch (IOException e) {
                                Log.e("ControlsFavoritePersistenceWrapper", "Failed to start write file", e);
                                return;
                            }
                        } catch (Throwable th) {
                            throw th;
                        }
                    }
                    if (!z || (backupManager = ControlsFavoritePersistenceWrapper.this.backupManager) == null) {
                        return;
                    }
                    backupManager.dataChanged();
                }
            });
        }
    }

    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r0v1, types: [boolean] */
    /* JADX WARN: Type inference failed for: r0v2, types: [java.lang.AutoCloseable] */
    /* JADX WARN: Type inference failed for: r0v4, types: [java.io.BufferedInputStream, java.io.InputStream, java.lang.AutoCloseable] */
    /* JADX WARN: Type inference failed for: r2v6, types: [java.lang.Object, org.xmlpull.v1.XmlPullParser] */
    /* JADX WARN: Type inference failed for: r5v0, types: [com.android.systemui.controls.controller.ControlsFavoritePersistenceWrapper] */
    @NotNull
    public final List<StructureInfo> readFavorites() {
        List<StructureInfo> xml;
        ?? Exists = this.file.exists();
        if (Exists == 0) {
            Log.d("ControlsFavoritePersistenceWrapper", "No favorites, returning empty list");
            return CollectionsKt__CollectionsKt.emptyList();
        }
        try {
            try {
                Exists = new BufferedInputStream(new FileInputStream(this.file));
                try {
                    Log.d("ControlsFavoritePersistenceWrapper", "Reading data from file: " + this.file);
                    synchronized (BackupHelper.Companion.getControlsDataLock()) {
                        ?? parser = Xml.newPullParser();
                        parser.setInput(Exists, null);
                        Intrinsics.checkExpressionValueIsNotNull(parser, "parser");
                        xml = parseXml(parser);
                    }
                    return xml;
                } catch (IOException e) {
                    throw new IllegalStateException("Failed parsing favorites file: " + this.file, e);
                } catch (XmlPullParserException e2) {
                    throw new IllegalStateException("Failed parsing favorites file: " + this.file, e2);
                }
            } finally {
                IoUtils.closeQuietly((AutoCloseable) Exists);
            }
        } catch (FileNotFoundException unused) {
            Log.i("ControlsFavoritePersistenceWrapper", "No file found");
            return CollectionsKt__CollectionsKt.emptyList();
        }
    }

    private final List<StructureInfo> parseXml(XmlPullParser xmlPullParser) throws XmlPullParserException, IOException {
        Ref$IntRef ref$IntRef = new Ref$IntRef();
        ArrayList arrayList = new ArrayList();
        ArrayList arrayList2 = new ArrayList();
        ComponentName componentNameUnflattenFromString = null;
        String attributeValue = null;
        while (true) {
            int next = xmlPullParser.next();
            ref$IntRef.element = next;
            if (next == 1) {
                return arrayList;
            }
            String name = xmlPullParser.getName();
            if (name == null) {
                name = "";
            }
            if (ref$IntRef.element == 2 && Intrinsics.areEqual(name, "structure")) {
                componentNameUnflattenFromString = ComponentName.unflattenFromString(xmlPullParser.getAttributeValue(null, "component"));
                attributeValue = xmlPullParser.getAttributeValue(null, "structure");
                if (attributeValue == null) {
                    attributeValue = "";
                }
            } else if (ref$IntRef.element == 2 && Intrinsics.areEqual(name, "control")) {
                String attributeValue2 = xmlPullParser.getAttributeValue(null, "id");
                String attributeValue3 = xmlPullParser.getAttributeValue(null, "title");
                String attributeValue4 = xmlPullParser.getAttributeValue(null, "subtitle");
                String str = attributeValue4 != null ? attributeValue4 : "";
                String attributeValue5 = xmlPullParser.getAttributeValue(null, "type");
                Integer numValueOf = attributeValue5 != null ? Integer.valueOf(Integer.parseInt(attributeValue5)) : null;
                if (attributeValue2 != null && attributeValue3 != null && numValueOf != null) {
                    arrayList2.add(new ControlInfo(attributeValue2, attributeValue3, str, numValueOf.intValue()));
                }
            } else if (ref$IntRef.element == 3 && Intrinsics.areEqual(name, "structure")) {
                if (componentNameUnflattenFromString == null) {
                    Intrinsics.throwNpe();
                }
                if (attributeValue == null) {
                    Intrinsics.throwNpe();
                }
                arrayList.add(new StructureInfo(componentNameUnflattenFromString, attributeValue, CollectionsKt___CollectionsKt.toList(arrayList2)));
                arrayList2.clear();
            }
        }
    }
}
