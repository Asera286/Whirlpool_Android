package edu.msu.elhazzat.whirpool;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.util.List;

/**
 * Created by christianwhite on 10/8/15.
 */
public class GeoJsonCoordinatesDeserializer implements JsonDeserializer<BaseCoordinates> {

    @Override
    public BaseCoordinates deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {

        JsonArray jArray = json.getAsJsonArray();
        String jStr = jArray.toString();

        if(jStr.length() >= 3 && jStr.substring(0, 3).equals("[[[")) {
            Type listType = new TypeToken<List<List<List<Double>>>>() {}.getType();
            List<List<List<Double>>> list = new Gson().fromJson(jStr, listType);
            PolygonCoordinates coordinatesPolygon = new PolygonCoordinates();
            coordinatesPolygon.setPolygon(list);
            return coordinatesPolygon;
        }
        else {
            if (jStr.length() >= 2 && jStr.substring(0, 2).equals("[[")) {
                Type listType = new TypeToken<List<List<Double>>>() {
                }.getType();
                List<List<Double>> list = new Gson().fromJson(jStr, listType);
                PolylineCoordinates coordinatesPolyline = new PolylineCoordinates();
                coordinatesPolyline.setPolyline(list);
                return coordinatesPolyline;
            } else if (jStr.length() >= 1 && jStr.substring(0, 1).equals("[")) {
                Type listType = new TypeToken<List<Double>>() {
                }.getType();
                List<Double> list = new Gson().fromJson(jStr, listType);
                PointCoordinates coordinatesPoint = new PointCoordinates();
                coordinatesPoint.setPoint(list);
                return coordinatesPoint;
            }
        }

        return null;
    }
}