package com.android.systemui.crdroid.carrierlabel;

import android.os.Environment;
import android.telephony.Rlog;
import android.util.Xml;
import com.android.internal.util.XmlUtils;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

/* loaded from: classes.dex */
public class SpnOverride {
    private HashMap<String, String> mCarrierSpnMap = new HashMap<>();

    public SpnOverride() throws XmlPullParserException {
        loadSpnOverrides();
    }

    public String getSpn(String str) {
        return this.mCarrierSpnMap.get(str);
    }

    private void loadSpnOverrides() throws XmlPullParserException {
        try {
            FileReader fileReader = new FileReader(new File(Environment.getRootDirectory(), "etc/spn-conf.xml"));
            try {
                XmlPullParser xmlPullParserNewPullParser = Xml.newPullParser();
                xmlPullParserNewPullParser.setInput(fileReader);
                XmlUtils.beginDocument(xmlPullParserNewPullParser, "spnOverrides");
                while (true) {
                    XmlUtils.nextElement(xmlPullParserNewPullParser);
                    if ("spnOverride".equals(xmlPullParserNewPullParser.getName())) {
                        this.mCarrierSpnMap.put(xmlPullParserNewPullParser.getAttributeValue(null, "numeric"), xmlPullParserNewPullParser.getAttributeValue(null, "spn"));
                    } else {
                        fileReader.close();
                        return;
                    }
                }
            } catch (IOException e) {
                Rlog.w("SpnOverride", "Exception in spn-conf parser " + e);
            } catch (XmlPullParserException e2) {
                Rlog.w("SpnOverride", "Exception in spn-conf parser " + e2);
            }
        } catch (FileNotFoundException unused) {
            Rlog.w("SpnOverride", "Can not open " + Environment.getRootDirectory() + "/etc/spn-conf.xml");
        }
    }
}
