# cordova-plugin-videoRecorder

该插件支持安卓和IOS录制视频 目前设置返回10秒  可自定义去修改


具体用法: MAX最长录制长度


  var videoRecorder =new VideoRecorder();
  
  videoRecorder.recordVideo(max,function(result){
  
    console.info(result)
    
		successHandler(JSON.parse(result))
    
	},function(result){
  
		console.info(result)
    
	});
  
   

