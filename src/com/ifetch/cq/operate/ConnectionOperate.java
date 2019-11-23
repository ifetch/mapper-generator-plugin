package com.ifetch.cq.operate;

import com.ifetch.cq.model.DatabaseConfig;

import java.util.List;

/**
 * Created by cq on 19-10-31.
 */
public interface ConnectionOperate {

    /**
     * 创建一个连接
     *
     * @return
     */
    boolean create(DatabaseConfig config);

    /**
     * 删除连接
     *
     * @return
     */
    boolean delete(Long id);

    /**
     * 连接数据库
     *
     * @return
     */
    boolean connection();

    /**
     * 编辑连接
     *
     * @return
     */
    boolean edit(DatabaseConfig config);

    /**
     * 查询所有连接
     *
     * @return
     */
    List<DatabaseConfig> query();

}
