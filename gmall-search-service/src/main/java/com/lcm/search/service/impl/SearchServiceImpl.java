package com.lcm.search.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.lcm.bean.PmsSearchParam;
import com.lcm.bean.PmsSearchSkuInfo;
import com.lcm.bean.PmsSkuAttrValue;
import com.lcm.service.SearchService;
import io.searchbox.client.JestClient;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.highlight.HighlightBuilder;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class SearchServiceImpl implements SearchService {

    @Autowired
    JestClient jestClient;

    @Override
    public List<PmsSearchSkuInfo> list(PmsSearchParam pmsSearchParam) {

        String SearchDsl = getSearchDsl(pmsSearchParam);

        List<PmsSearchSkuInfo> searchSkuInfos = new ArrayList<>();

        Search build = new Search.Builder(SearchDsl).addIndex("gmall0105").addType("PmsSkuInfo").build();
        try {
            SearchResult execute = jestClient.execute(build);
            List<SearchResult.Hit<PmsSearchSkuInfo, Void>> hits = execute.getHits(PmsSearchSkuInfo.class);
            for (SearchResult.Hit<PmsSearchSkuInfo, Void> hit : hits) {
                PmsSearchSkuInfo source = hit.source;

                //高亮字符串
                Map<String, List<String>> highlight = hit.highlight;
                if (highlight != null) {
                    String skuName = highlight.get("skuName").get(0);
                    source.setSkuName(skuName);
                }

                searchSkuInfos.add(source);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return searchSkuInfos;
    }

    private String getSearchDsl(PmsSearchParam pmsSearchParam) {

        String[] skuAttrValueList = pmsSearchParam.getValueId();
        //jest的dsl工具
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //bool
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();

        //filter
        if (StringUtils.isNotBlank(pmsSearchParam.getCatalog3Id())) {
            TermQueryBuilder termQueryBuilder = new TermQueryBuilder("catalog3Id", pmsSearchParam.getCatalog3Id());
            boolQueryBuilder.filter(termQueryBuilder);
        }
        if (skuAttrValueList != null) {
            for (String pmsSkuAttrValue : skuAttrValueList) {
                TermQueryBuilder termQueryBuilder = new TermQueryBuilder("skuAttrValueList.valueId", pmsSkuAttrValue);
                boolQueryBuilder.filter(termQueryBuilder);
            }
        }
        //must
        if (StringUtils.isNotBlank(pmsSearchParam.getKeyword())) {
            MatchQueryBuilder matchQueryBuilder = new MatchQueryBuilder("skuName", pmsSearchParam.getKeyword());
            boolQueryBuilder.must(matchQueryBuilder);
        }
        //query
        searchSourceBuilder.query(boolQueryBuilder);

        //highlight
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        //本身是“<em>”,设置自己样式
        highlightBuilder.preTags("<span style='color:red'>");
        highlightBuilder.field("skuName");
        highlightBuilder.postTags("</span>");
        searchSourceBuilder.highlight(highlightBuilder);

        //from
        searchSourceBuilder.from(0);
        searchSourceBuilder.size(20);

        return searchSourceBuilder.toString();
    }
}
