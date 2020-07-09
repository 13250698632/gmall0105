package com.lcm.service;

import com.lcm.bean.PmsSearchParam;
import com.lcm.bean.PmsSearchSkuInfo;

import java.util.List;

public interface SearchService {
    List<PmsSearchSkuInfo> list(PmsSearchParam pmsSearchParam);
}
