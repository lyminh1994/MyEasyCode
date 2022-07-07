package com.sjhy.plugin.ui.base;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorSettings;

/**
 * Editor settings initialization
 *
 * @author makejava
 * @version 1.0.0
 * @since 2021/08/13 09:26
 */
public class EditorSettingsInit {

    private EditorSettingsInit() {
    }

    public static void init(Editor editor) {
        EditorSettings editorSettings = editor.getSettings();
        // Close virtual space
        editorSettings.setVirtualSpace(false);
        // Close marker position (breakpoint position)
        editorSettings.setLineMarkerAreaShown(false);
        // Close the abbreviated guide
        editorSettings.setIndentGuidesShown(false);
        // Show line number
        editorSettings.setLineNumbersShown(true);
        // Support for code folding
        editorSettings.setFoldingOutlineShown(true);
        // Additional row, additional column (improved view)
        editorSettings.setAdditionalColumnsCount(3);
        editorSettings.setAdditionalLinesCount(3);
        // Do not show line breaks
        editorSettings.setCaretRowShown(false);
    }

}
