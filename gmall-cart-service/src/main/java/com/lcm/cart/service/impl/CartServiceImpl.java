package com.lcm.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.lcm.bean.OmsCartItem;
import com.lcm.cart.mapper.OmsCartItemMapper;
import com.lcm.service.CartService;
import com.lcm.util.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import javax.sound.sampled.Control;
import java.lang.reflect.Executable;
import java.util.*;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    OmsCartItemMapper omsCartItemMapper;
    
    @Autowired
    RedisUtil redisUtil;

    @Override
    public OmsCartItem ifCartExistByUser(String memberId, String skuId) {

        OmsCartItem omsCartItem = new OmsCartItem();
        omsCartItem.setMemberId(memberId);
        omsCartItem.setProductSkuId(skuId);
        OmsCartItem omsCartItem1 = omsCartItemMapper.selectOne(omsCartItem);

        return omsCartItem1;
    }

    @Override
    public void addCart(OmsCartItem omsCartItem) {

        if(StringUtils.isNotBlank(omsCartItem.getMemberId())){
            omsCartItemMapper.insertSelective(omsCartItem);
        }
    }

    @Override
    public void updateCart(OmsCartItem omsCartFromDb) {

        Example e = new Example(OmsCartItem.class);
        e.createCriteria().andEqualTo("id",omsCartFromDb.getId());

        //有的字段才更新，没有的字段不更新
        omsCartItemMapper.updateByExampleSelective(omsCartFromDb,e);
    }

    @Override
    public void flushCartCache(String memberId) {

        OmsCartItem omsCartItem = new OmsCartItem();
        omsCartItem.setMemberId(memberId);
        List<OmsCartItem> omsCartItems = omsCartItemMapper.select(omsCartItem);

        //同步redis缓存
        Jedis jedis = redisUtil.getJedis();

        //将集合转换成map
        Map<String,String> map = new HashMap<String ,String>();
        for (OmsCartItem cartItem : omsCartItems) {
            cartItem.setTotalPrice(cartItem.getPrice().multiply(cartItem.getQuantity()));
            map.put(cartItem.getProductSkuId(), JSON.toJSONString(cartItem));
        }

        jedis.del("user:"+memberId+":cart");
        jedis.hmset("user:"+memberId+":cart",map);

        jedis.close();
    }

    @Override
    public List<OmsCartItem> cartList(String userId) {

        List<OmsCartItem> omsCartItemList = new ArrayList<>();
        Jedis jedis = redisUtil.getJedis();
        try {
            //获取键值队，redis的hash缓存值
            List<String> hvals = jedis.hvals("user:" + userId + ":cart");
            for (String hval : hvals) {
                OmsCartItem omsCartItem = JSON.parseObject(hval, OmsCartItem.class);
                omsCartItemList.add(omsCartItem);
            }
        }catch (Exception e){
            //处理异常
            return null;
        }finally {
            jedis.close();
        }
        return omsCartItemList;
    }

    @Override
    public void checkCart(OmsCartItem omsCartItem) {

        Example e = new Example(OmsCartItem.class);
        e.createCriteria().andEqualTo("memberId",omsCartItem.getMemberId()).andEqualTo("productSkuId",omsCartItem.getProductSkuId());

        omsCartItemMapper.updateByExampleSelective(omsCartItem,e);

        //缓存同步
        flushCartCache(omsCartItem.getMemberId());
    }
}
