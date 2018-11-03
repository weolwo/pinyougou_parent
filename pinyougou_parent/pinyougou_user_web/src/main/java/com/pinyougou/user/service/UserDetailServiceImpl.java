package com.pinyougou.user.service;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.ArrayList;
import java.util.List;

/**
 * 扩展权限认证类
 * 这个类的主要作用是在登陆后得到用户名，可以根据用户名查询角色或执行一些逻辑。
 */
public class UserDetailServiceImpl implements UserDetailsService {
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        System.out.println("进入扩展权限认证类");
        //构建角色列表,传统项目可能数据库有专门的角色字段,需要取出案后遍历
        List<GrantedAuthority> authorities=new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        //这里的密码必须是空的，因为现在认证功能交级CAS实现了
        return new User(username,"",authorities);
    }
}
