package com.rikkei.bank.service.user;

import com.rikkei.bank.dto.auth.request.ChangePinRequest;
import com.rikkei.bank.dto.admin.response.UserResponse;
import com.rikkei.bank.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IUserService {

    void changePin(User user, ChangePinRequest request);

    Page<UserResponse> getAllUsers(int page, int size);

    void lockUser(Long userId, boolean lock);

    void assignRole(Long userId, String roleName);
}