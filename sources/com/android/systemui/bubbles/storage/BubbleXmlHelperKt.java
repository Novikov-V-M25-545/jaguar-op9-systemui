package com.android.systemui.bubbles.storage;

import android.util.Xml;
import com.android.internal.util.FastXmlSerializer;
import com.android.internal.util.XmlUtils;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

/* compiled from: BubbleXmlHelper.kt */
/* loaded from: classes.dex */
public final class BubbleXmlHelperKt {
    public static final void writeXml(@NotNull OutputStream stream, @NotNull List<BubbleEntity> bubbles) throws IllegalStateException, IOException, IllegalArgumentException {
        Intrinsics.checkParameterIsNotNull(stream, "stream");
        Intrinsics.checkParameterIsNotNull(bubbles, "bubbles");
        FastXmlSerializer fastXmlSerializer = new FastXmlSerializer();
        fastXmlSerializer.setOutput(stream, StandardCharsets.UTF_8.name());
        fastXmlSerializer.startDocument(null, Boolean.TRUE);
        fastXmlSerializer.startTag(null, "bs");
        fastXmlSerializer.attribute(null, "v", String.valueOf(1));
        Iterator<T> it = bubbles.iterator();
        while (it.hasNext()) {
            writeXmlEntry(fastXmlSerializer, (BubbleEntity) it.next());
        }
        fastXmlSerializer.endTag(null, "bs");
        fastXmlSerializer.endDocument();
    }

    private static final void writeXmlEntry(XmlSerializer xmlSerializer, BubbleEntity bubbleEntity) throws IllegalStateException, IOException, IllegalArgumentException {
        try {
            xmlSerializer.startTag(null, "bb");
            xmlSerializer.attribute(null, "uid", String.valueOf(bubbleEntity.getUserId()));
            xmlSerializer.attribute(null, "pkg", bubbleEntity.getPackageName());
            xmlSerializer.attribute(null, "sid", bubbleEntity.getShortcutId());
            xmlSerializer.attribute(null, "key", bubbleEntity.getKey());
            xmlSerializer.attribute(null, "h", String.valueOf(bubbleEntity.getDesiredHeight()));
            xmlSerializer.attribute(null, "hid", String.valueOf(bubbleEntity.getDesiredHeightResId()));
            String title = bubbleEntity.getTitle();
            if (title != null) {
                xmlSerializer.attribute(null, "t", title);
            }
            xmlSerializer.endTag(null, "bb");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @NotNull
    public static final List<BubbleEntity> readXml(@NotNull InputStream stream) throws XmlPullParserException, IOException, NumberFormatException {
        Intrinsics.checkParameterIsNotNull(stream, "stream");
        ArrayList arrayList = new ArrayList();
        XmlPullParser xmlPullParserNewPullParser = Xml.newPullParser();
        Intrinsics.checkExpressionValueIsNotNull(xmlPullParserNewPullParser, "Xml.newPullParser()");
        xmlPullParserNewPullParser.setInput(stream, StandardCharsets.UTF_8.name());
        XmlUtils.beginDocument(xmlPullParserNewPullParser, "bs");
        String attributeWithName = getAttributeWithName(xmlPullParserNewPullParser, "v");
        Integer numValueOf = attributeWithName != null ? Integer.valueOf(Integer.parseInt(attributeWithName)) : null;
        if (numValueOf != null && numValueOf.intValue() == 1) {
            int depth = xmlPullParserNewPullParser.getDepth();
            while (XmlUtils.nextElementWithin(xmlPullParserNewPullParser, depth)) {
                BubbleEntity xmlEntry = readXmlEntry(xmlPullParserNewPullParser);
                if (xmlEntry != null) {
                    arrayList.add(xmlEntry);
                }
            }
        }
        return arrayList;
    }

    private static final BubbleEntity readXmlEntry(XmlPullParser xmlPullParser) throws XmlPullParserException, IOException, NumberFormatException {
        String attributeWithName;
        String attributeWithName2;
        String attributeWithName3;
        while (xmlPullParser.getEventType() != 2) {
            xmlPullParser.next();
        }
        String attributeWithName4 = getAttributeWithName(xmlPullParser, "uid");
        if (attributeWithName4 != null) {
            int i = Integer.parseInt(attributeWithName4);
            String attributeWithName5 = getAttributeWithName(xmlPullParser, "pkg");
            if (attributeWithName5 != null && (attributeWithName = getAttributeWithName(xmlPullParser, "sid")) != null && (attributeWithName2 = getAttributeWithName(xmlPullParser, "key")) != null && (attributeWithName3 = getAttributeWithName(xmlPullParser, "h")) != null) {
                int i2 = Integer.parseInt(attributeWithName3);
                String attributeWithName6 = getAttributeWithName(xmlPullParser, "hid");
                if (attributeWithName6 != null) {
                    return new BubbleEntity(i, attributeWithName5, attributeWithName, attributeWithName2, i2, Integer.parseInt(attributeWithName6), getAttributeWithName(xmlPullParser, "t"));
                }
            }
        }
        return null;
    }

    private static final String getAttributeWithName(@NotNull XmlPullParser xmlPullParser, String str) {
        int attributeCount = xmlPullParser.getAttributeCount();
        for (int i = 0; i < attributeCount; i++) {
            if (Intrinsics.areEqual(xmlPullParser.getAttributeName(i), str)) {
                return xmlPullParser.getAttributeValue(i);
            }
        }
        return null;
    }
}
