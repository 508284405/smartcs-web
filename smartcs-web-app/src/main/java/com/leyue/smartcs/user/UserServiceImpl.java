package com.leyue.smartcs.user;

import com.leyue.smartcs.api.user.UserService;
import com.leyue.smartcs.user.executor.UserValidateTokenCmdExe;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserValidateTokenCmdExe validateTokenCmdExe;
    @Override
    public String validateUserToken(String token) {
        return validateTokenCmdExe.execute(token);
    }
}
