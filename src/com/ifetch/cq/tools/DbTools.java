package com.ifetch.cq.tools;

import com.ifetch.cq.model.DatabaseConfig;
import com.ifetch.cq.model.DbType;
import com.ifetch.cq.model.UITableColumnVO;
import com.intellij.openapi.diagnostic.Logger;

import java.sql.*;
import java.util.*;

/**
 * Created by Owen on 6/12/16.
 */
public class DbTools {

    private static final Logger _LOG = Logger.getInstance(DbTools.class);
    private static final int DB_CONNECTION_TIMEOUTS_SECONDS = 1;

    private static Map<DbType, Driver> drivers = new HashMap<>();

    static {
        drivers = new HashMap<>();
        DbType[] dbTypes = DbType.values();
        for (DbType dbType : dbTypes) {
            try {
                Class clazz = Class.forName(dbType.getDriverClass());
                Driver driver = (Driver) clazz.newInstance();
                _LOG.info("load driver class:" + driver);
                drivers.put(dbType, driver);
            } catch (Exception e) {
                _LOG.error("load driver error type:" + dbType, e);
            }
        }
    }

    public static boolean testConnection(DatabaseConfig config) {
        try {
            DbTools.getConnection(config);
        } catch (Exception e) {
            _LOG.error("testConnection:" + e.getMessage(), e);
            return false;
        }
        return true;
    }

    public static Connection getConnection(DatabaseConfig config) throws ClassNotFoundException, SQLException {
        String url = getConnectionUrlWithSchema(config);
        Properties props = new Properties();

        props.setProperty("user", config.getUsername()); //$NON-NLS-1$
        props.setProperty("password", config.getPassword()); //$NON-NLS-1$

        DriverManager.setLoginTimeout(DB_CONNECTION_TIMEOUTS_SECONDS);
        Connection connection = drivers.get(DbType.valueOf(config.getDbType())).connect(url, props);
        return connection;
    }

    public static List<String> getTableNames(DatabaseConfig config) throws Exception {
        try (Connection connection = getConnection(config)) {
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
        }
    }

    public static List<UITableColumnVO> getTableColumns(DatabaseConfig dbConfig, String tableName) throws Exception {
        try (Connection conn = getConnection(dbConfig)) {
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
        }
    }

    public static String getConnectionUrlWithSchema(DatabaseConfig dbConfig) throws ClassNotFoundException {
        DbType dbType = DbType.valueOf(dbConfig.getDbType());
        String connectionUrl = String.format(dbType.getConnectionUrlPattern(), dbConfig.getHost(), dbConfig.getPort(), dbConfig.getSchema(), dbConfig.getEncoding());
        _LOG.info("getConnectionUrlWithSchema, connection url: " + connectionUrl);
        return connectionUrl;
    }

}
