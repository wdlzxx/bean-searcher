package com.ejlchina.searcher.implement;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.ejlchina.searcher.SearchParamResolver;
import com.ejlchina.searcher.SearchResult;
import com.ejlchina.searcher.SearchResultConvertInfo;
import com.ejlchina.searcher.SearchResultResolver;
import com.ejlchina.searcher.SearchSql;
import com.ejlchina.searcher.SearchSqlExecutor;
import com.ejlchina.searcher.SearchSqlResolver;
import com.ejlchina.searcher.SearchTmpResult;
import com.ejlchina.searcher.Searcher;
import com.ejlchina.searcher.SearcherException;
import com.ejlchina.searcher.beanmap.SearchBeanMap;
import com.ejlchina.searcher.beanmap.SearchBeanCache;
import com.ejlchina.searcher.implement.pagination.Pagination;
import com.ejlchina.searcher.param.SearchParam;
import com.ejlchina.searcher.util.StringUtils;
import com.ejlchina.searcher.virtual.VirtualParamProcessor;

/***
 * @author Troy.Zhou @ 2017-03-20
 * 
 * 自动检索器 根据 Bean 的 Class 和请求参数，自动检索 Bean
 * 
 */
public class MainSearcher implements Searcher {

	
	private SearchParamResolver searchParamResolver;

	private SearchSqlResolver searchSqlResolver;

	private SearchSqlExecutor searchSqlExecutor;

	private SearchResultResolver searchResultResolver;

	private VirtualParamProcessor virtualParamProcessor;


	@Override
	public <T> SearchResult<T> search(Class<T> beanClass, Map<String, Object> paraMap) {
		return search(beanClass, paraMap, null, true, true, false);
	}

	@Override
	public <T> SearchResult<T> search(Class<T> beanClass, Map<String, Object> paraMap, String[] summaryFields) {
		return search(beanClass, paraMap, summaryFields, true, true, false);
	}

	@Override
	public <T> T searchFirst(Class<T> beanClass, Map<String, Object> paraMap) {
		paraMap.put(getPagination().getMaxParamName(), "1");
		List<T> list = search(beanClass, paraMap, null, false, true, false).getDataList();
		if (list.size() > 0) {
			return list.get(0);
		}
		return null;
	}

	@Override
	public <T> List<T> searchList(Class<T> beanClass, Map<String, Object> paraMap) {
		return search(beanClass, paraMap, null, false, true, false).getDataList();
	}

	@Override
	public <T> List<T> searchAll(Class<T> beanClass, Map<String, Object> paraMap) {	
		return search(beanClass, paraMap, null, false, true, true).getDataList();
	}

	@Override
	public <T> Number searchCount(Class<T> beanClass, Map<String, Object> paraMap) {
		return search(beanClass, paraMap, null, true, false, true).getTotalCount();
	}

	@Override
	public <T> Number searchSum(Class<T> beanClass, Map<String, Object> paraMap, String field) {
		Number[] results = searchSum(beanClass, paraMap, new String[] { field });
		if (results != null && results.length > 0) {
			return results[0];
		}
		return null;
	}

	@Override
	public <T> Number[] searchSum(Class<T> beanClass, Map<String, Object> paraMap, String[] fields) {
		if (fields == null || fields.length == 0) {
			throw new SearcherException("检索该 Bean【" + beanClass.getName() 
			+ "】的统计信息时，必须要指定需要统计的属性！");
		}
		return search(beanClass, paraMap, fields, false, false, true).getSummaries();
	}
	
	/// 私有方法

	private <T> SearchResult<T> search(Class<T> beanClass, Map<String, Object> paraMap, String[] summaryFields,
				boolean shouldQueryTotal, boolean shouldQueryList, boolean needNotLimit) {
		SearchBeanMap beanMap = SearchBeanCache.getSearchBeanMap(beanClass);

		List<String> fieldList = beanMap.getFieldList();
		SearchParam searchParam = searchParamResolver.resolve(fieldList, paraMap);
		searchParam.setSummaryFields(summaryFields);
		searchParam.setShouldQueryTotal(shouldQueryTotal);
		searchParam.setShouldQueryList(shouldQueryList);
		if (needNotLimit) {
			searchParam.setMax(null);
		}
		beanMap = virtualParamProcessor.process(beanMap);
		SearchSql searchSql = searchSqlResolver.resolve(beanMap, searchParam);
		searchSql.setShouldQueryCluster(shouldQueryTotal || (summaryFields != null && summaryFields.length > 0));
		searchSql.setShouldQueryList(shouldQueryList);
		SearchTmpResult searchTmpResult = searchSqlExecutor.execute(searchSql);
		@SuppressWarnings("unchecked")
		SearchResultConvertInfo<T> convertInfo = (SearchResultConvertInfo<T>) beanMap.getConvertInfo();
		SearchResult<T> result = searchResultResolver.resolve(convertInfo.with(beanClass), searchTmpResult);
		return consummateSearchResult(searchParam, result);
	}

	private <T> SearchResult<T> consummateSearchResult(SearchParam searchParam, SearchResult<T> result) {
		Integer max = searchParam.getMax();
		Long offset = searchParam.getOffset();
		if (offset == null) {
			offset = 0L;
		}
		result.setMax(max);
		result.setOffset(offset);
		int startPage = getPagination().getStartPage();
		Number totalCount = result.getTotalCount();
		if (max != null && totalCount != null) {
			long maxLong = max.longValue();
			long totalLong = totalCount.longValue();
			long totalPage = totalLong / maxLong;
			if (totalPage * maxLong < totalLong) {
				totalPage = totalPage + 1;
			}
			result.setTotalPage(totalPage);
			result.setPage(startPage + offset / max);
		} else {
			result.setTotalPage(1);
			result.setPage(startPage);
		}
		Number[] summaries = result.getSummaries();
		if (summaries != null) {
			for (int i = 0; i < summaries.length; i++) {
				if (summaries[i] == null) {
					summaries[i] = 0;
				}
			}
		}
		return result;
	}

	private Pagination getPagination() {
		return searchParamResolver.getPagination();
	}
	
	public SearchParamResolver getSearchParamResolver() {
		return searchParamResolver;
	}

	public void setSearchParamResolver(SearchParamResolver searchParamResolver) {
		this.searchParamResolver = searchParamResolver;
	}

	public SearchSqlResolver getSearchSqlResolver() {
		return searchSqlResolver;
	}

	public void setSearchSqlResolver(SearchSqlResolver searchSqlResolver) {
		this.searchSqlResolver = searchSqlResolver;
	}

	public SearchSqlExecutor getSearchSqlExecutor() {
		return searchSqlExecutor;
	}

	public void setSearchSqlExecutor(SearchSqlExecutor searchSqlExecutor) {
		this.searchSqlExecutor = searchSqlExecutor;
	}

	public SearchResultResolver getSearchResultResolver() {
		return searchResultResolver;
	}

	public void setSearchResultResolver(SearchResultResolver searchResultResolver) {
		this.searchResultResolver = searchResultResolver;
	}

	public VirtualParamProcessor getVirtualParamProcessor() {
		return virtualParamProcessor;
	}

	public void setVirtualParamProcessor(VirtualParamProcessor virtualParamProcessor) {
		this.virtualParamProcessor = virtualParamProcessor;
	}

}
