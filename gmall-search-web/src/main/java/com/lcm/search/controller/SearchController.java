package com.lcm.search.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.lcm.bean.*;
import com.lcm.service.AttrService;
import com.lcm.service.SearchService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;

@Controller
@CrossOrigin
public class SearchController {

    @Reference
    SearchService searchService;

    @Reference
    AttrService attrService;

    @RequestMapping("list.html")
    public String list(PmsSearchParam pmsSearchParam, ModelMap modelMap) {

        //调用搜索服务，返回搜索结果
        List<PmsSearchSkuInfo> pmsSearchSkuInfos = searchService.list(pmsSearchParam);
        modelMap.put("skuLsInfoList", pmsSearchSkuInfos);

        //抽取检索结果所包含的平台属性集合
        Set<String> set = new HashSet<String>();
        for (PmsSearchSkuInfo pmsSearchSkuInfo : pmsSearchSkuInfos) {
            for (PmsSkuAttrValue pmsSkuAttrValue : pmsSearchSkuInfo.getSkuAttrValueList()) {
                String valueId = pmsSkuAttrValue.getValueId();
                set.add(valueId);
            }
        }
        //根据valueid将属性列表查询出来
        List<PmsBaseAttrInfo> pmsBaseAttrInfos = attrService.getAttrValueListByValueId(set);
        modelMap.put("attrList", pmsBaseAttrInfos);

        //对平台属性集合进一步处理，去掉当前条件中valueId所在的属性组
        String[] delValueId = pmsSearchParam.getValueId();
        if (delValueId != null) {
            //面包屑
            List<PmsSearchCrumb> pmsSearchCrumbs = new ArrayList<>();
            for (String del : delValueId) {

                Iterator<PmsBaseAttrInfo> iterator = pmsBaseAttrInfos.iterator();

                PmsSearchCrumb pmsSearchCrumb = new PmsSearchCrumb();
                //生成面包屑的参数
                pmsSearchCrumb.setValueId(del);
                pmsSearchCrumb.setUrlParam(getUrlParamFromCrumb(pmsSearchParam, del));

                en:
                while (iterator.hasNext()) {
                    PmsBaseAttrInfo pmsBaseAttrInfo = iterator.next();
                    List<PmsBaseAttrValue> attrValueList = pmsBaseAttrInfo.getAttrValueList();
                    for (PmsBaseAttrValue pmsBaseAttrValue : attrValueList) {
                        String valueId = pmsBaseAttrValue.getId();
                        if (del.equals(valueId)) {
                            //查找面包屑的属性值名称
                            pmsSearchCrumb.setValueName(pmsBaseAttrValue.getValueName());
                            //删除该属性值所在的属性值
                            iterator.remove();
                            //删除面包屑后直接跳出多层循环
                            break en;
                        }
                    }
                }
                pmsSearchCrumbs.add(pmsSearchCrumb);
            }

            modelMap.put("attrValueSelectedList", pmsSearchCrumbs);
        }

        String urlParam = getUrlParam(pmsSearchParam);
        modelMap.put("urlParam", urlParam);

        String keyword = pmsSearchParam.getKeyword();
        if (StringUtils.isNotBlank(keyword)) {
            modelMap.put("keyword", keyword);
        }

        return "list";
    }

    private String getUrlParamFromCrumb(PmsSearchParam pmsSearchParam, String delValueId) {

        String keyword = pmsSearchParam.getKeyword();
        String catalog3Id = pmsSearchParam.getCatalog3Id();
        String[] skuAttrValueList = pmsSearchParam.getValueId();

        String urlParam = "";
        //搜索内容
        if (StringUtils.isNotBlank(keyword)) {
            if (StringUtils.isNotBlank(urlParam)) {
                urlParam += "&keyword=" + keyword;
            } else {
                urlParam += "keyword=" + keyword;
            }

        }
        //三级分类ID
        if (StringUtils.isNotBlank(catalog3Id)) {
            if (StringUtils.isNotBlank(urlParam)) {
                urlParam += "&catalog3Id=" + catalog3Id;
            } else {
                urlParam += "catalog3Id=" + catalog3Id;
            }

        }
        //属性值ID
        if (skuAttrValueList != null) {
            for (String pmsSkuAttrValue : skuAttrValueList) {
                String valueId = pmsSkuAttrValue;
                if (!valueId.equals(delValueId))
                    urlParam += "&valueId=" + valueId;
            }
        }

        return urlParam;
    }

    private String getUrlParam(PmsSearchParam pmsSearchParam) {

        String keyword = pmsSearchParam.getKeyword();
        String catalog3Id = pmsSearchParam.getCatalog3Id();
        String[] skuAttrValueList = pmsSearchParam.getValueId();

        String urlParam = "";
        //搜索内容
        if (StringUtils.isNotBlank(keyword)) {
            if (StringUtils.isNotBlank(urlParam)) {
                urlParam += "&keyword=" + keyword;
            } else {
                urlParam += "keyword=" + keyword;
            }

        }
        //三级分类ID
        if (StringUtils.isNotBlank(catalog3Id)) {
            if (StringUtils.isNotBlank(urlParam)) {
                urlParam += "&catalog3Id=" + catalog3Id;
            } else {
                urlParam += "catalog3Id=" + catalog3Id;
            }

        }
        //属性值ID
        if (skuAttrValueList != null) {
            for (String pmsSkuAttrValue : skuAttrValueList) {
                String valueId = pmsSkuAttrValue;
                urlParam += "&valueId=" + valueId;
            }
        }

        return urlParam;
    }

    @RequestMapping("index")
    public String index() {
        return "index";
    }
}
