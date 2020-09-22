package com.page.service;

import java.io.IOException;

public interface ItemPageService {
    /**生成商品详细页接口
     * @param goodsId
     * @return
     */
    public boolean genItemHtml(Long goodsId) throws IOException;

    /**删除商品详情页
     * @param goodsIds
     * @return
     */
    public boolean deleteItemHtml(Long[] goodsIds);
}
