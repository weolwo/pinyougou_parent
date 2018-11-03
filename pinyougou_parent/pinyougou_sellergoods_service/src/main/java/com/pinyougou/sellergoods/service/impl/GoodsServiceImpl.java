package com.pinyougou.sellergoods.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.abel533.entity.Example;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.pinyougou.mapper.*;
import com.pinyougou.pojo.*;
import com.pinyougou.pojogroup.Goods;
import com.pinyougou.sellergoods.service.GoodsService;
import entity.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 业务逻辑实现
 *
 * @author Steven
 */
@Service(interfaceClass = GoodsService.class)
@Transactional
public class GoodsServiceImpl implements GoodsService {

    @Autowired
    private TbGoodsMapper goodsMapper;

    @Autowired
    private TbGoodsDescMapper goodsDescMapper;

    @Autowired
    private TbItemMapper itemMapper;

    @Autowired
    private TbBrandMapper tbBrandMapper;

    @Autowired
    private TbSellerMapper sellerMapper;

    @Autowired
    private TbItemCatMapper itemCatMapper;

    /**
     * 查询全部
     */
    @Override
    public List<TbGoods> findAll() {
        return goodsMapper.select(null);
    }

    /**
     * 按分页查询
     */
    @Override
    public PageResult findPage(int pageNum, int pageSize) {

        PageResult<TbGoods> result = new PageResult<TbGoods>();
        //设置分页条件
        PageHelper.startPage(pageNum, pageSize);

        //查询数据
        List<TbGoods> list = goodsMapper.select(null);
        //保存数据列表
        result.setRows(list);

        //获取总记录数
        PageInfo<TbGoods> info = new PageInfo<TbGoods>(list);
        result.setTotal(info.getTotal());
        return result;
    }

    /**
     * 增加
     */
    @Override
    public void add(Goods goods) {
        //设置状态为未审核
        goods.getGoods().setAuditStatus("0");

        goods.getGoods().setIsMarketable("1");//默认上架
        goodsMapper.insertSelective(goods.getGoods());
       //int i=1/0;

        //设置商品id
        //保存商品描述信息
        goods.getGoodsDesc().setGoodsId(goods.getGoods().getId());
        goodsDescMapper.insertSelective(goods.getGoodsDesc());
        saveItemList(goods);


    }

    private void saveItemList(Goods goods) {
        //是否启用规格
        if ("1".equals(goods.getGoods().getIsEnableSpec())) {
            //插入商品itemlist
            for (TbItem item : goods.getItemList()) {
                //封装itemList
                //设置标题
                String title = goods.getGoods().getGoodsName();
                String itemSpec = item.getSpec();
                Map<String, Object> map = JSON.parseObject(item.getSpec());
                for (String key : map.keySet()) {
                    title += " " + map.get(key);
                }
                item.setTitle(title);

                setItemValus(goods, item);

                itemMapper.insert(item);
            }
        } else {
            //不启用规格,其实就是item在数据库中只有一条数据
            TbItem item = new TbItem();
            //设置标题
            item.setTitle(goods.getGoods().getGoodsName());

            //设置价格
            item.setPrice(goods.getGoods().getPrice());

            item.setIsDefault("1");//是否默认

            item.setStatus("1");//状态

            item.setNum(9999);//库存

            item.setSpec("{}");

            setItemValus(goods, item);

            itemMapper.insert(item);
        }
    }

    private void setItemValus(Goods goods, TbItem item) {
        //设置商品id
        item.setGoodsId(goods.getGoods().getId());

        item.setSellerId(goods.getGoods().getSellerId());//商家id

        item.setCategoryid(goods.getGoods().getCategory3Id());//分类id

        item.setCreateTime(new Date());//创建日期

        item.setUpdateTime(new Date());//更新日期


        //品牌名称
        TbBrand brand = tbBrandMapper.selectByPrimaryKey(goods.getGoods().getBrandId());
        item.setBrand(brand.getName());

        //店铺名称
        TbSeller seller = sellerMapper.selectByPrimaryKey(goods.getGoods().getSellerId());
        item.setSeller(seller.getNickName());

        //分类名称
        TbItemCat itemCat = itemCatMapper.selectByPrimaryKey(goods.getGoods().getCategory3Id());
        item.setCategory(itemCat.getName());

        //图片地址（取spu的第一个图片）
        List<Map> imageList = JSON.parseArray(goods.getGoodsDesc().getItemImages(), Map.class);
        if (imageList.size() > 0) {
            item.setImage((String) imageList.get(0).get("url"));
        }
    }


    /**
     * 修改
     */
    @Override
    public void update(Goods goods) {
        //设置商品状态未未审核
        goods.getGoods().setAuditStatus("0");

        goods.getGoods().setIsMarketable("1");//默认上架
        goodsMapper.updateByPrimaryKeySelective(goods.getGoods());

        //保存goodsDesc
        goodsDescMapper.updateByPrimaryKeySelective(goods.getGoodsDesc());

        // 保存SKU.更新前先删除SKU

        TbItem where = new TbItem();
        where.setGoodsId(goods.getGoods().getId());
        itemMapper.delete(where);
        saveItemList(goods);
    }

    /**
     * 根据ID获取实体
     *
     * @param id
     * @return
     */
    @Override
    public Goods findOne(Long id) {
        //封装googs
        Goods goods = new Goods();

        //设置Tbgoods
        TbGoods tbGoods = goodsMapper.selectByPrimaryKey(id);
        goods.setGoods(tbGoods);

        //设置goodDesc
        TbGoodsDesc tbGoodsDesc = goodsDescMapper.selectByPrimaryKey(id);
        goods.setGoodsDesc(tbGoodsDesc);

        //设置itemlist
        TbItem where = new TbItem();
        where.setGoodsId(id);
        List<TbItem> itemList = itemMapper.select(where);
        goods.setItemList(itemList);
        return goods;
    }

    /**
     * 批量删除
     * 这里的删除并非是物理删除，而是修改tb_goods表的is_delete字段为1 ，我们可以称之为“逻辑删除”
     */
    @Override
    public void delete(Long[] ids) {

        for (Long id : ids) {
            TbGoods goods = goodsMapper.selectByPrimaryKey(id);
            goods.setIsDelete("1");

            goodsMapper.updateByPrimaryKey(goods);
        }
    }


    @Override
    public PageResult findPage(TbGoods goods, int pageNum, int pageSize) {
        PageResult<TbGoods> result = new PageResult<TbGoods>();
        //设置分页条件
        PageHelper.startPage(pageNum, pageSize);

        //构建查询条件
        Example example = new Example(TbGoods.class);
        Example.Criteria criteria = example.createCriteria();
        //查询没删除的数据
        criteria.andIsNull("isDelete");
        if (goods != null) {
            //如果字段不为空
            if (goods.getSellerId() != null && goods.getSellerId().length() > 0) {
                // criteria.andLike("sellerId", "%" + goods.getSellerId() + "%");
                //此处应该改成精确查询
                criteria.andEqualTo("sellerId", goods.getSellerId());
            }
            //如果字段不为空
            if (goods.getGoodsName() != null && goods.getGoodsName().length() > 0) {
                criteria.andLike("goodsName", "%" + goods.getGoodsName() + "%");
            }
            //如果字段不为空
            if (goods.getAuditStatus() != null && goods.getAuditStatus().length() > 0) {
                criteria.andLike("auditStatus", "%" + goods.getAuditStatus() + "%");
            }
            //如果字段不为空
            if (goods.getIsMarketable() != null && goods.getIsMarketable().length() > 0) {
                criteria.andLike("isMarketable", "%" + goods.getIsMarketable() + "%");
            }
            //如果字段不为空
            if (goods.getCaption() != null && goods.getCaption().length() > 0) {
                criteria.andLike("caption", "%" + goods.getCaption() + "%");
            }
            //如果字段不为空
            if (goods.getSmallPic() != null && goods.getSmallPic().length() > 0) {
                criteria.andLike("smallPic", "%" + goods.getSmallPic() + "%");
            }
            //如果字段不为空
            if (goods.getIsEnableSpec() != null && goods.getIsEnableSpec().length() > 0) {
                criteria.andLike("isEnableSpec", "%" + goods.getIsEnableSpec() + "%");
            }
            //如果字段不为空
            if (goods.getIsDelete() != null && goods.getIsDelete().length() > 0) {
                criteria.andLike("isDelete", "%" + goods.getIsDelete() + "%");
            }

        }

        //查询数据
        List<TbGoods> list = goodsMapper.selectByExample(example);
        //保存数据列表
        result.setRows(list);

        //获取总记录数
        PageInfo<TbGoods> info = new PageInfo<TbGoods>(list);
        result.setTotal(info.getTotal());

        return result;
    }

    @Override
    public void updateStatus(Long[] ids, String stauts) {

        for (Long id : ids) {
            //根据id查询商品
            TbGoods goods = goodsMapper.selectByPrimaryKey(id);
            goods.setAuditStatus(stauts);
            //更新商品
            goodsMapper.updateByPrimaryKey(goods);
        }
    }

    @Override
    public List<TbItem> findItemListByGoodsIdandStatus(Long[] goodsIds, String status) {

        Example example=new Example(TbItem.class);
        Example.Criteria criteria = example.createCriteria();
        List longs = Arrays.asList(goodsIds);
        criteria.andIn("goodsId",longs);
        criteria.andEqualTo("status",status);
        List<TbItem> items = itemMapper.selectByExample(example);
        return items;
    }

}
