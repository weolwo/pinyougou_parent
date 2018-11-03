package com.pinyougou.sellergoods.service.impl;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.abel533.entity.Example;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.pinyougou.mapper.TbSpecificationMapper;
import com.pinyougou.mapper.TbSpecificationOptionMapper;
import com.pinyougou.pojo.TbSpecification;
import com.pinyougou.pojo.TbSpecificationOption;
import com.pinyougou.pojogroup.Specification;
import com.pinyougou.sellergoods.service.SpecificationService;
import entity.PageResult;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.List;

/**
 * 业务逻辑实现
 * @author Steven
 *
 */
@Service
public class SpecificationServiceImpl implements SpecificationService {

	@Autowired
	private TbSpecificationMapper specificationMapper;

	@Autowired
	private TbSpecificationOptionMapper specificationOptionMapper;

	/**
	 * 查询全部
	 */
	@Override
	public List<TbSpecification> findAll() {
		return specificationMapper.select(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		
		PageResult<TbSpecification> result = new PageResult<TbSpecification>();
        //设置分页条件
        PageHelper.startPage(pageNum, pageSize);

        //查询数据
        List<TbSpecification> list = specificationMapper.select(null);
        //保存数据列表
        result.setRows(list);

        //获取总记录数
        PageInfo<TbSpecification> info = new PageInfo<TbSpecification>(list);
        result.setTotal(info.getTotal());
		return result;
	}

    /**
	 * 增加
	 */
	@Override
	public void add(Specification specification) {
		//保存规格
		specificationMapper.insertSelective(specification.getSpecification());
		//保存规格选项
		for (TbSpecificationOption option : specification.getSpecificationOptionList()) {
			//设置规格id
			option.setSpecId(specification.getSpecification().getId());

			specificationOptionMapper.insertSelective(option);
		}
	}


	/**
	 * 修改
	 */
	@Override
	public void update(Specification specification){
		//更新规格信息
		specificationMapper.updateByPrimaryKeySelective(specification.getSpecification());
		//更新规格选项信息
		//先删除之前的所有选项
		TbSpecificationOption where=new TbSpecificationOption();
		where.setSpecId(specification.getSpecification().getId());
		specificationOptionMapper.delete(where);

		//再添加选项
		for (TbSpecificationOption specificationOption : specification.getSpecificationOptionList()) {

			//设置id
			specificationOption.setSpecId(specification.getSpecification().getId());

			specificationOptionMapper.insertSelective(specificationOption);
		}
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public Specification findOne(Long id){
		//封装Specification
		Specification resullt=new Specification();
		//查询Specification
		TbSpecification tbSpecification = specificationMapper.selectByPrimaryKey(id);
		resullt.setSpecification(tbSpecification);
		//查询规格项specificationOptionList
		TbSpecificationOption Where=new TbSpecificationOption();
		Where.setSpecId(id);
		List<TbSpecificationOption> options = specificationOptionMapper.select(Where);
		resullt.setSpecificationOptionList(options);
		return resullt;
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		//数组转list
		List longs = Arrays.asList(ids);
		//构建查询条件
		Example example = new Example(TbSpecification.class);
		Example.Criteria criteria = example.createCriteria();
		criteria.andIn("id", longs);

		//跟据查询条件删除数据
		specificationMapper.deleteByExample(example);

		//删除选项列表
		for (Long id : ids) {
			TbSpecificationOption where = new TbSpecificationOption();
			where.setSpecId(id);
			specificationOptionMapper.delete(where);
		}
	}



	@Override
	public PageResult findPage(TbSpecification specification, int pageNum, int pageSize) {
		PageResult<TbSpecification> result = new PageResult<TbSpecification>();
        //设置分页条件
        PageHelper.startPage(pageNum, pageSize);

        //构建查询条件
        Example example = new Example(TbSpecification.class);
        Example.Criteria criteria = example.createCriteria();
		
		if(specification!=null){			
						//如果字段不为空
			if (specification.getSpecName()!=null && specification.getSpecName().length()>0) {
				criteria.andLike("specName", "%" + specification.getSpecName() + "%");
			}
	
		}

        //查询数据
        List<TbSpecification> list = specificationMapper.selectByExample(example);
        //保存数据列表
        result.setRows(list);

        //获取总记录数
        PageInfo<TbSpecification> info = new PageInfo<TbSpecification>(list);
        result.setTotal(info.getTotal());
		
		return result;
	}
	
}
