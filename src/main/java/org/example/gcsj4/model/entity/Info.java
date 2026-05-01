package org.example.gcsj4.model.entity;

import java.io.Serializable;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

/**
 * Info 实体类
 * @author Hibiscus-code-generate
 */
@TableName("video_info")
public class Info implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
    * id
    */
    @TableId
    private Long id;

    /**
    * 上传用户ID
    */
    @TableField("user_id")
    private Long userId;

    /**
    * 视频原名
    */
    @TableField("video_name")
    private String videoName;

    /**
    * 后缀mp4等
    */
    @TableField("video_suffix")
    private String videoSuffix;

    /**
    * 服务器存储路径
    */
    @TableField("video_save_path")
    private String videoSavePath;

    /**
    * 视频时长秒
    */
    @TableField("video_duration")
    private Integer videoDuration;

    /**
    * 0待审核 1正常 2暴力
    */
    @TableField("status")
    private Byte status;

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
    public Long getUserId() {
    return userId;
    }

    public void setUserId(Long userId) {
    this.userId = userId;
    }
    public String getVideoName() {
    return videoName;
    }

    public void setVideoName(String videoName) {
    this.videoName = videoName;
    }
    public String getVideoSuffix() {
    return videoSuffix;
    }

    public void setVideoSuffix(String videoSuffix) {
    this.videoSuffix = videoSuffix;
    }
    public String getVideoSavePath() {
    return videoSavePath;
    }

    public void setVideoSavePath(String videoSavePath) {
    this.videoSavePath = videoSavePath;
    }
    public Integer getVideoDuration() {
    return videoDuration;
    }

    public void setVideoDuration(Integer videoDuration) {
    this.videoDuration = videoDuration;
    }
    public Byte getStatus() {
    return status;
    }

    public void setStatus(Byte status) {
    this.status = status;
    }
    public LocalDateTime getCreateTime() {
    return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
    this.createTime = createTime;
    }
}
