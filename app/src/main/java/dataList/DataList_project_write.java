package dataList;

import android.net.Uri;

import java.io.Serializable;

public class DataList_project_write implements Serializable {
    public static final int TEXT =0;
    public static final int IMAGE =1;
    public static final int MASTER =2;


    String name;
    int position;
    Uri imgUri;
    boolean master;
    int orientation;
    boolean already;

    public int getOrientation() {
        return orientation;
    }

    public void setOrientation(int orientation) {
        this.orientation = orientation;
    }



    public boolean isAlready() {
        return already;
    }

    public void setAlready(boolean already) {
        this.already = already;
    }

    public boolean isMaster() {
        return master;
    }

    public void setMaster(boolean master) {
        this.master = master;
    }

    public Uri getImgUri() {
        return imgUri;
    }

    public void setImgUri(Uri imgUri) {
        this.imgUri = imgUri;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }



    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


}
