package com.dongyimai.service;

import com.dongyimai.entity.PageResult;
import com.dongyimai.entity.Result;
import com.dongyimai.pojo.TbBrand;

import java.util.List;
import java.util.Map;

public interface BrandService {
    public List<TbBrand> findAll();

    //返回分页列表
    public PageResult findPage(int pageNum,int pageSize);

    public void add(TbBrand tbBrand);
    public void update(TbBrand tbBrand);

    /**根据ID获取实体
     * @param id
     * @return
     */
    public TbBrand findOne(long id);

    /**批量删除
     * @param ids
     */
    public void delete(long[] ids);

    /**根据条件分页查询
     * @param tbBrand
     * @param pageNum
     * @param pageSize
     * @return
     */
    public PageResult findPage(TbBrand tbBrand,int pageNum,int pageSize);

    /**品牌下拉框数据
     * @return
     */
    List<Map> selectOptionList();
}
