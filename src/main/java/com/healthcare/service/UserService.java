package com.healthcare.service;

import com.healthcare.model.User;
import java.util.List;

public interface UserService {
    User saveUser(User user);
    List<User> getAllUsers();
	String login(User loginRequest);
}