package ja.burhanrashid52.photoeditor;

import android.graphics.drawable.GradientDrawable;

public class GradientDrawableWithColor extends GradientDrawable {

    private int color = -1;

    @Override
    public void setColor(int argb) {
        super.setColor(argb);
        color = argb;
    }

    public int getLocalColor() {
        return color;
    }
}
