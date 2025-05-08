package com.leyue.smartcs.user.executor;

import com.leyue.smartcs.domain.gateway.UserGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserValidateTokenCmdExe {
    private final UserGateway userGateway;
    public String execute(String token) {
        return userGateway.validateUserToken(token);
    }
}
