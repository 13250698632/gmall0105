package com.lcm.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.lcm.bean.PmsBaseAttrInfo;
import com.lcm.bean.PmsProductImage;
import com.lcm.bean.PmsProductInfo;
import com.lcm.bean.PmsProductSaleAttr;
import com.lcm.service.SpuService;
import com.lcm.util.PmsUploadUtil;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Controller
@CrossOrigin
public class SpuController {

    @Reference
    SpuService pmsProductInfoService;

    @RequestMapping("spuImageList")
    @ResponseBody
    public List<PmsProductImage> spuImageList(String spuId) {

        List<PmsProductImage> pmsProductImages = pmsProductInfoService.spuImageList(spuId);

        return pmsProductImages;
    }

    @RequestMapping("spuSaleAttrList")
    @ResponseBody
    public List<PmsProductSaleAttr> spuSaleAttrList(String spuId) {

        List<PmsProductSaleAttr> pmsProductSaleAttrs = pmsProductInfoService.spuSaleAttrList(spuId);

        return pmsProductSaleAttrs;
    }

    @RequestMapping("fileUpload")
    @ResponseBody
    public String fileUpload(@RequestParam("file") MultipartFile multipartFile) {

        String path = PmsUploadUtil.uploadImage(multipartFile);
        return path;
    }


    @RequestMapping("saveSpuInfo")
    @ResponseBody
    public String saveSpuInfo(@RequestBody PmsProductInfo PmsProductInfo) {

        PmsProductInfo.setProductName(PmsProductInfo.getSpuName());
        pmsProductInfoService.saveSpuInfo(PmsProductInfo);

        return "succer";
    }

    @RequestMapping("spuList")
    @ResponseBody
    public List<PmsProductInfo> spuList(String catalog3Id) {

        List<PmsProductInfo> pmsProductInfos = pmsProductInfoService.spuList(catalog3Id);
        return pmsProductInfos;
    }
}
