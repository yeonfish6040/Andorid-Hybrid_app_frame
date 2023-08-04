package com.yeonfish.hybrid_app_frame.common.net;

import android.content.Context;
import android.webkit.CookieManager;

import com.yeonfish.hybrid_app_frame.common.util.Logger;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;

public class CustomCookieJar implements CookieJar {
    private final String TAG = "CustomCookieJar";
    private Context context;

    public CustomCookieJar(Context context) {
        this.context = context;
    }

    @Override
    public List<Cookie> loadForRequest(HttpUrl url) {
        CookieManager cookieManager = CookieManager.getInstance();
        List<Cookie> cookies = new ArrayList<>();

        String cookieString = cookieManager.getCookie(url.toString());
        if (cookieString != null && !cookieString.trim().isEmpty()) {
            String[] splitCookies = cookieString.split("[,;]");
            for (String splitCookie : splitCookies) {
                Cookie cookie = Cookie.parse(url, splitCookie.trim());
                if (cookie != null) {
                    cookies.add(cookie);
                }
            }
        }
        return cookies;
    }

    @Override
    public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
        Logger.info(TAG, "saveFromResponse " + url + " " + cookies);
        CookieManager cookieManager = CookieManager.getInstance();
        for (Cookie cookie : cookies) {
            cookieManager.setCookie(url.toString(), cookie.toString());
        }

        //TODO
        /*
        String cookieString = cookieManager.getCookie(url.toString());
        if (cookieString != null && !cookieString.trim().isEmpty()) {
            UserPreference.setUserCookie(context, cookieString);
        }
        */
    }
}
