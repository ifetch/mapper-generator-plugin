package com.ifetch.cq.config;

import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

/**
 * Created by cq on 19-10-24.
 */
public class MyProjectComponent implements ProjectComponent {
    static Project project = null;
    private Logger logger = Logger.getInstance(MyProjectComponent.class);

    @Override
    public void projectOpened() {
        //通知一个project已经完成加载
        System.out.println("projectOpened");
        logger.info("这是一个mapper 生成插件-一棵松");
    }

    @Override
    public void projectClosed() {
        System.out.println("projectClosed");
    }

    @Override
    public void initComponent() {
        //执行初始化操作以及与其他 components 的通信
        System.out.println("initComponent");
    }

    @Override
    public void disposeComponent() {
        //释放系统资源或执行其他清理
        System.out.println("disposeComponent");
    }

    @NotNull
    @Override
    public String getComponentName() {
        System.out.println("getComponentName");
        return "generatorMapper.myProjectComponent";
    }


    public MyProjectComponent(Project project) {
        this.project = project;
    }

    public Project getProject() {
        return project;
    }

    public static MyProjectComponent newInstance() {
        return ServiceManager.getService(project, MyProjectComponent.class);
    }
}
