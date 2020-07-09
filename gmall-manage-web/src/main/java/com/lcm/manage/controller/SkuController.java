package com.lcm.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.lcm.bean.PmsSkuInfo;
import com.lcm.service.SkuServicer;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@CrossOrigin
public class SkuController {

    @Reference
    SkuServicer skuServicer;

    @RequestMapping("es")
    @ResponseBody
    public String es() {

        List<PmsSkuInfo> sku = skuServicer.getAllSku();
        return "succer";
    }


    @RequestMapping("saveSkuInfo")
    @ResponseBody
    public String saveSkuInfo(@RequestBody PmsSkuInfo pmsSkuInfo) {

        pmsSkuInfo.setProductId(pmsSkuInfo.getSpuId());

        //图片默认处理
        String skuDefaultImg = pmsSkuInfo.getSkuDefaultImg();
        if (StringUtils.isBlank(skuDefaultImg)) {
            pmsSkuInfo.setSkuDefaultImg(pmsSkuInfo.getSkuImageList().get(0).getImgUrl());
        }

        skuServicer.saveSkuInfo(pmsSkuInfo);
        return "succer";
    }
}
