package com.sjhy.plugin.actions;

import com.google.common.collect.Lists;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiModifier;
import com.sjhy.plugin.tool.CacheDataUtils;
import com.sjhy.plugin.ui.SelectSavePath;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Generate code menu from Java class
 *
 * @author Mario Luo
 */
public class EasyCodeEntityAction extends AnAction {

    private final CacheDataUtils cacheDataUtils = CacheDataUtils.getInstance();

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        Project project = event.getProject();
        if (project == null) {
            return;
        }

        // Filter to select Java files
        VirtualFile[] psiFiles = event.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY);
        if (psiFiles == null) {
            return;
        }
        PsiManager psiManager = PsiManager.getInstance(project);
        List<PsiJavaFile> psiJavaFiles = Arrays.stream(psiFiles)
                .map(psiManager::findFile)
                .filter(f -> f instanceof PsiJavaFile)
                .map(f -> (PsiJavaFile) f)
                .collect(Collectors.toList());
        if (psiJavaFiles.size() == 0) {
            return;
        }

        // Get selected class
        List<PsiClass> psiClassList = resolvePsiClassByFile(psiJavaFiles);
        if (psiClassList.size() == 0) {
            return;
        }

        // Cache selected value
        cacheDataUtils.setSelectPsiClass(psiClassList.get(0));
        cacheDataUtils.setPsiClassList(psiClassList);
        new SelectSavePath(project, true).show();
    }

    /**
     * Parsing class
     */
    private List<PsiClass> resolvePsiClassByFile(List<PsiJavaFile> psiJavaFiles) {
        List<PsiClass> psiClassList = Lists.newArrayListWithCapacity(psiJavaFiles.size());
        for (PsiJavaFile psiJavaFile : psiJavaFiles) {
            Arrays.stream(psiJavaFile.getClasses())
                    .filter(o -> o.getModifierList() != null && o.getModifierList().hasModifierProperty(PsiModifier.PUBLIC))
                    .findFirst().ifPresent(psiClassList::add);
        }
        return psiClassList;
    }

    @Override
    public void update(@NotNull AnActionEvent event) {
        // No module does not show: select multiple modules
        Project project = event.getData(CommonDataKeys.PROJECT);
        Module module = event.getData(LangDataKeys.MODULE);
        if (project == null || module == null) {
            event.getPresentation().setVisible(false);
            return;
        }

        // Non-java files are not displayed
        VirtualFile file = event.getDataContext().getData(CommonDataKeys.VIRTUAL_FILE);
        if (file != null && !file.isDirectory() && !"java".equals(file.getExtension())) {
            event.getPresentation().setVisible(false);
        }
    }


}
