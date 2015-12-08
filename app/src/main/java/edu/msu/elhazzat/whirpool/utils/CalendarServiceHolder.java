package edu.msu.elhazzat.whirpool.utils;

import com.google.api.services.calendar.Calendar;

/**
 * Created by christianwhite on 10/17/15.
 */

/*************************************************************************
 * Singleton for persisting access to calendar information
 *************************************************************************/
public class CalendarServiceHolder {
    private com.google.api.services.calendar.Calendar mCalendarService = null;
    public Calendar getService() {return mCalendarService;}
    public void setService(Calendar service) {mCalendarService = service;}

    private static final CalendarServiceHolder holder = new CalendarServiceHolder();
    public static CalendarServiceHolder getInstance() {return holder;}
}