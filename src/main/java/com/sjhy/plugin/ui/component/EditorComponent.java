package com.sjhy.plugin.ui.component;

import com.intellij.ide.IdeBundle;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.editor.highlighter.EditorHighlighterFactory;
import com.intellij.openapi.editor.impl.EditorImpl;
import com.intellij.openapi.ui.Splitter;
import com.intellij.ui.BrowserHyperlinkListener;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.ui.SeparatorFactory;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import com.sjhy.plugin.entity.AbstractEditorItem;
import com.sjhy.plugin.tool.ProjectUtils;
import com.sjhy.plugin.ui.base.EditorSettingsInit;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

/**
 * Editor component
 *
 * @author makejava
 * @version 1.0.0
 * @since 2021/08/11 13:16
 */
public class EditorComponent<T extends AbstractEditorItem<?>> {
    /**
     * Main panel
     */
    @Getter
    private JPanel mainPanel;
    /**
     * Edited file
     */
    @Getter
    private T file;
    /**
     * Description
     */
    private String remark;

    /**
     * Editor component
     */
    private Editor editor;

    public EditorComponent(T file, String remark) {
        this.file = file;
        this.remark = remark;
        this.init();
    }

    public void init() {
        this.mainPanel = new JPanel(new BorderLayout());
        EditorFactory editorFactory = EditorFactory.getInstance();
        Document document = editorFactory.createDocument("");
        this.editor = editorFactory.createEditor(document);
        this.refreshUI();
        // Initial default settings
        EditorSettingsInit.init(this.editor);
        // Add monitoring events
        this.editor.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void beforeDocumentChange(@NotNull DocumentEvent event) {
                // document why this method is empty
            }

            @Override
            public void documentChanged(@NotNull DocumentEvent event) {
                if (file != null) {
                    file.changeFileContent(editor.getDocument().getText());
                }
            }
        });
        // Initialize the description panel
        this.initRemarkPanel();
    }

    private void initRemarkPanel() {
        // Description
        JEditorPane editorPane = new JEditorPane();
        // Html form display
        editorPane.setEditorKit(UIUtil.getHTMLEditorKit());
        // View only
        editorPane.setEditable(false);
        editorPane.setText(remark);
        // Add browser link listener event
        editorPane.addHyperlinkListener(new BrowserHyperlinkListener());

        // Description panel
        JPanel descriptionPanel = new JPanel(new GridBagLayout());
        descriptionPanel.add(SeparatorFactory.createSeparator(IdeBundle.message("label.description"), null),
                new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
                        JBUI.insetsBottom(2), 0, 0));
        descriptionPanel.add(ScrollPaneFactory.createScrollPane(editorPane),
                new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        JBUI.insetsTop(2), 0, 0));

        // Splitter
        Splitter splitter = new Splitter(true, 0.6F);
        splitter.setFirstComponent(editor.getComponent());
        splitter.setSecondComponent(descriptionPanel);

        this.mainPanel.add(splitter, BorderLayout.CENTER);
        this.mainPanel.setPreferredSize(JBUI.size(400, 300));
    }

    public void setFile(T file) {
        this.file = file;
        this.refreshUI();
    }

    private void refreshUI() {
        if (this.file == null) {
            ((EditorImpl)this.editor).setViewer(true);
            // Reset text content
            WriteCommandAction.runWriteCommandAction(ProjectUtils.getCurrProject(), () -> this.editor.getDocument().setText(""));
            ((EditorEx)editor).setHighlighter(EditorHighlighterFactory.getInstance().createEditorHighlighter(null, "demo.java.vm"));
        } else {
            ((EditorImpl)this.editor).setViewer(false);
            // Reset text content
            WriteCommandAction.runWriteCommandAction(ProjectUtils.getCurrProject(), () -> this.editor.getDocument().setText(this.file.fileContent()));
            ((EditorEx)editor).setHighlighter(EditorHighlighterFactory.getInstance().createEditorHighlighter(ProjectUtils.getCurrProject(), this.file.fileName()));
        }
    }
}
