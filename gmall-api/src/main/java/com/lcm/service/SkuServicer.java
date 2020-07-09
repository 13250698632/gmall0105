package com.lcm.service;

import com.lcm.bean.PmsSkuInfo;

import java.util.List;

public interface SkuServicer {
    void saveSkuInfo(PmsSkuInfo pmsSkuInfo);

    PmsSkuInfo getSkuById(String skuId);

    List<PmsSkuInfo> getSkuSaleAttrValueListBySpu(String productId);

    List<PmsSkuInfo> getAllSku();
}
