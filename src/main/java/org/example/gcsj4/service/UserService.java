package org.example.gcsj4.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.gcsj4.model.entity.User;
import org.example.gcsj4.utils.Result;

/**
 * User 服务接口
 * @author Hibiscus-code-generate
 */
public interface UserService extends IService<User> {
    Result<?> login(String username, String password);
    Result<?> register(String username, String password);
}
