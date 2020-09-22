package com.dongyimai.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.dongyimai.entity.PageResult;
import com.dongyimai.entity.Result;
import com.dongyimai.pojo.TbBrand;
import com.dongyimai.service.BrandService;
import com.sun.javafx.collections.MappingChange;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/brand/")
public class BrandController {
    @Reference
    private BrandService brandService;
    @RequestMapping("findAll")
    public List<TbBrand> findAll(){
        return brandService.findAll();
    }

    /**返回指定页码，行数，品牌列表
     * @param page
     * @param rows
     * @return
     */
    @RequestMapping("findPage")
    public PageResult findPage(int page,int rows){
        return brandService.findPage(page,rows);
    }

    @RequestMapping("add")
    public Result add(@RequestBody TbBrand tbBrand){
        try {
            brandService.add(tbBrand);
            return new Result(true,"添加成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"添加失败");
        }
    }

    /**获取实体
     * @param id
     * @return
     */
    @RequestMapping("findOne")
    public TbBrand findOne(long id){
        return brandService.findOne(id);
    }
    @RequestMapping("update")
    public Result update(@RequestBody TbBrand tbBrand){
        try {
            brandService.update(tbBrand);
            return new Result(true,"修改成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"修改失败");
        }
    }

    /**批量删除
     * @param ids
     * @return
     */
    @RequestMapping("delete")
    public Result delete(long[] ids){
        try {
            brandService.delete(ids);
            return new Result(true,"删除成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"删除失败");
        }
    }

    /**根据条件分页查询
     * @param tbBrand
     * @param page
     * @param rows
     * @return
     */
    @RequestMapping("search")
    public PageResult search(@RequestBody TbBrand tbBrand,int page,int rows){
        return brandService.findPage(tbBrand,page,rows);
    }

    @RequestMapping("selectOptionList")
    public List<Map> selectOptionList(){
        return brandService.selectOptionList();
    }
}
