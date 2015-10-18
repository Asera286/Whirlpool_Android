package edu.msu.elhazzat.whirpool.utils;

/**
 * Created by christianwhite on 10/16/15.
 */
public class TokenHolder {
    private String mToken;
    public String getToken() {return mToken;}
    public void setToken(String token) {mToken = token;}

    private static final TokenHolder holder = new TokenHolder();
    public static TokenHolder getInstance() {return holder;}
}