package com.yeonfish.hybrid_app_frame.common.net;

import android.content.Context;
import android.text.TextUtils;


import com.yeonfish.hybrid_app_frame.common.util.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.internal.http.HttpMethod;

/**
 * Created by aimh on 2017. 4. 19..
 */

public abstract class HttpLoader2 {

    enum PARAM_TYPE {
        HASH_MAP,
        JSON_OBJECT,
        JSON_ARRAY
    }

    private static final String TAG = HttpLoader2.class.getSimpleName();
    private static final MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json; charset=utf-8");

    private Context mContext;
    private LoadFinishListener mLoadFinishListener;
    private Object mParams;
    private String mURL;
    private HttpMethod mMethod;
    private boolean mIsCaching;
    private PARAM_TYPE mParamType;
    private APIStatus mAPIStatus;

    //통신 완료
    public interface LoadFinishListener {
        void OnLoadFinishListener(HttpLoader2 httpLoader, boolean isSuccessParse, APIStatus apiStatus);
    }

    public HttpLoader2(Context context, LoadFinishListener loadFinishListener) {
        mContext = context;
        mLoadFinishListener = loadFinishListener;
    }

    public void setLoader(Context context, LoadFinishListener loadFinishListener) {
        mContext = context;
        mLoadFinishListener = loadFinishListener;
    }

    public void setRequest(String url, HttpMethod method, HashMap<String, String> params, boolean isCaching) {
        mURL = url;
        mMethod = method;
        mIsCaching = isCaching;
        mParams = params;
        mParamType = PARAM_TYPE.HASH_MAP;
    }

    public void setRequest(String url, HttpMethod method, Object params, boolean isCaching) {
        mURL = url;
        mMethod = method;
        mIsCaching = isCaching;
        if (params instanceof HashMap) {
            mParams = null != params ? (HashMap<String, String>) params : null;
            mParamType = PARAM_TYPE.HASH_MAP;
        } else if (params instanceof JSONArray) {
            mParamType = PARAM_TYPE.JSON_ARRAY;
            mParams = null != params ? (JSONArray) params : null;

        } else if (params instanceof JSONObject) {
            mParamType = PARAM_TYPE.JSON_OBJECT;
            mParams = params;
        }
    }


    public void execute() {
        if (mIsCaching) {
            mURL = mURL.replace("apihub", "cache");
        }

        HttpRequest httpRequest = new HttpRequest(mMethod, mURL);
        if (mParamType == PARAM_TYPE.HASH_MAP) {
            httpRequest.setParams((HashMap<String, String>) mParams);
        }
        final Request request;
        if (mMethod.equals(HttpMethod.POST) || mMethod.equals(HttpMethod.PATCH)) {
            //JSONObject jsonBody;
            RequestBody body;
            if (null != mParams) {
                if (mParamType == PARAM_TYPE.HASH_MAP) {
                    JSONObject jsonBody = StringUtil.getJsonStringFromMap((HashMap<String, String>) mParams);
                    body = RequestBody.create(MEDIA_TYPE_JSON, jsonBody.toString());
                } else if (mParamType == PARAM_TYPE.JSON_OBJECT) {
                    body = RequestBody.create(MEDIA_TYPE_JSON, mParams.toString());
                } else {
                    body = RequestBody.create(MEDIA_TYPE_JSON, mParams.toString());
                }
            } else {
                body = RequestBody.create(MEDIA_TYPE_JSON, "");
            }


            if (mMethod.equals(HttpMethod.PATCH)) {
                request = new Request.Builder()
                        .url(mURL)
                        .patch(body)
                        .build();
            } else {
                request = new Request.Builder()
                        .url(mURL)
                        .post(body)
                        .build();
            }

        } else if (mMethod.equals(HttpMethod.DELETE)) {
            request = new Request.Builder()
                    .url(mURL)
                    .delete()
                    .build();

        } else {

            if (null != mParams && ((HashMap<String, String>) mParams).size() > 0) {
                if (mURL.indexOf("?") > -1) {
                    mURL = mURL + "&" + StringUtil.getHashMapToQueryString(((HashMap<String, String>) mParams));
                } else {
                    mURL = mURL + "?" + StringUtil.getHashMapToQueryString(((HashMap<String, String>) mParams));
                }
            }
            request = new Request.Builder()
                    .url(mURL)
                    .build();
        }


        Logger.debug(TAG,
                "[URL] : " + mURL,
                "[Method] : " + mMethod,
                "[Param Type] : " + mParamType,
                "[Params] : " + (null != mParams ? mParams.toString() : "")
        );
        OkHttpClient client = new OkHttpClient.Builder()
                .cookieJar(new CustomCookieJar(mContext))
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Logger.error(TAG, "failure " + e.getMessage());

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                boolean resultParse = false;
                boolean isSuccessful = response.isSuccessful();
                int responseCode = response.code();
                Logger.debug(TAG, "status", response.code());
                if (isSuccessful) {
                    try {
                        resultParse = parseJSONContents(response);
                        Logger.debug(TAG, "resultParse " + resultParse);
                        if (resultParse) {
                            mAPIStatus = new APIStatus();
                        }
                    } catch (JSONException e) {
                        Logger.error(TAG, "onReponse Error : " + e.getMessage());
                        e.printStackTrace();
                    }

                    if (mLoadFinishListener != null) {
                        mLoadFinishListener.OnLoadFinishListener(HttpLoader2.this, resultParse, mAPIStatus);
                    }


                } else {
                    Logger.error(TAG, "network, service error ", responseCode, request.url().toString());
                    if (responseCode == HttpStatus.ERROR_VALIDATION ||
                            responseCode == HttpStatus.ERROR_FORBIDDEN) {
                        try {
                            String responseBody = response.body().string();
                            JSONObject errorObj = new JSONObject(responseBody);
                            mAPIStatus = new APIStatus(errorObj);
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else {
                        mAPIStatus = new APIStatus();
                    }
                    mLoadFinishListener.OnLoadFinishListener(HttpLoader2.this, false, mAPIStatus);
                }
            }
        });
    }

    /**
     * JSON 파싱
     *
     * @param response JSON Data
     * @return
     */
    public abstract boolean parseJSONContents(Response response) throws JSONException;

    public class APIStatus {

        public String errorCode;
        public String errorMessage;

        public APIStatus() throws IOException {

        }

        public APIStatus(JSONObject obj) throws IOException {
            if (null != obj) {
                try {
                    JSONObject contents = obj;
                    if (contents.has("errors") && null != contents.getJSONObject("errors")) {
                        Logger.error(TAG, "errors : " + ((JSONObject) contents.get("errors")).get("code"));
                        setErrorCode(((JSONObject) contents.get("errors")).get("code").toString());
                        setErrorMessage(((JSONObject) contents.get("errors")).get("message").toString());

                        if (((JSONObject) contents.get("errors")).has("detail") && null != ((JSONObject) contents.get("errors")).getJSONArray("detail")) {
                            Logger.error(TAG, "error message " + ((JSONObject) contents.get("errors")).getJSONArray("detail").get(0).toString());
                            setErrorServiceMessage(((JSONObject) contents.get("errors")).getJSONArray("detail").get(0).toString());
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        public boolean isSuccess() {
            return TextUtils.isEmpty(getErrorCode());
        }

        public void setErrorCode(String code) {
            errorCode = code;
        }

        public String getErrorCode() {
            return errorCode;
        }

        public void setErrorMessage(String message) {
            errorMessage = message;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public void setErrorServiceMessage(String message) {
            errorMessage = message;
        }

        public String getErrorServiceMessage() {
            return errorMessage;
        }
    }

}