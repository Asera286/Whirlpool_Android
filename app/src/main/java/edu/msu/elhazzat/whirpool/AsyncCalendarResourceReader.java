package edu.msu.elhazzat.whirpool;

import android.os.AsyncTask;
import android.util.Log;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Created by christianwhite on 10/11/15.
 */
abstract class AsyncCalendarResourceReader extends AsyncTask<Void, Void, Void> {
    private List<Room> mRooms = new ArrayList<>();
    private static final String LOG_TAG = AsyncCalendarResourceReader.class.getSimpleName();

    private String mUrl;
    private String mToken;

    AsyncCalendarResourceReader(String url, String token) {
        mUrl = url;
        mToken = token;
    }

    public abstract void handleRooms(List<Room> rooms);

    @Override
    public Void doInBackground(Void... params) {
        String nextLink = mUrl;
        while(nextLink != null) {
            try {
                URL url = new URL(nextLink);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Authorization", "Bearer " + mToken);
                int responseCode = conn.getResponseCode();
                if (responseCode == HttpsURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new
                            InputStreamReader(conn.getInputStream()));
                    String responseXml = reader.readLine();
                    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                    try {
                        DocumentBuilder builder = factory.newDocumentBuilder();
                        Document xmlResource = builder.parse(new InputSource(new StringReader(responseXml)));
                        nextLink = getNextLink(xmlResource);
                        getEntries(xmlResource);
                    } catch (ParserConfigurationException e) {
                        Log.e(LOG_TAG, "Error: ", e);
                    } catch (SAXException e) {
                        Log.e(LOG_TAG, "Error: ", e);
                    }
                }
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error: ", e);
            }
        }
        return null;
    }

    @Override
    public void onPostExecute(Void param) {
        handleRooms(mRooms);
    }

    private String getNextLink(Document xmlResource) {
        NodeList links = xmlResource.getElementsByTagName("link");
        if(links != null) {
            for(int i = 0; i < links.getLength(); i++) {
                Node linkItem = links.item(i);
                String value = linkItem.getAttributes().getNamedItem("rel").getNodeValue();
                if(value.equals("next")) {
                     return linkItem.getAttributes().getNamedItem("href").getNodeValue();
                }
            }
        }
        return null;
    }

    private void getEntries(Document xmlResource) {
        NodeList entries = xmlResource.getElementsByTagName("entry");
        NodeList test = xmlResource.getElementsByTagNameNS("apps", "property");
        if(entries != null) {
            for(int i = 0; i < entries.getLength(); i++) {
                Room room = new Room();
                Node entry = entries.item(i);
                NodeList children = entry.getChildNodes();
                for(int k = 0; k < children.getLength(); k++) {
                    Node child = children.item(k);
                    if(child != null && child.getNodeName().equals("apps:property")) {
                        Node resourceItem = child.getAttributes().getNamedItem("name");
                        if(resourceItem != null) {
                            String resourceName = resourceItem.getNodeValue();
                            Node resourceNode = child.getAttributes().getNamedItem("value");
                            if(resourceNode != null) {
                                String resource = resourceNode.getNodeValue();
                                if (resourceName.equals("resourceCommonName")) {
                                    room.setName(resource);
                                } else if (resourceName.equals("resourceEmail")) {
                                    room.setEmail(resource);
                                }
                            }
                        }
                    }
                }
                if(room.getEmail() != null || room.getName() != null) {
                    mRooms.add(room);
                }
            }
        }
    }

}