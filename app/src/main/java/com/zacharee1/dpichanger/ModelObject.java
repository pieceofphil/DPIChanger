package com.zacharee1.dpichanger;

/**
 * Created by Zacha on 4/30/2017.
 */

enum ModelObject {

    INTRO(R.string.setup_intro_title, R.layout.setup_intro),
    ROOT(R.string.setup_root_title, R.layout.setup_root),
    NO_ROOT(R.string.setup_no_root_title, R.layout.setup_no_root),
    DONE(R.string.setup_done_title, R.layout.setup_done);

    private final int mTitleResId;
    private final int mLayoutResId;

    ModelObject(int titleResId, int layoutResId) {
        mTitleResId = titleResId;
        mLayoutResId = layoutResId;
    }

    public int getTitleResId() {
        return mTitleResId;
    }

    public int getLayoutResId() {
        return mLayoutResId;
    }

}
