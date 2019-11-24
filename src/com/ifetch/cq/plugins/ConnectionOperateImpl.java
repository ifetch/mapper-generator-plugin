package com.ifetch.cq.plugins;

import com.ifetch.cq.model.DatabaseConfig;
import com.ifetch.cq.config.DatabaseSettings;
import com.ifetch.cq.tools.StringTools;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Predicate;

/**
 * Created by cq on 19-10-31.
 */
public class ConnectionOperateImpl implements ConnectionOperate {

    static final Logger _LOG = Logger.getInstance(ConnectionOperateImpl.class);
    final ReadWriteLock lock = new ReentrantReadWriteLock();
    final Lock writeLock = lock.writeLock();
    final Lock readLock = lock.writeLock();

    DatabaseSettings databaseSettings;

    public ConnectionOperateImpl(Project project) {
        databaseSettings = ServiceManager.getService(project, DatabaseSettings.class);
        _LOG.info("ConnectionOperateImpl");
    }

    @Override
    public boolean create(DatabaseConfig config) {
        if (!predicate.test(config) && config.getId() != null) {
            return false;
        }
        List<DatabaseConfig> configs = query();
        for (DatabaseConfig item : configs) {
            if (config.getName().equals(item.getName())) {
                return false;
            }
        }
        try {
            if (writeLock.tryLock()) {
                config.setId(System.currentTimeMillis());
                configs.add(config);
                Map<String, DatabaseConfig> configMap = new HashMap<>();
                for (DatabaseConfig item : configs) {
                    configMap.put(item.getName(), item);
                }
                databaseSettings.setConnections(configMap);
                return true;
            }
        } finally {
            writeLock.unlock();
        }
        return false;
    }

    @Override
    public boolean delete(Long id) {
        List<DatabaseConfig> configs = query();
        if (configs == null || configs.isEmpty()) {
            return false;
        }
        DatabaseConfig config = null;
        for (DatabaseConfig item : configs) {
            if (item.getId() == id) {
                config = item;
                break;
            }
        }
        try {
            if (writeLock.tryLock() && config != null) {
                configs.remove(config);
                Map<String, DatabaseConfig> configMap = new HashMap<>();
                for (DatabaseConfig item : configs) {
                    configMap.put(item.getName(), item);
                }
                databaseSettings.setConnections(configMap);
                return true;
            }
        } finally {
            writeLock.unlock();
        }
        return false;
    }

    @Override
    public boolean connection() {

        return false;
    }

    @Override
    public boolean edit(DatabaseConfig config) {
        if (!predicate.test(config) || config.getId() == null) {
            return false;
        }
        List<DatabaseConfig> configs = query();
        if (config == null || configs.isEmpty()) {
            return false;
        }
        int index = -1;
        for (int i = 0; i < configs.size(); i++) {
            if (config.getId() == configs.get(i).getId()) {
                index = i;
                break;
            }
        }
        try {
            if (writeLock.tryLock()) {
                if (index >= 0) {
                    configs.remove(config);
                    configs.add(index, config);
                }
                return true;
            }
        } finally {
            writeLock.unlock();
        }
        return false;
    }

    @Override
    public List<DatabaseConfig> query() {
        List<DatabaseConfig> configs = new ArrayList<>();
        Map<String, DatabaseConfig> configMap = databaseSettings.getConfigList();
        if (configMap == null || configMap.isEmpty()) {
            return configs;
        }
        try {
            if (readLock.tryLock()) {
                configMap.forEach((k, v) -> configs.add(v));
                return configs;
            }
        } finally {
            readLock.unlock();
        }
        return configs;
    }

    private static Predicate<DatabaseConfig> predicate = (config) -> {
        if (config == null) {
            return false;
        } else if (StringTools.isEmpty(config.getName())) {
            return false;
        } else if (StringTools.isEmpty(config.getDbType())) {
            return false;
        } else if (StringTools.isEmpty(config.getHost())) {
            return false;
        } else if (StringTools.isEmpty(config.getPort())) {
            return false;
        } else if (StringTools.isEmpty(config.getSchema())) {
            return false;
        }
        return true;
    };
}
