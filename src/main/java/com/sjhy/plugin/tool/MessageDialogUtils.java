package com.sjhy.plugin.tool;

import com.intellij.openapi.project.Project;
import com.intellij.util.ui.UIUtil;
import com.sjhy.plugin.dict.GlobalDict;

import javax.swing.*;

/**
 * Message popup tool class Compatible processing of the message dialog popup
 *
 * @author makejava
 * @version 1.0.0
 * @since 2021/02/01 15:36
 */
public class MessageDialogUtils {

    private MessageDialogUtils() {
    }

    /**
     * Yes/No confirmation box
     *
     * @param msg Information
     * @return Confirm
     */
    public static boolean yesNo(String msg) {
        return yesNo(null, msg);
    }

    /**
     * Yes/No confirmation box
     *
     * @param project Project object
     * @param msg     Information
     * @return Confirm
     */
    public static boolean yesNo(Project project, String msg) {
        Object[] options = new Object[]{"Yes", "No"};
        return JOptionPane.showOptionDialog(null,
                msg, GlobalDict.TITLE_INFO,
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE,
                UIUtil.getQuestionIcon(),
                options, options[0]) == 0;
    }

    /**
     * Show confirmation box
     *
     * @param msg     Confirmation box message
     * @param project Project
     * @return Click the button
     */
    public static int yesNoCancel(Project project, String msg, String yesText, String noText, String cancelText) {
        Object[] options = new Object[]{yesText, noText, cancelText};
        return JOptionPane.showOptionDialog(null,
                msg, GlobalDict.TITLE_INFO,
                JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
                UIUtil.getQuestionIcon(),
                options, options[0]);
    }

}
