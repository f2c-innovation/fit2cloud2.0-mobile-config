package com.fit2cloud.config.controller;

import com.fit2cloud.commons.utils.ResultHolder;
import com.fit2cloud.config.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("impersonateLogin")
public class UserController {

    @Resource
    private UserService userService;

    @GetMapping("/getUserkeysByuserId/{userId}")
    public Object impersonateLogin (@PathVariable String userId) throws Exception{
        try {
            Object o = userService.getKey(userId);
            return o;
        }catch (Exception e){
            return e.getMessage();
        }
    }
}
