package com.android.settingslib.datetime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import libcore.timezone.CountryTimeZones;
import libcore.timezone.TimeZoneFinder;

/* loaded from: classes.dex */
public final class ZoneGetter$ZoneGetterData {
    public List<String> lookupTimeZoneIdsByCountry(String str) {
        CountryTimeZones countryTimeZonesLookupCountryTimeZones = TimeZoneFinder.getInstance().lookupCountryTimeZones(str);
        if (countryTimeZonesLookupCountryTimeZones == null) {
            return null;
        }
        return extractTimeZoneIds(countryTimeZonesLookupCountryTimeZones.getTimeZoneMappings());
    }

    private static List<String> extractTimeZoneIds(List<CountryTimeZones.TimeZoneMapping> list) {
        ArrayList arrayList = new ArrayList(list.size());
        Iterator<CountryTimeZones.TimeZoneMapping> it = list.iterator();
        while (it.hasNext()) {
            arrayList.add(it.next().getTimeZoneId());
        }
        return Collections.unmodifiableList(arrayList);
    }
}
