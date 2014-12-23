package org.zywx.wbpalmstar.plugin.uexqq;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.zywx.wbpalmstar.base.BUtility;
import org.zywx.wbpalmstar.engine.EBrowserView;
import org.zywx.wbpalmstar.engine.universalex.EUExBase;
import org.zywx.wbpalmstar.engine.universalex.EUExCallback;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.tencent.connect.common.Constants;
import com.tencent.connect.share.QQShare;
import com.tencent.connect.share.QzoneShare;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;

public class EUExQQ extends EUExBase {
    private static final String TAG = "EUExQQ"; 
    private Context mContext;
    public static Tencent mTencent;
    public static String mAppid = "222222";
    private static final String CB_LOGIN = "uexQQ.cbLogin";
    private static final String CB_SHARE_QQ = "uexQQ.cbShareQQ";
    private static final String TAG_RET = "ret";
    private static final String TAG_DATA = "data";

    public EUExQQ(Context context, EBrowserView eBrw) {
        super(context, eBrw);
        this.mContext = context;
    }

    @Override
    protected boolean clean() {
        return false;
    }

    public void login(String[] params){
        if(params.length < 0){
            return;
        }
        mAppid = params[0];
        Log.i(TAG, "login->mAppid = " + mAppid);
        initTencent(mAppid);
        if(!mTencent.isSessionValid()){
            mTencent.login((Activity )mContext, "all", loginListener);
        }
    }
    
    private void initTencent(String appId){
        if (mTencent == null) {
            mTencent = Tencent.createInstance(appId, mContext);
        }
    }
    
    IUiListener loginListener = new BaseUiListener() {
        @Override
        protected void doComplete(JSONObject values) {
            initOpenidAndToken(values);
        }
    };

    private class BaseUiListener implements IUiListener {
        @Override
        public void onComplete(Object response) {
            Log.i(TAG, "BaseUiListener->onComplete->response = " + response);
            if (null == response) {
                loginCallBack(EUExCallback.F_C_FAILED, "server no response!");
                return;
            }
            JSONObject jsonResponse = (JSONObject) response;
            if (null != jsonResponse && jsonResponse.length() == 0) {
                loginCallBack(EUExCallback.F_C_FAILED, "server no response!");
                return;
            }
            doComplete((JSONObject)response);
        }

        protected void doComplete(JSONObject values) {

        }

        @Override
        public void onError(UiError e) {
            loginCallBack(EUExCallback.F_C_FAILED, "errorCode:" + e.errorCode + "," + e.errorDetail);
        }

        @Override
        public void onCancel() {
            loginCallBack(EUExCallback.F_C_FAILED, "user cancel");
        }
    }

    IUiListener qqShareListener = new IUiListener() {
        @Override
        public void onCancel() {
            //if (shareType != QQShare.SHARE_TO_QQ_TYPE_IMAGE) {
                //Util.toastMessage(QQShareActivity.this, "onCancel: ");
            //}
            jsCallbackAsyn(CB_SHARE_QQ, 0, EUExCallback.F_C_INT,
                    String.valueOf(EUExCallback.F_C_FAILED));
        }
        @Override
        public void onComplete(Object response) {
            jsCallbackAsyn(CB_SHARE_QQ, 0, EUExCallback.F_C_INT,
                    String.valueOf(EUExCallback.F_C_SUCCESS));
        }
        @Override
        public void onError(UiError e) {
            jsCallbackAsyn(CB_SHARE_QQ, 0, EUExCallback.F_C_INT,
                    String.valueOf(EUExCallback.F_C_FAILED));
        }
    };
    
    public void initOpenidAndToken(JSONObject jsonObject) {
        try {
            String token = jsonObject.getString(Constants.PARAM_ACCESS_TOKEN);
            String expires = jsonObject.getString(Constants.PARAM_EXPIRES_IN);
            String openId = jsonObject.getString(Constants.PARAM_OPEN_ID);
            if (!TextUtils.isEmpty(token) && !TextUtils.isEmpty(expires)
                    && !TextUtils.isEmpty(openId)) {
                mTencent.setAccessToken(token, expires);
                mTencent.setOpenId(openId);
            }
            JSONObject dataJson = new JSONObject();
            dataJson.put(Constants.PARAM_ACCESS_TOKEN, token);
            dataJson.put(Constants.PARAM_OPEN_ID, openId);
            dataJson.put(Constants.PARAM_EXPIRES_IN, expires);
            loginCallBack(EUExCallback.F_C_SUCCESS, dataJson.toString());
        } catch(Exception e) {
            loginCallBack(EUExCallback.F_C_FAILED, e.getMessage());
            e.printStackTrace();
        }
    }

    public void loginCallBack(int ret, String data) {
        Map<String, String> map = new HashMap<String, String>();
        map.put(TAG_RET, String.valueOf(ret));
        map.put(TAG_DATA, data);
        JSONObject json = new JSONObject(map);
        jsCallbackAsyn(CB_LOGIN, 0, EUExCallback.F_C_JSON, 
                json.toString());
    }
    
    public void shareWebImgTextToQQ(String[] params){
        if(params.length < 2){
            return;
        }
        String appId = params[0];
        initTencent(appId);
        String jsonData = params[1];
        parseWebImgData(jsonData);
    }
    
    private void parseWebImgData(String jsonData){
        try {
            final Bundle params = new Bundle();
            params.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_DEFAULT);//分享的类型�?图文分享(普�?分享)填Tencent.SHARE_TO_QQ_TYPE_DEFAULT
            JSONObject json = new JSONObject(jsonData);
            //必�?
            String title = json.get(QQShare.SHARE_TO_QQ_TITLE).toString();
            params.putString(QQShare.SHARE_TO_QQ_TITLE, title);//分享的标�? �?��30个字符�?
            String targetUrl = json.get(QQShare.SHARE_TO_QQ_TARGET_URL).toString();
            params.putString(QQShare.SHARE_TO_QQ_TARGET_URL, targetUrl);//这条分享消息被好友点击后的跳转URL�?
            
            //可�?
            if(json.has(QQShare.SHARE_TO_QQ_SUMMARY)){
                String summary = json.get(QQShare.SHARE_TO_QQ_SUMMARY).toString();
                params.putString(QQShare.SHARE_TO_QQ_SUMMARY, summary);//分享的消息摘要，�?��40个字。可�?
            }
            if(json.has(QQShare.SHARE_TO_QQ_IMAGE_URL)){
                String imageUrl = getImageUrl(json.get(QQShare.SHARE_TO_QQ_IMAGE_URL).toString());
                params.putString(QQShare.SHARE_TO_QQ_IMAGE_URL, imageUrl);//分享图片的URL或�?本地路径
            }
            if(json.has(QQShare.SHARE_TO_QQ_APP_NAME)){
                String appName = json.get(QQShare.SHARE_TO_QQ_APP_NAME).toString();
                params.putString(QQShare.SHARE_TO_QQ_APP_NAME,  appName);//手Q客户端顶部，替换“返回�?按钮文字，如果为空，用返回代�?
            }
            if(json.has(QQShare.SHARE_TO_QQ_EXT_INT)){
                params.putInt(QQShare.SHARE_TO_QQ_EXT_INT,  Integer.valueOf(json.get(QQShare.SHARE_TO_QQ_EXT_INT).toString()));
            }
            //QQShare.SHARE_TO_QQ_FLAG_QZONE_AUTO_OPEN，分享时自动打开分享到QZone的对话框�?
            //QQShare.SHARE_TO_QQ_FLAG_QZONE_ITEM_HIDE，分享时隐藏分享到QZone按钮
            doToQQShare(params);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    
    private String getImageUrl(String url) {
        if(url.startsWith(BUtility.F_HTTP_PATH)){
            return url;
        }
        String imgPath = BUtility.makeRealPath(
                BUtility.makeUrl(mBrwView.getCurrentUrl(), url),
                mBrwView.getCurrentWidget().m_widgetPath,
                mBrwView.getCurrentWidget().m_wgtType);
        if(imgPath.startsWith("/")){
            return imgPath;
        }
        return Utils.copyImage(mContext, imgPath);
    }

    private void doToQQShare(Bundle params) {
        mTencent.shareToQQ((Activity)mContext, params, qqShareListener);
    }

    public void shareLocalImgToQQ(String[] params){
        if(params.length < 2){
            return;
        }
        String appId = params[0];
        initTencent(appId);
        String jsonData = params[1];
        parseLocalImgData(jsonData);
    }
    
    private void parseLocalImgData(String jsonData){
        try {
            Bundle params = new Bundle();
            //分享类型，分享纯图片时填写QQShare.SHARE_TO_QQ_TYPE_IMAGE�?
            params.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_IMAGE);
            JSONObject json = new JSONObject(jsonData);
            String imageLocalUrl = json.get(QQShare.SHARE_TO_QQ_IMAGE_LOCAL_URL).toString();

            //�?��分享的本地图片路径�?
            params.putString(QQShare.SHARE_TO_QQ_IMAGE_LOCAL_URL, getImageUrl(imageLocalUrl));
            
            if(json.has(QQShare.SHARE_TO_QQ_APP_NAME)){
                String appName = json.get(QQShare.SHARE_TO_QQ_APP_NAME).toString();
                //手Q客户端顶部，替换“返回�?按钮文字，如果为空，用返回代替�?
                params.putString(QQShare.SHARE_TO_QQ_APP_NAME, appName);
            }

            //分享额外选项，两种类型可选（默认是不隐藏分享到QZone按钮且不自动打开分享到QZone的对话框）：
            //QQShare.SHARE_TO_QQ_FLAG_QZONE_AUTO_OPEN，分享时自动打开分享到QZone的对话框�?
            //QQShare.SHARE_TO_QQ_FLAG_QZONE_ITEM_HIDE，分享时隐藏分享到QZone按钮�?
            if(json.has(QQShare.SHARE_TO_QQ_EXT_INT)){
                params.putInt(QQShare.SHARE_TO_QQ_EXT_INT,  Integer.valueOf(json.get(QQShare.SHARE_TO_QQ_EXT_INT).toString()));
            }
            doToQQShare(params);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void shareAudioToQQ(String[] params){
        if(params.length < 2){
            return;
        }
        String appId = params[0];
        initTencent(appId);
        String jsonData = params[1];
        parseAudioData(jsonData);
    }

    private void parseAudioData(String jsonData) {
        try {
            Bundle params = new Bundle();
            //分享类型，分享纯图片时填写QQShare.SHARE_TO_QQ_TYPE_IMAGE�?
            params.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_AUDIO);
            JSONObject json = new JSONObject(jsonData);
            //必�?
            String title = json.get(QQShare.SHARE_TO_QQ_TITLE).toString();
            params.putString(QQShare.SHARE_TO_QQ_TITLE, title);//分享的标�? �?��30个字符�?
            String targetUrl = json.get(QQShare.SHARE_TO_QQ_TARGET_URL).toString();
            params.putString(QQShare.SHARE_TO_QQ_TARGET_URL, targetUrl);//这条分享消息被好友点击后的跳转URL�?
            String audio_url = json.get(QQShare.SHARE_TO_QQ_AUDIO_URL).toString();
            params.putString(QQShare.SHARE_TO_QQ_AUDIO_URL, audio_url);
            //可�?
            if(json.has(QQShare.SHARE_TO_QQ_SUMMARY)){
                String summary = json.get(QQShare.SHARE_TO_QQ_SUMMARY).toString();
                params.putString(QQShare.SHARE_TO_QQ_SUMMARY, summary);//分享的消息摘要，�?��40个字。可�?
            }
            if(json.has(QQShare.SHARE_TO_QQ_IMAGE_URL)){
                String imageUrl = getImageUrl(json.get(QQShare.SHARE_TO_QQ_IMAGE_URL).toString());
                params.putString(QQShare.SHARE_TO_QQ_IMAGE_URL, imageUrl);//分享图片的URL或�?本地路径
            }
            if(json.has(QQShare.SHARE_TO_QQ_APP_NAME)){
                String appName = json.get(QQShare.SHARE_TO_QQ_APP_NAME).toString();
                params.putString(QQShare.SHARE_TO_QQ_APP_NAME,  appName);//手Q客户端顶部，替换“返回�?按钮文字，如果为空，用返回代�?
            }

            //分享额外选项，两种类型可选（默认是不隐藏分享到QZone按钮且不自动打开分享到QZone的对话框）：
            //QQShare.SHARE_TO_QQ_FLAG_QZONE_AUTO_OPEN，分享时自动打开分享到QZone的对话框�?
            //QQShare.SHARE_TO_QQ_FLAG_QZONE_ITEM_HIDE，分享时隐藏分享到QZone按钮�?
            if(json.has(QQShare.SHARE_TO_QQ_EXT_INT)){
                params.putInt(QQShare.SHARE_TO_QQ_EXT_INT,  Integer.valueOf(json.get(QQShare.SHARE_TO_QQ_EXT_INT).toString()));
            }
            doToQQShare(params);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void shareAppToQQ(String[] params){
        if(params.length < 2){
            return;
        }
        String appId = params[0];
        initTencent(appId);
        String jsonData = params[1];
        parseAppData(jsonData);
    }

    private void parseAppData(String jsonData) {
        try {
            Bundle params = new Bundle();
            params.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_APP);
            JSONObject json = new JSONObject(jsonData);
            //必�?
            String title = json.get(QQShare.SHARE_TO_QQ_TITLE).toString();
            params.putString(QQShare.SHARE_TO_QQ_TITLE, title);//分享的标�? �?��30个字符�?

            //可�?
            if(json.has(QQShare.SHARE_TO_QQ_SUMMARY)){
                String summary = json.get(QQShare.SHARE_TO_QQ_SUMMARY).toString();
                params.putString(QQShare.SHARE_TO_QQ_SUMMARY, summary);//分享的消息摘要，�?��40个字。可�?
            }
            if(json.has(QQShare.SHARE_TO_QQ_IMAGE_URL)){
                String imageUrl = getImageUrl(json.get(QQShare.SHARE_TO_QQ_IMAGE_URL).toString());
                params.putString(QQShare.SHARE_TO_QQ_IMAGE_URL, imageUrl);//分享图片的URL或�?本地路径
            }
            if(json.has(QQShare.SHARE_TO_QQ_APP_NAME)){
                String appName = json.get(QQShare.SHARE_TO_QQ_APP_NAME).toString();
                params.putString(QQShare.SHARE_TO_QQ_APP_NAME,  appName);//手Q客户端顶部，替换“返回�?按钮文字，如果为空，用返回代�?
            }

            //分享额外选项，两种类型可选（默认是不隐藏分享到QZone按钮且不自动打开分享到QZone的对话框）：
            //QQShare.SHARE_TO_QQ_FLAG_QZONE_AUTO_OPEN，分享时自动打开分享到QZone的对话框�?
            //QQShare.SHARE_TO_QQ_FLAG_QZONE_ITEM_HIDE，分享时隐藏分享到QZone按钮�?
            if(json.has(QQShare.SHARE_TO_QQ_EXT_INT)){
                params.putInt(QQShare.SHARE_TO_QQ_EXT_INT,  Integer.valueOf(json.get(QQShare.SHARE_TO_QQ_EXT_INT).toString()));
            }
            doToQQShare(params);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    
    public void shareImgTextToQZone(String[] params){
        if(params.length < 2){
            return;
        }
        String appId = params[0];
        initTencent(appId);
        String jsonData = params[1];
        parseImgTextToQZoneData(jsonData);
    }

    private void parseImgTextToQZoneData(String jsonData) {
        try {
            Bundle params = new Bundle();
            params.putInt(QzoneShare.SHARE_TO_QZONE_KEY_TYPE, QzoneShare.SHARE_TO_QZONE_TYPE_IMAGE_TEXT);
            JSONObject json = new JSONObject(jsonData);
            //必�?
            String title = json.get(QzoneShare.SHARE_TO_QQ_TITLE).toString();
            params.putString(QzoneShare.SHARE_TO_QQ_TITLE, title);//分享的标�? �?��30个字符�?
            String targetUrl = json.get(QzoneShare.SHARE_TO_QQ_TARGET_URL).toString();
            params.putString(QzoneShare.SHARE_TO_QQ_TARGET_URL, targetUrl);//这条分享消息被好友点击后的跳转URL�?
            //可�?
            if(json.has(QzoneShare.SHARE_TO_QQ_SUMMARY)){
                String summary = json.get(QzoneShare.SHARE_TO_QQ_SUMMARY).toString();
                params.putString(QzoneShare.SHARE_TO_QQ_SUMMARY, summary);//分享的消息摘要，�?��40个字。可�?
            }
            if(json.has(QzoneShare.SHARE_TO_QQ_IMAGE_URL)){
                ArrayList<String> paths = new ArrayList<String>();
                JSONArray jsonArray = json.getJSONArray(QzoneShare.SHARE_TO_QQ_IMAGE_URL);
                for (int i = 0; i < jsonArray.length(); i++) {
                    String path = jsonArray.opt(i).toString();
                    paths.add(getImageUrl(path));
                }
                params.putStringArrayList(QzoneShare.SHARE_TO_QQ_IMAGE_URL, paths);//分享图片的URL或�?本地路径
            }
            doToQZoneShare(params);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void doToQZoneShare(Bundle params) {
        mTencent.shareToQzone((Activity)mContext, params, qqShareListener);
    }
}