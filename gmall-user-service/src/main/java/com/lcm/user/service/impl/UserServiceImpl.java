package com.lcm.user.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.lcm.bean.UmsMember;
import com.lcm.service.UserService;
import com.lcm.user.mapper.UserMapper;
import com.lcm.util.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    UserMapper userMapper;

    @Autowired
    RedisUtil redisUtil;

    @Override
    public List<UmsMember> getAllUser() {
        List<UmsMember> umsMembers = userMapper.selectAll();
        return umsMembers;
    }

    @Override
    public UmsMember login(UmsMember umsMember) {

        Jedis jedis = null;
        try {
            jedis = redisUtil.getJedis();

            if (jedis != null) {

                String userInfo = jedis.get("user:" + umsMember.getUsername() + umsMember.getPassword() + ":info");

                if (StringUtils.isNotBlank(userInfo)) {

                    //密码正确
                    UmsMember umsMemberFromCache = JSON.parseObject(userInfo, UmsMember.class);
                    return umsMemberFromCache;
                }
            }
            //链接redis失败开启数据库
            UmsMember umsMemberFromDb = loginFromDb(umsMember);
            if (umsMemberFromDb != null) {
                jedis.setex("user:" + umsMember.getUsername() + umsMember.getPassword() + ":info", 60 * 60 * 24, JSON.toJSONString(umsMemberFromDb));
            }
            return umsMemberFromDb;

        } catch (Exception e) {
            return null;
        } finally {
            jedis.close();
        }
    }

    private UmsMember loginFromDb(UmsMember umsMember) {
        List<UmsMember> umsMembers = userMapper.select(umsMember);
        if(umsMembers != null){
            return umsMembers.get(0);
        }
        return null;
    }
}
