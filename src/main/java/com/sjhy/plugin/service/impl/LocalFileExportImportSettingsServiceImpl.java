package com.sjhy.plugin.service.impl;

import com.intellij.ide.actions.OpenFileAction;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.fileChooser.*;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.impl.LoadTextUtil;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileWrapper;
import com.intellij.util.ExceptionUtil;
import com.sjhy.plugin.dict.GlobalDict;
import com.sjhy.plugin.dto.SettingsStorageDTO;
import com.sjhy.plugin.service.ExportImportSettingsService;
import com.sjhy.plugin.tool.JSON;
import com.sjhy.plugin.tool.ProjectUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Local file import and export settings service implementation
 *
 * @author makejava
 * @version 1.0.0
 * @since 2021/08/11 17:28
 */
public class LocalFileExportImportSettingsServiceImpl implements ExportImportSettingsService {
    /**
     * Export settings
     *
     * @param settingsStorage Settings to export
     */
    @Override
    public void exportConfig(SettingsStorageDTO settingsStorage) {
        // 1.Choose a storage location
        FileSaverDialog saveFileDialog = FileChooserFactory.getInstance().createSaveFileDialog(new FileSaverDescriptor("Save Config As Json", "Save to"), ProjectUtils.getCurrProject());
        VirtualFileWrapper saveFile = saveFileDialog.save((VirtualFile) null, "EasyCodeConfig.json");
        if (saveFile == null) {
            return;
        }
        File file = saveFile.getFile();
        // 2.Perform export
        FileUtil.createIfDoesntExist(file);
        WriteCommandAction.runWriteCommandAction(ProjectUtils.getCurrProject(), () -> {
            try {
                byte[] bytes = JSON.toJsonByFormat(settingsStorage).getBytes(StandardCharsets.UTF_8);
                VirtualFile virtualFile = VfsUtil.findFileByIoFile(file, true);
                if (virtualFile != null) {
                    virtualFile.setBinaryContent(bytes);
                    FileDocumentManager.getInstance().reloadFiles(virtualFile);
                }

                // Initiate notification
                Notification notification = new Notification(
                        Notifications.SYSTEM_MESSAGES_GROUP_ID,
                        "Easy code notify",
                        "Easy code config file export to",
                        NotificationType.INFORMATION);
                notification.addAction(new AnAction(file.getName()) {
                    @Override
                    public void actionPerformed(@NotNull AnActionEvent e) {
                        // Open a file
                        if (virtualFile != null) {
                            OpenFileAction.openFile(virtualFile, ProjectUtils.getCurrProject());
                        }
                    }
                });
                Notifications.Bus.notify(notification, ProjectUtils.getCurrProject());
            } catch (IOException e) {
                ExceptionUtil.rethrow(e);
            }
        });
    }

    /**
     * Import settings
     *
     * @return Setup information
     */
    @Override
    public SettingsStorageDTO importConfig() {
        VirtualFile virtualFile = FileChooser.chooseFile(FileChooserDescriptorFactory.createSingleFileDescriptor("json"), ProjectUtils.getCurrProject(), null);
        if (virtualFile == null) {
            Messages.showWarningDialog("Config file not found！", GlobalDict.TITLE_INFO);
            return null;
        }
        String json = LoadTextUtil.loadText(virtualFile).toString();
        return JSON.parse(json, SettingsStorageDTO.class);
    }
}
