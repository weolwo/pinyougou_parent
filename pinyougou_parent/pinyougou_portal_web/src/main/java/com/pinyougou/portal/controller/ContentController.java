package com.pinyougou.portal.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.content.service.ContentService;
import com.pinyougou.pojo.TbContent;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 广告管理控制类
 */
@RestController
@RequestMapping("content")
public class ContentController {

    @Reference
    private ContentService contentService;

    /**
     * 根据id查询广告列表
     * @param categoryId
     * @return
     */
    @RequestMapping("findByCategoryId")
    public List<TbContent> findByCategoryId(Long categoryId){

        return contentService.findByCategoryId(categoryId);
    }
}
