package chb.gui;


import javax.swing.*;
import java.awt.*;

public class ControlPanel extends JFrame {

    // The size of the main frame window.
    public int WIN_HEIGHT = 0;
    public int WIN_WIDTH = 0;

    // Position of the window when it first appears.
    public int WIN_X = 0;
    public int WIN_Y = 0;

    // The default title of the main frame window.
    public String WIN_TITLE = "";


    public ControlPanel() throws HeadlessException {
        super();

        setCPSize();
        setCPTitle();
        setCPLocation();
        this.addWindowListener(new CPWindowListener().setParentFrame(this));

        setCPParameters();
        this.setVisible(true);
    }

    /**
     * Static method to start the application.
     *
     * @return
     */
    public static int work() {
        new ControlPanel();
        return 0;
    }

    /**
     * Set the location of main window when it first appears.
     */
    private void setCPLocation() {
        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        if (d == null) {
            return;
        }

        this.WIN_X = (d.width - this.WIN_WIDTH) / 2;
        this.WIN_Y = (d.height - this.WIN_HEIGHT) / 2;
    }

    /**
     * Set the title of the main window.
     */
    private void setCPTitle() {
        this.WIN_TITLE = "语音合成系统v1-陈宏葆";
    }

    /**
     * Set the size of main window.
     */
    private void setCPSize() {
        this.WIN_WIDTH = 700;
        this.WIN_HEIGHT = 400;
    }

    /**
     * Set all the parameters into the super class
     * to control the JFrame behaviour.
     */
    private void setCPParameters() {
        super.setLocation(this.WIN_X, this.WIN_Y);
        super.setTitle(this.WIN_TITLE);

        super.setSize(this.WIN_WIDTH, this.WIN_HEIGHT);
        super.setResizable(false);
    }
}
