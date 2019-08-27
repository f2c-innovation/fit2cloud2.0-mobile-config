package com.fit2cloud.config.controller;

import com.fit2cloud.commons.server.base.domain.User;
import com.fit2cloud.commons.utils.ResultHolder;
import com.fit2cloud.config.service.UserService;
import io.swagger.annotations.ApiOperation;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("anonymous/impersonateLogin")
public class UserController {

    @Resource
    private UserService userService;

    @PostMapping("/getUserkeysByuserId")
    @ApiOperation("移动端用户查看密钥对")
    public Object impersonateLogin (@RequestBody User user) throws Exception{
        try {
            Object o = userService.getKey(user.getId(), user.getPassword());
            return o;
        }catch (Exception e){
            return e.getMessage();
        }
    }
}
