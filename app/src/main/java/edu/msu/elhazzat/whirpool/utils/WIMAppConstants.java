package edu.msu.elhazzat.whirpool.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by christianwhite on 11/5/15.
 */
public class WIMAppConstants {
    public static final String WHIRLPOOL_DEFAULT = "whirlpoolDefault";
    public static final String BENSON_ROAD = "Benson Road";
    public static final String BHTC = "BHTC";
    public static final String EDGEWATER = "Edgewater";
    public static final String GHQ = "GHQ";
    public static final String HARBORTOWN = "Harbortown";
    public static final String HILLTOP_150 = "Hilltop 150";
    public static final String HILLTOP_211 = "Hilltop 211";
    public static final String MMC = "MMC";
    public static final String R_AND_E = "R&E";
    public static final String RIVERVIEW = "Riverview";
    public static final String ST_JOE_TECH_CENTER = "St. Joe Tech Center";

    public static final Map<String, String> WHIRLPOOL_ABBRV_MAP = new HashMap<>();

    static {
        WHIRLPOOL_ABBRV_MAP.put(BENSON_ROAD, "BEN");
        WHIRLPOOL_ABBRV_MAP.put(BHTC, "BHTC");
        WHIRLPOOL_ABBRV_MAP.put(EDGEWATER, "ETC");
        WHIRLPOOL_ABBRV_MAP.put(GHQ, "GHQ");
        WHIRLPOOL_ABBRV_MAP.put(HARBORTOWN, "HBT");
        WHIRLPOOL_ABBRV_MAP.put(HILLTOP_150, "HTPS");
        WHIRLPOOL_ABBRV_MAP.put(HILLTOP_211, "HTPN");
        WHIRLPOOL_ABBRV_MAP.put(MMC, "MMC");
        WHIRLPOOL_ABBRV_MAP.put(R_AND_E, "R&E");
        WHIRLPOOL_ABBRV_MAP.put(RIVERVIEW, "RV");
        WHIRLPOOL_ABBRV_MAP.put(ST_JOE_TECH_CENTER, "SJTC");
    }

    public static final int MAP_SELECTED_ROOM_COLOR = -7748364;
    public static final int DEFAULT_MAP_CAMERA_ZOOM = 20;
}
