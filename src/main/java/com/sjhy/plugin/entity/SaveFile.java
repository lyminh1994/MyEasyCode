package com.sjhy.plugin.entity;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.testFramework.LightVirtualFile;
import com.sjhy.plugin.dto.GenerateOptions;
import com.sjhy.plugin.tool.CompareFileUtils;
import com.sjhy.plugin.tool.FileUtils;
import com.sjhy.plugin.tool.MessageDialogUtils;
import com.sjhy.plugin.tool.ProjectUtils;
import lombok.Data;
import lombok.NonNull;

import java.io.File;

/**
 * File to save
 * <p>
 * If the file is saved in the project path, use the psi object provided by idea to operate. If the file is saved in a
 * non-project path, use java raw IO stream operations.
 *
 * @author makejava
 * @version 1.0.0
 * @since 2020/04/20 22:54
 */
@Data
public class SaveFile {

    private static final Logger LOG = Logger.getInstance(SaveFile.class);
    /**
     * View project
     */
    private Project project;
    /**
     * Document content
     */
    private String content;
    /**
     * File tools
     */
    private FileUtils fileUtils = FileUtils.getInstance();
    /**
     * Callback object
     */
    private Callback callback;
    /**
     * Build configuration
     */
    private GenerateOptions generateOptions;

    /**
     * Save document
     *
     * @param project         Project
     * @param content         Content
     * @param callback        Callback
     * @param generateOptions Build options
     */
    public SaveFile(@NonNull Project project, @NonNull String content, @NonNull Callback callback, @NonNull GenerateOptions generateOptions) {
        this.project = project;
        this.callback = callback;
        this.content = content.replace("\r", "");
        this.generateOptions = generateOptions;
    }

    /**
     * Is the file a project file
     *
     * @return Is it a project file
     */
    private boolean isProjectFile() {
        VirtualFile baseDir = ProjectUtils.getBaseDir(project);
        // The project base directory cannot be obtained, it may be the Default project, and the non-project file is returned directly
        if (baseDir == null) {
            return false;
        }
        // Path comparison, to determine whether the project path is a sub-path of the file save path
        String projectPath = handlerPath(baseDir.getPath());
        String tmpFilePath = handlerPath(callback.getSavePath());
        if (tmpFilePath.length() > projectPath.length()) {
            if (!"/".equals(tmpFilePath.substring(projectPath.length(), projectPath.length() + 1))) {
                return false;
            }
        }
        return tmpFilePath.indexOf(projectPath) == 0;
    }

    /**
     * Process paths, unify separators and convert to lowercase
     *
     * @param path Path
     * @return Processed path
     */
    private String handlerPath(String path) {
        return handlerPath(path, true);
    }

    /**
     * Process paths, unify separators and convert to lowercase
     *
     * @param path      Path
     * @param lowerCase Whether to lowercase
     * @return Processed path
     */
    private String handlerPath(String path, boolean lowerCase) {
        // Uniform Separator
        path = path.replace("\\", "/");
        // Avoid repeating delimiters
        path = path.replace("//", "/");
        // Uniform lowercase
        return lowerCase ? path.toLowerCase() : path;
    }

    /**
     * Write through the Psi file that comes with IDEA
     */
    public void write() {
        if (!Boolean.TRUE.equals(callback.getWriteFile())) {
            return;
        }
        // Check if a directory exists
        VirtualFile baseDir = ProjectUtils.getBaseDir(project);
        if (baseDir == null) {
            throw new IllegalStateException("Project base path does not exist");
        }
        // Handling save paths
        String savePath = handlerPath(callback.getSavePath(), false);
        if (isProjectFile()) {
            // Remove the previous part of the save path
            savePath = savePath.substring(handlerPath(baseDir.getPath()).length());
        } else {
            baseDir = null;
        }
        // Delete beginning and end/符号
        while (savePath.startsWith("/")) {
            savePath = savePath.substring(1);
        }
        while (savePath.endsWith("/")) {
            savePath = savePath.substring(0, savePath.length() - 1);
        }
        // Find out if the save directory exists
        VirtualFile saveDir;
        if (baseDir == null) {
            saveDir = VfsUtil.findFileByIoFile(new File(savePath), false);
        } else {
            saveDir = VfsUtil.findRelativeFile(baseDir, savePath.split("/"));
        }
        // Prompt to create a directory
        VirtualFile directory = titleCreateDir(saveDir, baseDir, savePath);
        if (directory == null) {
            return;
        }
        VirtualFile psiFile = directory.findChild(callback.getFileName());
        // Save or overwrite
        saveOrReplaceFile(psiFile, directory);
    }

    /**
     * Prompt to create a directory
     *
     * @param saveDir Save route
     * @return Whether to give up execution
     */
    private VirtualFile titleCreateDir(VirtualFile saveDir, VirtualFile baseDir, String savePath) {
        if (saveDir != null) {
            return saveDir;
        }
        // Try to create directory
        String msg = String.format("Directory %s Not Found, Confirm Create?", callback.getSavePath());
        if (Boolean.TRUE.equals(generateOptions.getTitleSure())) {
            saveDir = fileUtils.createChildDirectory(project, baseDir, savePath);
            return saveDir;
        } else if (Boolean.TRUE.equals(generateOptions.getTitleRefuse())) {
            return null;
        } else {
            if (MessageDialogUtils.yesNo(project, msg)) {
                saveDir = fileUtils.createChildDirectory(project, baseDir, savePath);
                return saveDir;
            }
        }
        return null;
    }

    /**
     * Save or replace file
     *
     * @param file      Document
     * @param directory Content
     */
    private void saveOrReplaceFile(VirtualFile file, VirtualFile directory) {
        PsiDocumentManager psiDocumentManager = PsiDocumentManager.getInstance(project);
        Document document;
        // The file does not exist and create it directly
        if (file == null) {
            file = fileUtils.createChildFile(project, directory, callback.getFileName());
            if (file == null) {
                return;
            }
            document = coverFile(file);
        } else {
            // Prompt to overwrite files
            if (Boolean.TRUE.equals(generateOptions.getTitleSure())) {
                // Default is
                document = coverFile(file);
            } else if (Boolean.TRUE.equals(generateOptions.getTitleRefuse())) {
                // No by default
                return;
            } else {
                String msg = String.format("File %s Exists, Select Operate Mode?", file.getPath());
                int result = MessageDialogUtils.yesNoCancel(project, msg, "Convert", "Compare", "Cancel");
                switch (result) {
                    case Messages.YES:
                        // Overwrite file
                        document = coverFile(file);
                        break;
                    case Messages.NO:
                        // Also format code when comparing code
                        String newText = content;
                        if (Boolean.TRUE.equals(callback.getReformat())) {
                            // Keep the old file content, overwrite the old file with the new file to perform formatting, and then restore the old file content
                            String oldText = getFileText(file);
                            Document tmpDoc = coverFile(file);
                            // Format code
                            FileUtils.getInstance().reformatFile(project, file);
                            // Commit document changes, not commit files in VCS
                            psiDocumentManager.commitDocument(tmpDoc);
                            // Get new file content
                            newText = getFileText(file);
                            // Restore old files
                            coverFile(file, oldText);
                        }
                        FileType fileType = FileTypeManager.getInstance().getFileTypeByFileName(callback.getFileName());
                        CompareFileUtils.showCompareWindow(project, file, new LightVirtualFile(callback.getFileName(), fileType, newText));
                        return;
                    case Messages.CANCEL:
                    default:
                        return;
                }
            }
        }
        // Perform code formatting operations
        if (Boolean.TRUE.equals(callback.getReformat())) {
            FileUtils.getInstance().reformatFile(project, file);
        }
        // Commit document changes, not commit files in VCS
        if (document != null) {
            psiDocumentManager.commitDocument(document);
        }
    }

    private String getFileText(VirtualFile file) {
        FileDocumentManager fileDocumentManager = FileDocumentManager.getInstance();
        Document document = fileDocumentManager.getDocument(file);
        if (document == null) {
            throw new IllegalStateException("virtual file to document failure");
        }
        return document.getText();
    }

    /**
     * Overwrite file
     *
     * @param file Document
     * @return The overwritten document object
     */
    private Document coverFile(VirtualFile file) {
        return coverFile(file, content);
    }

    /**
     * Overwrite file
     *
     * @param file Document
     * @param text Document content
     * @return The overwritten document object
     */
    private Document coverFile(VirtualFile file, String text) {
        return FileUtils.getInstance().writeFileContent(project, file, callback.getFileName(), text);
    }
}
