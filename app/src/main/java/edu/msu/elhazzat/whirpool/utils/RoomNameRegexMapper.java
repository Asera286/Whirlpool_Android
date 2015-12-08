package edu.msu.elhazzat.whirpool.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by christianwhite on 11/5/15.
 */

/*************************************************************************
 * Map resource names to building/room names -
 * This application pulls "resourse" data from google to identify building
 * room emails/resource names - we need to extract the building
 * abbreviation and simplified room names from this content
 *************************************************************************/
public class RoomNameRegexMapper {
    public static final Map<String, String> REGEX_MAP = new HashMap<>();
    static {
        // used to remove nick names from room names pull
        REGEX_MAP.put(WIMConstants.RIVERVIEW, "([ABC]\\d{3}-\\d{2}$|[ABC]\\d{3})");
        REGEX_MAP.put(WIMConstants.GHQ, "(N\\d{3}-\\d{2}$|N\\d{3})");
    }

    /**
     * fix room name extracted from resource
     * @param buildingName
     * @param roomName
     * @return
     */
    public static String getGeoJsonRoomNameFromMap(String buildingName, String roomName) {
        String patternString = REGEX_MAP.get(buildingName);
        if(patternString != null) {
            Pattern pattern = Pattern.compile(patternString);
            Matcher matcher = pattern.matcher(roomName);
            String match = null;
            if (matcher.find()) {
                return matcher.group(1);
            }
        }
        return null;
    }

    /**
     * pull building name from resource
     * @param resourceName
     * @return
     */
    public static String getBuildingNameFromResource(String resourceName) {
        String[] resourceSplit = resourceName.split("-");
        if(resourceSplit.length > 1) {
            return resourceSplit[2].trim();
        }
        return null;
    }

    /**
     * pull room name from resource
     * @param resourceName
     * @return
     */
    public static String getRoomNameFromResource(String resourceName) {
        String[] resourceSplit = resourceName.split("-");
        if(resourceSplit.length > 3) {
            String locationName = resourceSplit[3].trim();
            return locationName;
        }
        return null;
    }
}
