package org.example.gcsj4.model.entity;

import java.io.Serializable;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * AuditReport 实体类
 * @author Hibiscus-code-generate
 */
@TableName("video_audit_report")
public class AuditReport implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
    * id
    */
    @TableId
    private Long id;

    /**
    * 视频ID
    */
    @TableField("video_id")
    private Long videoId;

    /**
    * 总抽帧数
    */
    @TableField("total_frame_num")
    private Integer totalFrameNum;

    /**
    * 暴力帧数
    */
    @TableField("violence_frame_num")
    private Integer violenceFrameNum;

    /**
    * 暴力占比
    */
    @TableField("violence_ratio")
    private BigDecimal violenceRatio;

    /**
    * 审核结论
    */
    @TableField("conclusion")
    private String conclusion;

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
    public Integer getTotalFrameNum() {
    return totalFrameNum;
    }

    public void setTotalFrameNum(Integer totalFrameNum) {
    this.totalFrameNum = totalFrameNum;
    }
    public Integer getViolenceFrameNum() {
    return violenceFrameNum;
    }

    public void setViolenceFrameNum(Integer violenceFrameNum) {
    this.violenceFrameNum = violenceFrameNum;
    }
    public BigDecimal getViolenceRatio() {
    return violenceRatio;
    }

    public void setViolenceRatio(BigDecimal violenceRatio) {
    this.violenceRatio = violenceRatio;
    }
    public String getConclusion() {
    return conclusion;
    }

    public void setConclusion(String conclusion) {
    this.conclusion = conclusion;
    }
    public LocalDateTime getCreateTime() {
    return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
    this.createTime = createTime;
    }
}
