package com.leyue.smartcs.common.util;

import com.alibaba.fastjson2.JSON;
import com.leyue.smartcs.config.JwtConfig;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.time.Duration;
import java.util.Base64;
import java.util.List;

/**
 * JWT令牌工具类
 */
@Slf4j
@Component
public class JwtTokenUtil {

    @Autowired
    private JwtConfig jwtConfig;

    /**
     * 解析JWT令牌
     *
     * @param token JWT令牌
     * @return Claims
     */
    public Claims parseToken(String token) {
        try {
            PublicKey publicKey = getPublicKey();
            return Jwts.parserBuilder()
                    .setSigningKey(publicKey)
                    .setAllowedClockSkewSeconds(jwtConfig.getClockSkew())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            log.error("解析JWT令牌失败: {}", e.getMessage());
            throw new RuntimeException("JWT令牌解析失败", e);
        }
    }

    /**
     * 验证JWT令牌
     *
     * @param token JWT令牌
     * @return 是否有效
     */
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (Exception e) {
            log.warn("JWT令牌验证失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 从令牌中获取用户ID
     *
     * @param token JWT令牌
     * @return 用户ID
     */
    public Long getUserId(String token) {
        Claims claims = parseToken(token);
        return claims.get("userId", Long.class);
    }

    /**
     * 从令牌中获取用户名
     *
     * @param token JWT令牌
     * @return 用户名
     */
    public String getUsername(String token) {
        Claims claims = parseToken(token);
        return claims.get("username", String.class);
    }

    /**
     * 从令牌中获取用户角色
     *
     * @param token JWT令牌
     * @return 用户角色列表
     */
    @SuppressWarnings("unchecked")
    public List<String> getUserRoles(String token) {
        Claims claims = parseToken(token);
        Object rolesObj = claims.get("roles");
        if (rolesObj instanceof List) {
            return (List<String>) rolesObj;
        } else if (rolesObj instanceof String) {
            return JSON.parseArray((String) rolesObj, String.class);
        }
        return List.of();
    }

    /**
     * 从令牌中获取用户菜单
     *
     * @param token JWT令牌
     * @return 用户菜单列表
     */
    @SuppressWarnings("unchecked")
    public List<Object> getUserMenus(String token) {
        Claims claims = parseToken(token);
        Object menusObj = claims.get("menus");
        if (menusObj instanceof List) {
            return (List<Object>) menusObj;
        } else if (menusObj instanceof String) {
            return JSON.parseArray((String) menusObj, Object.class);
        }
        return List.of();
    }

    /**
     * 获取公钥
     *
     * @return PublicKey
     */
    private PublicKey getPublicKey() {
        try {
            String publicKeyContent = jwtConfig.getPublicKey()
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s", "");
            
            byte[] keyBytes = Base64.getDecoder().decode(publicKeyContent);
            X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePublic(spec);
        } catch (Exception e) {
            log.error("获取公钥失败: {}", e.getMessage());
            throw new RuntimeException("公钥解析失败", e);
        }
    }
} 