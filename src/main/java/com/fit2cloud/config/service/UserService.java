package com.fit2cloud.config.service;

import com.fit2cloud.commons.server.base.domain.User;
import com.fit2cloud.config.dao.ext.ExtGetUserKeyMapper;
import com.fit2cloud.config.keycloak.keycloakLoginToken;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
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

    //例如 http://103.235.232.207/auth/
    @Value("${keycloak-server-imperson-address}")
    private String keyloakServerImpersonAddress;

    //例如 cmp
    @Value("${keycloak.realm}")
    private String keycloakRealm;

    private String getToken(String serverAddress, String username, String password) {
        HttpHeaders tokenHeaders = new HttpHeaders();
        tokenHeaders.setAccept(new ArrayList<>(Collections.singleton(MediaType.APPLICATION_JSON)));
        tokenHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> mvm = new LinkedMultiValueMap<>();
        mvm.add("client_id", "cmp-client");
        mvm.add("username", username);
        mvm.add("password", password);
        mvm.add("grant_type", "password");
        HttpEntity<Object> formEntity = new HttpEntity<>(mvm, tokenHeaders);
        String tokenUrl = String.format("%s%s", serverAddress, "/realms/" + this.keycloakRealm + "/protocol/openid-connect/token");
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
        String msg = "";
        Map map = new HashMap();
        if (StringUtils.isBlank(user) || StringUtils.isBlank(password)) {
            msg = "user or password can't be null";
            map.put("status", false);
            map.put("message", msg);
            return map;
        }
        try {
            //获得token
            String token = this.getToken(keyloakServerImpersonAddress, user, password);
            map.put("status", true);
            return map;
        }catch (Exception e){
            msg = e.getMessage() + ": username or password error";
        }
        map.put("status", false);
        map.put("message", msg);
        return map;

    }
}
