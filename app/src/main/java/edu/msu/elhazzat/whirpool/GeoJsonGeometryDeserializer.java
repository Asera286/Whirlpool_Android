package edu.msu.elhazzat.whirpool;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

/**
 * Created by christianwhite on 10/13/15.
 */
public class GeoJsonGeometryDeserializer implements JsonDeserializer<GeoJsonGeometry> {

    @Override
    public GeoJsonGeometry deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {

        JsonObject jsonObj = (JsonObject)json;
        String type = jsonObj.get("type").getAsString();
        JsonElement coordinatesJson1 = jsonObj.get("coordinates");
        JsonArray arr = coordinatesJson1.getAsJsonArray();
        String coordinatesJson = arr.toString();

        Type listType = null;
        Geometry<?> geometry = null;
        switch(type) {
            case "Point":
                listType = new TypeToken<List<Double>>() {}.getType();
                List<Double> point = new Gson().fromJson(coordinatesJson, listType);
                geometry = new GeoJsonPoint(point);
                break;
            case "LineString":
                listType = new TypeToken<List<List<Double>>>() {}.getType();
                List<List<Double>> lineString = new Gson().fromJson(coordinatesJson, listType);
                geometry = new GeoJsonPolyline(lineString);
                break;
            case "Polygon":
                listType = new TypeToken<List<List<List<Double>>>>() {}.getType();
                List<List<List<Double>>> polygon = new Gson().fromJson(coordinatesJson, listType);
                geometry = new GeoJsonPolygon(polygon);
                break;
        }

        GeoJsonGeometry geoJsonGeometry = new GeoJsonGeometry();
        geoJsonGeometry.setType(type);
        geoJsonGeometry.setGeometry(geometry);

        return geoJsonGeometry;
    }

}