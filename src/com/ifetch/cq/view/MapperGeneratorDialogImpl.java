package com.ifetch.cq.view;

import com.ifetch.cq.bridge.MybatisGeneratorBridge;
import com.ifetch.cq.model.DatabaseConfig;
import com.ifetch.cq.model.GeneratorConfig;
import com.ifetch.cq.model.Result;
import com.ifetch.cq.model.UITableColumnVO;
import com.ifetch.cq.operate.ConnectionOperate;
import com.ifetch.cq.operate.ConnectionOperateImpl;
import com.ifetch.cq.tools.ConfigHelper;
import com.ifetch.cq.tools.DbTools;
import com.ifetch.cq.tools.StringTools;
import com.ifetch.cq.tools.UIProgressCallback;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import org.mybatis.generator.config.ColumnOverride;
import org.mybatis.generator.config.IgnoredColumn;

import javax.swing.tree.*;
import java.sql.SQLRecoverableException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by cq on 19-10-22.
 */
@SuppressWarnings("all")
public class MapperGeneratorDialogImpl extends MapperGeneratorDialog {

    private static final Logger _LOG = Logger.getInstance(MapperGeneratorDialogImpl.class);

    private DatabaseConfig config;

    private List<IgnoredColumn> ignoredColumns;

    private List<ColumnOverride> columnOverrides;

    private List<UITableColumnVO> columnVOs = new ArrayList<>();

    private ConnectionOperate connectionOperate;

    public MapperGeneratorDialogImpl(Project project) {
        super(project);
        connectionOperate = new ConnectionOperateImpl();
        init();
    }

    @Override
    Result<Boolean> addConnection() {
        ConnectionView connectionView = new ConnectionView(this, null);
        connectionView.show();
        Result<Boolean> result = new Result<>(true);
        result.setT(true);
        return result;
    }

    /**
     * 连接数据库
     *
     * @param config
     * @param treeNode
     * @return
     */
    @Override
    Result<Boolean> openConnection(DatabaseConfig config, DefaultMutableTreeNode treeNode) {
        Result<Boolean> result = new Result<>(true);
        try {
            List<String> tables = DbTools.getTableNames(config);
            if (tables != null && tables.size() > 0) {
                treeNode.removeAllChildren();
                for (String table : tables) {
                    DefaultMutableTreeNode newChild = new DefaultMutableTreeNode(table, false);
                    treeNode.add(newChild);
                }
            }
            result.setT(true).setDesc("数据库连接操作成功");
        } catch (SQLRecoverableException e) {
            result.setT(false).setDesc("连接数据库超时");
            _LOG.error("openConnection.连接数据库超时", e);
        } catch (Exception e) {
            result.setT(false).setDesc("连接数据库失败 msg:" + e.getMessage());
            _LOG.error("openConnection.连接数据库失败", e);
        }
        return result;
    }

    @Override
    Result<Boolean> editConnection(DatabaseConfig config) {
        Result<Boolean> result = new Result<>(true);
        result.setT(true);
        ConnectionView connectionView = new ConnectionView(this, config);
        connectionView.show();
        return result;
    }

    @Override
    Result<Boolean> delConnection(DatabaseConfig config) {
        Result<Boolean> result = new Result<>(true);
        boolean idDel = connectionOperate.delete(config.getId());
        if (idDel) {
            return result.setT(true).setDesc("数据库删除成功");
        } else {
            return result.setT(false).setDesc("数据库删除失败");
        }
    }

    @Override
    Result<Boolean> getTableInfo(String tableName, DatabaseConfig config) {
        Result<Boolean> result = new Result<>(true);
        try {
            List<UITableColumnVO> columns = DbTools.getTableColumns(config, tableName);
            setValue(tableName, StringTools.dbStringToCamelStyle(tableName), config, columns);
            result.setT(true);
        } catch (Exception err) {
            _LOG.error("getTableInfo err:" + err.getMessage(), err);
            result.setT(false).setDesc("连接数据库失败err:" + err.getMessage());
        }
        return result;
    }

    @Override
    Result<Boolean> madeTableColumn() {
        Result<Boolean> result = new Result<>(true);
        try {
            SelectTableColumnView columnView = new SelectTableColumnView(myProject, columnVOs);
            columnView.show();
            columnVOs = columnView.getColumnVOs();
        } catch (Exception e) {
            _LOG.error("madeTableColumn error:" + e.getMessage(), e);
        }
        return result;
    }

    @Override
    Result<Boolean> generatorCode() {
        Result<Boolean> result = new Result<>(true);
        try {
            forkColumn();
            GeneratorConfig generatorConfig = getGeneratorConfigFromUI();
            MybatisGeneratorBridge bridge = new MybatisGeneratorBridge(getConnectionLibPath(config.getDbType()));
            bridge.setGeneratorConfig(generatorConfig);
            bridge.setDatabaseConfig(config);
            bridge.setIgnoredColumns(ignoredColumns);
            bridge.setColumnOverrides(columnOverrides);
            UIProgressCallback callback = new UIProgressCallback();
            bridge.setProgressCallback(callback);
            bridge.generate();
            result.setT(true).setDesc(callback.getProgressText());
        } catch (Exception e) {
            result.setT(false).setDesc("代码生成出错 msg:" + e.getMessage());
            _LOG.error("GeneratorCodeListener.actionPerformed " + e.getMessage(), e);
        }
        return result;
    }

    @Override
    List<DatabaseConfig> configs() {
        return connectionOperate.query();
    }

    public void forkColumn() {
        ignoredColumns = new ArrayList<>();
        columnOverrides = new ArrayList<>();
        if (columnVOs != null && columnVOs.size() > 0) {
            for (UITableColumnVO item : columnVOs) {
                if (!item.isChecked()) {
                    IgnoredColumn ignoredColumn = new IgnoredColumn(item.getColumnName());
                    ignoredColumns.add(ignoredColumn);
                } else if (item.getTypeHandle() != null || item.getJavaType() != null || item.getPropertyName() != null) { // unchecked and have typeHandler value
                    ColumnOverride columnOverride = new ColumnOverride(item.getColumnName());
                    columnOverride.setTypeHandler(item.getTypeHandle());
                    columnOverride.setJavaProperty(item.getPropertyName());
                    columnOverride.setJavaType(item.getJavaType());
                    columnOverrides.add(columnOverride);
                }
            }
        }
    }

    public void setValue(String tableName, String entityName, DatabaseConfig config, List<UITableColumnVO> columnVOs) {
        super.iTableName.setText(tableName);
        super.iEntityName.setText(entityName);
        this.columnVOs = columnVOs;
        this.config = config;
    }

    public String getConnectionLibPath(String dbType) {
        return ConfigHelper.findConnectorLibPath(dbType, super.myProject);
    }
}
