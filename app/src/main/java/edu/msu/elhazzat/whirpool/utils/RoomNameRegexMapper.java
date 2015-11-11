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
        REGEX_MAP.put(WIMAppConstants.RIVERVIEW, "(B\\d{3}-\\d{2}$|B\\d{3})");
        REGEX_MAP.put(WIMAppConstants.GHQ, "(N\\d{3}-\\d{2}$|N\\d{3})");
     /*   REGEX_MAP.put(WIMAppConstants.BENSON_ROAD, );
        REGEX_MAP.put(WIMAppConstants.EDGEWATER, );
        REGEX_MAP.put(WIMAppConstants.BHTC, );
        REGEX_MAP.put(WIMAppConstants.GHQ, );
        REGEX_MAP.put(WIMAppConstants.HARBORTOWN, );
        REGEX_MAP.put(WIMAppConstants.HILLTOP_150, );
        REGEX_MAP.put(WIMAppConstants.HILLTOP_211, );
        REGEX_MAP.put(WIMAppConstants.MMC, );
        REGEX_MAP.put(WIMAppConstants.ST_JOE_TECH_CENTER, );
        REGEX_MAP.put(WIMAppConstants.R_AND_E, )*/
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
