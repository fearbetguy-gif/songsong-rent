package com.songsong.rent.service;

import com.songsong.rent.dto.UserUpdateRequest;
import com.songsong.rent.entity.User;

import java.util.List;

public interface UserService {

    User register(String username, String nickname, String password);

    User login(String username, String password);

    User adminLogin(String username, String password);

    List<User> list();

    void update(UserUpdateRequest request);

    void deleteById(Long id);
}
