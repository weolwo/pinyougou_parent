package com.pinyougou.page.service;

/**
 * 商品详细页接口
 */
public interface ItemPageService {

    /**
     * 生成商品详细页
     *
     * @param goodsId 商品id
     */
    public boolean genItemHtml(Long goodsId);

    /**
     * 删除商品详情页
     * @param goodsIds 商品id
     * @return boolean
     */
    public boolean deleteGenItemHtml(Long[] goodsIds);

}
