package com.lcm.search;

import com.alibaba.dubbo.config.annotation.Reference;
import com.lcm.bean.PmsSearchSkuInfo;
import com.lcm.bean.PmsSkuInfo;
import com.lcm.service.SkuServicer;
import io.searchbox.client.JestClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@SpringBootApplication
public class GmallSearchServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(GmallSearchServiceApplication.class, args);
    }
}
