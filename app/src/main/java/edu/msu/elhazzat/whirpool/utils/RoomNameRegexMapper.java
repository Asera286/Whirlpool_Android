package edu.msu.elhazzat.whirpool.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by christianwhite on 11/5/15.
 */
public class RoomNameRegexMapper {
    public static final Map<String, String> REGEX_MAP = new HashMap<>();
    static {
        REGEX_MAP.put(WIMConstants.RIVERVIEW, "([ABC]\\d{3}-\\d{2}$|[ABC]\\d{3})");
        REGEX_MAP.put(WIMConstants.GHQ, "(N\\d{3}-\\d{2}$|N\\d{3})");
     /*   REGEX_MAP.put(WIMConstants.BENSON_ROAD, );
        REGEX_MAP.put(WIMConstants.EDGEWATER, );
        REGEX_MAP.put(WIMConstants.BHTC, );
        REGEX_MAP.put(WIMConstants.GHQ, );
        REGEX_MAP.put(WIMConstants.HARBORTOWN, );
        REGEX_MAP.put(WIMConstants.HILLTOP_150, );
        REGEX_MAP.put(WIMConstants.HILLTOP_211, );
        REGEX_MAP.put(WIMConstants.MMC, );
        REGEX_MAP.put(WIMConstants.ST_JOE_TECH_CENTER, );
        REGEX_MAP.put(WIMConstants.R_AND_E, )*/
    }

    public static String getGeoJsonRoomNameFromMap(String buildingName, String roomName) {
        String patternString = REGEX_MAP.get(buildingName);
        Pattern pattern = Pattern.compile(patternString);
        Matcher matcher = pattern.matcher(roomName);
        String match = null;
        if(matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    public static String getBuildingNameFromResource(String resourceName) {
        String[] resourceSplit = resourceName.split("-");
        String locationName = resourceSplit[2].trim();
        return locationName;

    }

    public static String getRoomNameFromResource(String resourceName) {
        String[] resourceSplit = resourceName.split("-");
        String locationName = resourceSplit[3].trim();
        return locationName;
    }
}
