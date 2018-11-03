package com.pinyougou.sellergoods.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.abel533.entity.Example;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.pinyougou.mapper.TbSpecificationOptionMapper;
import com.pinyougou.mapper.TbTypeTemplateMapper;
import com.pinyougou.pojo.TbSpecificationOption;
import com.pinyougou.pojo.TbTypeTemplate;
import com.pinyougou.sellergoods.service.TypeTemplateService;
import entity.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 业务逻辑实现
 *
 * @author Steven
 */
@Service
public class TypeTemplateServiceImpl implements TypeTemplateService {

    @Autowired
    private TbTypeTemplateMapper typeTemplateMapper;

    @Autowired
    private TbSpecificationOptionMapper specificationOptionMapper;

    /**
     * 查询全部
     */
    @Override
    public List<TbTypeTemplate> findAll() {
        return typeTemplateMapper.select(null);
    }

    /**
     * 按分页查询
     */
    @Override
    public PageResult findPage(int pageNum, int pageSize) {

        PageResult<TbTypeTemplate> result = new PageResult<TbTypeTemplate>();
        //设置分页条件
        PageHelper.startPage(pageNum, pageSize);

        //查询数据
        List<TbTypeTemplate> list = typeTemplateMapper.select(null);
        //保存数据列表
        result.setRows(list);

        //获取总记录数
        PageInfo<TbTypeTemplate> info = new PageInfo<TbTypeTemplate>(list);
        result.setTotal(info.getTotal());
        return result;
    }

    /**
     * 增加
     */
    @Override
    public void add(TbTypeTemplate typeTemplate) {
        typeTemplateMapper.insertSelective(typeTemplate);
    }


    /**
     * 修改
     */
    @Override
    public void update(TbTypeTemplate typeTemplate) {
        typeTemplateMapper.updateByPrimaryKeySelective(typeTemplate);
    }

    /**
     * 根据ID获取实体
     *
     * @param id
     * @return
     */
    @Override
    public TbTypeTemplate findOne(Long id) {
        return typeTemplateMapper.selectByPrimaryKey(id);
    }

    /**
     * 批量删除
     */
    @Override
    public void delete(Long[] ids) {
        //数组转list
        List longs = Arrays.asList(ids);
        //构建查询条件
        Example example = new Example(TbTypeTemplate.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andIn("id", longs);

        //跟据查询条件删除数据
        typeTemplateMapper.deleteByExample(example);
    }


    @Override
    public PageResult findPage(TbTypeTemplate typeTemplate, int pageNum, int pageSize) {
        PageResult<TbTypeTemplate> result = new PageResult<TbTypeTemplate>();
        //设置分页条件
        PageHelper.startPage(pageNum, pageSize);

        //构建查询条件
        Example example = new Example(TbTypeTemplate.class);
        Example.Criteria criteria = example.createCriteria();

        if (typeTemplate != null) {
            //如果字段不为空
            if (typeTemplate.getName() != null && typeTemplate.getName().length() > 0) {
                criteria.andLike("name", "%" + typeTemplate.getName() + "%");
            }
            //如果字段不为空
            if (typeTemplate.getSpecIds() != null && typeTemplate.getSpecIds().length() > 0) {
                criteria.andLike("specIds", "%" + typeTemplate.getSpecIds() + "%");
            }
            //如果字段不为空
            if (typeTemplate.getBrandIds() != null && typeTemplate.getBrandIds().length() > 0) {
                criteria.andLike("brandIds", "%" + typeTemplate.getBrandIds() + "%");
            }
            //如果字段不为空
            if (typeTemplate.getCustomAttributeItems() != null && typeTemplate.getCustomAttributeItems().length() > 0) {
                criteria.andLike("customAttributeItems", "%" + typeTemplate.getCustomAttributeItems() + "%");
            }

        }

        //查询数据
        List<TbTypeTemplate> list = typeTemplateMapper.selectByExample(example);
        //保存数据列表
        result.setRows(list);

        //获取总记录数
        PageInfo<TbTypeTemplate> info = new PageInfo<TbTypeTemplate>(list);
        result.setTotal(info.getTotal());

        //缓存品牌和规格
        saveToRedis();
        return result;
    }

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 缓存品牌和规格到redis中
     */
    private void saveToRedis() {

        List<TbTypeTemplate> templateList = findAll();

        for (TbTypeTemplate typeTemplate : templateList) {

            //缓存品牌
            List<Map> brandList = JSON.parseArray(typeTemplate.getBrandIds(), Map.class);
            redisTemplate.boundHashOps("brandList").put(typeTemplate.getId(), brandList);

            //缓存规格
            List<Map> specList = findSpecList(typeTemplate.getId());
            redisTemplate.boundHashOps("specList").put(typeTemplate.getId(), specList);
        }


    }

    @Override
    public List<Map> findSpecList(Long id) {
        //查询模板信息
        TbTypeTemplate typeTemplate = typeTemplateMapper.selectByPrimaryKey(id);
        //JSON.parseArray(a,b)把json串转成List<Map>,将[{"id":27,"text":"网络"},{"id":32,"text":"机身内存"},options[{},{}]]字符串转换成集合,
        // 集合中放什么,第二个参数决定,放map就会给你封装成map泛型的list集合
        List<Map> maps = JSON.parseArray(typeTemplate.getSpecIds(), Map.class);

        //遍历规格列表,获取id,在根据id查询对应的规格项中的规格名
        TbSpecificationOption where = null;
        for (Map map : maps) {

            where = new TbSpecificationOption();

            where.setSpecId(new Long(map.get("id").toString()));

            List<TbSpecificationOption> options = specificationOptionMapper.select(where);

            map.put("options", options);
        }
        return maps;
    }


}
