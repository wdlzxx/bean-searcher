package com.ejlchina.searcher;

import com.ejlchina.searcher.param.FetchType;

import java.util.Map;

/***
 * 请求参数解析器接口
 * 
 * @author Troy.Zhou @ 2017-03-20
 * */
public interface ParamResolver {

	/**
	 * @param beanMeta 元数据
	 * @param fetchType Fetch 类型
	 * @param paraMap 原始检索参数
	 * @return SearchParam
	 * */
	SearchParam resolve(BeanMeta<?> beanMeta, FetchType fetchType, Map<String, Object> paraMap);
	
}
