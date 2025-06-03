package com.leyue.smartcs.config.context;

import com.alibaba.ttl.TransmittableThreadLocal;
import lombok.Data;

import java.security.Principal;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户上下文
 * 用于在线程中存储当前用户信息
 */
@Data
public class UserContext {
    private static final TransmittableThreadLocal<UserInfo> userHolder = new TransmittableThreadLocal<>();

    /**
     * 设置当前用户信息
     *
     * @param userInfo 用户信息
     */
    public static void setCurrentUser(UserInfo userInfo) {
        userHolder.set(userInfo);
    }

    /**
     * 获取当前用户信息
     *
     * @return 用户信息
     */
    public static UserInfo getCurrentUser() {
        return userHolder.get();
    }

    /**
     * 清理当前用户信息
     * 防止内存泄漏，在请求结束时调用
     */
    public static void clear() {
        userHolder.remove();
    }

    /**
     * 检查当前用户是否有指定权限
     *
     * @param permission 权限标识
     * @return 是否有权限
     * @deprecated 使用 {@link #hasUrlAccess(String)} 代替
     */
    @Deprecated
    public static boolean hasPermission(String permission) {
        UserInfo userInfo = getCurrentUser();
        if (userInfo == null || userInfo.getPermissions() == null) {
            return false;
        }
        return userInfo.getPermissions().contains(permission);
    }

    /**
     * 检查当前用户是否拥有指定角色
     *
     * @param role 角色标识
     * @return 是否有角色
     */
    public static boolean hasRole(String role) {
        UserInfo userInfo = getCurrentUser();
        if (userInfo == null || userInfo.getRoles() == null) {
            return false;
        }
        return userInfo.getRoles().stream().anyMatch(r -> r.getRoleCode().equals(role));
    }

    /**
     * 检查当前用户是否有权访问指定URL
     * 通过判断用户的菜单路径中是否包含要访问的URL
     *
     * @param url 要访问的URL
     * @return 是否有访问权限
     */
    public static boolean hasUrlAccess(String url) {
        UserInfo userInfo = getCurrentUser();
        if (userInfo == null || userInfo.getRoles() == null) {
            return false;
        }

        // 如果是超级管理员，直接返回true
        if (userInfo.isAdmin()) {
            return true;
        }

        // 遍历所有角色下的菜单，检查是否包含要访问的URL
        for (Role role : userInfo.getRoles()) {
            if (role.getMenus() != null) {
                for (Menu menu : role.getMenus()) {
                    if (url.startsWith(menu.getPath())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * 用户信息类
     */
    @Data
    public static class UserInfo implements Principal {
        /**
         * 用户ID
         */
        private Long id;

        /**
         * 用户名
         */
        private String username;

        /**
         * 手机号
         */
        private String mobile;

        /**
         * 昵称
         */
        private String nickname;

        /**
         * 用户角色列表
         */
        private List<Role> roles;

        /**
         * 用户权限列表
         */
        private List<String> permissions;

        /**
         * 是否为管理员
         */
        private boolean admin;

        public boolean isAdmin() {
            if (roles == null) {
                return false;
            }
            admin = roles.stream().anyMatch(role -> "ROLE_ADMIN".equals(role.getRoleCode()));
            return admin;
        }


        public List<String> getPermissions() {
            if (roles == null) {
                return Collections.emptyList();
            }
            return roles.stream().filter(role -> role.getMenus() != null)
                    .flatMap(role -> role.getMenus().stream())
                    .map(Menu::getPath).collect(Collectors.toList());
        }

        @Override
        public String getName() {
            return username;
        }
    }

    /**
     * 角色信息
     */
    @Data
    public static class Role {
        /**
         * 角色ID
         */
        private Long id;

        /**
         * 角色名称
         */
        private String roleName;

        /**
         * 角色编码
         */
        private String roleCode;

        /**
         * 角色下的菜单列表
         */
        private List<Menu> menus;
    }

    /**
     * 菜单信息
     */
    @Data
    public static class Menu {
        /**
         * 菜单ID
         */
        private Long id;

        /**
         * 菜单名称
         */
        private String menuName;

        /**
         * 菜单路径
         */
        private String path;

        /**
         * 菜单图标
         */
        private String icon;

        /**
         * 父菜单ID
         */
        private Long parentId;

        /**
         * 子菜单
         */
        private List<Menu> children;
    }
}