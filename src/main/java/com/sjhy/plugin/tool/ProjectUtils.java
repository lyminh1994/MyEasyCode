package com.sjhy.plugin.tool;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.WindowManager;

import java.awt.*;

/**
 * IDEA project related tools
 *
 * @author tangcent
 * @version 1.0.0
 * @since 2020/02/14 18:35
 */
public class ProjectUtils {

    private ProjectUtils() {
    }

    /**
     * Get the current item object
     *
     * @return Current project object
     */
    public static Project getCurrProject() {
        ProjectManager projectManager = ProjectManager.getInstance();
        Project[] openProjects = projectManager.getOpenProjects();
        if (openProjects.length == 0) {
            // When no project is opened, it will appear when entering the settings page
            return projectManager.getDefaultProject();
        } else if (openProjects.length == 1) {
            // If there is only one open project, use the open project
            return openProjects[0];
        }

        // If a project window is active
        try {
            WindowManager wm = WindowManager.getInstance();
            for (Project project : openProjects) {
                Window window = wm.suggestParentWindow(project);
                if (window != null && window.isActive()) {
                    return project;
                }
            }
        } catch (Exception ignored) {
            // ignored
        }

        // Otherwise, use the default project
        return projectManager.getDefaultProject();
    }

    /**
     * For legacy compatibility, the method already exists @see {@link com.intellij.openapi.project.ProjectUtil#guessProjectDir(Project)}
     *
     * @param project Project object
     * @return Base directory
     */
    public static VirtualFile getBaseDir(Project project) {
        if (project.isDefault()) {
            return null;
        }
        Module[] modules = ModuleManager.getInstance(project).getModules();
        Module module = null;
        if (modules.length == 1) {
            module = modules[0];
        } else {
            for (Module item : modules) {
                if (item.getName().equals(project.getName())) {
                    module = item;
                    break;
                }
            }
        }
        if (module != null) {
            ModuleRootManager moduleRootManager = ModuleRootManager.getInstance(module);
            for (VirtualFile contentRoot : moduleRootManager.getContentRoots()) {
                if (contentRoot.isDirectory() && contentRoot.getName().equals(module.getName())) {
                    return contentRoot;
                }
            }
        }
        String basePath = project.getBasePath();
        if (basePath == null) {
            throw new NullPointerException();
        }
        return LocalFileSystem.getInstance().findFileByPath(basePath);
    }
}
