package com.pc.videoRecorder;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.LOG;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;

public class VideoRecorder extends CordovaPlugin {

    private static final String ACTION_RECORDVIDEO = "recordVideo";

    private CallbackContext callbackContext;

    public boolean execute(String action, final JSONArray args, final CallbackContext callbackContext) throws JSONException {
        this.callbackContext = callbackContext;
        if (ACTION_RECORDVIDEO.equals(action)) {
        	int max=10;
            final JSONObject params = args.getJSONObject(0);
            if (params.has("max")) {
            	max = params.getInt("max");
            }
            final Intent videoRecorderIntent = new Intent(cordova.getActivity(), RecordVideoActivity.class);
            videoRecorderIntent.putExtra("MAX_SECOND", max);
            cordova.startActivityForResult(this, videoRecorderIntent, 0);
            return true;
        }
        return false;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
    	if ((requestCode == 200 || resultCode == 200)&&data!=null) {
            callbackContext.success(toJson(data));
        }else{
        	callbackContext.error("未录制视频");
        }
    }
    
    public String toJson(Intent data){
        LOG.d("aaaaa","{\"videoUrl\":\""+"file://"+data.getStringExtra("videoUrl")+"\",\"imgUrl\":\""+"file://"+data.getStringExtra("imgUrl")+"\",\"duration\":\"" + data.getIntExtra("duration",0) + "\"}");
    	return "{\"videoUrl\":\""+"file://"+data.getStringExtra("videoUrl")+"\",\"imgUrl\":\""+"file://"+data.getStringExtra("imgUrl")+"\",\"duration\":\"" + data.getIntExtra("duration",0) + "\"}";
    }
}
