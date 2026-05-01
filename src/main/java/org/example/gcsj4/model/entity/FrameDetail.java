package org.example.gcsj4.model.entity;

import java.io.Serializable;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

/**
 * FrameDetail 实体类
 * @author Hibiscus-code-generate
 */
@TableName("video_frame_detail")
public class FrameDetail implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
    * id
    */
    @TableId
    private Long id;

    /**
    * 关联视频ID
    */
    @TableField("video_id")
    private Long videoId;

    /**
    * 帧图片路径
    */
    @TableField("frame_save_path")
    private String frameSavePath;

    /**
    * 视频第几秒
    */
    @TableField("video_second")
    private Integer videoSecond;

    /**
    * 0正常 1暴力
    */
    @TableField("is_violence")
    private Byte isViolence;

    /**
    * 识别标签:fight,knife
    */
    @TableField("detect_labels")
    private String detectLabels;

    /**
    * create_time
    */
    @TableField("create_time")
    private LocalDateTime createTime;


    public Long getId() {
    return id;
    }

    public void setId(Long id) {
    this.id = id;
    }
    public Long getVideoId() {
    return videoId;
    }

    public void setVideoId(Long videoId) {
    this.videoId = videoId;
    }
    public String getFrameSavePath() {
    return frameSavePath;
    }

    public void setFrameSavePath(String frameSavePath) {
    this.frameSavePath = frameSavePath;
    }
    public Integer getVideoSecond() {
    return videoSecond;
    }

    public void setVideoSecond(Integer videoSecond) {
    this.videoSecond = videoSecond;
    }
    public Byte getIsViolence() {
    return isViolence;
    }

    public void setIsViolence(Byte isViolence) {
    this.isViolence = isViolence;
    }
    public String getDetectLabels() {
    return detectLabels;
    }

    public void setDetectLabels(String detectLabels) {
    this.detectLabels = detectLabels;
    }
    public LocalDateTime getCreateTime() {
    return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
    this.createTime = createTime;
    }
}
