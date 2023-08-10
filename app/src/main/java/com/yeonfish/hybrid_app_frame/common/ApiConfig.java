package com.yeonfish.hybrid_app_frame.common;

import kotlin.jvm.JvmStatic;

public class ApiConfig {
    @JvmStatic
    public static String getApiUrl(String url) {
        StringBuilder apiUrl = new StringBuilder();
        apiUrl.append(url);
        return apiUrl.toString();
    }
}
