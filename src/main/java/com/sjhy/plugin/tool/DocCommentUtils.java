package com.sjhy.plugin.tool;

import com.intellij.psi.PsiElement;
import com.intellij.psi.javadoc.PsiDocComment;
import com.intellij.psi.javadoc.PsiDocToken;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

/**
 * Documentation annotation tool class
 *
 * @author makejava
 * @version 1.0.0
 * @since 2021/08/16 17:37
 */
public class DocCommentUtils {

    private DocCommentUtils() {
    }

    /**
     * Get the annotation information, get the first text type annotation content, if it does not exist, return null
     *
     * @param docComment Documentation Notes
     * @return Comment content
     */
    public static String getComment(@Nullable PsiDocComment docComment) {
        if (docComment == null) {
            return null;
        }
        return Arrays.stream(docComment.getDescriptionElements())
            .filter(PsiDocToken.class::isInstance)
            .map(PsiElement::getText)
            .findFirst()
            .map(String::trim)
            .orElse(null);
    }

}
