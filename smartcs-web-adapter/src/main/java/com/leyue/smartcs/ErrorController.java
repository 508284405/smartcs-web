package com.leyue.smartcs;

import com.alibaba.cola.dto.Response;
import com.leyue.smartcs.config.BadCredentialsException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/error")
public class ErrorController {

    @RequestMapping("/unauthorized")
    public Response handleUnauthorized(String message) {
        throw new BadCredentialsException(message);
    }

    @RequestMapping("/forbidden")
    public Response handleForbidden(String message) {
        return Response.buildFailure(String.valueOf(HttpStatus.FORBIDDEN.value()),
                message != null ? message : "Access Denied");
    }
}