package com.ifetch.cq.bridge;

import com.ifetch.cq.model.DatabaseConfig;
import com.ifetch.cq.model.DbType;
import com.ifetch.cq.model.GeneratorConfig;
import com.ifetch.cq.plugins.DbRemarksCommentGenerator;
import com.ifetch.cq.tools.DbTools;
import com.ifetch.cq.tools.StringTools;
import org.mybatis.generator.api.MyBatisGenerator;
import org.mybatis.generator.api.ProgressCallback;
import org.mybatis.generator.api.ShellCallback;
import org.mybatis.generator.config.*;
import org.mybatis.generator.internal.DefaultShellCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

/**
 * The bridge between GUI and the mybatis generator. All the operation to  mybatis generator should proceed through this
 * class
 * <p>
 * Created by Owen on 6/30/16.
 */
public class MybatisGeneratorBridge {

    private GeneratorConfig generatorConfig;

    private DatabaseConfig selectedDatabaseConfig;

    private ProgressCallback progressCallback;

    private List<IgnoredColumn> ignoredColumns;

    private List<ColumnOverride> columnOverrides;

    private String classpathEntry;

    public MybatisGeneratorBridge(String classPath) {
        this.classpathEntry = classPath;
    }


    public void generate() throws Exception {
        Context context = new Context(ModelType.CONDITIONAL);
        context.setId("myid");
        context.setTargetRuntime("MyBatis3");

        /** 识别 数据库关键字 **／
         context.addProperty("autoDelimitKeywords", "true");
         /** Table configuration **/
        context.addTableConfiguration(getTableConfiguration(context));
        /** jdbc 配置 **/
        context.setJdbcConnectionConfiguration(getJdbcConfig());
        /** java model **/
        context.setJavaModelGeneratorConfiguration(getModelConfig());
        /** Mapper configuration **/
        context.setSqlMapGeneratorConfiguration(getMapperConfig());
        /** DAO **/
        context.setJavaClientGeneratorConfiguration(getDaoConfig());
        // Comment
        context.setCommentGeneratorConfiguration(getCommentConfig());
        // set java file encoding
        context.addProperty(PropertyRegistry.CONTEXT_JAVA_FILE_ENCODING, generatorConfig.getEncoding());

        /** 加载插件 **/
        loadPlugin(true, context, serializablePlugin);
        loadPlugin(generatorConfig.isNeedToStringHashcodeEquals(), context, toStringPlugin);
        loadPlugin(generatorConfig.isNeedToStringHashcodeEquals(), context, equalsHashCodePlugin);
        loadPlugin(generatorConfig.isUseLombokPlugin(), context, lombokPlugin);
        loadPlugin(generatorConfig.isDaoRepository(), context, repositoryPlugin);
        loadPlugin(generatorConfig.isUseDaoPublicMethod(), context, daoExtendStylePlugin);
        loadPlugin(generatorConfig.isUseForUpdate(), context, mySQLForUpdatePlugin);

        loadPlugin(generatorConfig.isUseExample() && generatorConfig.isNeedPage(), context, mysqlLimitPlugin);


        if (generatorConfig.isUseJSR310()) {
            JavaTypeResolverConfiguration javaTypeResolverConfiguration = new JavaTypeResolverConfiguration();
            javaTypeResolverConfiguration.setConfigurationType("com.ifetch.cq.plugins.JavaTypeResolverImpl");
            context.setJavaTypeResolverConfiguration(javaTypeResolverConfiguration);
        }
        List<String> warnings = new ArrayList<>();
        Set<String> fullyqualifiedTables = new HashSet<>();
        Set<String> contexts = new HashSet<>();
        ShellCallback shellCallback = new DefaultShellCallback(true); // override=true

        Configuration configuration = new Configuration();
        configuration.addContext(context);
        configuration.addClasspathEntry(this.classpathEntry);

        MyBatisGenerator myBatisGenerator = new MyBatisGenerator(configuration, shellCallback, warnings);

        // if overrideXML selected, delete oldXML ang generate new one
        if (generatorConfig.isCoverXml()) {
            String mappingXMLFilePath = getMappingXMLFilePath(generatorConfig);
            File mappingXMLFile = new File(mappingXMLFilePath);
            if (mappingXMLFile.exists()) {
                mappingXMLFile.delete();
            }
        }

        myBatisGenerator.generate(progressCallback, contexts, fullyqualifiedTables);
    }


    private String getMappingXMLFilePath(GeneratorConfig generatorConfig) {
        StringBuilder sb = new StringBuilder();
        sb.append(generatorConfig.getXmlPath()).append("/");
        sb.append(generatorConfig.getXmlPackage()).append("/");
        if (!StringTools.isEmpty(generatorConfig.getMapperName())) {
            sb.append(generatorConfig.getMapperName()).append(".xml");
        } else {
            sb.append(generatorConfig.getEntityName()).append("Mapper.xml");
        }
        return sb.toString();
    }

    /**
     * 生成表字段描述配置
     *
     * @return
     */
    public CommentGeneratorConfiguration getCommentConfig() {
        CommentGeneratorConfiguration commentConfig = new CommentGeneratorConfiguration();
        commentConfig.setConfigurationType(DbRemarksCommentGenerator.class.getName());
        if (generatorConfig.isNeedComment()) {
            commentConfig.addProperty("columnRemarks", "true");
        }
        if (generatorConfig.isJpaAnnotation()) {
            commentConfig.addProperty("annotations", "true");
        }
        return commentConfig;
    }

    /**
     * 加载插件
     *
     * @param context
     */
    public void loadPlugin(boolean isLoad, Context context, Supplier<PluginConfiguration> supplier) {
        if (isLoad) {
            PluginConfiguration plugin = supplier.get();
            if (plugin != null) {
                context.addPluginConfiguration(plugin);
            }
        }
    }

    public JavaModelGeneratorConfiguration getModelConfig() {
        JavaModelGeneratorConfiguration modelConfig = new JavaModelGeneratorConfiguration();
        modelConfig.setTargetPackage(generatorConfig.getEntityPackage());
        modelConfig.setTargetProject(generatorConfig.getEntityPath());
        return modelConfig;
    }

    public JavaClientGeneratorConfiguration getDaoConfig() {
        JavaClientGeneratorConfiguration daoConfig = new JavaClientGeneratorConfiguration();
        daoConfig.setConfigurationType("XMLMAPPER");
        daoConfig.setTargetPackage(generatorConfig.getMapperPackage());
        daoConfig.setTargetProject(generatorConfig.getMapperPath());
        return daoConfig;
    }

    public SqlMapGeneratorConfiguration getMapperConfig() {
        SqlMapGeneratorConfiguration mapperConfig = new SqlMapGeneratorConfiguration();
        mapperConfig.setTargetPackage(generatorConfig.getXmlPackage());
        mapperConfig.setTargetProject(generatorConfig.getXmlPath());
        return mapperConfig;
    }

    public TableConfiguration getTableConfiguration(Context context) {
        TableConfiguration tableConfig = new TableConfiguration(context);
        tableConfig.setTableName(generatorConfig.getTableName());
        tableConfig.setDomainObjectName(generatorConfig.getEntityName());
        String dbType = selectedDatabaseConfig.getDbType();
        if (DbType.MySQL.name().equals(dbType)) {
            // 由于beginningDelimiter和endingDelimiter的默认值为双引号(")，在Mysql中不能这么写，所以还要将这两个默认值改为`
            context.addProperty("beginningDelimiter", "`");
            context.addProperty("endingDelimiter", "`");
        } else if (DbType.PostgreSQL.name().equals(dbType)) {
            // 针对 postgresql 单独配置
            tableConfig.setDelimitIdentifiers(true);
        }
        if (generatorConfig.isUseSchema()) {
            if (DbType.MySQL.name().equals(dbType)) {
                tableConfig.setSchema(selectedDatabaseConfig.getSchema());
            } else if (DbType.Oracle.name().equals(dbType)) {
                //Oracle的schema为用户名，如果连接用户拥有dba等高级权限，若不设schema，会导致把其他用户下同名的表也生成一遍导致mapper中代码重复
                tableConfig.setSchema(selectedDatabaseConfig.getUsername());
            } else {
                tableConfig.setCatalog(selectedDatabaseConfig.getSchema());
            }
        }
        //添加GeneratedKey主键生成
        if (!StringTools.isEmpty(generatorConfig.getId())) {
            String dbType2 = dbType;
            if (DbType.MySQL.name().equals(dbType2)) {
                dbType2 = "JDBC";
            }
            tableConfig.setGeneratedKey(new GeneratedKey(generatorConfig.getId(), dbType2, true, null));
        }

        if (generatorConfig.getMapperName() != null) {
            tableConfig.setMapperName(generatorConfig.getMapperName());
        }
        // add ignore columns
        if (ignoredColumns != null) {
            ignoredColumns.forEach(tableConfig::addIgnoredColumn);
        }
        if (columnOverrides != null) {
            columnOverrides.forEach(tableConfig::addColumnOverride);
        }
        if (generatorConfig.isUseActualColumnNames()) {
            tableConfig.addProperty("useActualColumnNames", "true");
        }
        if (!generatorConfig.isUseExample()) {
            tableConfig.setUpdateByExampleStatementEnabled(false);
            tableConfig.setCountByExampleStatementEnabled(false);
            tableConfig.setDeleteByExampleStatementEnabled(false);
            tableConfig.setSelectByExampleStatementEnabled(false);
        }
        return tableConfig;
    }

    public JDBCConnectionConfiguration getJdbcConfig() throws Exception {
        String dbType = selectedDatabaseConfig.getDbType();
        JDBCConnectionConfiguration jdbcConfig = new JDBCConnectionConfiguration();
        jdbcConfig.setDriverClass(DbType.valueOf(selectedDatabaseConfig.getDbType()).getDriverClass());
        jdbcConfig.setConnectionURL(DbTools.getConnectionUrlWithSchema(selectedDatabaseConfig));
        jdbcConfig.setUserId(selectedDatabaseConfig.getUsername());
        jdbcConfig.setPassword(selectedDatabaseConfig.getPassword());
        if (DbType.MySQL.name().equals(dbType)) {
            jdbcConfig.addProperty("nullCatalogMeansCurrent", "true");
        }
        if (DbType.Oracle.name().equals(dbType)) {
            jdbcConfig.getProperties().setProperty("remarksReporting", "true");
        }
        return jdbcConfig;
    }

    public void setProgressCallback(ProgressCallback progressCallback) {
        this.progressCallback = progressCallback;
    }

    public void setIgnoredColumns(List<IgnoredColumn> ignoredColumns) {
        this.ignoredColumns = ignoredColumns;
    }

    public void setColumnOverrides(List<ColumnOverride> columnOverrides) {
        this.columnOverrides = columnOverrides;
    }

    public void setDatabaseConfig(DatabaseConfig databaseConfig) {
        this.selectedDatabaseConfig = databaseConfig;
    }

    public void setGeneratorConfig(GeneratorConfig generatorConfig) {
        this.generatorConfig = generatorConfig;
    }

    /**
     * 加载Lombox插件
     *
     * @param context
     */
    public Supplier<PluginConfiguration> lombokPlugin = () -> {
        PluginConfiguration lombokPlugin = new PluginConfiguration();
        lombokPlugin.addProperty("type", "com.softwareloop.mybatis.generator.plugins.LombokPlugin");
        lombokPlugin.setConfigurationType("com.softwareloop.mybatis.generator.plugins.LombokPlugin");
        return lombokPlugin;
    };

    /**
     * 序列化插件
     */
    public Supplier<PluginConfiguration> serializablePlugin = () -> {
        PluginConfiguration serializablePluginConfiguration = new PluginConfiguration();
        serializablePluginConfiguration.addProperty("type", "org.mybatis.generator.plugins.SerializablePlugin");
        serializablePluginConfiguration.setConfigurationType("org.mybatis.generator.plugins.SerializablePlugin");
        return serializablePluginConfiguration;
    };

    public Supplier<PluginConfiguration> equalsHashCodePlugin = () -> {
        PluginConfiguration equalsHashCodePlugin = new PluginConfiguration();
        equalsHashCodePlugin.addProperty("type", "org.mybatis.generator.plugins.EqualsHashCodePlugin");
        equalsHashCodePlugin.setConfigurationType("org.mybatis.generator.plugins.EqualsHashCodePlugin");
        return equalsHashCodePlugin;
    };

    public Supplier<PluginConfiguration> toStringPlugin = () -> {
        PluginConfiguration toStringPlugin = new PluginConfiguration();
        toStringPlugin.addProperty("type", "org.mybatis.generator.plugins.ToStringPlugin");
        toStringPlugin.setConfigurationType("org.mybatis.generator.plugins.ToStringPlugin");
        return toStringPlugin;
    };

    public Supplier<PluginConfiguration> mysqlLimitPlugin = () -> {
        String dbType = selectedDatabaseConfig.getDbType();
        if (DbType.MySQL.name().equals(dbType) || DbType.PostgreSQL.name().equals(dbType)) {
            PluginConfiguration pluginConfiguration = new PluginConfiguration();
            pluginConfiguration.addProperty("type", "com.ifetch.cq.plugins.MySQLLimitPlugin");
            pluginConfiguration.setConfigurationType("com.ifetch.cq.plugins.MySQLLimitPlugin");
            return pluginConfiguration;
        }
        return null;
    };
    //repository 插件
    public Supplier<PluginConfiguration> repositoryPlugin = () -> {
        String dbType = selectedDatabaseConfig.getDbType();
        if (DbType.MySQL.name().equals(dbType) || DbType.PostgreSQL.name().equals(dbType)) {
            PluginConfiguration pluginConfiguration = new PluginConfiguration();
            pluginConfiguration.addProperty("type", "com.ifetch.cq.plugins.RepositoryPlugin");
            pluginConfiguration.setConfigurationType("com.ifetch.cq.plugins.RepositoryPlugin");
            return pluginConfiguration;
        }
        return null;
    };

    public Supplier<PluginConfiguration> daoExtendStylePlugin = () -> {
        String dbType = selectedDatabaseConfig.getDbType();
        if (DbType.MySQL.name().equals(dbType) || DbType.PostgreSQL.name().equals(dbType)) {
            PluginConfiguration pluginConfiguration = new PluginConfiguration();
            pluginConfiguration.addProperty("useExample", String.valueOf(generatorConfig.isUseExample()));
            pluginConfiguration.addProperty("type", "com.ifetch.cq.plugins.CommonDAOInterfacePlugin");
            pluginConfiguration.setConfigurationType("com.ifetch.cq.plugins.CommonDAOInterfacePlugin");
            return pluginConfiguration;
        }
        return null;
    };

    public Supplier<PluginConfiguration> mySQLForUpdatePlugin = () -> {
        String dbType = selectedDatabaseConfig.getDbType();
        if (DbType.MySQL.name().equals(dbType) || DbType.PostgreSQL.name().equals(dbType)) {
            PluginConfiguration pluginConfiguration = new PluginConfiguration();
            pluginConfiguration.addProperty("type", "com.ifetch.cq.plugins.MySQLForUpdatePlugin");
            pluginConfiguration.setConfigurationType("com.ifetch.cq.plugins.MySQLForUpdatePlugin");
            return pluginConfiguration;
        }
        return null;
    };

}
