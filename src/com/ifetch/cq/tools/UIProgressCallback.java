package com.ifetch.cq.tools;

import org.mybatis.generator.api.ProgressCallback;

/**
 * Created by Owen on 6/21/16.
 */
public class UIProgressCallback implements ProgressCallback {

    private String progressText;

    public UIProgressCallback() {
    }

    @Override
    public void introspectionStarted(int totalTasks) {
        this.progressText = "开始代码检查";

    }

    @Override
    public void generationStarted(int totalTasks) {
        this.progressText = "开始代码生成";
    }

    @Override
    public void saveStarted(int totalTasks) {
        this.progressText = "开始保存生成的文件";
    }

    @Override
    public void startTask(String taskName) {
        this.progressText = "代码生成任务开始";
    }

    @Override
    public void done() {
        this.progressText = "代码生成完成";
    }

    @Override
    public void checkCancel() throws InterruptedException {
    }

    public String getProgressText() {
        return progressText;
    }
}
