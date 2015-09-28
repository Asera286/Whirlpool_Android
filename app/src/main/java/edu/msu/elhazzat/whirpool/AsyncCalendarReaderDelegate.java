package edu.msu.elhazzat.whirpool;

import java.util.List;

public interface AsyncCalendarReaderDelegate {
    public void onAsyncFinished(List<com.google.api.services.calendar.model.Event> events);
}
