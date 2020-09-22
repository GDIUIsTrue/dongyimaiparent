package com.search.serviceImpl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.dongyimai.pojo.TbItem;
import com.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.*;

import javax.crypto.Cipher;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service(timeout = 3000)
public class ItemSearchServiceImpl implements ItemSearchService {
    @Autowired
    private SolrTemplate solrTemplate;
    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public Map search(Map searchMap) {
        //关键字空格处理
        String keywords = (String)searchMap.get("keywords");
        searchMap.put("keywords",keywords.replace(" ",""));

        Map map = new HashMap();
        //1.1按关键字查询（高亮显示）
        map.putAll(searchList(searchMap));

        //2.根据关键字查询商品分类
        List categoryList = searchCategoryList(searchMap);
        map.put("categoryList",categoryList);

        //假若用户选择了分类 那么按照用户选择的分类进行对redis的品牌和规格进行查询
        //如果没有选择分类 则按照分类的第一个排序分类 进行对品牌和规格的查询
        if (!"".equals(searchMap.get("category"))){
            map.putAll(searchBrandAndSpecList(searchMap.get("category")+""));
        }else {
            //取查询商品的 第一个分类 所对应的品牌与规格
            if (categoryList.size()>0){
                map.putAll(searchBrandAndSpecList(categoryList.get(0)+""));
            }
        }

        return map;
    }

    /**设置搜索关键字高亮显示
     * @param searchMap
     * @return
     */
    private Map searchList(Map searchMap){
        Map map = new HashMap();
        //1、创建一个支持高亮查询器对象
        SimpleHighlightQuery query = new SimpleHighlightQuery();
        //2、设定需要高亮处理字段
        HighlightOptions highlightOptions = new HighlightOptions();
        highlightOptions.addField("item_title");
        //3、设置高亮前缀
        highlightOptions.setSimplePrefix("<em style='color:red'>");
        //4、设置高亮后缀
        highlightOptions.setSimplePostfix("</em>");
        //5、关联高亮选项到高亮查询器对象
        query.setHighlightOptions(highlightOptions);
        //6、设定查询条件

        //6.1创建关键字查询条件
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        query.addCriteria(criteria);

        //6.2按分类筛选
        if(!"".equals(searchMap.get("category"))){
            Criteria criteria1 = new Criteria("item_category").is(searchMap.get("category"));
            FilterQuery filterQuery = new SimpleFilterQuery(criteria1);
            query.addFilterQuery(filterQuery);
        }

        //6.3按品牌筛选
        if(!"".equals(searchMap.get("brand"))){
            Criteria criteria1 = new Criteria("item_brand").is(searchMap.get("brand"));
            FilterQuery filterQuery = new SimpleFilterQuery(criteria1);
            query.addFilterQuery(filterQuery);
        }

        //6.4按规格筛选，循环过滤规格
        if (searchMap.get("spec")!=null){
            Map<String,String> specMap = (Map)searchMap.get("spec");
            for(Map.Entry<String,String> entry : specMap.entrySet()){
                Criteria criteria1 = new Criteria("item_spec_"+entry.getKey()).is(entry.getValue());
                FilterQuery filterQuery = new SimpleFilterQuery(criteria1);
                query.addFilterQuery(filterQuery);
            }
        }

        //6.5按价格筛选
        if(!"".equals(searchMap.get("price"))){
            String[] price = ((String)searchMap.get("price")).split("-");
            if(price[0].equals("0")){//如果区间起点等于0
                Criteria criteria1 = new Criteria("item_price").lessThanEqual(price[1]);
                FilterQuery filterQuery = new SimpleFilterQuery(criteria1);
                query.addFilterQuery(filterQuery);
            }else if("*".equals(price[1])){
                Criteria criteria1 = new Criteria("item_price").greaterThanEqual(price[0]);
                FilterQuery filterQuery = new SimpleFilterQuery(criteria1);
                query.addFilterQuery(filterQuery);
            }else {
                Criteria criteria1 = new Criteria("item_price").lessThanEqual(price[1]);
                FilterQuery filterQuery = new SimpleFilterQuery(criteria1);
                query.addFilterQuery(filterQuery);
                Criteria criteria2 = new Criteria("item_price").greaterThanEqual(price[0]);
                FilterQuery filterQuery2 = new SimpleFilterQuery(criteria2);
                query.addFilterQuery(filterQuery2);
            }

        }

        //6.6分页查询
           //首页
        Integer pageNo = (Integer)searchMap.get("pageNo");
        if (pageNo==null){
            pageNo=1;
        }
        Integer pageSize = (Integer)searchMap.get("pageSize");
        if (pageSize==null){
            pageSize=10;
        }
           //每页的开始索引和条数
        query.setOffset((pageNo-1)*pageSize);
        query.setRows(pageSize);

        //6.7获取排序对象和排序属性
        String sortValue = (String)searchMap.get("sort");
        String sortFiled = (String)searchMap.get("sortFiled");
        if (sortFiled != null && sortFiled.length() > 0){
            if ("ASC".equals(sortValue)){
                Sort sort = new Sort(Sort.Direction.ASC,"item_"+sortFiled);
                query.addSort(sort);
            }
            if ("DESC".equals(sortValue)){
                Sort sort = new Sort(Sort.Direction.DESC,"item_"+sortFiled);
                query.addSort(sort);
            }
        }

        //7、发出带高亮数据查询请求
        HighlightPage<TbItem> page = solrTemplate.queryForHighlightPage(query,TbItem.class);
        //8、获取高亮集合入口
        List<HighlightEntry<TbItem>> highlightEntryList = page.getHighlighted();
        //9、遍历高亮集合
        for (HighlightEntry<TbItem> highlightEntry : highlightEntryList) {
            //获取基本数据对象
            TbItem tbItem = highlightEntry.getEntity();
            if (highlightEntry.getHighlights().size()>0 && highlightEntry.getHighlights().get(0).getSnipplets().size()>0){
                List<HighlightEntry.Highlight> highlightList = highlightEntry.getHighlights();
                //高亮结果集合
                List<String> snipplets = highlightList.get(0).getSnipplets();
                //获取第一个高亮字段对应的高亮结果，设置到商品标题
                tbItem.setTitle(snipplets.get(0));
            }
        }
        //把带高亮数据集合存放map
        map.put("rows",page.getContent());
        map.put("totalPages",page.getTotalPages());//总页数
        map.put("totalElements",page.getTotalElements());//总条数
        return map;
    }

    /**查询分类列表
     * @param searchMap
     * @return
     */
    private List searchCategoryList(Map searchMap){
        List<String> list = new ArrayList<>();
        Query query = new SimpleQuery();
        //按照关键字查询
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        query.addCriteria(criteria);
        //设置分组选项
        GroupOptions groupOptions = new GroupOptions().addGroupByField("item_category");
        query.setGroupOptions(groupOptions);
        //得到分组页
        GroupPage<TbItem> page = solrTemplate.queryForGroupPage(query, TbItem.class);
        //根据列得到分组结果集
        GroupResult<TbItem> groupResult = page.getGroupResult("item_category");
        //得到分组结果入口页
        Page<GroupEntry<TbItem>> groupEntries = groupResult.getGroupEntries();
        //得到分组入口集合
        List<GroupEntry<TbItem>> content = groupEntries.getContent();
        for (GroupEntry<TbItem> entry : content) {
            list.add(entry.getGroupValue());//将分组结果的名称封装到返回值中
        }
        return list;
    }

    /**查询品牌和规格列表
     * @param category
     * @return
     */
    private Map searchBrandAndSpecList(String category){
        Map map = new HashMap();
        Long typeId = (Long) redisTemplate.boundHashOps("itemCat").get(category);//获取模板Id
        if (typeId != null) {
            //根据模板ID查询品牌列表
            List brandList = (List) redisTemplate.boundHashOps("brandList").get(typeId);
            //品牌列表添加到返回值
            map.put("brandList",brandList);
            //根据模板ID查询规格列表
            List specList = (List) redisTemplate.boundHashOps("specList").get(typeId);
            map.put("specList",specList);
        }
        return map;
    }

    /**添加商品
     * @param list
     */
    @Override
    public void importList(List<TbItem> list) {
        for (TbItem item : list) {
            Map<String,String> specMap = JSON.parseObject(item.getSpec(),Map.class);
            Map map = new HashMap();
            for(Map.Entry entry : specMap.entrySet()){
                map.put("item_spec_"+entry.getKey(),entry.getValue());
            }
            item.setSpecMap(map);
        }
        solrTemplate.saveBeans(list);
        solrTemplate.commit();
    }

    /**删除数据
     * @param goodsIdList
     */
    @Override
    public void deleteByGoodsIds(List goodsIdList) {
        System.out.println("删除商品ID:"+goodsIdList);
        Query query = new SimpleQuery();
        Criteria criteria = new Criteria("item_goodsid").in(goodsIdList);
        query.addCriteria(criteria);
        solrTemplate.delete(query);
        solrTemplate.commit();
    }


}
