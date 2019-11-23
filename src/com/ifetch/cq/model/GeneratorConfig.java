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

    private boolean needPage;

    private boolean needComment;

    private boolean needToStringHashcodeEquals;

    private boolean jpaAnnotation;

    /**
     * 使用实际列名
     */
    private boolean useActualColumnNames;

    private String encoding;

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

    public boolean isNeedToStringHashcodeEquals() {
        return needToStringHashcodeEquals;
    }

    public void setNeedToStringHashcodeEquals(boolean needToStringHashcodeEquals) {
        this.needToStringHashcodeEquals = needToStringHashcodeEquals;
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
}
