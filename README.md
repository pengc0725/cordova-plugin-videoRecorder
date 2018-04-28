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

ios：
 已知Bug  苹果无法正常退出，我的解决方案是把上移取消录制改成了  上移退出
 ios为swift3编写   有能力的可以自己改改  本人只是一个java开发者  swift水平有限
 
 
 
 邮箱:76211031@qq.com  欢迎大家提出意见
   

