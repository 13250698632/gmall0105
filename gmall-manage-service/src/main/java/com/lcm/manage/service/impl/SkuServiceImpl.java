package com.lcm.manage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.lcm.bean.*;
import com.lcm.manage.mapper.PmsSkuAttrValueMapper;
import com.lcm.manage.mapper.PmsSkuImageMapper;
import com.lcm.manage.mapper.PmsSkuInfoMapper;
import com.lcm.manage.mapper.PmsSkuSaleAttrValueMapper;
import com.lcm.service.SkuServicer;
import com.lcm.util.RedisUtil;
import io.searchbox.client.JestClient;
import io.searchbox.core.Index;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.queryparser.xml.builders.FilteredQueryBuilder;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
public class SkuServiceImpl implements SkuServicer {

    @Autowired
    PmsSkuInfoMapper pmsSkuInfoMapper;

    @Autowired
    PmsSkuAttrValueMapper pmsSkuAttrValueMapper;

    @Autowired
    PmsSkuSaleAttrValueMapper pmsSkuSaleAttrValueMapper;

    @Autowired
    PmsSkuImageMapper pmsSkuImageMapper;

    @Autowired
    RedisUtil redisUtil;

    @Autowired
    JestClient jestClient;

    @Override
    public void saveSkuInfo(PmsSkuInfo pmsSkuInfo) {

        //插入skuinfo
        int insert = pmsSkuInfoMapper.insert(pmsSkuInfo);

        //插入平台属性关联
        List<PmsSkuAttrValue> skuAttrValueList = pmsSkuInfo.getSkuAttrValueList();
        for (PmsSkuAttrValue pmsSkuAttrValue : skuAttrValueList) {
            pmsSkuAttrValue.setSkuId(pmsSkuInfo.getId());
            //插入非空的值
            pmsSkuAttrValueMapper.insertSelective(pmsSkuAttrValue);
        }

        //插入销售属性关联
        List<PmsSkuSaleAttrValue> skuSaleAttrValueList = pmsSkuInfo.getSkuSaleAttrValueList();
        for (PmsSkuSaleAttrValue pmsSkuSaleAttrValue : skuSaleAttrValueList) {
            pmsSkuSaleAttrValue.setSkuId(pmsSkuInfo.getId());
            //插入非空的值
            pmsSkuSaleAttrValueMapper.insertSelective(pmsSkuSaleAttrValue);
        }

        //插入图片信息
        List<PmsSkuImage> skuImageList = pmsSkuInfo.getSkuImageList();
        for (PmsSkuImage pmsSkuImage : skuImageList) {
            pmsSkuImage.setSkuId(pmsSkuInfo.getId());
            //插入非空的值
            pmsSkuImageMapper.insertSelective(pmsSkuImage);
        }

    }


    private PmsSkuInfo getSkuBuIdFromDB(String skuId) {
        PmsSkuInfo pmsSkuInfo = new PmsSkuInfo();
        pmsSkuInfo.setId(skuId);
        PmsSkuInfo pmsSkuInfos = pmsSkuInfoMapper.selectOne(pmsSkuInfo);

        //查询sku图片
        PmsSkuImage pmsSkuImage = new PmsSkuImage();
        pmsSkuImage.setSkuId(skuId);
        pmsSkuInfos.setSkuImageList(pmsSkuImageMapper.select(pmsSkuImage));
        return pmsSkuInfos;
    }

    @Override
    public PmsSkuInfo getSkuById(String skuId) {

        PmsSkuInfo pmsSkuInfo = new PmsSkuInfo();

        //链接缓存
        Jedis jedis = redisUtil.getJedis();
        //查询缓存
        String skuKey = "sku:" + skuId + ":info";
        String skuJson = jedis.get("skuKey");
        if (StringUtils.isNotBlank(skuJson)) {
            pmsSkuInfo = JSON.parseObject(skuJson, PmsSkuInfo.class);
        } else {

            //如果缓存中没有，查询mysql

            //设置分布式锁
            String token = UUID.randomUUID().toString();
            String OK = jedis.set("sku:" + skuId + ":lock", token, "nx", "px", 1000 * 10);
            if (StringUtils.isNotBlank(OK) && OK.equals("OK")) {
                //设置成功，有权在10秒的过期时间内访问数据库
                pmsSkuInfo = getSkuBuIdFromDB(skuId);
                if (pmsSkuInfo != null) {
                    //mysql查询结果存入redis
                    jedis.set("sku:" + skuId + ":info", JSON.toJSONString(pmsSkuInfo));
                } else {
                    //数据库不存在该sku
                    //为了防止缓存穿透，null值或者空字符串设置给redis,并且把空字符串设置3分钟
                    jedis.setex("sku:" + skuId + ":info", 60 * 3, JSON.toJSONString(""));
                }

                //在访问mysql后，将mysql的分布式锁释放
                String lockToken = jedis.get("sku:" + skuId + ":lock");
                if (StringUtils.isNotBlank(lockToken) && lockToken.equals(token)) {
                    jedis.del("sku:" + skuId + ":lock");//用token确认删除的是自己的锁
                }
                //如果碰巧在查询redis锁还没删除的时候，正在网络传输时，锁过期了
                //怎么办？
                //String script ="if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
                //jedis.eval(script, Collections.singletonList("lock"),Collections.singletonList(token));

            } else {
                //设置失败,递归(该线程在睡眠几秒后，重新访问本方法)
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return getSkuById(skuId);
            }


        }

        jedis.close();

        return pmsSkuInfo;
    }

    @Override
    public List<PmsSkuInfo> getSkuSaleAttrValueListBySpu(String productId) {

        List<PmsSkuInfo> pmsSkuInfos = pmsSkuInfoMapper.selectSkuSaleAttrValueListBySpu(productId);
        return pmsSkuInfos;
    }

    @Override
    public List<PmsSkuInfo> getAllSku() {
        List<PmsSkuInfo> pmsSkuInfos = pmsSkuInfoMapper.selectAll();

//        for (PmsSkuInfo pmsSkuInfo : pmsSkuInfos) {
//
//            PmsSkuAttrValue pmsSkuAttrValue = new PmsSkuAttrValue();
//            pmsSkuAttrValue.setSkuId(pmsSkuInfo.getId());
//
//            List<PmsSkuAttrValue> pmsSkuAttrValues = pmsSkuAttrValueMapper.select(pmsSkuAttrValue);
//            pmsSkuInfo.setSkuAttrValueList(pmsSkuAttrValues);
//
//        }
//
//


        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();

        //filter
        TermQueryBuilder termQueryBuilder = new TermQueryBuilder("skuAttrValueList.valueId", "41");
        boolQueryBuilder.filter(termQueryBuilder);
        //must
        MatchQueryBuilder matchQueryBuilder = new MatchQueryBuilder("skuName", "华为");
        boolQueryBuilder.must(matchQueryBuilder);

        searchSourceBuilder.query(boolQueryBuilder);

        List<PmsSearchSkuInfo> searchSkuInfos = new ArrayList<>();

        Search build = new Search.Builder(searchSourceBuilder.toString()).addIndex("gmall0105").addType("PmsSkuInfo").build();
        try {
            SearchResult execute = jestClient.execute(build);
            List<SearchResult.Hit<PmsSearchSkuInfo, Void>> hits = execute.getHits(PmsSearchSkuInfo.class);
            for (SearchResult.Hit<PmsSearchSkuInfo, Void> hit : hits) {
                PmsSearchSkuInfo source = hit.source;
                searchSkuInfos.add(source);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        return pmsSkuInfos;

    }

    public void test(List<PmsSkuInfo> pmsSkuInfos) {
        try {

            List<PmsSearchSkuInfo> pmsSearchSkuInfos = new ArrayList<>();
            for (PmsSkuInfo pmsSkuInfo : pmsSkuInfos) {
                PmsSearchSkuInfo PmsSearchSkuInfo = new PmsSearchSkuInfo();
                BeanUtils.copyProperties(pmsSkuInfo, PmsSearchSkuInfo);
                pmsSearchSkuInfos.add(PmsSearchSkuInfo);
            }

            for (PmsSearchSkuInfo pmsSearchSkuInfo : pmsSearchSkuInfos) {

                jestClient.execute(new Index.Builder(pmsSearchSkuInfo).index("gmall0105").type("PmsSkuInfo").id(pmsSearchSkuInfo.getId()).build());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
