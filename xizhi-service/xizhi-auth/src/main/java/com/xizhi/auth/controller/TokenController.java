package com.xizhi.auth.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.anji.captcha.model.common.ResponseModel;
import com.anji.captcha.service.CaptchaService;
import com.xizhi.auth.form.LoginForm;
import com.xizhi.auth.service.AccessTokenService;
import com.xizhi.auth.service.SysLoginService;
import com.xizhi.common.core.domain.R;
import com.xizhi.system.domain.SysUser;

@RestController
public class TokenController
{
    @Autowired
    private AccessTokenService tokenService;

    @Autowired
    private SysLoginService    sysLoginService;

    @Autowired
    private CaptchaService     captchaService;

    @PostMapping("login")
    public R login(@RequestBody LoginForm form)
    {
        // 用户登录
        SysUser user = sysLoginService.login(form.getUsername(), form.getPassword());
        // 获取登录token
        return R.ok(tokenService.createToken(user));
    }

    @PostMapping("login/slide")
    public R loginSilde(@RequestBody LoginForm form)
    {
        ResponseModel response = captchaService.verification(form.getCaptchaVO());
        if (response.isSuccess())
        {
            // 用户登录
            SysUser user = sysLoginService.login(form.getUsername(), form.getPassword());
            // 获取登录token
            return R.ok(tokenService.createToken(user));
        }
        return R.error().put("repCode", response.getRepCode());
    }

    @PostMapping("logout")
    public R logout(HttpServletRequest request)
    {
        String token=request.getHeader("token");
        SysUser user=tokenService.queryByToken(token);
        if (null != user)
        {
            sysLoginService.logout(user.getLoginName());
            tokenService.expireToken(user.getUserId());
        }
        return R.ok();
    }
}
