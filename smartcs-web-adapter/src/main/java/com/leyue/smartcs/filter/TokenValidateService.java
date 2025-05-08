package com.leyue.smartcs.filter;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.TypeReference;
import com.leyue.smartcs.api.user.UserService;
import com.leyue.smartcs.context.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class TokenValidateService {
    
    @Autowired
    private UserService userService;

    public boolean validateToken(String token) {
        try {
            String response = userService.validateUserToken(token);
            JSONObject result = JSONObject.parseObject(response);
            
            if (result.getBooleanValue("success")) {
                JSONObject userInfo = result.getJSONObject("data");
                if (userInfo != null) {
                    UserContext.UserInfo currentUser = userInfo.toJavaObject(new TypeReference<UserContext.UserInfo>() {
                    });
                    // 保存到ThreadLocal
                    UserContext.setCurrentUser(currentUser);
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            log.error("验证token失败", e);
            return false;
        }
    }
}