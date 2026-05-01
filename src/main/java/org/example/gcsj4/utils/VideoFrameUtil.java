package org.example.gcsj4.utils;

import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class VideoFrameUtil {

    /**
     * 按间隔秒抽帧
     * @param videoPath 视频路径
     * @param saveDir 帧保存目录
     * @param intervalSec 间隔几秒抽一帧
     * @return 帧图片路径列表
     */
    public static List<String> extractFrames(String videoPath, String saveDir, int intervalSec) throws Exception {
        List<String> imgList = new ArrayList<>();
        File dir = new File(saveDir);
        if(!dir.exists()) dir.mkdirs();

        FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(videoPath);
        grabber.start();
        double fps = grabber.getFrameRate();
        int frameInterval = (int) (fps * intervalSec);

        int frameIndex = 0;
        int imgIndex = 0;
        Frame frame;
        Java2DFrameConverter converter = new Java2DFrameConverter();

        while ((frame = grabber.grabImage()) != null){
            if(frameIndex % frameInterval == 0){
                BufferedImage image = converter.convert(frame);
                if(image != null){
                    String imgPath = saveDir + File.separator + "frame_"+imgIndex+".jpg";
                    ImageIO.write(image,"jpg",new File(imgPath));
                    imgList.add(imgPath);
                    imgIndex++;
                }
            }
            frameIndex++;
        }
        grabber.stop();
        return imgList;
    }
}