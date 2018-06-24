package com.quangtd.trimview;

import java.io.File;

/**
 * Created by hainguyen on 9/6/2016.
 */
public abstract class LogConfig {
    private boolean isDebugMode = true;
    private File backupPath;

    public LogConfig() {
    }

    public void setDebugMode(boolean isDebugMode) {
        this.isDebugMode = isDebugMode;
    }

    public boolean isDebugMode() {
        return this.isDebugMode;
    }

    public File getBackupPath() {
        return this.backupPath;
    }
}
