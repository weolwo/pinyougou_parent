package com.pinyougou.search.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.search.service.ItemSearchService;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 搜索服务控制类
 */
@RestController
@RequestMapping("itemsearch")
public class ItemSearchController {

    @Reference
    private ItemSearchService itemSearchService;


    /**
     * 关键字索索
     * @param searchMap
     * @return
     */
    @RequestMapping("search")
    public Map search(@RequestBody Map searchMap) {

        return itemSearchService.search(searchMap);

    }
}
