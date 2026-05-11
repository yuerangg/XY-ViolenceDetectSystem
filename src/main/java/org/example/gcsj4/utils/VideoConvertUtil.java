package org.example.gcsj4.utils;

import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;

import java.io.File;

public class VideoConvertUtil {

    /**
     * 将 AVI 视频转换为 MP4 格式
     * @param sourcePath 源文件路径
     * @param targetPath 目标文件路径
     * @return 转换后的文件路径
     */


    public static String convertToMp4(String sourcePath, String targetPath) throws Exception {
        FFmpegFrameGrabber grabber = null;
        FFmpegFrameRecorder recorder = null;

        try {
            // 初始化抓取器
            grabber = new FFmpegFrameGrabber(sourcePath);
            grabber.start();

            // 获取视频属性
            int width = grabber.getImageWidth();
            int height = grabber.getImageHeight();
            int audioChannels = grabber.getAudioChannels();
            double frameRate = grabber.getFrameRate();
            long totalFrames = grabber.getLengthInFrames();

            // 初始化录制器
            recorder = new FFmpegFrameRecorder(targetPath, width, height, audioChannels);
            recorder.setVideoCodec(org.bytedeco.ffmpeg.global.avcodec.AV_CODEC_ID_H264);
            recorder.setFormat("mp4");
            recorder.setFrameRate(frameRate > 0 ? frameRate : 25);
            recorder.setVideoBitrate(grabber.getVideoBitrate() > 0 ? grabber.getVideoBitrate() : 1000000);

            if (audioChannels > 0) {
                recorder.setAudioCodec(org.bytedeco.ffmpeg.global.avcodec.AV_CODEC_ID_AAC);
                recorder.setAudioBitrate(grabber.getAudioBitrate() > 0 ? grabber.getAudioBitrate() : 128000);
                recorder.setSampleRate(grabber.getSampleRate() > 0 ? grabber.getSampleRate() : 44100);
                recorder.setAudioChannels(audioChannels);
            }

            recorder.start();

            // 逐帧处理
            Frame frame;
            int frameCount = 0;
            while ((frame = grabber.grab()) != null) {
                if (frame.image != null) {
                    recorder.record(frame);
                    frameCount++;
                } else if (frame.samples != null && audioChannels > 0) {
                    recorder.record(frame);
                }
            }

            System.out.println("视频转换完成，共处理 " + frameCount + " 帧");
            return targetPath;

        } finally {
            if (recorder != null) {
                try {
                    recorder.stop();
                    recorder.release();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (grabber != null) {
                try {
                    grabber.stop();
                    grabber.release();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 检查是否需要转换
     */
    public static boolean needsConversion(String fileName) {
        if (fileName == null) return false;
        String lowerName = fileName.toLowerCase();
        return lowerName.endsWith(".avi") ||
                lowerName.endsWith(".mkv") ||
                lowerName.endsWith(".flv") ||
                lowerName.endsWith(".wmv");
    }
}
