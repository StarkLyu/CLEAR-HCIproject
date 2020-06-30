package com.example.clear;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SearchResultUnit {
    public PositionInfo position;
    public String starttimeStr;
    public String endtimeStr;
    public Date starttime;
    public Date endtime;
    public int recordId;
    public int length;
    public int period;
    public int protectionLevel;

    public SearchResultUnit(PositionInfo pos, String start, String end, int record, int per, int protect) throws ParseException {
        position = pos;
        starttimeStr = start;
        endtimeStr = end;
        recordId=record;
        period = per;
        protectionLevel = protect;

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        starttime = format.parse(starttimeStr);
        endtime = format.parse(endtimeStr);
        length = (int) ((endtime.getTime() - starttime.getTime()) / 1000);
    }

    public String ResultString()
    {
        return "地点：" + position.positionName + " 时间：" + starttimeStr + " " + endtimeStr + " " + period + " " + protectionLevel;
    }
}
