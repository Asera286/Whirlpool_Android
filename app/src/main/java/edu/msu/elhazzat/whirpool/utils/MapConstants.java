package edu.msu.elhazzat.whirpool.utils;

import com.google.android.gms.maps.model.LatLng;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by christianwhite on 11/30/15.
 */
public class MapConstants {
    public static final int DEFAULT_SELECTED_ROOM_COLOR = -7748364;
    public static final int DEFAULT_CAMERA_ZOOM_LEVEL = 19;
    public static final int DEFAULT_BUILDILNG_ZOOM_LEVEL = 18;
    public static final int DEFAULT_FILL_COLOR = -1556;
    public static final int DEFAULT_STROKE_COLOR = -4868683;
    public static final int DEFAULT_STROKE_WIDTH = 1;

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
        DEFAULT_COORD_MAP.put("RV", new LatLng(42.1122928978815,-86.4693121212619));
        DEFAULT_COORD_MAP.put("GHQ",new LatLng(42.1495124220138,-86.4429421190094));
    }

}
