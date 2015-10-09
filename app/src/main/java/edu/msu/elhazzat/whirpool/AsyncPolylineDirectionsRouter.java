package edu.msu.elhazzat.whirpool;

import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.PolyUtil;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Created by christianwhite on 10/4/15.
 */
public class AsyncPolylineDirectionsRouter extends AsyncTask<Void, Integer, ArrayList<LatLng>> {

    private static final String LOG_TAG = AsyncPolylineDirectionsRouter.class.getSimpleName();

    public interface AsyncPolylineDirectionsRouterDelegate {
        public void handleRouteCoordinates(ArrayList<LatLng> coordinates);
    }

    private static final String GOOGLE_MAPS_URL_BASE =
            "http://maps.googleapis.com/maps/api/directions/xml?sensor=false&language=pt";

    private AsyncPolylineDirectionsRouterDelegate mDelegate;
    private String mFrom;
    private String mDestination;

    public AsyncPolylineDirectionsRouter(AsyncPolylineDirectionsRouterDelegate delegate, String from, String to) {
        mDelegate = delegate;
        mFrom = from;
        mDestination = to;
    }

    private String buildQueryUrl() {
        StringBuilder url = new StringBuilder(GOOGLE_MAPS_URL_BASE);

        url.append("&origin=");
        url.append(mFrom.replace(' ', '+'));
        url.append("&destination=");
        url.append(mDestination.replace(' ', '+'));

        return url.toString();
    }

    private Document getXmlDocument(InputStream stream) {
        try {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setIgnoringComments(true);

            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = documentBuilder.parse(stream);
            document.getDocumentElement().normalize();

            return document;
        }
        catch(ParserConfigurationException e) {
            Log.e(LOG_TAG, "Error: ", e);
        }
        catch(SAXException e) {
            Log.e(LOG_TAG, "Error: ", e);
        }
        catch(IOException e) {
            Log.e(LOG_TAG, "Error: ", e);
        }

        return null;
    }

    @Override
    protected ArrayList<LatLng> doInBackground(Void... params) {
        try {
            String url = buildQueryUrl();
            URL xmlUrl = new URL(url);
            InputStream stream = xmlUrl.openStream();

            Document document = getXmlDocument(stream);

            if(document == null) {
                return null;
            }

            String status = document.getElementsByTagName("status").item(0).getTextContent();

            if(!"OK".equals(status)) {
                return null;
            }

            Element leg = (Element) document.getElementsByTagName("leg").item(0);
            NodeList steps = leg.getElementsByTagName("step");

            ArrayList<LatLng> coordinates = new ArrayList<LatLng>();
            for(int i = 0; i < steps.getLength(); i++) {
                Node nodeStep = steps.item(i);
                if(nodeStep.getNodeType() == Node.ELEMENT_NODE) {

                    Element step = (Element) nodeStep;

                    List<LatLng> partialCoordinates = PolyUtil.decode(step.getElementsByTagName("points")
                            .item(0).getTextContent());

                    coordinates.addAll(partialCoordinates);
                }
            }
            return coordinates;
        }
        catch(IOException e) {
            Log.e(LOG_TAG, "Error: ", e);
            return null;
        }
    }

    @Override
    protected void onPostExecute(ArrayList<LatLng> coordinates) {
        mDelegate.handleRouteCoordinates(coordinates);
    }
}

