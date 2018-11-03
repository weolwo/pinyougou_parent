package com.pinyougou.user.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.abel533.entity.Example;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.pinyougou.mapper.TbUserMapper;
import com.pinyougou.pojo.TbUser;
import com.pinyougou.user.service.UserService;
import entity.PageResult;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

import javax.jms.*;
import java.util.*;

/**
 * 业务逻辑实现
 *
 * @author Steven
 */
@Service(timeout = 10000)
public class UserServiceImpl implements UserService {

    @Autowired
    private TbUserMapper userMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private Destination smsDestination;

    @Autowired
    private JmsTemplate jmsTemplate;

    @Value("${template_code}")
    private String template_code;



    /**
     * 查询全部
     */
    @Override
    public List<TbUser> findAll() {
        return userMapper.select(null);
    }

    /**
     * 按分页查询
     */
    @Override
    public PageResult findPage(int pageNum, int pageSize) {

        PageResult<TbUser> result = new PageResult<TbUser>();
        //设置分页条件
        PageHelper.startPage(pageNum, pageSize);

        //查询数据
        List<TbUser> list = userMapper.select(null);
        //保存数据列表
        result.setRows(list);

        //获取总记录数
        PageInfo<TbUser> info = new PageInfo<TbUser>(list);
        result.setTotal(info.getTotal());
        return result;
    }

    /**
     * 增加,用户注册
     */
    @Override
    public void add(TbUser user) {
        user.setUpdated(new Date());//更新时间
        user.setCreated(new Date());//注册时间
        user.setSourceType("1");//注册来源,1:pc
        user.setPassword(DigestUtils.md5Hex(user.getPassword()));//对密码加密
        userMapper.insertSelective(user);
    }


    /**
     * 修改
     */
    @Override
    public void update(TbUser user) {
        userMapper.updateByPrimaryKeySelective(user);
    }

    /**
     * 根据ID获取实体
     *
     * @param id
     * @return
     */
    @Override
    public TbUser findOne(Long id) {
        return userMapper.selectByPrimaryKey(id);
    }

    /**
     * 批量删除
     */
    @Override
    public void delete(Long[] ids) {
        //数组转list
        List longs = Arrays.asList(ids);
        //构建查询条件
        Example example = new Example(TbUser.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andIn("id", longs);

        //跟据查询条件删除数据
        userMapper.deleteByExample(example);
    }


    @Override
    public PageResult findPage(TbUser user, int pageNum, int pageSize) {
        PageResult<TbUser> result = new PageResult<TbUser>();
        //设置分页条件
        PageHelper.startPage(pageNum, pageSize);

        //构建查询条件
        Example example = new Example(TbUser.class);
        Example.Criteria criteria = example.createCriteria();

        if (user != null) {
            //如果字段不为空
            if (user.getUsername() != null && user.getUsername().length() > 0) {
                criteria.andLike("username", "%" + user.getUsername() + "%");
            }
            //如果字段不为空
            if (user.getPassword() != null && user.getPassword().length() > 0) {
                criteria.andLike("password", "%" + user.getPassword() + "%");
            }
            //如果字段不为空
            if (user.getPhone() != null && user.getPhone().length() > 0) {
                criteria.andLike("phone", "%" + user.getPhone() + "%");
            }
            //如果字段不为空
            if (user.getEmail() != null && user.getEmail().length() > 0) {
                criteria.andLike("email", "%" + user.getEmail() + "%");
            }
            //如果字段不为空
            if (user.getSourceType() != null && user.getSourceType().length() > 0) {
                criteria.andLike("sourceType", "%" + user.getSourceType() + "%");
            }
            //如果字段不为空
            if (user.getNickName() != null && user.getNickName().length() > 0) {
                criteria.andLike("nickName", "%" + user.getNickName() + "%");
            }
            //如果字段不为空
            if (user.getName() != null && user.getName().length() > 0) {
                criteria.andLike("name", "%" + user.getName() + "%");
            }
            //如果字段不为空
            if (user.getStatus() != null && user.getStatus().length() > 0) {
                criteria.andLike("status", "%" + user.getStatus() + "%");
            }
            //如果字段不为空
            if (user.getHeadPic() != null && user.getHeadPic().length() > 0) {
                criteria.andLike("headPic", "%" + user.getHeadPic() + "%");
            }
            //如果字段不为空
            if (user.getQq() != null && user.getQq().length() > 0) {
                criteria.andLike("qq", "%" + user.getQq() + "%");
            }
            //如果字段不为空
            if (user.getIsMobileCheck() != null && user.getIsMobileCheck().length() > 0) {
                criteria.andLike("isMobileCheck", "%" + user.getIsMobileCheck() + "%");
            }
            //如果字段不为空
            if (user.getIsEmailCheck() != null && user.getIsEmailCheck().length() > 0) {
                criteria.andLike("isEmailCheck", "%" + user.getIsEmailCheck() + "%");
            }
            //如果字段不为空
            if (user.getSex() != null && user.getSex().length() > 0) {
                criteria.andLike("sex", "%" + user.getSex() + "%");
            }

        }

        //查询数据
        List<TbUser> list = userMapper.selectByExample(example);
        //保存数据列表
        result.setRows(list);

        //获取总记录数
        PageInfo<TbUser> info = new PageInfo<TbUser>(list);
        result.setTotal(info.getTotal());

        return result;
    }

    @Override
    public void createSmsCode(final String phone) {
        //生成验证码
        final String vCode = (long) (Math.random() * 1000000) + "";
        System.out.println(vCode);
        redisTemplate.boundHashOps("smsVCode").put(phone, vCode);
        //将生成的验证码发送到activemq
        jmsTemplate.send(smsDestination, new MessageCreator() {
            @Override
            public Message createMessage(Session session) throws JMSException {
                MapMessage mapMessage = session.createMapMessage();
                mapMessage.setString("mobile", phone);//手机号
                mapMessage.setString("template_code", template_code);//模板编号
                mapMessage.setString("sign_name", "小七猫");//签名
                Map map = new HashMap();
                map.put("number", vCode);
                mapMessage.setString("param", JSON.toJSONString(map));
                return mapMessage;
            }
        });
    }

    @Override
    public boolean checkSmsCode(String phone, String smsCode) {
        System.out.println("用户输入验证码:"+smsCode);
        if (StringUtils.isNotBlank(phone) && StringUtils.isNotBlank(smsCode)) {
            //取出redis中存储的系统生成的验证码
            String systemVCode = (String) redisTemplate.boundHashOps("smsVCode").get(phone);
            System.out.println("系统验证码:"+systemVCode);
            if (StringUtils.isNotBlank(systemVCode)) {

                if (!smsCode.equals(systemVCode)) {
                    return false;
                }else {
                    return true;
                }
            }
        }
        return false;
    }

}
