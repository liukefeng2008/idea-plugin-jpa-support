package com.ifengxue.plugin.gui;

import com.ifengxue.plugin.Constants;
import com.ifengxue.plugin.component.ColumnFieldMappingEditor;
import com.ifengxue.plugin.entity.Column;
import com.ifengxue.plugin.entity.Table;
import com.ifengxue.plugin.gui.table.TableFactory;
import com.ifengxue.plugin.i18n.LocaleContextHolder;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import javax.swing.JComponent;
import org.jetbrains.annotations.Nullable;

public class ColumnFieldMappingEditorDialog extends DialogWrapper {

  private final ColumnFieldMappingEditor columnFieldMappingEditor;
  private final Table table;

  protected ColumnFieldMappingEditorDialog(@Nullable Project project, boolean canBeParent,
      Table table) {
    super(project, canBeParent);
    this.table = table;
    columnFieldMappingEditor = new ColumnFieldMappingEditor();
    init();
    setTitle(LocaleContextHolder.format("column_field_mapping_title"));

    new TableFactory()
        .decorateTable(columnFieldMappingEditor.getTableMapping(), Column.class, this.table.getColumns());
  }

  @Nullable
  @Override
  protected JComponent createCenterPanel() {
    return columnFieldMappingEditor.getRootComponent();
  }

  @Nullable
  @Override
  protected String getDimensionServiceKey() {
    return Constants.NAME + ":" + getClass().getName();
  }
}
