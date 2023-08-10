package com.yeonfish.hybrid_app_frame.common.util;

import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.UnderlineSpan;
import android.util.Base64;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class StringUtil {

    public static String getHashMapToQueryString(HashMap<String, String> params) {
        return getHashMapToQueryString(params, null);
    }

    public static String getHashMapToQueryString(HashMap<String, String> params, String encoding) {
        String encodingType = TextUtils.isEmpty(encoding) ? null : encoding;
        StringBuilder retval = new StringBuilder();
        String value;
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (retval.length() > 0) {
                retval.append("&");
            }
            value = TextUtils.isEmpty(encodingType) ? entry.getValue() : urlEncode(entry.getValue(), encoding);
            retval.append(entry.getKey() != null ? entry.getKey() : "");
            retval.append("=");
            retval.append(value != null ? value : "");
        }
        return retval.toString();
    }

    public static HashMap<String, String> getQueryStringToHashMap(String queryString) {
        HashMap<String, String> returnHashMap = new HashMap<>();
        String[] splitString = queryString.split("&");
        for (String s : splitString) {
            String[] tmp = s.split("=");
            if (tmp.length > 1) {
                returnHashMap.put(tmp[0], tmp[1]);
            }
        }
        return returnHashMap;
    }

    public static Bundle getJsonObjectToBundle(JsonObject jsonObject) {
        Bundle bundle = null;
        if (jsonObject != null) {
            bundle = new Bundle();
            Set<Map.Entry<String, JsonElement>> entries = jsonObject.entrySet();
            for (Map.Entry<String, JsonElement> entry : entries) {
                String key = entry.getKey();
                String value = entry.getValue().toString();
                bundle.putString(key, value);
            }
        }
        return bundle;
    }

    private static String urlEncode(String value, String encoding) {
        try {
            return URLEncoder.encode(value, encoding);
        } catch (java.io.UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String convertCommaNumber(int number) {
        DecimalFormat formatter = new DecimalFormat("#,###");
        return formatter.format((long) number);
    }

    public static String convertSymbolNumber(int number) {
        char[] suffix = {' ', 'k', 'm', 'b', 't', 'p', 'e'};
        int value = (int) Math.floor(Math.log10(number));
        int base = value / 4;
        if (value >= 4 && base < suffix.length) {
            return new DecimalFormat("#0.0").format(number / Math.pow(10.0, base * 3)) + suffix[base];
        } else {
            return new DecimalFormat("#,##0").format((long) number);
        }
    }

    public static Spannable convertUnderline(String content) {
        Spannable undelineContent = new SpannableString(content);
        undelineContent.setSpan(new UnderlineSpan(), 0, undelineContent.length(), 0);
        return undelineContent;
    }

    public static String convertUserNameWithStar(String userName) {
        if (userName.length() > 2) {
            return userName.substring(0, userName.length() - 2) + "**";
        } else {
            return userName;
        }
    }

    private static final String DEFAULT_REPLACE_LINETAG = "\n";

    public static String replaceLineTag(String value) {
        String result = "";
        try {
            result = value.replace("\\n", DEFAULT_REPLACE_LINETAG).replace("<br>", DEFAULT_REPLACE_LINETAG);
            if (!TextUtils.equals(DEFAULT_REPLACE_LINETAG, DEFAULT_REPLACE_LINETAG)) {
                result = value.replace(DEFAULT_REPLACE_LINETAG, DEFAULT_REPLACE_LINETAG);
            }
        } catch (Exception e) {
            Logger.error("StringUtil", "replaceLineTag");
        }
        return result;
    }

    public static String htmlSpeicalChars(String value) {
        return replaceLineTag(value).replace("<li>", "").replace("</li>", "");
    }

    public static String removeBrTag(String value) {
        return value.replace("<br>", "").replace("<br />", "");
    }

    public static String[] getCustomIntentUrl(String data) {
        String[] retData = new String[2];
        retData[1] = "";
        retData[0] = retData[1];
        if (data.contains("scheme=")) {
            String[] arrMainData = data.split("#Intent;");
            if (arrMainData.length < 2) {
                return retData;
            }
            String host = "";
            if (arrMainData[0].contains("intent://")) {
                host = arrMainData[0].replace("intent", "");
            } else {
                host = arrMainData[0].replace("intent:", "://");
            }
            String[] arrData = arrMainData[1].split(";");
            if (arrData.length < 1) {
                return retData;
            }
            for (String arrDatum : arrData) {
                if (arrDatum.contains("scheme=")) {
                    retData[0] = arrDatum.replace("scheme=", "") + host;
                }
                if (arrDatum.contains("package=")) {
                    retData[1] = arrDatum.replace("package=", "");
                }
            }
        } else {
            String[] arrMainData = data.split("#Intent;");
            if (arrMainData.length < 2) {
                return retData;
            }
            retData[0] = arrMainData[0].replace("intent:", "");
            String[] arrData = arrMainData[1].split(";");
            if (arrData.length < 1) {
                return retData;
            }
            for (String arrDatum : arrData) {
                if (arrDatum.contains("package=")) {
                    retData[1] = arrDatum.replace("package=", "");
                }
            }
        }
        Logger.debug("Intent Custom URL", "retData[0] : " + retData[0]);
        Logger.debug("Intent Custom URL", "retData[1] : " + retData[1]);
        return retData;
    }

    public static String decodeBase64(String str) {
        return new String(Base64.decode(str, Base64.DEFAULT));
    }

    public static String toStringFirstCharUpper(String str) {
        String lowerString = str.toLowerCase();
        String transString = lowerString.substring(0, 1).toUpperCase() + lowerString.substring(1);
        return transString;
    }

    public static String toHexColorCode(String hexCode) {
        if (!isEmpty(hexCode) && !hexCode.contains("rgb")) {
            String color;
            String[] splitedColor = hexCode.split("#");
            if (splitedColor[1].length() < 4) {
                color = "#" + splitedColor[1] + splitedColor[1];
            } else {
                color = hexCode;
            }
            return color;
        }
        return "#000000";
    }

    public static Spanned htmlToString(String source) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Html.fromHtml(source, Html.FROM_HTML_MODE_LEGACY);
        } else {
            return Html.fromHtml(source);
        }
    }

    public static JSONObject getJsonStringFromMap(HashMap<String, String> map) {
        JSONObject json = new JSONObject();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            try {
                json.put(entry.getKey(), entry.getValue());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return json;
    }

    public static boolean isEmpty(String str) {
        return str == null || str.equals("null") || TextUtils.isEmpty(str);
    }

    public static String decodeURL(String url) {
        if (url != null) {
            String retval = url.replace("%", "%25");
            try {
                retval = URLDecoder.decode(retval, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                return retval;
            }
            return retval;
        }
        return "";
    }

    public static String decode(String url) {
        String returnUrl = "";
        if (url != null) {
            try {
                returnUrl = URLDecoder.decode(url, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                return returnUrl;
            }
        }
        return returnUrl;
    }
}