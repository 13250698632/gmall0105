package com.lcm.manage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.lcm.bean.PmsSkuAttrValue;
import com.lcm.bean.PmsSkuImage;
import com.lcm.bean.PmsSkuInfo;
import com.lcm.bean.PmsSkuSaleAttrValue;
import com.lcm.manage.mapper.PmsSkuAttrValueMapper;
import com.lcm.manage.mapper.PmsSkuImageMapper;
import com.lcm.manage.mapper.PmsSkuInfoMapper;
import com.lcm.manage.mapper.PmsSkuSaleAttrValueMapper;
import com.lcm.service.SkuServicer;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

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
}
