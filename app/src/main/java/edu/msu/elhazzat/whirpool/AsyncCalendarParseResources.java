package edu.msu.elhazzat.whirpool;

import android.os.AsyncTask;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Created by christianwhite on 10/13/15.
 */
public class AsyncCalendarParseResources extends AsyncTask<Void, Void, Void> {
    private Document resourceXml;

    @Override
    public Void doInBackground(Void... params) {
        Element leg = (Element) resourceXml.getElementsByTagName("feed").item(0);
        NodeList steps = leg.getElementsByTagName("step");

        return null;
    }
}
