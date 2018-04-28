var exec = require("cordova/exec");

var VideoRecorder =function(){};

VideoRecorder.prototype.recordVideo=function(max,successCallback,errorCallback){
	if(!max){
		max=10;
	}
	var option={
		max:max
	}
	
	 exec(successCallback, errorCallback, 'VideoRecoder', 'recordVideo', [option]);
}



module.exports = VideoRecorder;
