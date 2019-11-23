package com.ifetch.cq.config;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleComponent;
import com.intellij.openapi.module.ModuleManager;
import org.jetbrains.annotations.NotNull;

/**
 * Created by cq on 19-10-24.
 */
public class MyModuleComponent implements ModuleComponent {
    private Module module;

    @Override
    public void projectOpened() {
        System.out.println("projectOpened");
    }

    @Override
    public void projectClosed() {
        System.out.println("projectClosed");
    }

    @Override
    public void moduleAdded() {
        //用于通知 module 已经被添加到 project 中
        System.out.println("moduleAdded");
    }

    @Override
    public void initComponent() {
        System.out.println("initComponent");
    }

    @Override
    public void disposeComponent() {
        System.out.println("disposeComponent");
    }

    @NotNull
    @Override
    public String getComponentName() {
        System.out.println("getComponentName");
        return "generatorMapper.myModuleComponent";
    }

    public MyModuleComponent(Module module) {
        this.module = module;
    }

    public Module getModule() {
        return module;
    }

    public Module getModule(String name) {
        return ModuleManager.getInstance(module.getProject()).findModuleByName(name);
    }

    public Module[] getModules() {
        return ModuleManager.getInstance(module.getProject()).getModules();
    }

}
