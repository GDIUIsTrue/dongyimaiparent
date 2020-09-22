package com.dongyimai.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.dongyimai.entity.PageResult;
import com.dongyimai.entity.Result;
import com.dongyimai.group.Goods;
import com.dongyimai.pojo.TbGoods;
import com.dongyimai.pojo.TbItem;
import com.dongyimai.service.GoodsService;
//import com.page.service.ItemPageService;
//import com.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import java.util.Arrays;
import java.util.List;
/**
 * controller
 * @author Administrator
 *
 */
@RestController
@RequestMapping("/goods/")
public class GoodsController {

	@Reference
	private GoodsService goodsService;

	//@Reference(timeout = 40000)
	//private ItemPageService itemPageService;

	@Autowired
	private Destination queueSolrDestination;//用于发送solr导入的数据
	@Autowired
	private Destination queueSolrDeleteDestination;//用户在索引库中删除记录
    @Autowired
	private Destination topicPageDestination;//生成静态页面发布订阅模型
    @Autowired
	private Destination topicPageDeleteDestination;//用于删除静态网页的消息
	@Autowired
	private JmsTemplate jmsTemplate;

	/**
	 * 返回全部列表
	 * @return
	 */
	@RequestMapping("findAll")
	public List<TbGoods> findAll(){			
		return goodsService.findAll();
	}
	
	
	/**
	 * 返回全部列表
	 * @return
	 */
	@RequestMapping("findPage")
	public PageResult  findPage(int page,int rows){			
		return goodsService.findPage(page, rows);
	}
	
	/**
	 * 增加
	 * @param goods
	 * @return
	 */
	@RequestMapping("add")
	public Result add(@RequestBody Goods goods){
		try {
			goodsService.add(goods);
			return new Result(true, "增加成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "增加失败");
		}
	}
	
	/**
	 * 修改
	 * @param goods
	 * @return
	 */
	@RequestMapping("update")
	public Result update(@RequestBody Goods goods){
		try {
			goodsService.update(goods);
			return new Result(true, "修改成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "修改失败");
		}
	}	
	
	/**
	 * 获取实体
	 * @param id
	 * @return
	 */
	@RequestMapping("findOne")
	public Goods findOne(Long id){
		return goodsService.findOne(id);		
	}
	
	/**
	 * 批量删除
	 * @param ids
	 * @return
	 */
	@RequestMapping("delete")
	public Result delete(final Long [] ids){
		try {
			goodsService.delete(ids);
			//itemSearchService.deleteByGoodsIds(Arrays.asList(ids));

			jmsTemplate.send(queueSolrDeleteDestination, new MessageCreator() {
				@Override
				public Message createMessage(Session session) throws JMSException {
					return session.createObjectMessage(ids);
				}
			});
            //删除页面
			jmsTemplate.send(topicPageDeleteDestination, new MessageCreator() {
				@Override
				public Message createMessage(Session session) throws JMSException {
					return session.createObjectMessage(ids);
				}
			});
			return new Result(true, "删除成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "删除失败");
		}
	}
	
		/**
	 * 查询+分页
	 * @param goods
	 * @param page
	 * @param rows
	 * @return
	 */
	@RequestMapping("search")
	public PageResult search(@RequestBody TbGoods goods, int page, int rows  ){
		return goodsService.findPage(goods, page, rows);		
	}

	//@Reference
	//private ItemSearchService itemSearchService;

	/**更改商品状态
	 * @param ids
	 * @param status
	 * @return
	 */
	@RequestMapping("updateStatus")
	public Result updateStatus(Long[] ids,String status){
		try {
			goodsService.updateStatus(ids, status);
			//按照SPU ID查询 SKU列表(状态为1)
			if (status.equals("1")){//审核通过
				List<TbItem> itemList = goodsService.findItemListByGoodsIdAndStatus(ids,status);
				//调用搜索接口实现数据批量导入
				if (itemList.size()>0){
					/*//导入solr
					itemSearchService.importList(itemList);*/
					//将list对象转换为string 存入solr
					final String jsonString = JSON.toJSONString(itemList);

					//将solr服务器数据同步
					jmsTemplate.send(queueSolrDestination, new MessageCreator() {
						@Override
						public Message createMessage(Session session) throws JMSException {
							return session.createTextMessage(jsonString);
						}
					});

					//生成商品详情页
					/*for (Long id : ids) {
						itemPageService.genItemHtml(id);
					}*/

					//将要生成页面的ids存入到mq中
					for (final Long goodsId : ids) {
						jmsTemplate.send(topicPageDestination, new MessageCreator() {
							@Override
							public Message createMessage(Session session) throws JMSException {
								return session.createTextMessage(goodsId+"");
							}
						});
					}
				}else {
					System.out.println("没有明细数据");
				}
			}
			return new Result(true, "成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "失败");
		}
	}
}