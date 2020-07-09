package com.lcm.search;

import com.alibaba.dubbo.config.annotation.Reference;
import com.lcm.bean.PmsSearchSkuInfo;
import com.lcm.bean.PmsSkuInfo;
import com.lcm.service.SkuServicer;
import io.searchbox.client.JestClient;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest
class GmallSearchServiceApplicationTests {

    @Test
    void contextLoads() {


    }

}
