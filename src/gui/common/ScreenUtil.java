/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gui.common;

import java.awt.Frame;
import java.awt.Insets;
import java.awt.Toolkit;

/**
 *
 * @author iychoi
 */
public class ScreenUtil {
    public static int getScreenWorkingWidth() {
        return java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds().width;
    }

    public static int getScreenWorkingHeight() {
        return java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds().height;
    }
    
    public static Insets getScreenInsets() {
        Insets si = Toolkit.getDefaultToolkit().getScreenInsets(new Frame().getGraphicsConfiguration());
        return si;
    }
}
