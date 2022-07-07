package com.sjhy.plugin.tool;

import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiModifier;
import com.intellij.psi.PsiModifierList;

/**
 * Generate code business tool classes from entity classes
 *
 * @author Mario Luo
 */
public final class PsiClassGenerateUtils {

    private PsiClassGenerateUtils() {
    }

    /**
     * Is it a primary key field
     */
    public static boolean isPkField(PsiField field) {
        if("id".equals(field.getName())) {
            return true;
        }
        if (existsAnnotation(field, "org.springframework.data.annotation.Id")) {
            return true;
        }
        return existsAnnotation(field, "javax.persistence.Id" );
    }

    /**
     * Whether to skip this field
     */
    public static boolean isSkipField(PsiField field) {
        PsiModifierList modifierList = field.getModifierList();
        if(modifierList != null && modifierList.hasExplicitModifier(PsiModifier.STATIC)) {
            return true;
        }
        if (existsAnnotation(field, "org.springframework.data.annotation.Transient")) {
            return true;
        }
        return existsAnnotation(field, "javax.persistence.Transient");
    }

    private static boolean existsAnnotation(PsiField field, String clsName) {
        return getAnnotation(field, clsName) != null;
    }

    private static PsiAnnotation getAnnotation(PsiField field, String clsName) {
        PsiModifierList list = field.getModifierList();
        return list == null ? null : list.findAnnotation(clsName);
    }
}
