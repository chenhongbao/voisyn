package chb.gui;


import javax.swing.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

public class CPWindowListener implements WindowListener{
    public JFrame parent = null;

    public CPWindowListener setParentFrame(JFrame p) {
         this.parent = p;
        return this;
    }

    @Override
    public void windowOpened(WindowEvent e) {}

    @Override
    public void windowClosing(WindowEvent e) {}

    @Override
    public void windowClosed(WindowEvent e) {
        this.parent.dispose();
    }

    @Override
    public void windowIconified(WindowEvent e) {}

    @Override
    public void windowDeiconified(WindowEvent e) {}

    @Override
    public void windowActivated(WindowEvent e) {}

    @Override
    public void windowDeactivated(WindowEvent e) {}
}
