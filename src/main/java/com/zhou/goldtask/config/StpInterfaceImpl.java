package com.zhou.goldtask.config;

import cn.dev33.satoken.stp.StpInterface;
import com.zhou.goldtask.entity.UserEntity;
import com.zhou.goldtask.repository.UserRepository;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * 自定义权限加载接口实现类
 */
@Component    // 保证此类被 SpringBoot 扫描，完成 Sa-Token 的自定义权限验证扩展
public class StpInterfaceImpl implements StpInterface {
    @Resource
    private UserRepository userRepository;

    /**
     * 返回一个账号所拥有的权限码集合
     */
    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        return userRoles(loginId);
    }

    private List<String> userRoles(Object loginId) {
        UserEntity user = userRepository.findById(loginId.toString()).orElse(null);
        if (user == null) {
            return new ArrayList<>();
        }
        return user.getRoles();
    }

    /**
     * 返回一个账号所拥有的角色标识集合 (权限与角色可分开校验)
     */
    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        return userRoles(loginId);
    }
}