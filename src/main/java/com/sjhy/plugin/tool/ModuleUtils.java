package com.sjhy.plugin.tool;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.sjhy.plugin.dict.GlobalDict;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jps.model.java.JavaSourceRootType;

import java.util.List;

/**
 * Module tool class
 *
 * @author makejava
 * @version 1.0.0
 * @since 2018/09/01 17:15
 */
public final class ModuleUtils {
    /**
     * Disable constructor
     */
    private ModuleUtils() {
        throw new UnsupportedOperationException();
    }

    /**
     * Get the module path
     *
     * @param module Module
     * @return Path
     */
    public static VirtualFile getModuleDir(@NotNull Module module) {
        String modulePath = ModuleUtil.getModuleDirPath(module);
        // Uniform Path Splitting Symbol
        modulePath = modulePath.replace("\\", "/");
        // Try to eliminate incorrect paths
        if (modulePath.contains(".idea/modules/")) {
            modulePath = modulePath.replace(".idea/modules/","");
        }
        if (modulePath.contains(".idea/modules")) {
            modulePath = modulePath.replace(".idea/modules","");
        }
        if (modulePath.contains("/.idea")) {
            modulePath = modulePath.replace("/.idea","");
        }
        VirtualFile dir = VirtualFileManager.getInstance().findFileByUrl(String.format("file://%s", modulePath));
        if (dir == null) {
            Messages.showInfoMessage("Unable to get Module path, path=" + modulePath, GlobalDict.TITLE_INFO);
        }
        return dir;
    }

    /**
     * Get the source code folder of the module, does not exist
     *
     * @param module Module object
     * @return Folder path
     */
    public static VirtualFile getSourcePath(@NotNull Module module) {
        List<VirtualFile> virtualFileList = ModuleRootManager.getInstance(module).getSourceRoots(JavaSourceRootType.SOURCE);
        if (CollectionUtil.isEmpty(virtualFileList)) {
            VirtualFile modulePath = getModuleDir(module);
            // Try to intelligently identify the source code path (through the above method, IDEA cannot get the source code path 100%)
            VirtualFile srcDir = VfsUtil.findRelativeFile(modulePath, "src", "main", "java");
            if (srcDir != null && srcDir.isDirectory()) {
                return srcDir;
            }
            return modulePath;
        }
        if (virtualFileList.size() > 1) {
            for (VirtualFile file : virtualFileList) {
                String tmpPath = file.getPath();
                if (!tmpPath.contains("build") && !tmpPath.contains("generated")) {
                    return file;
                }
            }
        }
        return virtualFileList.get(0);
    }

    /**
     * Determine if a module exists in a source code folder
     *
     * @param module Module object
     * @return Does it exist
     */
    public static boolean existsSourcePath(Module module) {
        return !CollectionUtil.isEmpty(ModuleRootManager.getInstance(module).getSourceRoots(JavaSourceRootType.SOURCE));
    }
}
