package com.ifetch.cq.action;

import com.ifetch.cq.view.MapperGeneratorDialogImpl;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;

/**
 * Created by cq on 19-10-20.
 */
public class MybatisGeneratorPlugin extends AnAction {
    private static final Logger _LOG = Logger.getInstance(MybatisGeneratorPlugin.class);

    @Override
    public void actionPerformed(AnActionEvent event) {
        // TODO: insert action logic here
        Project project = event.getProject();
        if (project != null) {
            try {
                MapperGeneratorDialogImpl mainView = new MapperGeneratorDialogImpl(project);
                mainView.show();
            } catch (Exception e) {
                _LOG.error("mapper generator plugin error e:" + e.getMessage(), e);
            }
        } else {
            _LOG.error("mapper generator plugin error 未选中项目");
        }
    }
}
