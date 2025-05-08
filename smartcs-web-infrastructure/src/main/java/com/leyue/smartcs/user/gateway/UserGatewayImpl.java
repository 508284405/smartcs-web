package com.leyue.smartcs.user.gateway;

import com.leyue.smartcs.domain.user.gateway.UserGateway;
import com.leyue.smartcs.user.feign.UserValidateClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserGatewayImpl implements UserGateway {
    private final UserValidateClient userValidateClient;
    public String validateUserToken(String token){
       return userValidateClient.validateUserToken(token);
    }
}
