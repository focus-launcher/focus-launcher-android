package io.focuslauncher.phone.models;

import java.util.ArrayList;

/**
 * Created by parth on 4/5/18.
 */

public class ImageItem {
    public final String name;
    public boolean isVisable = false;
    public ArrayList<String> drawableId = new ArrayList<>();

    public ImageItem(String name, ArrayList<String> drawableId, boolean isVisable) {
        this.name = name;
        this.drawableId = drawableId;
        this.isVisable = isVisable;
    }

    public String getName() {
        return name;
    }

    public boolean isVisable() {
        return isVisable;
    }

    public void setVisable(boolean visable) {
        isVisable = visable;
    }

    public ArrayList<String> getDrawableId() {
        return drawableId;
    }
}
