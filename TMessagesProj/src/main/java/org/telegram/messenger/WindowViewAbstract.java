package org.telegram.messenger;

import android.content.Context;
import android.graphics.Bitmap;

import org.telegram.ui.ActionBar.INavigationLayout;
import org.telegram.ui.Components.SizeNotifierFrameLayout;

public abstract class WindowViewAbstract extends SizeNotifierFrameLayout {
    public WindowViewAbstract(Context context) {
        super(context);
    }

    public WindowViewAbstract(Context context, INavigationLayout layout) {
        super(context, layout);
    }

    public abstract void drawBlurBitmap(Bitmap bitmap, float aFloat);

    public abstract int getPaddingUnderContainer();

    public abstract int getBottomPadding2();
}
