//
//  PreviewVideo.swift
//  VideoRecording
//
//  Created by air on 17/10/26.
//  Copyright © 2017年 iwhere. All rights reserved.
//

import Foundation

import UIKit
import AVFoundation



@available(iOS 8.0, *)
class PreviewVideoController: UIViewController{
    
    var url,imgUrl: NSURL!
    //var delegte: MYDelegte?
    var videoRecorderPlugin: VideoRecorderPlugin?
    var videoRecordController: VideoRecordController?
    var rootViewController: UIViewController?
    var callBackId : String?
    
    //  蒙版
    var effectView = UIVisualEffectView()
    
    //  开始、停止按钮
    var startButton: UIButton!
    var playIconImageView: UIImageView!
    var playView: UIView!
    
    var isplay: Bool!
    var player: AVPlayer!
    var playerLayer: AVPlayerLayer!
    var img: UIImage?
    var first: Bool?
    override func viewDidLoad() {
        super.viewDidLoad()
        self.title="视频预览"
        isplay = false
        loadbarButton()
        
        loadVideo()
        
    }
    
    func getUrl(tmpstr :NSURL){
        self.url = tmpstr
    }
    
    override func prefersStatusBarHidden() -> Bool {
        return true
    }
    
    func goBack(){
        self.dismissViewControllerAnimated(true, completion: nil)
    }
    
    func goBack2(){
        let json = "{\"videoUrl\":\"\(String(self.url))\",\"imgUrl\":\"\(String(self.imgUrl))\"}"
        print(json)
        let pluginResult = CDVPluginResult(status: CDVCommandStatus_OK, messageAsString: json)
        self.videoRecorderPlugin?.commandDelegate?.sendPluginResult(pluginResult, callbackId: self.callBackId)
        self.dismissViewControllerAnimated(false, completion: nil)
        self.videoRecordController?.dismissViewControllerAnimated(false, completion: nil)
    }
    
    func loadbarButton(){
        let leftBotton = UIButton(type: UIButtonType.Custom)
        leftBotton.frame = CGRect(x: 0, y: 0, width: 60, height: 30)
        let img = UIImage(named: "back")?.imageWithRenderingMode(UIImageRenderingMode.AlwaysOriginal)
        leftBotton.setImage(img, forState: UIControlState.Normal)
        leftBotton.setTitle("返回", forState: UIControlState.Normal)
        leftBotton.setTitleColor(UIColor.blackColor(), forState: UIControlState.Normal)
        leftBotton.addTarget(self, action: Selector("goBack"), forControlEvents: UIControlEvents.TouchUpInside)
        let leftItem = UIBarButtonItem(customView: leftBotton)
        let spacer = UIBarButtonItem(barButtonSystemItem: UIBarButtonSystemItem.FixedSpace, target: nil, action: nil)
        spacer.width = -10
        self.navigationItem.leftBarButtonItems=[spacer,leftItem]
        
        let rightBotton = UIButton(type: UIButtonType.System)
        rightBotton.frame = CGRect(x: UIScreen.mainScreen().bounds.width-30, y: 0, width:35, height: 30)
        rightBotton.setTitle("完成", forState: UIControlState.Normal)
        rightBotton.setTitleColor(UIColor.blackColor(), forState: UIControlState.Normal)
        rightBotton.addTarget(self, action: Selector("goBack2"), forControlEvents: UIControlEvents.TouchUpInside)
        let rightItem = UIBarButtonItem(customView: rightBotton)
        self.navigationItem.rightBarButtonItem = rightItem
    }
    
    func loadVideo(){
        if img == nil{
            img = getVideoImage(self.url)
            
            let path = NSSearchPathForDirectoriesInDomains(.DocumentDirectory, .UserDomainMask, true)
            let documentDirectory = path[0] as String
            let filePath: String? = "\(documentDirectory)/\(String(NSDate())).png"
            let result = UIImagePNGRepresentation(img!)?.writeToFile(filePath!, atomically: true)
            if result == true {
                imgUrl = NSURL(fileURLWithPath: filePath!)
            }
        }
        
        let height = UIApplication.sharedApplication().statusBarFrame.height + 30
        playIconImageView = UIImageView(frame: CGRectMake(0, height, view.bounds.width, view.bounds.height - height))
        playIconImageView.image = img
        playIconImageView.contentMode = UIViewContentMode.ScaleAspectFit
        playIconImageView.backgroundColor = UIColor.blackColor()
        view.addSubview(playIconImageView)
        
        // 添加蒙版
        effectView.removeFromSuperview()
        effectView.frame = playIconImageView.bounds
        effectView.effect = UIBlurEffect(style: UIBlurEffectStyle.Dark)
        effectView.alpha = 0.4
        playIconImageView.addSubview(effectView)
        
        //  添加图标
        startButton = UIButton(frame: CGRectMake(0, 0, 64, 64))
        startButton.center = CGPointMake((view.bounds.width / 2), (view.bounds.height - height) / 2)
        startButton.layer.cornerRadius = 32.0
        startButton.layer.masksToBounds = true
        let img2 = UIImage(named: "play")?.imageWithRenderingMode(UIImageRenderingMode.AlwaysOriginal)
        startButton.setBackgroundImage(img2, forState: UIControlState.Normal)
        startButton.addTarget(self, action: Selector("playVideo"), forControlEvents: UIControlEvents.TouchUpInside)
        playView = UIView(frame: CGRectMake(0, height, view.bounds.width, view.bounds.height - height))
        playView.backgroundColor = UIColor.clearColor()
        view.addSubview(playView)
        playView.addSubview(startButton)
    }
    
    func playVideo(){
        if isplay == false{
            isplay = true
            addPlayLayar()
            player.play()
        }
     }
    
    func addPlayLayar(){
        let height = UIApplication.sharedApplication().statusBarFrame.height + 30
        let playerItem = AVPlayerItem(URL: self.url)
        NSNotificationCenter.defaultCenter().addObserver(self, selector: Selector("playFinish"), name: AVPlayerItemDidPlayToEndTimeNotification, object: playerItem)
        player = AVPlayer(playerItem: playerItem)
        playerLayer = AVPlayerLayer(player: player)
        playerLayer.frame = CGRect(x: 0,y: height,width: view.bounds.width, height: view.bounds.height - height)
        playView.backgroundColor = UIColor.blackColor()
        self.view.layer.addSublayer(playerLayer)
    }
    
    func playFinish(){
         playView.backgroundColor = UIColor.clearColor()
         playerLayer.removeFromSuperlayer()
         isplay = false
    }
    
    //  通过文件路径获取截图:
    func getVideoImage(videoUrl: NSURL) -> UIImage? {
        //  获取截图
        let videoAsset = AVURLAsset(URL: videoUrl)
        let cmTime = CMTime(seconds: 1, preferredTimescale: 10)
        let imageGenerator = AVAssetImageGenerator(asset: videoAsset)
        imageGenerator.appliesPreferredTrackTransform = true
        if let cgImage = try? imageGenerator.copyCGImageAtTime(cmTime, actualTime: nil) {
            let image = UIImage(CGImage: cgImage)
            return image
        } else {
            print("获取缩略图失败")
        }
        
        return nil
    }
    
    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    override func shouldAutorotate() -> Bool {
        return false
    }
    
    override func supportedInterfaceOrientations() -> UIInterfaceOrientationMask {
        return UIInterfaceOrientationMask.Portrait
    }
    
    override func preferredInterfaceOrientationForPresentation() -> UIInterfaceOrientation{
        return UIInterfaceOrientation.Portrait
    }
    
    override func willAnimateRotationToInterfaceOrientation(toInterfaceOrientation: UIInterfaceOrientation, duration: NSTimeInterval) {
            playView.layoutSubviews();
            loadVideo()
            super.willAnimateRotationToInterfaceOrientation(toInterfaceOrientation, duration: duration)
    }
}