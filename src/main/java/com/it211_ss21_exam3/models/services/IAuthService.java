package com.it211_ss21_exam3.models.services;

import com.it211_ss21_exam3.models.dtos.req.LoginReq;
import com.it211_ss21_exam3.models.dtos.req.RegisterReq;
import com.it211_ss21_exam3.models.dtos.res.JwtRes;
import com.it211_ss21_exam3.models.dtos.res.TokenRefreshRes;

public interface IAuthService
{
    void register(RegisterReq req);

    JwtRes login(LoginReq req);

    TokenRefreshRes refreshToken(String refreshToken);

    void logout(Long userId);
}