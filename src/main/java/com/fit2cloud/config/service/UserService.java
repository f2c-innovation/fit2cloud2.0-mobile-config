package com.fit2cloud.config.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fit2cloud.commons.server.base.domain.User;
import com.fit2cloud.config.dao.ext.ExtGetUserKeyMapper;
import com.fit2cloud.config.keycloak.keycloakLoginToken;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.UnauthorizedException;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * @Author maguohao
 * @Date 2019/8/8 6:20 PM
 * @Version 1.0
 **/
@Service
public class UserService {


    @Resource
    private RestTemplate remoteRestTemplate;

    @Resource
    private ExtGetUserKeyMapper extGetUserKeyMapper;

    // http://103.235.232.207/auth/
    @Value("http://103.235.232.207/auth/")
    private String keyloakServerImpersonAddress;

    // admin
    @Value("admin")
    private String keycloakAdmin;

    // Password123@keycloak
    @Value("Password123@keycloak")
    private String keycloakPassword;

    // cmp
    @Value("cmp")
    private String keycloakRealm;


    /**
     * 模拟登陆
     * @param request
     * @param response
     * @param searchName
     */
    public Object impersonateLogin(HttpServletRequest request, HttpServletResponse response, String searchName, String password) {
        try {

            if (StringUtils.isBlank(keycloakAdmin) || StringUtils.isBlank(keycloakPassword)) {
                throw new RuntimeException("请先设置 KeyCloak用户名和密码！");
            }

            //获得token
            String token = this.getToken(keyloakServerImpersonAddress, keycloakAdmin, keycloakPassword);
            //根据email获得keycloak中用户的Id
            String userUrl = String.format("%s%s%s", keyloakServerImpersonAddress, "/admin/realms/" + keycloakRealm + "/users?search=", searchName);

            String userId = getUserId(userUrl, token);

            if (StringUtils.isBlank(userId)) {
                throw new Exception("在keycloak中未找到：" + searchName);
            }
            //模拟登录
            String impersonationUrl = String.format("%s%s%s%s", keyloakServerImpersonAddress, "/admin/realms/" + keycloakRealm + "/users/", userId, "/impersonation");

            HttpHeaders userHeaders = new HttpHeaders();
            userHeaders.setAccept(new ArrayList<>(Collections.singleton(MediaType.APPLICATION_JSON)));
            userHeaders.set("Authorization", String.format("%s%s", "bearer ", token));
            HttpEntity<Object> userEntity = new HttpEntity<>(null, userHeaders);
            ResponseEntity<String> exchange = remoteRestTemplate.exchange(impersonationUrl, HttpMethod.POST, userEntity, String.class);
            JSONObject objects = JSON.parseObject(exchange.getBody());
            String redirectUrl = objects.getString("redirect");
            //获得url,set cookie 重定向
            List<String> cookiesToSet = exchange.getHeaders().get("Set-Cookie");
            Cookie cookie = null;
            for (int i = 0; i < cookiesToSet.size(); i++) {
                response.addHeader("Set-Cookie", cookiesToSet.get(i));
            }
            return getKey(searchName, password);
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    private String getUserId(String url, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(new ArrayList<>(Collections.singleton(MediaType.APPLICATION_JSON)));
        headers.set("Authorization", String.format("%s%s", "bearer ", token));
        HttpEntity<Object> entity = new HttpEntity<>(null, headers);
        ResponseEntity<String> exchange = remoteRestTemplate.exchange(url, HttpMethod.GET, entity, String.class);
        JSONArray objects = JSON.parseArray(exchange.getBody());
        if (objects.size() <= 0) {
            return null;
        }
        return objects.getJSONObject(0).getString("id");
    }

    private String getToken(String serverAddress, String username, String password) {
        HttpHeaders tokenHeaders = new HttpHeaders();
        tokenHeaders.setAccept(new ArrayList<>(Collections.singleton(MediaType.APPLICATION_JSON)));
        tokenHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> mvm = new LinkedMultiValueMap<>();
        mvm.add("client_id", "admin-cli");
        mvm.add("username", username);
        mvm.add("password", password);
        mvm.add("grant_type", "password");
        HttpEntity<Object> formEntity = new HttpEntity<>(mvm, tokenHeaders);
        String tokenUrl = String.format("%s%s", serverAddress, "/realms/master/protocol/openid-connect/token");
        ResponseEntity<keycloakLoginToken> keycloakTokenEntity = remoteRestTemplate.postForEntity(tokenUrl, formEntity, keycloakLoginToken.class);
        return Objects.requireNonNull(keycloakTokenEntity.getBody()).getAccessToken();
    }

    /**
     * 获取用户accesskey 与secretKey 与sourceId
     * @param name
     * @return
     */
    public Object getKey(String name, String password) {
        Map map = this.login(name, password);
        if (map.get("status").equals(true)) {
            List<User> userList = extGetUserKeyMapper.getUserList(name);
            String userId = userList.get(0).getId();
            return extGetUserKeyMapper.getUserKey(userId).get(0);
        }
        return map;
    }

    /**
     * 模拟登录认证
     * @param user
     * @param password
     * @return
     */
    public Map login(String user, String password) {
        String msg;
        Map map = new HashMap();
        if (StringUtils.isBlank(user) || StringUtils.isBlank(password)) {
            msg = "user or password can't be null";
            map.put("status", false);
            map.put("message", msg);
            return map;
        }

        UsernamePasswordToken token = new UsernamePasswordToken(StringUtils.trim(user), StringUtils.trim(password));
        Subject subject = SecurityUtils.getSubject();

        try {
            subject.login(token);
            if (subject.isAuthenticated()) {
                map.put("status", true);
                return map;
            } else {
                map.put("status", true);
                return map;
            }
        } catch (IncorrectCredentialsException | UnknownAccountException e) {
            msg = "UnknownAccountException. " + e.getMessage();
        } catch (ExcessiveAttemptsException e) {
            msg = "excessive attempts";
        } catch (LockedAccountException e) {
            msg = "the user has been locked.";
        } catch (DisabledAccountException e) {
            msg = "the user has been disabled. ";
        } catch (ExpiredCredentialsException e) {
            msg = "user expires. ";
        } catch (UnauthorizedException e) {
            msg = "not authorized. " + e.getMessage();
        } catch (AuthenticationException e) {
            msg = "AuthenticationException. " + e.getMessage();
        }
        map.put("status", false);
        map.put("message", msg);
        return map;

    }
}
