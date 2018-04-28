import Foundation  
import WebKit  

@available(iOS 8.0, *)  
@objc(HWPVideoRecorderPlugin) class VideoRecorderPlugin : CDVPlugin {  

    func initialize(command: CDVInvokedUrlCommand) {  
        print("VideoRecorderPlugin initialization")  
    }  

    func recordVideo(command: CDVInvokedUrlCommand) {  
        print("recordVideo")

		let mapVc = VideoRecordController()
        mapVc.callBackId = command.callbackId  
        mapVc.VideoRecorderPlugin = self  
        self.viewController?.presentViewController(mapVc, animated: true,completion: nil)  
    }  
}