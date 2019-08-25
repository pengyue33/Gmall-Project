package com.atgg.gmall.list.service;

import com.alibaba.dubbo.config.annotation.Service;
import com.atgg.gmall.been.SkuLsInfo;
import com.atgg.gmall.been.SkuLsParams;
import com.atgg.gmall.been.SkuLsResult;
import com.atgg.gmall.service.ListService;
import io.searchbox.client.JestClient;
import io.searchbox.core.DocumentResult;

import io.searchbox.core.Index;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import io.searchbox.core.search.aggregation.TermsAggregation;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.TermsBuilder;
import org.elasticsearch.search.aggregations.metrics.percentiles.hdr.InternalHDRPercentileRanks;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;


import javax.swing.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class ListServiceImpl implements ListService {

      @Autowired
      JestClient jestClient;
        //es中index
    public static final String ES_INDEX="gmall";
       //es中的type
    public static final String ES_TYPE="SkuInfo";

    @Override
    public void saveSkuInfo(SkuLsInfo skuLsInfo) {
        // 保存数据
        Index index = new Index.Builder(skuLsInfo).index(ES_INDEX).type(ES_TYPE).id(skuLsInfo.getId()).build();
        try {
            DocumentResult documentResult = jestClient.execute(index);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
     //es条件查询方法
    @Override
    public SkuLsResult search(SkuLsParams skuLsParams) {
     String query =   makeQueryStringForSearch(skuLsParams);
     Search search = new Search.Builder(query).addIndex(ES_INDEX).addType(ES_TYPE).build();
        SearchResult searchResult=null;
        try {
            searchResult = jestClient.execute(search);

        } catch (IOException e) {
            e.printStackTrace();
        }
        SkuLsResult skuLsResult= makeResultForSearch(searchResult,skuLsParams);

        return skuLsResult;

    }
     //封装es查询结果
    private  SkuLsResult makeResultForSearch(SearchResult searchResult,SkuLsParams skuLsParams) {
          //封装查询结果
        SkuLsResult skuLsResult = new SkuLsResult();
         //skuLsInfo集合
        ArrayList<SkuLsInfo> skuLsInfos = new ArrayList<>();
        List<SearchResult.Hit<SkuLsInfo, Void>> hits = searchResult.getHits(SkuLsInfo.class);
        for (SearchResult.Hit<SkuLsInfo, Void> hit : hits) {
            SkuLsInfo skuLsInfo = hit.source;
                 //设置高亮
            if(hit.highlight!=null&&hit.highlight.size()>0){
                List<String> skuNameList = hit.highlight.get("skuName");
                String skuName = skuNameList.get(0);
                skuLsInfo.setSkuName(skuName);
            }
              //放入skuLsInfo集合
            skuLsInfos.add(skuLsInfo);
        }
          //skuLsInfo集合放入查询结果集中
        skuLsResult.setSkuLsInfoList(skuLsInfos);
            //设置总记录数
        Long total = searchResult.getTotal();
        skuLsResult.setTotal(total);
          //设置总页数
        int pageSize = skuLsParams.getPageSize();
        long totalPages = (total + pageSize - 1) / pageSize;
        skuLsResult.setTotalPages(totalPages);
        //集合来存储平台属性值Id
        List<String> attrValueIdList = new ArrayList<>();
               //聚合结果集
        TermsAggregation groupby_attr = searchResult.getAggregations().getTermsAggregation("groupby_attr");
        List<TermsAggregation.Entry> buckets = groupby_attr.getBuckets();
        for (TermsAggregation.Entry bucket : buckets) {
            String key = bucket.getKey();
            // 将valueId 放入集合中
            attrValueIdList.add(key);
        }
           //装置平台属性值Id
        skuLsResult.setAttrValueIdList(attrValueIdList);
        return skuLsResult;
    }

    //构造es的动态查询语句
    private String makeQueryStringForSearch(SkuLsParams skuLsParams) {
        // 创建查询器
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //bool
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        //skuName条件
        if(skuLsParams.getKeyword()!=null&& skuLsParams.getKeyword().length()>0){
            MatchQueryBuilder matchQueryBuilder = new MatchQueryBuilder("skuName", skuLsParams.getKeyword());
            boolQueryBuilder.must(matchQueryBuilder);
            //设置高亮
            HighlightBuilder highlighter = searchSourceBuilder.highlighter();
             //设置高亮属性
            highlighter.preTags("<span style=color:red>");
            highlighter.postTags("</span>");
            highlighter.field("skuName");
             //放入查询器中
            searchSourceBuilder.highlight(highlighter);
        }
          //catalog3Id条件 判断三级分类Id
        if(skuLsParams.getCatalog3Id()!=null &&skuLsParams.getCatalog3Id().length()>0){
            TermQueryBuilder catalog3Id = new TermQueryBuilder("catalog3Id", skuLsParams.getCatalog3Id());
            boolQueryBuilder.filter(catalog3Id);
        }
        // 平台属性值Id
        String[] valueIds = skuLsParams.getValueId();
        if(valueIds!=null&&valueIds.length>0){
            for (String valueId : valueIds) {
                TermQueryBuilder termQueryBuilder = new TermQueryBuilder("skuAttrValueList.valueId", valueId);
                boolQueryBuilder.filter(termQueryBuilder);
            }
        }
         //查询
        searchSourceBuilder.query(boolQueryBuilder);
         //分页条件
        int from = (skuLsParams.getPageNo() - 1) * skuLsParams.getPageSize();
        searchSourceBuilder.from(from);
        searchSourceBuilder.size(skuLsParams.getPageSize());
        //排序
        searchSourceBuilder.sort("hotScore", SortOrder.DESC);
        //聚合
        TermsBuilder groupby_attr = AggregationBuilders.terms("groupby_attr").field("skuAttrValueList.valueId");
        searchSourceBuilder.aggregation(groupby_attr);
        // 动态生成的dsl 语句！
        String  query= searchSourceBuilder.toString();
        System.err.println(query);
        return  query;

    }


}
