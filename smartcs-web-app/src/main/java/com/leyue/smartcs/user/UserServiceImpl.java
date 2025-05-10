package com.leyue.smartcs.user;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.TypeReference;
import com.leyue.smartcs.api.user.UserService;
import com.leyue.smartcs.context.UserContext;
import com.leyue.smartcs.user.executor.UserValidateTokenCmdExe;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserValidateTokenCmdExe validateTokenCmdExe;

    @Override
    public boolean validateUserToken(String token) {
        String response = validateTokenCmdExe.execute(token);
        JSONObject result = JSONObject.parseObject(response);

        if (result.getBooleanValue("success")) {
            JSONObject userInfo = result.getJSONObject("data");
            if (userInfo != null) {
                UserContext.UserInfo currentUser = userInfo.toJavaObject(new TypeReference<>() {
                });
                // 保存到ThreadLocal
                UserContext.setCurrentUser(currentUser);
                return true;
            }
        }
        return false;
    }
}
