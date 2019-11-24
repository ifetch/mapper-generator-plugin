package com.ifetch.cq.model;

/**
 * Created by Owen on 6/14/16.
 */
public enum DbType {

    MySQL("com.mysql.jdbc.Driver", "jdbc:mysql://%s:%s/%s?useUnicode=true&useSSL=false&characterEncoding=%s", "mysql-connector-java"),
    Oracle("oracle.jdbc.driver.OracleDriver", "jdbc:oracle:thin:@%s:%s:%s", "ojdbc14"),
    PostgreSQL("org.postgresql.Driver", "jdbc:postgresql://%s:%s/%s", "postgresql"),
    SqlServer("com.microsoft.sqlserver.jdbc.SQLServerDriver", "jdbc:sqlserver://%s:%s;databaseName=%s", "sqljdbc4"),
    Sqlite("org.sqlite.JDBC", "jdbc:sqlite:%s", "sqlite-jdbc");

    private final String driverClass;
    private final String connectionUrlPattern;
    private final String connectorJarFile;

    DbType(String driverClass, String connectionUrlPattern, String connectorJarFile) {
        this.driverClass = driverClass;
        this.connectionUrlPattern = connectionUrlPattern;
        this.connectorJarFile = connectorJarFile;
    }

    public static DbType getByName(String name) {
        for (DbType dbType : DbType.values()) {
            if (dbType.name().equals(name)) {
                return dbType;
            }
        }
        return null;
    }

    public String getDriverClass() {
        return driverClass;
    }

    public String getConnectionUrlPattern() {
        return connectionUrlPattern;
    }

    public String getConnectorJarFile() {
        return connectorJarFile;
    }
}