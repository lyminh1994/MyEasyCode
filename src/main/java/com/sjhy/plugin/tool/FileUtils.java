package com.sjhy.plugin.tool;

import com.intellij.codeInsight.actions.AbstractLayoutCodeProcessor;
import com.intellij.codeInsight.actions.OptimizeImportsProcessor;
import com.intellij.codeInsight.actions.ReformatCodeProcessor;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.util.ExceptionUtil;
import com.sjhy.plugin.dict.GlobalDict;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.List;

/**
 * File tools
 *
 * @author makejava
 * @version 1.0.0
 * @since 2018/07/17 13:10
 */
public class FileUtils {
    private static final Logger LOG = Logger.getInstance(FileUtils.class);
    private static volatile FileUtils fileUtils;

    /**
     * Singleton pattern
     */
    public static FileUtils getInstance() {
        if (fileUtils == null) {
            synchronized (FileUtils.class) {
                if (fileUtils == null) {
                    fileUtils = new FileUtils();
                }
            }
        }
        return fileUtils;
    }

    private FileUtils() {
    }

    /**
     * Create subdirectories
     *
     * @param project File object
     * @param parent  Parent directory
     * @param dirName Subdirectory
     * @return Directory object
     */
    public VirtualFile createChildDirectory(Project project, VirtualFile parent, String dirName) {
        return WriteCommandAction.runWriteCommandAction(project, (Computable<VirtualFile>) () -> {
            try {
                return VfsUtil.createDirectoryIfMissing(parent, dirName);
            } catch (IOException e) {
                Messages.showWarningDialog("Directory creation failed：" + dirName, GlobalDict.TITLE_INFO);
                return null;
            }
        });
    }

    /**
     * Create sub file
     *
     * @param project  Project object
     * @param parent   Parent directory
     * @param fileName Sub file name
     * @return File object
     */
    public VirtualFile createChildFile(Project project, VirtualFile parent, String fileName) {
        return WriteCommandAction.runWriteCommandAction(project, (Computable<VirtualFile>) () -> {
            PsiManager psiManager = PsiManager.getInstance(project);
            try {
                PsiDirectory directory = psiManager.findDirectory(parent);
                if (directory != null) {
                    PsiFile psiFile = directory.createFile(fileName);
                    return psiFile.getVirtualFile();
                }
                return parent.createChildData(new Object(), fileName);
            } catch (IOException e) {
                Messages.showWarningDialog("File creation failed：" + fileName, GlobalDict.TITLE_INFO);
                return null;
            }
        });
    }

    /**
     * Set file content
     *
     * @param project Project object
     * @param file    Document
     * @param text    Document content
     * @return The overwritten document object
     */
    public Document writeFileContent(Project project, VirtualFile file, String fileName, String text) {
        FileDocumentManager fileDocumentManager = FileDocumentManager.getInstance();
        Document document = fileDocumentManager.getDocument(file);
        if (document == null) {
            WriteCommandAction.runWriteCommandAction(project, () -> {
                try {
                    file.setBinaryContent(text.getBytes());
                } catch (IOException e) {
                    throw new IllegalStateException("Binary file write failed, fileName：" + fileName);
                }
            });
            return fileDocumentManager.getDocument(file);
        }
        WriteCommandAction.runWriteCommandAction(project, () -> document.setText(text));
        PsiDocumentManager psiDocumentManager = PsiDocumentManager.getInstance(project);
        // Commit changes, not commit files in VCS
        psiDocumentManager.commitDocument(document);
        return document;
    }

    /**
     * Format virtual file
     *
     * @param project     Project object
     * @param virtualFile Dummy file
     */
    public void reformatFile(Project project, VirtualFile virtualFile) {
        PsiFile psiFile = PsiManager.getInstance(project).findFile(virtualFile);
        if (psiFile == null) {
            return;
        }
        reformatFile(project, Collections.singletonList(psiFile));
    }

    /**
     * Perform formatting
     *
     * @param project     Project object
     * @param psiFileList File List
     */
    @SuppressWarnings("unchecked")
    public void reformatFile(Project project, List<PsiFile> psiFileList) {
        if (CollectionUtil.isEmpty(psiFileList)) {
            return;
        }
        // Attempt to format the file
        AbstractLayoutCodeProcessor processor = new ReformatCodeProcessor(project, psiFileList.toArray(new PsiFile[0]), null, false);

        // Clean up the code for compatibility with older versions, the processor is not yet available in older versions of IDEA
        try {
            Class<AbstractLayoutCodeProcessor> codeCleanupCodeProcessorCls = (Class<AbstractLayoutCodeProcessor>) Class.forName("com.intellij.codeInsight.actions.CodeCleanupCodeProcessor");
            Constructor<AbstractLayoutCodeProcessor> constructor = codeCleanupCodeProcessorCls.getConstructor(AbstractLayoutCodeProcessor.class);
            processor = constructor.newInstance(processor);
        } catch (ClassNotFoundException ignored) {
            // Class does not exist directly ignore
        } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
            // Throw unknown exception
            ExceptionUtil.rethrow(e);
        }
        // Execute processing
        processor.run();
    }
}
