package edu.msu.elhazzat.whirpool.rest;

/**
 * Created by christianwhite on 11/5/15.
 */
public class GCSRestConstants {
    public static final String GCS_BASE_URL = "https://whirlpool-indoor-maps.appspot.com/";
    public static final String GCS_BLOBSTORE_BASE_URL = GCS_BASE_URL + "blobstore/ops?";
    public static final String GCS_ROOM_BASE_URL = GCS_BASE_URL + "room?";
    public static final String GCS_BUILDING_BASE_URL = GCS_BASE_URL + "building?";

    // room response json keys
    public static final String SUCCESS_KEY = "success";
    public static final String BUILDING_NAME_KEY = "building_name";
    public static final String ROOMS_KEY = "rooms";
    public static final String ROOM_NAME_KEY = "room_name";
    public static final String OCC_STATUS_KEY = "occupancy_status";
    public static final String CAPACITY_KEY = "capacity";
    public static final String EXT_KEY = "extension";
    public static final String ROOM_TYPE_KEY = "room_type";
    public static final String EMAIL_KEY = "email";
    public static final String RESOURCE_NAME_KEY = "resource_name";
}
