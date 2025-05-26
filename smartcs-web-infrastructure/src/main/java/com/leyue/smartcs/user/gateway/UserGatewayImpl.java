package com.leyue.smartcs.user.gateway;

import com.leyue.smartcs.domain.user.gateway.UserGateway;
import com.leyue.smartcs.common.feign.UserCenterClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserGatewayImpl implements UserGateway {
    private final UserCenterClient userCenterClient;
    public String validateUserToken(String token){
       return userCenterClient.validateUserToken(token);
    }
}
