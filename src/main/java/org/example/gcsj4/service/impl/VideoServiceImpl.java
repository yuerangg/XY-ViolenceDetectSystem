package org.example.gcsj4.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import lombok.RequiredArgsConstructor;
import org.example.gcsj4.mapper.AuditReportMapper;
import org.example.gcsj4.mapper.FrameDetailMapper;
import org.example.gcsj4.mapper.InfoMapper;
import org.example.gcsj4.model.entity.AuditReport;
import org.example.gcsj4.model.entity.FrameDetail;
import org.example.gcsj4.model.entity.Info;
import org.example.gcsj4.service.VideoService;
import org.example.gcsj4.utils.Result;
import org.example.gcsj4.utils.VideoFrameUtil;
import org.example.gcsj4.utils.YoloDetectUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VideoServiceImpl extends ServiceImpl<InfoMapper, Info> implements VideoService {

    private final FrameDetailMapper frameDetailMapper;
    private final AuditReportMapper reportMapper;
    private final YoloDetectUtil yoloDetectUtil;

    @Value("${file.upload.video-path}")
    private String videoUploadPath;
    @Value("${file.upload.frame-path}")
    private String frameUploadPath;

    // 抽帧间隔：3秒1帧，省额度省性能
    private static final int INTERVAL_SEC = 3;
    // 暴力帧占比阈值：超过10%判定为暴力视频
    private static final double VIOLENCE_THRESHOLD = 0.1;

    @Override
    public Result<?> uploadVideo(Long userId, MultipartFile file) {
        try {
            // 原始文件名
            String originalName = file.getOriginalFilename();
            String suffix = originalName.substring(originalName.lastIndexOf("."));
            // 唯一文件名
            String uuidName = IdUtil.simpleUUID() + suffix;
            String savePath = videoUploadPath + uuidName;

            // 保存视频到本地
            File dest = new File(savePath);
            FileUtil.writeBytes(file.getBytes(), dest);

            // 保存数据库记录
            Info video = new Info();
            video.setUserId(userId);
            video.setVideoName(originalName);
            video.setVideoSuffix(suffix);
            video.setVideoSavePath(savePath);
            video.setStatus((byte) 0); // 待审核
            save(video);

            // 异步开始审核
            asyncAuditVideo(video.getId());
            return Result.success(video.getId());
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("视频上传失败");
        }
    }

    @Async
    @Override
    public void asyncAuditVideo(Long videoId) {
        try {
            Info video = getById(videoId);
            String videoPath = video.getVideoSavePath();
            // 当前视频帧保存目录
            String curFramePath = frameUploadPath + videoId + File.separator;

            // 1. 视频抽帧
            List<String> framePathList = VideoFrameUtil.extractFrames(videoPath, curFramePath, INTERVAL_SEC);
            int totalFrame = framePathList.size();
            int violenceCount = 0;

            // 2. 逐帧YOLO识别
            for (int i = 0; i < framePathList.size(); i++) {
                String framePath = framePathList.get(i);
                YoloDetectUtil.DetectResult detectResult = yoloDetectUtil.detect(framePath);

                FrameDetail detail = new FrameDetail();
                detail.setVideoId(videoId);
                detail.setFrameSavePath(framePath);
                detail.setVideoSecond(i * INTERVAL_SEC);
                detail.setIsViolence((byte) (detectResult.isViolence() ? 1 : 0));
                detail.setDetectLabels(detectResult.getLabels());
                detail.setCreateTime(LocalDateTime.now());
                frameDetailMapper.insert(detail);

                if (detectResult.isViolence()) {
                    violenceCount++;
                }
            }

            // 3. 计算暴力占比
            BigDecimal ratio = BigDecimal.valueOf((double) violenceCount / totalFrame)
                    .setScale(2, BigDecimal.ROUND_HALF_UP);
            // 4. 判定视频状态
            int status = ratio.doubleValue() > VIOLENCE_THRESHOLD ? 2 : 1;

            // 更新视频状态
            video.setStatus((byte) status);
            updateById(video);

            // 5. 保存审核报告
            AuditReport report = new AuditReport();
            report.setVideoId(videoId);
            report.setTotalFrameNum(totalFrame);
            report.setViolenceFrameNum(violenceCount);
            report.setViolenceRatio(ratio);
            report.setConclusion(status == 2 ? "检测到暴力内容，审核不通过" : "无暴力内容，审核通过");
            report.setCreateTime(LocalDateTime.now());
            reportMapper.insert(report);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Result<?> getVideoResult(Long videoId) {
        Info video = getById(videoId);
        if (video == null) {
            return Result.fail("视频不存在");
        }
        LambdaQueryWrapper<AuditReport> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AuditReport::getVideoId, videoId);
        AuditReport report = reportMapper.selectOne(wrapper);
        return Result.success(report);
    }

    @Override
    public Result<?> listByUserId(Long userId) {
        LambdaQueryWrapper<Info> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Info::getUserId, userId)
                .orderByDesc(Info::getCreateTime);
        List<Info> list = list(wrapper);
        return Result.success(list);
    }
}