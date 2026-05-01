package org.example.gcsj4.model.entity;

import java.io.Serializable;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

/**
 * User 实体类
 * @author Hibiscus-code-generate
 */
@TableName("sys_user")
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
    * id
    */
    @TableId
    private Long id;

    /**
    * 账号
    */
    @TableField("username")
    private String username;

    /**
    * 密码
    */
    @TableField("password")
    private String password;

    /**
    * admin/user
    */
    @TableField("role")
    private String role;

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
    public String getUsername() {
    return username;
    }

    public void setUsername(String username) {
    this.username = username;
    }
    public String getPassword() {
    return password;
    }

    public void setPassword(String password) {
    this.password = password;
    }
    public String getRole() {
    return role;
    }

    public void setRole(String role) {
    this.role = role;
    }
    public LocalDateTime getCreateTime() {
    return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
    this.createTime = createTime;
    }
}
