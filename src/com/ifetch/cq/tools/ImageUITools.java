package com.ifetch.cq.tools;

import com.intellij.util.ui.JBImageIcon;

import javax.swing.*;
import java.awt.*;

/**
 * Created by cq on 19-10-22.
 */
public class ImageUITools {


    public static String computer = "icons/computer.png";
    public static String config = "icons/config-list.png";
    public static String db = "icons/j-db.png";
    public static String table = "icons/j-table.png";

    public static JBImageIcon dbImageIcon = ImageUITools.getImageIcon(ImageUITools.db, 20, 20);

    public static JBImageIcon tableImageIcon = ImageUITools.getImageIcon(ImageUITools.table, 16, 16);

    public static JBImageIcon getImageIcon(String path) {
        return getImageIcon(path, 40, 40);
    }

    public static JBImageIcon getImageIcon(String path, int weight, int height) {
        String url = PathTools.newInstance().getResourcePath(path);
        Image image = new ImageIcon(url).getImage();
        Image image1 = image.getScaledInstance(weight, height, Image.SCALE_DEFAULT);
        return new JBImageIcon(image1);
    }


    ImageUITools() {
    }
}
