package com.bbbbiu.biu.lib.util;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

import com.bbbbiu.biu.R;
import com.bbbbiu.biu.lib.httpd.HttpDaemon;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Properties;

/**
 * Created by YieldNull at 4/23/16
 */
public class HttpConstants {
    public static class Computer {
        public static final String HOST = "http://www.bbbbiu.com";
        public static final String URL_BIND = HOST + "/bind";
        public static final String URL_UPLOAD = HOST + "/api/upload";
        public static final String URL_FILE_LIST = HOST + "/api/filelist";


        private static final String BIND_WHAT_UPLOAD = "upload";
        private static final String BIND_WHAT_DOWNLOAD = "download";

        public static String getBindUploadUrl(String uid) {
            return URL_BIND + "?uid=" + uid + "&what=" + BIND_WHAT_UPLOAD;
        }

        public static String getBindDownloadUrl(String uid) {
            return URL_BIND + "?uid=" + uid + "&what=" + BIND_WHAT_DOWNLOAD;
        }

        public static String getUploadUrl(String uid) {
            return URL_UPLOAD + "?uid=" + uid;
        }

        public static HashMap<String, String> getUploadFormData(String uid) {
            HashMap<String, String> hashMap = new HashMap<>();
            hashMap.put("uid", uid);
            return hashMap;
        }

        public static String getDownloadUrl(String uid, String fileUri) {
            return HOST + fileUri + "?uid=" + uid;
        }

        public static String getManifestUrl(String uid) {
            return URL_FILE_LIST + "?uid=" + uid;
        }
    }


    public static class Android {
        public static final String URL_MANIFEST = "/manifest";
        public static final String URL_UPLOAD = "/upload";

        public static String getManifestUrl(InetAddress serverAddress) {
            return "http://" + serverAddress.getHostAddress() + ":" + HttpDaemon.getPort() + URL_MANIFEST;
        }

        public static String getSendUrl(InetAddress serverAddress) {
            return "http://" + serverAddress.getHostAddress() + ":" + HttpDaemon.getPort() + URL_UPLOAD;
        }
    }

    public static class Apple {

    }

}