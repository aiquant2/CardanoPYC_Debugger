package com.pyc.cardanopyc_debugger.diagnostic;

import com.intellij.openapi.components.Service;

@Service(Service.Level.PROJECT)
public final class HaskellStatusBarService {
    private HaskellStatusBarWidget widget;

    public void setWidget(HaskellStatusBarWidget widget) {
        this.widget = widget;
    }

    public HaskellStatusBarWidget getWidget() {
        return widget;
    }
}
