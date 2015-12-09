package edu.msu.elhazzat.wim.utils;

import android.graphics.Color;

import com.google.android.gms.maps.model.LatLng;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by christianwhite on 11/30/15.
 */
public class MapConstants {
    public static final float DEFAULT_CAMERA_ZOOM_LEVEL = 19;
    public static final float DEFAULT_BUILDING_ZOOM_LEVEL = 18.5f;
    public static final int DEFAULT_STROKE_WIDTH = 1;

    public static final int DEFAULT_SELECTED_ROOM_COLOR = Color.parseColor("#89C4F4");
    public static final int DEFAULT_OCCUPIED_COLOR = Color.parseColor("#D35400");
    public static final int DEFAULT_UNOCCUPIED_COLOR = Color.parseColor("#1BBC9B");
    public static final int DEFAULT_FILL_COLOR = Color.parseColor("#FFF9EC");
    public static final int DEFAULT_STROKE_COLOR = Color.parseColor("#6C7A89");

    // Building constants
    public static final String HALLWAY = "HW";
    public static final String STAIRS = "STR";
    public static final String MENS_BATHROOM = "MB";
    public static final String WOMENS_BATHROOM = "WB";
    public static final String ELEVATOR = "ELV";
    public static final String UNISEX_BATHROOM = "UX";
    public static final String EXIT = "EXT";

    public static final int DEFAULT_FLOOR = 1;

    public static final Map<String, LatLng> DEFAULT_COORD_MAP = new HashMap<>();

    static {
        DEFAULT_COORD_MAP.put("RV", new LatLng(42.1124711728183,-86.4681740900058));
        DEFAULT_COORD_MAP.put("GHQ",new LatLng(42.1495124220138,-86.4429421190094));
    }

}
