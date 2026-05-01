package org.example.gcsj4.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.gcsj4.model.entity.User;
import org.example.gcsj4.mapper.UserMapper;
import org.example.gcsj4.service.UserService;
import org.example.gcsj4.utils.Result;
import org.springframework.stereotype.Service;

/**
 * User 服务实现类
 * @author Hibiscus-code-generate
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    @Override
    public Result<?> login(String username, String password) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username)
                .eq(User::getPassword, password);
        User user = getOne(wrapper);
        if (user == null) {
            return Result.fail("账号或密码错误");
        }
        return Result.success(user);
    }

    @Override
    public Result<?> register(String username, String password) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        if (count(wrapper) > 0) {
            return Result.fail("账号已存在");
        }
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setRole("user");
        save(user);
        return Result.success("注册成功");
    }
}
