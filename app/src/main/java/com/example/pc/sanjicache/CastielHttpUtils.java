package com.example.pc.sanjicache;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by pc on 2017/12/7.
 */

public class CastielHttpUtils {
    public static InputStream castielDownLoad(String key) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) new URL(key).openConnection();
        return conn.getInputStream();
    }
}
