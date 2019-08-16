package com.fit2cloud.config.controller;

import com.fit2cloud.config.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
@RequestMapping("impersonateLogin")
public class UserController {

    @Resource
    private UserService userService;

    @GetMapping("/getUserkeysByuserId/{userId}")
    public Object impersonateLogin (HttpServletRequest request, HttpServletResponse response, @PathVariable String userId) throws Exception{
        try {
            return userService.impersonateLogin(request, response, userId);
        }catch (Exception e){
            return e.getMessage();
        }
    }
}
