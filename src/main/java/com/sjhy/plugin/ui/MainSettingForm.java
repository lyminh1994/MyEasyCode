package com.sjhy.plugin.ui;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.sjhy.plugin.dto.SettingsStorageDTO;
import com.sjhy.plugin.service.impl.ClipboardExportImportSettingsServiceImpl;
import com.sjhy.plugin.service.impl.LocalFileExportImportSettingsServiceImpl;
import com.sjhy.plugin.service.impl.NetworkExportImportSettingsServiceImpl;
import com.sjhy.plugin.tool.MessageDialogUtils;
import com.sjhy.plugin.tool.StringUtils;
import com.sjhy.plugin.ui.component.ExportImportComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Objects;

/**
 * @author makejava
 * @version 1.0.0
 * @since 2021/08/07 09:22
 */
public class MainSettingForm implements Configurable, Configurable.Composite, BaseSettings {
    private JLabel versionLabel;
    private JButton resetBtn;
    private JButton pushBtn;
    private JButton pullBtn;
    private JButton exportByNetBtn;
    private JButton importByNetBtn;
    private JButton exportByFileBtn;
    private JButton importByFileBtn;
    private JButton exportByClipboardBtn;
    private JButton importByClipboardBtn;
    private JPanel mainPanel;
    private JTextField userSecureEditor;
    private JTextField authorEditor;
    private JLabel userSecureLabel;
    private JLabel userSecureTitle;

    /**
     * Sub configuration
     */
    private Configurable[] childConfigurableArray;

    public MainSettingForm() {
        // document why this constructor is empty
    }

    private void initLocalExportEvent() {
        new ExportImportComponent(this.exportByFileBtn, this.importByFileBtn, new LocalFileExportImportSettingsServiceImpl(), this::loadChildSettingsStore);
        new ExportImportComponent(this.exportByNetBtn, this.importByNetBtn, new NetworkExportImportSettingsServiceImpl(), this::loadChildSettingsStore);
        new ExportImportComponent(this.exportByClipboardBtn, this.importByClipboardBtn, new ClipboardExportImportSettingsServiceImpl(), this::loadChildSettingsStore);
    }

    private void initEvent() {
        this.resetBtn.addActionListener(e -> {
            boolean result = MessageDialogUtils.yesNo("Confirm to restore default settings. All your custom settings will be lost. Are you sure to continue?");
            if (result) {
                // Reload configuration after resetting defaults
                getSettingsStorage().resetDefaultVal();
                this.loadSettingsStore();
                this.loadChildSettingsStore();
            }
        });

        this.userSecureEditor.addCaretListener(e -> {
            String userSecure = this.userSecureEditor.getText();
            if (StringUtils.isEmpty(userSecure)) {
                this.pullBtn.setEnabled(false);
                this.pushBtn.setEnabled(false);
            } else {
                this.pullBtn.setEnabled(true);
                this.pushBtn.setEnabled(true);
            }
        });
    }

    @Override
    public String getDisplayName() {
        return "EasyCode";
    }

    @Nullable
    @Override
    public String getHelpTopic() {
        return getDisplayName();
    }

    @Override
    public @NotNull Configurable[] getConfigurables() {
        this.childConfigurableArray = new Configurable[]{
                new TypeMapperSettingForm(),
                new TemplateSettingForm(),
                new ColumnConfigSettingForm(),
                new GlobalConfigSettingForm(),
        };
        this.loadChildSettingsStore();
        return this.childConfigurableArray;
    }

    private void loadChildSettingsStore() {
        // Initial device configuration information
        for (Configurable configurable : this.childConfigurableArray) {
            if (configurable instanceof BaseSettings) {
                ((BaseSettings) configurable).loadSettingsStore();
            }
        }
    }

    @Override
    public @Nullable JComponent createComponent() {
        // TODO: Temporarily hide undeveloped UI components
        this.pushBtn.setVisible(false);
        this.pullBtn.setVisible(false);
        this.userSecureEditor.setVisible(false);
        this.userSecureTitle.setVisible(false);
        this.userSecureLabel.setVisible(false);
        // load storage data
        this.loadSettingsStore();
        // initialization event
        this.initEvent();
        this.initLocalExportEvent();
        return mainPanel;
    }

    @Override
    public boolean isModified() {
        if (!Objects.equals(this.authorEditor.getText(), getSettingsStorage().getAuthor())) {
            return true;
        }
        return !Objects.equals(this.userSecureEditor.getText(), getSettingsStorage().getUserSecure());
    }

    @Override
    public void apply() throws ConfigurationException {
        String author = this.authorEditor.getText();
        if (StringUtils.isEmpty(author)) {
            throw new ConfigurationException("Author name can't empty!");
        }
        getSettingsStorage().setAuthor(author);
        String userSecure = this.userSecureEditor.getText();
        getSettingsStorage().setUserSecure(userSecure);
    }

    /**
     * Load configuration information
     *
     * @param settingsStorage Configuration information
     */
    @Override
    public void loadSettingsStore(SettingsStorageDTO settingsStorage) {
        this.versionLabel.setText(settingsStorage.getVersion());
        this.authorEditor.setText(settingsStorage.getAuthor());
        this.userSecureEditor.setText(settingsStorage.getUserSecure());
        if (StringUtils.isEmpty(settingsStorage.getUserSecure())) {
            this.pullBtn.setEnabled(false);
            this.pushBtn.setEnabled(false);
        } else {
            this.pullBtn.setEnabled(true);
            this.pushBtn.setEnabled(true);
        }
    }
}
