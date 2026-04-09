package com.android.systemui.crdroid.header;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.Settings;
import android.util.Log;
import com.android.internal.util.crdroid.Utils;
import com.android.systemui.crdroid.header.StatusBarHeaderMachine;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

/* loaded from: classes.dex */
public class DaylightHeaderProvider implements StatusBarHeaderMachine.IStatusBarHeaderProvider {
    private PendingIntent mAlarmIntent;
    private int mAlarmIntervalMinutes = 0;
    private Context mContext;
    private int mHeaderIndex;
    private String mHeaderName;
    private List<DaylightHeaderInfo> mHeadersList;
    private boolean mLinearMode;
    private String mPackageName;
    private boolean mRandomMode;
    private Resources mRes;
    private String mSettingHeaderPackage;

    @Override // com.android.systemui.crdroid.header.StatusBarHeaderMachine.IStatusBarHeaderProvider
    public String getName() {
        return "daylight";
    }

    private class DaylightHeaderInfo {
        public int mDay;
        public int mHour;
        public String mImage;
        public int mMonth;
        public int mType;

        private DaylightHeaderInfo() {
            this.mType = 0;
            this.mHour = -1;
            this.mDay = -1;
            this.mMonth = -1;
        }
    }

    public DaylightHeaderProvider(Context context) {
        this.mContext = context;
    }

    @Override // com.android.systemui.crdroid.header.StatusBarHeaderMachine.IStatusBarHeaderProvider
    public void settingsChanged(Uri uri) throws Throwable {
        String stringForUser = Settings.System.getStringForUser(this.mContext.getContentResolver(), "status_bar_daylight_header_pack", -2);
        if (Settings.System.getIntForUser(this.mContext.getContentResolver(), "status_bar_custom_header", 0, -2) == 1) {
            stopAlarm();
            if (stringForUser == null) {
                loadDefaultHeaderPackage();
            } else {
                String str = this.mSettingHeaderPackage;
                if (str == null || !stringForUser.equals(str)) {
                    this.mSettingHeaderPackage = stringForUser;
                    loadCustomHeaderPackage();
                }
            }
            startAlarm();
        }
    }

    @Override // com.android.systemui.crdroid.header.StatusBarHeaderMachine.IStatusBarHeaderProvider
    public void enableProvider() throws Throwable {
        settingsChanged(null);
    }

    @Override // com.android.systemui.crdroid.header.StatusBarHeaderMachine.IStatusBarHeaderProvider
    public void disableProvider() {
        stopAlarm();
    }

    private void stopAlarm() {
        if (this.mAlarmIntent != null) {
            ((AlarmManager) this.mContext.getSystemService("alarm")).cancel(this.mAlarmIntent);
        }
        this.mAlarmIntent = null;
    }

    private void startAlarm() {
        Calendar calendar = Calendar.getInstance();
        AlarmManager alarmManager = (AlarmManager) this.mContext.getSystemService("alarm");
        PendingIntent pendingIntent = this.mAlarmIntent;
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent);
        }
        this.mAlarmIntent = PendingIntent.getBroadcast(this.mContext, 0, new Intent("com.android.systemui.crdroid.header.STATUS_BAR_HEADER_UPDATE"), 268435456);
        if (this.mAlarmIntervalMinutes == 0) {
            calendar.add(11, 1);
            calendar.set(12, 0);
        }
        alarmManager.setInexactRepeating(1, calendar.getTimeInMillis(), this.mAlarmIntervalMinutes == 0 ? 3600000L : r0 * 60 * 1000, this.mAlarmIntent);
    }

    private void loadCustomHeaderPackage() throws Throwable {
        if (this.mSettingHeaderPackage.indexOf("/") != -1) {
            String[] strArrSplit = this.mSettingHeaderPackage.split("/");
            this.mPackageName = strArrSplit[0];
            this.mHeaderName = strArrSplit[1];
        } else {
            this.mPackageName = this.mSettingHeaderPackage;
            this.mHeaderName = null;
        }
        try {
            this.mRes = this.mContext.getPackageManager().getResourcesForApplication(this.mPackageName);
            loadHeaders();
        } catch (Exception e) {
            Log.e("DaylightHeaderProvider", "Failed to load icon pack " + this.mHeaderName, e);
            this.mRes = null;
        }
        if (this.mRes == null) {
            Log.w("DaylightHeaderProvider", "Header pack loading failed - loading default");
            loadDefaultHeaderPackage();
        }
    }

    private void loadDefaultHeaderPackage() throws Throwable {
        this.mPackageName = "com.android.systemui";
        this.mHeaderName = null;
        this.mSettingHeaderPackage = "com.android.systemui";
        try {
            this.mRes = this.mContext.getPackageManager().getResourcesForApplication(this.mPackageName);
            loadHeaders();
        } catch (Exception unused) {
            this.mRes = null;
        }
        if (this.mRes == null) {
            Log.w("DaylightHeaderProvider", "No default package found");
        }
    }

    private void loadHeaders() throws Throwable {
        InputStream inputStreamOpen;
        this.mHeadersList = new ArrayList();
        XmlPullParser xmlPullParserNewPullParser = null;
        try {
            String str = this.mHeaderName;
            if (str == null) {
                inputStreamOpen = this.mRes.getAssets().open("daylight_header.xml");
            } else {
                inputStreamOpen = this.mRes.getAssets().open(this.mHeaderName.substring(str.lastIndexOf(".") + 1) + ".xml");
            }
        } catch (Throwable th) {
            th = th;
            inputStreamOpen = null;
        }
        try {
            xmlPullParserNewPullParser = XmlPullParserFactory.newInstance().newPullParser();
            xmlPullParserNewPullParser.setInput(inputStreamOpen, "UTF-8");
            loadResourcesFromXmlParser(xmlPullParserNewPullParser);
            if (xmlPullParserNewPullParser instanceof XmlResourceParser) {
                ((XmlResourceParser) xmlPullParserNewPullParser).close();
            }
            if (inputStreamOpen != null) {
                try {
                    inputStreamOpen.close();
                } catch (IOException unused) {
                }
            }
        } catch (Throwable th2) {
            th = th2;
            if (xmlPullParserNewPullParser instanceof XmlResourceParser) {
                ((XmlResourceParser) xmlPullParserNewPullParser).close();
            }
            if (inputStreamOpen != null) {
                try {
                    inputStreamOpen.close();
                } catch (IOException unused2) {
                }
            }
            throw th;
        }
    }

    private void loadResourcesFromXmlParser(XmlPullParser xmlPullParser) throws XmlPullParserException, IOException {
        int eventType = xmlPullParser.getEventType();
        this.mRandomMode = false;
        this.mLinearMode = false;
        this.mAlarmIntervalMinutes = 0;
        do {
            if (eventType == 2) {
                String name = xmlPullParser.getName();
                if (name.equalsIgnoreCase("day_header")) {
                    if (!this.mRandomMode) {
                        DaylightHeaderInfo daylightHeaderInfo = new DaylightHeaderInfo();
                        daylightHeaderInfo.mHour = -1;
                        daylightHeaderInfo.mType = 0;
                        String attributeValue = xmlPullParser.getAttributeValue(null, "day");
                        if (attributeValue != null) {
                            daylightHeaderInfo.mDay = Integer.valueOf(attributeValue).intValue();
                        }
                        String attributeValue2 = xmlPullParser.getAttributeValue(null, "month");
                        if (attributeValue2 != null) {
                            daylightHeaderInfo.mMonth = Integer.valueOf(attributeValue2).intValue();
                        }
                        String attributeValue3 = xmlPullParser.getAttributeValue(null, "hour");
                        if (attributeValue3 != null) {
                            daylightHeaderInfo.mHour = Integer.valueOf(attributeValue3).intValue();
                        }
                        String attributeValue4 = xmlPullParser.getAttributeValue(null, "image");
                        if (attributeValue4 != null) {
                            daylightHeaderInfo.mImage = attributeValue4;
                        }
                        if (daylightHeaderInfo.mImage != null && daylightHeaderInfo.mDay != -1 && daylightHeaderInfo.mMonth != -1) {
                            this.mHeadersList.add(daylightHeaderInfo);
                        }
                    }
                } else if (name.equalsIgnoreCase("hour_header")) {
                    if (!this.mRandomMode) {
                        DaylightHeaderInfo daylightHeaderInfo2 = new DaylightHeaderInfo();
                        daylightHeaderInfo2.mType = 1;
                        String attributeValue5 = xmlPullParser.getAttributeValue(null, "hour");
                        if (attributeValue5 != null) {
                            daylightHeaderInfo2.mHour = Integer.valueOf(attributeValue5).intValue();
                        }
                        String attributeValue6 = xmlPullParser.getAttributeValue(null, "image");
                        if (attributeValue6 != null) {
                            daylightHeaderInfo2.mImage = attributeValue6;
                        }
                        if (daylightHeaderInfo2.mImage != null && daylightHeaderInfo2.mHour != -1) {
                            this.mHeadersList.add(daylightHeaderInfo2);
                        }
                    }
                } else if (name.equalsIgnoreCase("random_header") || name.equalsIgnoreCase("list_header")) {
                    this.mRandomMode = name.equalsIgnoreCase("random_header");
                    this.mLinearMode = name.equalsIgnoreCase("list_header");
                    DaylightHeaderInfo daylightHeaderInfo3 = new DaylightHeaderInfo();
                    daylightHeaderInfo3.mType = 2;
                    String attributeValue7 = xmlPullParser.getAttributeValue(null, "image");
                    if (attributeValue7 != null) {
                        daylightHeaderInfo3.mImage = attributeValue7;
                    }
                    if (daylightHeaderInfo3.mImage != null) {
                        this.mHeadersList.add(daylightHeaderInfo3);
                    }
                } else if (name.equalsIgnoreCase("change_interval")) {
                    this.mAlarmIntervalMinutes = Integer.valueOf(xmlPullParser.getAttributeValue(null, "minutes")).intValue();
                }
            }
            eventType = xmlPullParser.next();
        } while (eventType != 1);
        if (this.mRandomMode) {
            Collections.shuffle(this.mHeadersList);
        }
        if (this.mLinearMode || this.mRandomMode) {
            return;
        }
        this.mAlarmIntervalMinutes = 0;
    }

    private DaylightHeaderInfo getLastHourHeader(List<DaylightHeaderInfo> list) {
        DaylightHeaderInfo daylightHeaderInfo = null;
        if (list != null && list.size() != 0) {
            int i = -1;
            for (DaylightHeaderInfo daylightHeaderInfo2 : list) {
                int i2 = daylightHeaderInfo2.mHour;
                if (i2 != -1 && (daylightHeaderInfo == null || i2 > i)) {
                    daylightHeaderInfo = daylightHeaderInfo2;
                    i = i2;
                }
            }
        }
        return daylightHeaderInfo;
    }

    private DaylightHeaderInfo getFirstHourHeader(List<DaylightHeaderInfo> list) {
        DaylightHeaderInfo daylightHeaderInfo = null;
        if (list != null && list.size() != 0) {
            int i = -1;
            for (DaylightHeaderInfo daylightHeaderInfo2 : list) {
                int i2 = daylightHeaderInfo2.mHour;
                if (i2 != -1 && (daylightHeaderInfo == null || i2 < i)) {
                    daylightHeaderInfo = daylightHeaderInfo2;
                    i = i2;
                }
            }
        }
        return daylightHeaderInfo;
    }

    @Override // com.android.systemui.crdroid.header.StatusBarHeaderMachine.IStatusBarHeaderProvider
    public Drawable getCurrent(Calendar calendar) throws Throwable {
        List<DaylightHeaderInfo> list;
        if (!Utils.isPackageInstalled(this.mContext, this.mPackageName)) {
            Log.w("DaylightHeaderProvider", "Header pack no longer available - loading default " + this.mPackageName);
            loadDefaultHeaderPackage();
        }
        if (this.mRes != null && (list = this.mHeadersList) != null && list.size() != 0) {
            try {
                if (!this.mRandomMode && !this.mLinearMode) {
                    List<DaylightHeaderInfo> todayHeaders = getTodayHeaders(calendar);
                    if (todayHeaders.size() != 0) {
                        DaylightHeaderInfo firstHourHeader = getFirstHourHeader(todayHeaders);
                        DaylightHeaderInfo lastHourHeader = getLastHourHeader(todayHeaders);
                        if (firstHourHeader != null && lastHourHeader != null) {
                            DaylightHeaderInfo matchingHeader = getMatchingHeader(calendar, todayHeaders);
                            Resources resources = this.mRes;
                            return resources.getDrawable(resources.getIdentifier(matchingHeader.mImage, "drawable", this.mPackageName), null);
                        }
                        Resources resources2 = this.mRes;
                        return resources2.getDrawable(resources2.getIdentifier(todayHeaders.get(0).mImage, "drawable", this.mPackageName), null);
                    }
                    List<DaylightHeaderInfo> hourHeaders = getHourHeaders();
                    if (hourHeaders.size() != 0) {
                        DaylightHeaderInfo matchingHeader2 = getMatchingHeader(calendar, hourHeaders);
                        Resources resources3 = this.mRes;
                        return resources3.getDrawable(resources3.getIdentifier(matchingHeader2.mImage, "drawable", this.mPackageName), null);
                    }
                }
                DaylightHeaderInfo daylightHeaderInfo = this.mHeadersList.get(this.mHeaderIndex);
                int i = this.mHeaderIndex + 1;
                this.mHeaderIndex = i;
                if (i == this.mHeadersList.size()) {
                    if (this.mRandomMode) {
                        Collections.shuffle(this.mHeadersList);
                    }
                    this.mHeaderIndex = 0;
                }
                Resources resources4 = this.mRes;
                return resources4.getDrawable(resources4.getIdentifier(daylightHeaderInfo.mImage, "drawable", this.mPackageName), null);
            } catch (Resources.NotFoundException unused) {
                Log.w("DaylightHeaderProvider", "No drawable found for " + calendar + " in " + this.mPackageName);
            }
        }
        return null;
    }

    private boolean isItToday(Calendar calendar, DaylightHeaderInfo daylightHeaderInfo) {
        return calendar.get(2) + 1 == daylightHeaderInfo.mMonth && calendar.get(5) == daylightHeaderInfo.mDay;
    }

    private List<DaylightHeaderInfo> getTodayHeaders(Calendar calendar) {
        ArrayList arrayList = new ArrayList();
        for (DaylightHeaderInfo daylightHeaderInfo : this.mHeadersList) {
            if (daylightHeaderInfo.mType == 0 && isItToday(calendar, daylightHeaderInfo)) {
                arrayList.add(daylightHeaderInfo);
            }
        }
        return arrayList;
    }

    private List<DaylightHeaderInfo> getHourHeaders() {
        ArrayList arrayList = new ArrayList();
        for (DaylightHeaderInfo daylightHeaderInfo : this.mHeadersList) {
            if (daylightHeaderInfo.mType == 1) {
                arrayList.add(daylightHeaderInfo);
            }
        }
        return arrayList;
    }

    private DaylightHeaderInfo getMatchingHeader(Calendar calendar, List<DaylightHeaderInfo> list) {
        DaylightHeaderInfo firstHourHeader = getFirstHourHeader(list);
        DaylightHeaderInfo lastHourHeader = getLastHourHeader(list);
        Iterator<DaylightHeaderInfo> it = list.iterator();
        DaylightHeaderInfo daylightHeaderInfo = firstHourHeader;
        while (it.hasNext()) {
            DaylightHeaderInfo next = it.next();
            if (next.mHour > calendar.get(11)) {
                return next == firstHourHeader ? lastHourHeader : daylightHeaderInfo;
            }
            daylightHeaderInfo = next;
        }
        return lastHourHeader;
    }
}
