package com.ifengxue.plugin.component;

import com.ifengxue.plugin.util.Editors;
import com.intellij.lang.java.JavaLanguage;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.ui.LanguageTextField;
import com.intellij.ui.ScrollPaneFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import lombok.Data;

@Data
public class SourceCodeViewer {

    private JPanel rootComponent;
    private JPanel sourceCodePanel;
    private LanguageTextField txtSourceCode;

    public SourceCodeViewer() {
        Project defaultProject = ProjectManager.getInstance().getDefaultProject();
        txtSourceCode = new LanguageTextField(JavaLanguage.INSTANCE, defaultProject, "",
            (value, language, project) -> Editors
                .createSourceEditor(project, JavaLanguage.INSTANCE, value, false)
                .getDocument(), false);
        JScrollPane scrollPane = ScrollPaneFactory.createScrollPane(txtSourceCode);
        sourceCodePanel.add(scrollPane);
    }

}
