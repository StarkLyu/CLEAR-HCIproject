package com.example.clear.danger;

import com.example.clear.SearchResultUnit;

import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class DangerCalculation {
    public Date start;
    public Date end;
    public int searchPeriod;
    public int searchLength;
    public int protectionLevel;

    public DangerCalculation(String starttime, String endtime, int period, int level) throws ParseException {
        SetSearchInfo(starttime, endtime, period);
        protectionLevel = level;
    }

    public void SetSearchInfo(String starttime, String endtime, int period) throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        start = format.parse(starttime);
        end = format.parse(endtime);
        searchPeriod = period;
        searchLength = (int) ((end.getTime() - start.getTime()) / 1000);
    }

    public float Danger(List<SearchResultUnit> results)
    {
        float res = 0;
        int size = results.size();
        for (int i = 0; i < size; i++)
        {
            SearchResultUnit unit = results.get(i);
            res += TimeFactor(unit.length, searchLength, unit.period, searchPeriod) * protectionFactor(unit.protectionLevel);
        }
        return res * protectionFactor(protectionLevel);
    }

    public float TimeFactor(float patientLength, float searchLength, float patientPeriod, float searchPeriod)
    {
        float T1, T2, t1, t2;
        if (patientLength < searchLength)
        {
            T1 = patientLength;
            t1 = patientPeriod;
            T2 = searchLength;
            t2 = searchPeriod;
        }
        else
        {
            T1 = searchLength;
            t1 = searchPeriod;
            T2 = patientLength;
            t2 = patientPeriod;
        }
        return (t1 * t1 + 2 * T1 * t1 + t1 * t2) / (T1 * T2);
    }

    public float protectionFactor(int level)
    {
        switch (level)
        {
            case 0:
                return 1;
            case 1:
                return 0.3f;
            case 2:
                return 0.01f;
            default:
                return 0;
        }
    }
}
