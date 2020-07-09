package com.lcm.itme.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.lcm.bean.PmsProductSaleAttr;
import com.lcm.bean.PmsSkuAttrValue;
import com.lcm.bean.PmsSkuInfo;
import com.lcm.bean.PmsSkuSaleAttrValue;
import com.lcm.service.SkuServicer;
import com.lcm.service.SpuService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class ItmeControlle {

    @Reference
    SkuServicer skuServicer;

    @Reference
    SpuService spuService;

    @RequestMapping("{skuId}.html")
    public String item(@PathVariable String skuId, ModelMap modelMap) {

        PmsSkuInfo pmsSkuInfo = skuServicer.getSkuById(skuId);
        modelMap.put("skuInfo", pmsSkuInfo);

        //查询颜色及版本
        List<PmsProductSaleAttr> pmsProductSaleAttrs = spuService.spuSaleAttrListCheckBySku(pmsSkuInfo.getProductId(), pmsSkuInfo.getId());
        modelMap.put("spuSaleAttrListCheckBySku", pmsProductSaleAttrs);

        //查询当前sku的spu的其他sku的集合的hash表
        Map<String, String> skuSaleAttrHash = new HashMap<>();
        List<PmsSkuInfo> pmsSkuInfos = skuServicer.getSkuSaleAttrValueListBySpu(pmsSkuInfo.getProductId());

        for (PmsSkuInfo skuInfo : pmsSkuInfos) {
            String k = "";
            String v = skuInfo.getId();

            List<PmsSkuSaleAttrValue> skuSaleAttrValueList = skuInfo.getSkuSaleAttrValueList();

            for (PmsSkuSaleAttrValue pmsSkuSaleAttrValue : skuSaleAttrValueList) {
                k += pmsSkuSaleAttrValue.getSaleAttrValueId() + "|";
            }

            skuSaleAttrHash.put(k, v);
        }

        //将sku的销售属性hash表放到页面
        String skuSaleAttrHashJsonString = JSON.toJSONString(skuSaleAttrHash);
        modelMap.put("skuSaleAttrHashJsonString", skuSaleAttrHashJsonString);
        return "item";
    }

}
