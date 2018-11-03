package com.pinyougou.test;

import com.pinyougou.mapper.UserMapper;
import com.pinyougou.pojo.User;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author Steven
 * @version 1.0
 * @description com.pinyougou.test
 * @date 2018-8-24
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath*:spring/applicationContext-*.xml")
public class UserMapperTest {
    @Autowired
    private UserMapper userMapper;

    @Test
    public void testQueryById(){
        User user = userMapper.selectByPrimaryKey(24);
        System.out.println(user);
    }
}
