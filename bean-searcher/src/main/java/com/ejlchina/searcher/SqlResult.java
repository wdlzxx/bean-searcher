package com.ejlchina.searcher;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * SQL 执行结果
 */
public class SqlResult {

    /**
     * 检索 SQL 信息
     */
    private final SearchSql searchSql;

    /**
     * 列表查询结果集
     */
    private ResultSet dataListResult;

    /**
     * 聚合查询结果集
     */
    private ResultSet clusterResult;


    public SqlResult(SearchSql searchSql) {
        this.searchSql = searchSql;
    }

    /**
     * 关闭结果集
     */
    public void closeResultSet() {
        try {
            if (dataListResult != null) {
                dataListResult.close();
            }
            if (clusterResult != null) {
                clusterResult.close();
            }
        } catch (SQLException e) {
            throw new SearcherException("Can not close statement or resultSet!", e);
        }
    }

    public SearchSql getSearchSql() {
        return searchSql;
    }

    public ResultSet getDataListResult() {
        return dataListResult;
    }

    public void setDataListResult(ResultSet dataListResult) {
        this.dataListResult = dataListResult;
    }

    public ResultSet getClusterResult() {
        return clusterResult;
    }

    public void setClusterResult(ResultSet clusterResult) {
        this.clusterResult = clusterResult;
    }

}