package com.lcm.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.lcm.bean.PmsBaseCatalog1;
import com.lcm.bean.PmsBaseCatalog2;
import com.lcm.bean.PmsBaseCatalog3;
import com.lcm.service.CatalogService;
import com.sun.org.apache.xml.internal.resolver.Catalog;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@CrossOrigin
public class CatalogController {

    @Reference
    CatalogService catalogService;

    @RequestMapping("getCatalog3")
    @ResponseBody
    public List<PmsBaseCatalog3> getCatalog3(String catalog2Id) {

        List<PmsBaseCatalog3> PmsBaseCatalog3List = catalogService.getCatalog3(catalog2Id);
        return PmsBaseCatalog3List;
    }

    @RequestMapping("getCatalog2")
    @ResponseBody
    public List<PmsBaseCatalog2> getCatalog2(String catalog1Id) {

        List<PmsBaseCatalog2> PmsBaseCatalog2List = catalogService.getCatalog2(catalog1Id);
        return PmsBaseCatalog2List;
    }

    @RequestMapping("getCatalog1")
    @ResponseBody
    public List<PmsBaseCatalog1> getCatalog1() {

        List<PmsBaseCatalog1> PmsBaseCatalog1List = catalogService.getCatalog1();
        return PmsBaseCatalog1List;
    }
}
