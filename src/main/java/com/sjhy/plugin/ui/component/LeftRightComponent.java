package com.sjhy.plugin.ui.component;

import com.intellij.openapi.ui.Splitter;
import com.intellij.util.ui.JBUI;
import lombok.Getter;

import javax.swing.*;
import java.awt.*;

/**
 * Left and right components
 *
 * @author makejava
 * @version 1.0.0
 * @since 2021/08/10 16:49
 */
public class LeftRightComponent {
    /**
     * Main panel
     */
    @Getter
    private JPanel mainPanel;
    /**
     * Left panel
     */
    private JPanel leftPanel;
    /**
     * Right panel
     */
    private JPanel rightPanel;
    /**
     * Split ratio
     */
    private float proportion;
    /**
     * Default window size
     */
    private Dimension preferredSize;

    public LeftRightComponent(JPanel leftPanel, JPanel rightPanel) {
        this(leftPanel, rightPanel, 0.2F, JBUI.size(400, 300));
    }

    public LeftRightComponent(JPanel leftPanel, JPanel rightPanel, float proportion, Dimension preferredSize) {
        this.leftPanel = leftPanel;
        this.rightPanel = rightPanel;
        this.proportion = proportion;
        this.preferredSize = preferredSize;
        this.init();
    }

    private void init() {
        this.mainPanel = new JPanel(new BorderLayout());
        Splitter splitter = new Splitter(false, proportion);
        splitter.setFirstComponent(this.leftPanel);
        splitter.setSecondComponent(this.rightPanel);
        this.mainPanel.add(splitter, BorderLayout.CENTER);
        mainPanel.setPreferredSize(this.preferredSize);
    }
}
