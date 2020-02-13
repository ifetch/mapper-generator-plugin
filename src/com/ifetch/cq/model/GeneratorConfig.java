package com.ifetch.cq.model;

import java.io.Serializable;

/**
 * GeneratorConfig is the Config of mybatis generator config exclude database
 * config
 * <p>
 * Created by Owen on 6/16/16.
 */
public class GeneratorConfig implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;

    private String tableName;

    private String projectPath;

    private String entityName;

    private String entityPath;

    private String entityPackage;

    private String mapperName;

    private String mapperPath;

    private String mapperPackage;

    private String xmlPath;

    private String xmlPackage;

    private boolean useExample;

    private boolean needPage;

    private boolean needComment;

    private boolean coverXml;

    private boolean useLombokPlugin;

    private boolean needToStringHashcodeEquals;

    private boolean useJSR310;

    private boolean useForUpdate;

    private boolean useDaoPublicMethod;

    private boolean daoRepository;

    private boolean jpaAnnotation;

    /**
     * 使用实际列名
     */
    private boolean useActualColumnNames;

    private String encoding;

    private boolean useSchema;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getProjectPath() {
        return projectPath;
    }

    public void setProjectPath(String projectPath) {
        this.projectPath = projectPath;
    }

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    public String getEntityPath() {
        return entityPath;
    }

    public void setEntityPath(String entityPath) {
        this.entityPath = entityPath;
    }

    public String getEntityPackage() {
        return entityPackage;
    }

    public void setEntityPackage(String entityPackage) {
        this.entityPackage = entityPackage;
    }

    public String getMapperName() {
        return mapperName;
    }

    public void setMapperName(String mapperName) {
        this.mapperName = mapperName;
    }

    public String getMapperPath() {
        return mapperPath;
    }

    public void setMapperPath(String mapperPath) {
        this.mapperPath = mapperPath;
    }

    public String getMapperPackage() {
        return mapperPackage;
    }

    public void setMapperPackage(String mapperPackage) {
        this.mapperPackage = mapperPackage;
    }

    public String getXmlPath() {
        return xmlPath;
    }

    public void setXmlPath(String xmlPath) {
        this.xmlPath = xmlPath;
    }

    public String getXmlPackage() {
        return xmlPackage;
    }

    public void setXmlPackage(String xmlPackage) {
        this.xmlPackage = xmlPackage;
    }

    public boolean isUseExample() {
        return useExample;
    }

    public void setUseExample(boolean useExample) {
        this.useExample = useExample;
    }

    public boolean isNeedPage() {
        return needPage;
    }

    public void setNeedPage(boolean needPage) {
        this.needPage = needPage;
    }

    public boolean isNeedComment() {
        return needComment;
    }

    public void setNeedComment(boolean needComment) {
        this.needComment = needComment;
    }

    public boolean isCoverXml() {
        return coverXml;
    }

    public void setCoverXml(boolean coverXml) {
        this.coverXml = coverXml;
    }

    public boolean isUseLombokPlugin() {
        return useLombokPlugin;
    }

    public void setUseLombokPlugin(boolean useLombokPlugin) {
        this.useLombokPlugin = useLombokPlugin;
    }

    public boolean isNeedToStringHashcodeEquals() {
        return needToStringHashcodeEquals;
    }

    public void setNeedToStringHashcodeEquals(boolean needToStringHashcodeEquals) {
        this.needToStringHashcodeEquals = needToStringHashcodeEquals;
    }

    public boolean isUseJSR310() {
        return useJSR310;
    }

    public void setUseJSR310(boolean useJSR310) {
        this.useJSR310 = useJSR310;
    }

    public boolean isUseForUpdate() {
        return useForUpdate;
    }

    public void setUseForUpdate(boolean useForUpdate) {
        this.useForUpdate = useForUpdate;
    }

    public boolean isUseDaoPublicMethod() {
        return useDaoPublicMethod;
    }

    public void setUseDaoPublicMethod(boolean useDaoPublicMethod) {
        this.useDaoPublicMethod = useDaoPublicMethod;
    }

    public boolean isDaoRepository() {
        return daoRepository;
    }

    public void setDaoRepository(boolean daoRepository) {
        this.daoRepository = daoRepository;
    }

    public boolean isJpaAnnotation() {
        return jpaAnnotation;
    }

    public void setJpaAnnotation(boolean jpaAnnotation) {
        this.jpaAnnotation = jpaAnnotation;
    }

    public boolean isUseActualColumnNames() {
        return useActualColumnNames;
    }

    public void setUseActualColumnNames(boolean useActualColumnNames) {
        this.useActualColumnNames = useActualColumnNames;
    }

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public boolean isUseSchema() {
        return useSchema;
    }

    public void setUseSchema(boolean useSchema) {
        this.useSchema = useSchema;
    }
}
