package com.sjhy.plugin.ui.base;

import com.intellij.openapi.ui.InputValidator;
import com.sjhy.plugin.tool.StringUtils;

import java.util.Collection;

/**
 * Input exists validator
 *
 * @author makejava
 * @version 1.0.0
 * @since 2021/08/10 17:16
 */
public class InputExistsValidator implements InputValidator {

    private final Collection<String> itemList;

    public InputExistsValidator(Collection<String> itemList) {
        this.itemList = itemList;
    }

    @Override
    public boolean checkInput(String inputString) {
        return !StringUtils.isEmpty(inputString) && !itemList.contains(inputString);
    }

    @Override
    public boolean canClose(String inputString) {
        return this.checkInput(inputString);
    }
}
