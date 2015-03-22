package com.Atlas.framework;

import fonts.FontsOverride;

public final class Application extends android.app.Application {
    @Override
    public void onCreate() {
        super.onCreate();
        FontsOverride.changeDefaultFont(this);
    }
}