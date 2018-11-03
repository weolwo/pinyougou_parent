package com.pinyougou.content.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.abel533.entity.Example;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.pinyougou.content.service.ContentService;
import com.pinyougou.mapper.TbContentMapper;
import com.pinyougou.pojo.TbContent;
import entity.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Arrays;
import java.util.List;

/**
 * 业务逻辑实现
 *
 * @author Steven
 */
@Service
public class ContentServiceImpl implements ContentService {

    @Autowired
    private TbContentMapper contentMapper;

    /**
     * 查询全部
     */
    @Override
    public List<TbContent> findAll() {
        return contentMapper.select(null);
    }

    /**
     * 按分页查询
     */
    @Override
    public PageResult findPage(int pageNum, int pageSize) {

        PageResult<TbContent> result = new PageResult<TbContent>();
        //设置分页条件
        PageHelper.startPage(pageNum, pageSize);

        //查询数据
        List<TbContent> list = contentMapper.select(null);
        //保存数据列表
        result.setRows(list);

        //获取总记录数
        PageInfo<TbContent> info = new PageInfo<TbContent>(list);
        result.setTotal(info.getTotal());
        return result;
    }

    /**
     * 增加
     */
    @Override
    public void add(TbContent content) {

        contentMapper.insertSelective(content);

        //清楚redis中该组的缓存
        redisTemplate.boundHashOps("content").delete(content.getCategoryId());
    }


    /**
     * 修改
     */
    @Override
    public void update(TbContent content) {
        //考虑到用户可能会修改广告的分类，这样需要把原分类的缓存和新分类的缓存都清除掉
        //查询更新前的分类id
        Long categoryId = contentMapper.selectByPrimaryKey(content.getId()).getCategoryId();
        //先删除当前广告类型id
        redisTemplate.boundHashOps("content").delete(content.getCategoryId());

        contentMapper.updateByPrimaryKeySelective(content);
        //如果用户修改了广告类型的id,那么就清除修改前的缓存
        if (categoryId.longValue() != content.getCategoryId()) {

            redisTemplate.boundHashOps("content").delete(categoryId);
        }
    }

    /**
     * 根据ID获取实体
     *
     * @param id
     * @return
     */
    @Override
    public TbContent findOne(Long id) {
        return contentMapper.selectByPrimaryKey(id);
    }

    /**
     * 批量删除
     */
    @Override
    public void delete(Long[] ids) {
        //数组转list
        List longs = Arrays.asList(ids);
        //构建查询条件
        Example example = new Example(TbContent.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andIn("id", longs);

        //清楚缓存  必需在删除之前，查询出来，清除缓存
        List<TbContent> contents = contentMapper.selectByExample(example);
        for (TbContent content : contents) {
            redisTemplate.boundHashOps("content").delete(content.getCategoryId());
        }
        //跟据查询条件删除数据
        contentMapper.deleteByExample(example);
    }


    @Override
    public PageResult findPage(TbContent content, int pageNum, int pageSize) {
        PageResult<TbContent> result = new PageResult<TbContent>();
        //设置分页条件
        PageHelper.startPage(pageNum, pageSize);

        //构建查询条件
        Example example = new Example(TbContent.class);
        Example.Criteria criteria = example.createCriteria();

        if (content != null) {
            //如果字段不为空
            if (content.getTitle() != null && content.getTitle().length() > 0) {
                criteria.andLike("title", "%" + content.getTitle() + "%");
            }
            //如果字段不为空
            if (content.getUrl() != null && content.getUrl().length() > 0) {
                criteria.andLike("url", "%" + content.getUrl() + "%");
            }
            //如果字段不为空
            if (content.getPic() != null && content.getPic().length() > 0) {
                criteria.andLike("pic", "%" + content.getPic() + "%");
            }
            //如果字段不为空
            if (content.getStatus() != null && content.getStatus().length() > 0) {
                criteria.andLike("status", "%" + content.getStatus() + "%");
            }

        }

        //查询数据
        List<TbContent> list = contentMapper.selectByExample(example);
        //保存数据列表
        result.setRows(list);

        //获取总记录数
        PageInfo<TbContent> info = new PageInfo<TbContent>(list);
        result.setTotal(info.getTotal());

        return result;
    }

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public List<TbContent> findByCategoryId(Long categoryId) {
        //先从缓存中查询广告列表,没有再去数据库查,然后在存入缓存中
        List<TbContent> contents = (List<TbContent>) redisTemplate.boundHashOps("content").get(categoryId);
        if (contents == null) {
            System.out.println("从数据库中读取数据");
            Example example = new Example(TbContent.class);
            Example.Criteria criteria = example.createCriteria();
            criteria.andEqualTo("categoryId", categoryId);
            //查询状态未有效的广告
            criteria.andEqualTo("status", "1");
            //设置排序,多个字段用逗号分开
            example.setOrderByClause("sortOrder asc");
            contents = contentMapper.selectByExample(example);

            //把数据放入缓存
            redisTemplate.boundHashOps("content").put(categoryId, contents);
        } else {
            //方便测试
            System.out.println("从缓存中读取数据");

        }

        return contents;
    }

}
