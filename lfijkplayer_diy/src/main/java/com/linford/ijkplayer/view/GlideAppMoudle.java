package com.linford.ijkplayer.view;

import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.module.AppGlideModule;

@GlideModule
public class GlideAppMoudle extends AppGlideModule {
    @Override public boolean isManifestParsingEnabled() {
        return false;
    }
}
