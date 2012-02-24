package chb.gui;


import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

public class ControlPanel extends JFrame {

    // The size of the main frame window.
    public int WIN_HEIGHT = 400;
    public int WIN_WIDTH = 500;

    // The default title of the main frame window.
    public String WIN_TITLE = "语音合成系统v1-陈宏葆";


    public ControlPanel() throws HeadlessException {

        this.setSize(this.WIN_WIDTH, this.WIN_HEIGHT);
        this.setTitle(this.WIN_TITLE);
        this.addWindowListener(new CPWindowListener().setParentFrame(this));

        this.setVisible(true);
    }

    public static int work() {
        new ControlPanel();
        return 0;
    }
}
