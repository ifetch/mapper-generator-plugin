package com.ifetch.cq.tools;

import com.ifetch.cq.model.DatabaseConfig;
import com.ifetch.cq.model.DbType;
import com.ifetch.cq.model.Result;
import com.ifetch.cq.model.UITableColumnVO;
import com.intellij.ide.plugins.cl.PluginClassLoader;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;

import java.sql.*;
import java.util.*;

/**
 * Created by Owen on 6/12/16.
 */
public class DbTools {

    private static final Logger _LOG = Logger.getInstance(DbTools.class);
    private static final int DB_CONNECTION_TIMEOUTS_SECONDS = 1;

    private static Map<DbType, Driver> drivers = new HashMap<>();

    public static Driver getDriver(Project project, DbType type) {
        Driver driver = drivers.get(type);
        if (driver != null) {
            return driver;
        }
        Class clazz = ConfigHelper.loadClass(project, type, (PluginClassLoader) type.getClass().getClassLoader());
        if (clazz == null) {
            throw new RuntimeException("请在项目中引入 " + type + "驱动包" + type.getConnectorJarFile() + ".jar");
        }
        try {
            driver = (Driver) clazz.newInstance();
            if (driver != null) {
                drivers.put(type, driver);
            }
        } catch (Exception e) {
            _LOG.error("load driver error type:" + type, e);
        }
        return driver;
    }

    public static Result<Boolean> testConnection(Project project, DatabaseConfig config) {
        Result<Boolean> result = new Result<>(true);
        try {
            DbTools.getConnection(project, config);
            return result.setT(true);
        } catch (Exception e) {
            _LOG.error("testConnection:" + e.getMessage(), e);
            result.setT(false).setDesc(e.getMessage());
        }
        return result;
    }

    public static Connection getConnection(Project project, DatabaseConfig config) throws ClassNotFoundException, SQLException {
        DbType type = DbType.valueOf(config.getDbType());
        String url = getConnectionUrlWithSchema(config);

        Properties props = new Properties();
        props.setProperty("user", config.getUsername()); //$NON-NLS-1$
        props.setProperty("password", config.getPassword()); //$NON-NLS-1$

        DriverManager.setLoginTimeout(DB_CONNECTION_TIMEOUTS_SECONDS);
        Driver driver = getDriver(project, type);
        if (driver == null) {
            throw new RuntimeException("get db driver fail . dbType:" + type);
        }
        Connection connection = driver.connect(url, props);
        return connection;
    }

    public static List<String> getTableNames(Project project, DatabaseConfig config) throws Exception {
        Connection connection = getConnection(project, config);
        try {
            List<String> tables = new ArrayList<>();
            DatabaseMetaData md = connection.getMetaData();
            ResultSet rs;
            if (DbType.valueOf(config.getDbType()) == DbType.SQL_Server) {
                String sql = "select name from sysobjects  where xtype='u' or xtype='v' ";
                rs = connection.createStatement().executeQuery(sql);
                while (rs.next()) {
                    tables.add(rs.getString("name"));
                }
            } else if (DbType.valueOf(config.getDbType()) == DbType.Oracle) {
                rs = md.getTables(null, config.getUsername().toUpperCase(), null, new String[]{"TABLE", "VIEW"});
            } else if (DbType.valueOf(config.getDbType()) == DbType.Sqlite) {
                String sql = "Select name from sqlite_master;";
                rs = connection.createStatement().executeQuery(sql);
                while (rs.next()) {
                    tables.add(rs.getString("name"));
                }
            } else {
                // rs = md.getTables(null, config.getUsername().toUpperCase(), null, null);
                rs = md.getTables(null, "%", "%", new String[]{"TABLE", "VIEW"});            //针对 postgresql 的左侧数据表显示
            }
            while (rs.next()) {
                tables.add(rs.getString(3));
            }
            return tables;
        } finally {
            connection.close();
        }
    }

    public static List<UITableColumnVO> getTableColumns(Project project, DatabaseConfig dbConfig, String tableName) throws Exception {
        Connection conn = getConnection(project, dbConfig);
        try {
            DatabaseMetaData md = conn.getMetaData();
            ResultSet rs = md.getColumns(null, null, tableName, null);
            List<UITableColumnVO> columns = new ArrayList<>();
            while (rs.next()) {
                UITableColumnVO columnVO = new UITableColumnVO();
                String columnName = rs.getString("COLUMN_NAME");
                columnVO.setColumnName(columnName);
                columnVO.setJdbcType(rs.getString("TYPE_NAME"));
                columns.add(columnVO);
            }
            return columns;
        } finally {
            conn.close();
        }
    }

    public static String getConnectionUrlWithSchema(DatabaseConfig dbConfig) throws ClassNotFoundException {
        DbType dbType = DbType.valueOf(dbConfig.getDbType());
        String connectionUrl = String.format(dbType.getConnectionUrlPattern(), dbConfig.getHost(), dbConfig.getPort(), dbConfig.getSchema(), dbConfig.getEncoding());
        _LOG.info("getConnectionUrlWithSchema, connection url: " + connectionUrl);
        return connectionUrl;
    }

}
