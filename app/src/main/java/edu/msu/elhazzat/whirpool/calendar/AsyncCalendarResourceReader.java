package edu.msu.elhazzat.whirpool.calendar;

import android.os.AsyncTask;
import android.util.Log;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import edu.msu.elhazzat.whirpool.model.RoomModel;

/**
 * Created by christianwhite on 10/11/15.
 */
public abstract class AsyncCalendarResourceReader extends AsyncTask<Void, Void, Void> {

    private static final String LOG_TAG = AsyncCalendarResourceReader.class.getSimpleName();
    private List<RoomModel> mRoomModels = new ArrayList<>();

    private String mUrl;
    private String mToken;

    public AsyncCalendarResourceReader(String url, String token) {
        mUrl = url;
        mToken = token;
    }

    public abstract void handleRooms(List<RoomModel> roomModels);

    @Override
    public Void doInBackground(Void... params) {
        String nextLink = mUrl;
        while(nextLink != null) {
            Document feedPage = getFeedPage(nextLink, mToken);
            nextLink = getNextLink(feedPage);
            getEntries(feedPage);
        }
        return null;
    }

    /**
     * Get single xml feed page returned via google resources api
     * @param urlStr
     * @param token
     * @return
     */
    private Document getFeedPage(String urlStr, String token) {
        try {
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authorization", "Bearer " + token);
            int responseCode = conn.getResponseCode();
            if (responseCode == HttpsURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new
                        InputStreamReader(conn.getInputStream()));
                String responseXml = reader.readLine();
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                try {
                    DocumentBuilder builder = factory.newDocumentBuilder();
                    return builder.parse(new InputSource(new StringReader(responseXml)));
                } catch (SAXException e) {

                } catch (ParserConfigurationException e) {

                } catch (IOException e) {

                }
            }
        } catch (ProtocolException e) {

        } catch (IOException e) {
            Log.e(LOG_TAG, "Error :", e);
        }

        return null;
    }

    /**
     * Resources api paginates results - need to grab link for next get request
     * @param xmlResource
     * @return
     */
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

    /**
     * Fetch the room/email information for the resulitng resources
     * @param xmlResource
     */
    private void getEntries(Document xmlResource) {
        NodeList entries = xmlResource.getElementsByTagName("entry");
        if(entries != null) {

            //iterate over entries
            for(int i = 0; i < entries.getLength(); i++) {
                RoomModel roomModel = new RoomModel();
                HashMap<String, String> roomResources = new HashMap<>();

                Node entry = entries.item(i);
                NodeList children = entry.getChildNodes();

                for(int k = 0; k < children.getLength(); k++) {
                    Node child = children.item(k);

                    //find tag which will contain attribute "name", "value" and resources
                    if(child != null && child.getNodeName().equals("apps:property")) {
                        NamedNodeMap attributes = child.getAttributes();
                        if(attributes != null) {
                            Node nameAttrNode = child.getAttributes().getNamedItem("name");
                            Node resourceNode = child.getAttributes().getNamedItem("value");
                            if(nameAttrNode != null && resourceNode != null) {
                                String nameValue = nameAttrNode.getNodeValue();
                                String resource = resourceNode.getNodeValue();

                                if (nameValue.equals("resourceCommonName")) {
                                    roomResources.put("name", resource);
                                }
                                else if (nameValue.equals("resourceEmail")) {
                                    roomResources.put("email", resource);
                                }
                            }
                        }
                    }
                }

                //if we have successfully grabbed an email/name, populate our list
                if(roomResources.containsKey("name") && roomResources.containsKey("email")) {
                    String roomName = roomResources.get("name");
                    String[] splitRec = roomName.split("-");
                    String name = "";
                    if (splitRec.length > 1) {
                        String city = splitRec[1];
                        name = city.replaceAll("^\\s+|\\s+$", "");
                        if (!name.isEmpty() && name.equals("Benton Harbor")) {
                            roomModel.setName(roomName);
                            roomModel.setEmail(roomResources.get("email"));
                            mRoomModels.add(roomModel);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onPostExecute(Void param) {
        handleRooms(mRoomModels);
    }
}