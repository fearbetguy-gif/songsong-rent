package com.songsong.rent.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.songsong.rent.common.ResultCode;
import com.songsong.rent.dto.UserUpdateRequest;
import com.songsong.rent.entity.User;
import com.songsong.rent.exception.BusinessException;
import com.songsong.rent.mapper.UserMapper;
import com.songsong.rent.service.UserService;
import com.songsong.rent.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public User register(String username, String nickname, String password) {
        if (!StringUtils.hasText(username) || !StringUtils.hasText(nickname) || !StringUtils.hasText(password)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "用户名、昵称或密码不能为空");
        }
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, username);
        User existUser = userMapper.selectOne(queryWrapper);
        if (existUser != null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "用户名已存在");
        }
        User user = new User();
        user.setUsername(username);
        user.setNickname(nickname);
        user.setIsAdmin(0);
        user.setPassword(passwordEncoder.encode(password));
        userMapper.insert(user);

        User result = new User();
        result.setId(user.getId());
        result.setUsername(user.getUsername());
        result.setNickname(user.getNickname());
        result.setIsAdmin(user.getIsAdmin());
        result.setToken(JwtUtil.createToken(user.getId(), user.getIsAdmin()));
        return result;
    }

    @Override
    public User login(String username, String password) {
        if (!StringUtils.hasText(username) || !StringUtils.hasText(password)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "用户名或密码不能为空");
        }
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, username);
        User user = userMapper.selectOne(queryWrapper);
        if (user == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "用户不存在或密码错误");
        }

        // 验证 BCrypt 密码
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "用户不存在或密码错误");
        }

        User result = new User();
        result.setId(user.getId());
        result.setUsername(user.getUsername());
        result.setNickname(user.getNickname());
        result.setIsAdmin(user.getIsAdmin());
        result.setToken(JwtUtil.createToken(user.getId(), user.getIsAdmin()));
        return result;
    }

    @Override
    public User adminLogin(String username, String password) {
        User user = login(username, password);
        if (user.getIsAdmin() == null || user.getIsAdmin() != 1) {
            throw new BusinessException(ResultCode.FORBIDDEN, "该账号无后台管理权限");
        }
        return user;
    }

    @Override
    public List<User> list() {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(User::getId);
        List<User> users = userMapper.selectList(queryWrapper);
        for (User user : users) {
            user.setPassword(null);
        }
        return users;
    }

    @Override
    public void update(UserUpdateRequest request) {
        User user = userMapper.selectById(request.getId());
        if (user == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "用户不存在");
        }
        user.setNickname(request.getNickname().trim());
        user.setIsAdmin(request.getIsAdmin());
        userMapper.updateById(user);
    }

    @Override
    public void deleteById(Long id) {
        userMapper.deleteById(id);
    }
}
