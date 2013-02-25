package chb.gui;

import javax.sound.midi.*;
import javax.sound.sampled.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.font.FontRenderContext;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.io.*;
import java.net.URL;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.util.Vector;


/**
 * A JukeBox for sampled and midi sound files.  Features duration progress,
 * seek slider, pan and volume controls.
 */
public class ControlPanel extends JPanel implements Runnable, LineListener, MetaEventListener {

    final int bufSize = 16384;
    PlaybackMonitor playbackMonitor = new PlaybackMonitor();

    Vector sounds = new Vector();
    Thread thread;
    Sequencer sequencer;
    boolean midiEOM, audioEOM;
    Synthesizer synthesizer;
    MidiChannel channels[];
    Object currentSound;
    String currentName;
    double duration;
    int num;
    boolean bump;
    boolean paused = false;
    boolean analyzed = false;
    JButton startB, pauseB, analyB, prevB, nextB;
    JTable table;
    JSlider panSlider, gainSlider;
    JSlider seekSlider;
    CPTable jukeTable;
    Loading loading;
    Credits credits;
    String errStr;
    CPControls controls;

    // Text shown on the buttons.
    private final String analyB_name = "分析";
    private final String startB_name = "播放";
    private final String pauseB_name = "暂停";
    private String nextB_name = "下一个";
    private String prevB_name = "上一个";
    private String stopB_name = "停止";
    private String resumB_name = "继续";
    private String gain_txt = "音量";
    private String pan_txt = "均衡";


    public ControlPanel(String dirName) {
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(5, 5, 5, 5));

        if (dirName != null) {
            loadCP(dirName);
        }

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                jukeTable = new CPTable(), controls = new CPControls());
        splitPane.setContinuousLayout(true);
        add(splitPane);
    }


    public void open() {

        try {

            sequencer = MidiSystem.getSequencer();

            if (sequencer instanceof Synthesizer) {
                synthesizer = (Synthesizer) sequencer;
                channels = synthesizer.getChannels();
            }

        } catch (Exception ex) {
            javax.swing.JOptionPane.showMessageDialog(this, "不支持MIDI格式");
            return;
        }
        sequencer.addMetaEventListener(this);
        (credits = new Credits()).start();
    }


    public void close() {
        if (credits != null && credits.isAlive()) {
            credits.interrupt();
        }
        if (thread != null && startB != null) {
            startB.doClick(0);
        }
        if (jukeTable != null && jukeTable.frame != null) {
            jukeTable.frame.dispose();
            jukeTable.frame = null;
        }
        if (sequencer != null) {
            sequencer.close();
        }
    }


    public void loadCP(String name) {
        try {
            File file = new File(name);
            if (file != null && file.isDirectory()) {
                String files[] = file.list();
                for (int i = 0; i < files.length; i++) {
                    File leafFile = new File(file.getAbsolutePath(), files[i]);
                    if (leafFile.isDirectory()) {
                        loadCP(leafFile.getAbsolutePath());
                    } else {
                        addSound(leafFile);
                    }
                }
            } else if (file != null && file.exists()) {
                addSound(file);
            }
        } catch (SecurityException ex) {
            reportStatus(ex.toString());
        } catch (Exception ex) {
            reportStatus(ex.toString());
        }
    }


    private void addSound(File file) {
        String s = file.getName();
        if (s.endsWith(".txt")) {
            sounds.add(file);
        }
    }


    public boolean loadSound(Object object) {

        duration = 0.0;
        (loading = new Loading()).start();

        if (object instanceof URL) {
            // Wave filename is made by appending '.wav' to the original name.
            currentName = ((URL) object).getFile();
            currentName += ".wav";
            // Clean the URL and get the wave file path on disk.
            String[] names = currentName.split("/");
            for (String s : names) {
                if (s.endsWith(".wav")) {
                    currentName = "./tmp/" + s;
                    break;
                }
            }
            if (new File(currentName).exists() == false) {
                JOptionPane.showMessageDialog(this, "文件\'" + currentName + "\'不存在，请先分析文本文件。");
                return false;
            }

            playbackMonitor.repaint();
            try {
                currentSound = AudioSystem.getAudioInputStream((URL) object);
            } catch (Exception e) {
                try {
                    currentSound = MidiSystem.getSequence((URL) object);
                } catch (InvalidMidiDataException imde) {
                    System.out.println("Unsupported audio file.");
                    return false;
                } catch (Exception ex) {
                    ex.printStackTrace();
                    currentSound = null;
                    return false;
                }
            }
        } else if (object instanceof File) {
            // Wave filename is made by appending '.wav' to the original name.
            currentName = ((File) object).getName();
            currentName += ".wav";
            currentName = "./tmp/" + currentName;
            if (new File(currentName).exists() == false) {
                JOptionPane.showMessageDialog(this, "文件\'" + currentName + "\'不存在，请先分析文本文件。");
                return false;
            }

            playbackMonitor.repaint();
            try {
                currentSound = AudioSystem.getAudioInputStream((File) object);
            } catch (Exception e1) {
                try {
                    FileInputStream is = new FileInputStream((File) object);
                    currentSound = new BufferedInputStream(is, 1024);
                } catch (Exception e3) {
                    e3.printStackTrace();
                    currentSound = null;
                    return false;
                }
                //}
            }
        }


        loading.interrupt();

        // user pressed stop or changed tabs while loading
        if (sequencer == null) {
            currentSound = null;
            return false;
        }

        if (currentSound instanceof AudioInputStream) {
            try {
                AudioInputStream stream = (AudioInputStream) currentSound;
                AudioFormat format = stream.getFormat();

                /**
                 * we can't yet open the device for ALAW/ULAW playback,
                 * convert ALAW/ULAW to PCM
                 */
                if ((format.getEncoding() == AudioFormat.Encoding.ULAW) ||
                        (format.getEncoding() == AudioFormat.Encoding.ALAW)) {
                    AudioFormat tmp = new AudioFormat(
                            AudioFormat.Encoding.PCM_SIGNED,
                            format.getSampleRate(),
                            format.getSampleSizeInBits() * 2,
                            format.getChannels(),
                            format.getFrameSize() * 2,
                            format.getFrameRate(),
                            true);
                    stream = AudioSystem.getAudioInputStream(tmp, stream);
                    format = tmp;
                }
                DataLine.Info info = new DataLine.Info(
                        Clip.class,
                        stream.getFormat(),
                        ((int) stream.getFrameLength() *
                                format.getFrameSize()));

                Clip clip = (Clip) AudioSystem.getLine(info);
                clip.addLineListener(this);
                clip.open(stream);
                currentSound = clip;
                seekSlider.setMaximum((int) stream.getFrameLength());
            } catch (Exception ex) {
                ex.printStackTrace();
                currentSound = null;
                return false;
            }
        } else if (currentSound instanceof Sequence || currentSound instanceof BufferedInputStream) {
            try {
                sequencer.open();
                if (currentSound instanceof Sequence) {
                    sequencer.setSequence((Sequence) currentSound);
                } else {
                    sequencer.setSequence((BufferedInputStream) currentSound);
                }
                seekSlider.setMaximum((int) (sequencer.getMicrosecondLength() / 1000));

            } catch (InvalidMidiDataException imde) {
                System.out.println("Unsupported audio file.");
                currentSound = null;
                return false;
            } catch (Exception ex) {
                ex.printStackTrace();
                currentSound = null;
                return false;
            }
        }

        seekSlider.setValue(0);

        // enable seek, pan, and gain sliders for sequences as well as clips
        seekSlider.setEnabled(true);
        panSlider.setEnabled(true);
        gainSlider.setEnabled(true);

        duration = getDuration();

        return true;
    }


    public void playSound() {
        playbackMonitor.start();
        setGain();
        setPan();
        midiEOM = audioEOM = bump = false;
        if (currentSound instanceof Sequence || currentSound instanceof BufferedInputStream && thread != null) {
            sequencer.start();
            while (!midiEOM && thread != null && !bump) {
                try {
                    thread.sleep(99);
                } catch (Exception e) {
                    break;
                }
            }
            sequencer.stop();
            sequencer.close();
        } else if (currentSound instanceof Clip && thread != null) {
            Clip clip = (Clip) currentSound;
            clip.start();
            try {
                thread.sleep(99);
            } catch (Exception e) {
            }
            while ((paused || clip.isActive()) && thread != null && !bump) {
                try {
                    thread.sleep(99);
                } catch (Exception e) {
                    break;
                }
            }
            clip.stop();
            clip.close();
        }
        currentSound = null;
        playbackMonitor.stop();
    }


    public double getDuration() {
        double duration = 0.0;
        if (currentSound instanceof Sequence) {
            duration = ((Sequence) currentSound).getMicrosecondLength() / 1000000.0;
        } else if (currentSound instanceof BufferedInputStream) {
            duration = sequencer.getMicrosecondLength() / 1000000.0;
        } else if (currentSound instanceof Clip) {
            Clip clip = (Clip) currentSound;
            duration = clip.getBufferSize() /
                    (clip.getFormat().getFrameSize() * clip.getFormat().getFrameRate());
        }
        return duration;
    }


    public double getSeconds() {
        double seconds = 0.0;
        if (currentSound instanceof Clip) {
            Clip clip = (Clip) currentSound;
            seconds = clip.getFramePosition() / clip.getFormat().getFrameRate();
        } else if ((currentSound instanceof Sequence) || (currentSound instanceof BufferedInputStream)) {
            try {
                seconds = sequencer.getMicrosecondPosition() / 1000000.0;
            } catch (IllegalStateException e) {
                System.out.println("TEMP: IllegalStateException " +
                        "on sequencer.getMicrosecondPosition(): " + e);
            }
        }
        return seconds;
    }


    public void update(LineEvent event) {
        if (event.getType() == LineEvent.Type.STOP && !paused) {
            audioEOM = true;
        }
    }


    public void meta(MetaMessage message) {
        if (message.getType() == 47) {  // 47 is end of track
            midiEOM = true;
        }
    }


    private void reportStatus(String msg) {
        if ((errStr = msg) != null) {
            System.out.println(errStr);
            playbackMonitor.repaint();
        }
        if (credits != null && credits.isAlive()) {
            credits.interrupt();
        }
    }


    public Thread getThread() {
        return thread;
    }


    public void start() {
        thread = new Thread(this);
        thread.setName("ControlPanel");
        thread.start();
    }


    public void stop() {
        if (thread != null) {
            thread.interrupt();
        }
        thread = null;
    }


    public void run() {
        table.scrollRectToVisible(new Rectangle(0, 0, 1, 1));
        for (; num < sounds.size() && thread != null; num++) {
            table.scrollRectToVisible(new Rectangle(0, (num + 2) * (table.getRowHeight() + table.getRowMargin()), 1, 1));
            table.setRowSelectionInterval(num, num);
            if (loadSound(sounds.get(num)) == true) {
                playSound();
            }
            // take a little break between sounds
            try {
                thread.sleep(222);
            } catch (Exception e) {
                break;
            }
        }
        num = 0;


        if (thread != null) {
            startB.doClick();
        }
        thread = null;
        currentName = null;
        currentSound = null;
        playbackMonitor.repaint();
    }


    public void setPan() {

        int value = panSlider.getValue();

        if (currentSound instanceof Clip) {
            try {
                Clip clip = (Clip) currentSound;
                FloatControl panControl =
                        (FloatControl) clip.getControl(FloatControl.Type.PAN);
                panControl.setValue(value / 100.0f);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else if (currentSound instanceof Sequence || currentSound instanceof BufferedInputStream) {
            for (int i = 0; i < channels.length; i++) {
                channels[i].controlChange(10, (int) (((double) value + 100.0) / 200.0 * 127.0));
            }
        }
    }


    public void setGain() {
        double value = gainSlider.getValue() / 100.0;

        if (currentSound instanceof Clip) {
            try {
                Clip clip = (Clip) currentSound;
                FloatControl gainControl =
                        (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                float dB = (float)
                        (Math.log(value == 0.0 ? 0.0001 : value) / Math.log(10.0) * 20.0);
                gainControl.setValue(dB);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else if (currentSound instanceof Sequence || currentSound instanceof BufferedInputStream) {
            for (int i = 0; i < channels.length; i++) {
                channels[i].controlChange(7, (int) (value * 127.0));

            }
        }
    }


    /**
     * GUI controls for analyze, start, stop, previous, next, pan and gain.
     */
    class CPControls extends JPanel implements ActionListener, ChangeListener {

        public CPControls() {
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

            JPanel p1 = new JPanel();
            p1.setLayout(new BoxLayout(p1, BoxLayout.Y_AXIS));
            p1.setBorder(new EmptyBorder(10, 0, 5, 0));
            JPanel p2 = new JPanel();
            analyB = addButton(analyB_name, p2, analyzed == false);
            startB = addButton(startB_name, p2, sounds.size() != 0);
            pauseB = addButton(pauseB_name, p2, false);
            p1.add(p2);
            JPanel p3 = new JPanel();
            prevB = addButton(prevB_name, p3, false);
            nextB = addButton(nextB_name, p3, false);
            p1.add(p3);
            add(p1);

            JPanel p4 = new JPanel(new BorderLayout());
            EmptyBorder eb = new EmptyBorder(5, 20, 10, 20);
            BevelBorder bb = new BevelBorder(BevelBorder.LOWERED);
            p4.setBorder(new CompoundBorder(eb, bb));
            p4.add(playbackMonitor);
            seekSlider = new JSlider(JSlider.HORIZONTAL, 0, 100, 0);
            seekSlider.setEnabled(false);
            seekSlider.addChangeListener(this);
            p4.add("South", seekSlider);
            add(p4);

            JPanel p5 = new JPanel();
            p5.setLayout(new BoxLayout(p5, BoxLayout.X_AXIS));
            p5.setBorder(new EmptyBorder(5, 5, 10, 5));
            panSlider = new JSlider(-100, 100, 0);
            panSlider.addChangeListener(this);
            TitledBorder tb = new TitledBorder(new EtchedBorder());
            tb.setTitle(pan_txt + " = 0.0");
            panSlider.setBorder(tb);
            p5.add(panSlider);

            gainSlider = new JSlider(0, 100, 80);
            gainSlider.addChangeListener(this);
            tb = new TitledBorder(new EtchedBorder());
            tb.setTitle(gain_txt + " = 80");
            gainSlider.setBorder(tb);
            p5.add(gainSlider);
            add(p5);
        }

        private JButton addButton(String name, JPanel panel, boolean state) {
            JButton b = new JButton(name);
            b.addActionListener(this);
            b.setEnabled(state);
            b.setBackground(Color.CYAN);
            panel.add(b);
            return b;
        }

        public void stateChanged(ChangeEvent e) {
            JSlider slider = (JSlider) e.getSource();
            int value = slider.getValue();
            if (slider.equals(seekSlider)) {
                if (currentSound instanceof Clip) {
                    ((Clip) currentSound).setFramePosition(value);
                } else if (currentSound instanceof Sequence) {
                    sequencer.setMicrosecondPosition(value * 1000);
                } else if (currentSound instanceof BufferedInputStream) {
                    long dur = sequencer.getMicrosecondLength();
                    sequencer.setMicrosecondPosition(value * 1000);
                }
                playbackMonitor.repaint();
                return;
            }
            TitledBorder tb = (TitledBorder) slider.getBorder();
            String s = tb.getTitle();
            if (s.startsWith(pan_txt)) {
                s = s.substring(0, s.indexOf('=') + 1) + s.valueOf(value / 100.0);
                if (currentSound != null) {
                    setPan();
                }
            } else {
                if (s.startsWith(gain_txt)) {
                    s = s.substring(0, s.indexOf('=') + 1) + s.valueOf(value);
                    if (currentSound != null) {
                        setGain();
                    }
                }
            }
            tb.setTitle(s);
            slider.repaint();
        }


        public void setComponentsEnabled(boolean state) {
            seekSlider.setEnabled(state);
            pauseB.setEnabled(state);
            prevB.setEnabled(state);
            nextB.setEnabled(state);
        }


        public void actionPerformed(ActionEvent e) {
            JButton button = (JButton) e.getSource();
            if (button.getText().equals(startB_name)) {
                if (credits != null) {
                    credits.interrupt();
                }
                paused = false;
                num = table.getSelectedRow();
                num = num == -1 ? 0 : num;
                start();
                button.setText(stopB_name);
                setComponentsEnabled(true);
            } else if (button.getText().equals(stopB_name)) {
                credits = new Credits();
                credits.start();
                paused = false;
                stop();
                button.setText(startB_name);
                pauseB.setText(pauseB_name);
                setComponentsEnabled(false);
            } else if (button.getText().equals(pauseB_name)) {
                paused = true;
                if (currentSound instanceof Clip) {
                    ((Clip) currentSound).stop();
                } else if (currentSound instanceof Sequence || currentSound instanceof BufferedInputStream) {
                    sequencer.stop();
                }
                playbackMonitor.stop();
                pauseB.setText(resumB_name);
            } else if (button.getText().equals(resumB_name)) {
                paused = false;
                if (currentSound instanceof Clip) {
                    ((Clip) currentSound).start();
                } else if (currentSound instanceof Sequence || currentSound instanceof BufferedInputStream) {
                    sequencer.start();
                }
                playbackMonitor.start();
                pauseB.setText(pauseB_name);
            } else if (button.getText().equals(prevB_name)) {
                paused = false;
                pauseB.setText(pauseB_name);
                num = num - 1 < 0 ? sounds.size() - 1 : num - 2;
                bump = true;
            } else if (button.getText().equals(nextB_name)) {
                paused = false;
                pauseB.setText(pauseB_name);
                num = num + 1 == sounds.size() ? -1 : num;
                bump = true;
            } else if (button.getText().equals(analyB_name)) {

                analyB.setText(analyB_name + "中");
                analyB.setEnabled(false);
                startB.setEnabled(false);

                //TODO Starts a thread to do TTS
                // The thread should have an reference to the two buttons.
            }
        }
    }  // End CPControls


    /**
     * Displays current sound and time elapsed.
     */
    public class PlaybackMonitor extends JPanel implements Runnable {

        String welcomeStr = "语音合成系统";
        Thread pbThread;
        Color black = new Color(20, 20, 25);
        Color jfcBlue = new Color(255, 255, 255);
        Color jfcDarkBlue = jfcBlue.darker();
        Font font24 = new Font("serif", Font.BOLD, 24);
        Font font28 = new Font("serif", Font.BOLD, 28);
        Font font42 = new Font("serif", Font.BOLD, 42);
        FontMetrics fm28, fm42;

        public PlaybackMonitor() {
            fm28 = getFontMetrics(font28);
            fm42 = getFontMetrics(font42);
        }

        public void paint(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            Dimension d = getSize();
            g2.setBackground(black);
            g2.clearRect(0, 0, d.width, d.height);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(jfcBlue);

            if (errStr != null) {
                g2.setFont(new Font("serif", Font.BOLD, 18));
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
                g2.drawString("ERROR", 5, 20);
                AttributedString as = new AttributedString(errStr);
                Font font12 = new Font("serif", Font.PLAIN, 12);
                as.addAttribute(TextAttribute.FONT, font12, 0, errStr.length());
                AttributedCharacterIterator aci = as.getIterator();
                FontRenderContext frc = g2.getFontRenderContext();
                LineBreakMeasurer lbm = new LineBreakMeasurer(aci, frc);
                float x = 5, y = 25;
                lbm.setPosition(0);
                while (lbm.getPosition() < errStr.length()) {
                    TextLayout tl = lbm.nextLayout(d.width - x - 5);
                    if (!tl.isLeftToRight()) {
                        x = d.width - tl.getAdvance();
                    }
                    tl.draw(g2, x, y += tl.getAscent());
                    y += tl.getDescent() + tl.getLeading();
                }
            } else if (currentName == null) {
                FontRenderContext frc = g2.getFontRenderContext();
                TextLayout tl = new TextLayout(welcomeStr, font28, frc);
                float x = (float) (d.width / 2 - tl.getBounds().getWidth() / 2);
                tl.draw(g2, x, d.height / 2);
                if (credits != null) {
                    credits.render(d, g2);
                }
            } else {
                g2.setFont(font24);
                g2.drawString(currentName, 5, fm28.getHeight() - 5);
                if (duration <= 0.0) {
                    loading.render(d, g2);
                } else {
                    double seconds = getSeconds();
                    if (midiEOM || audioEOM) {
                        seconds = duration;
                    }
                    if (seconds > 0.0) {
                        g2.setFont(font42);
                        String s = String.valueOf(seconds);
                        s = s.substring(0, s.indexOf('.') + 2);
                        int strW = (int) fm42.getStringBounds(s, g2).getWidth();
                        g2.drawString(s, d.width - strW - 9, fm42.getAscent());

                        int num = 30;
                        int progress = (int) (1.0 * seconds / duration * num);
                        double ww = ((double) (d.width - 10) / (double) num);
                        double hh = (int) (d.height * 0.25);
                        double x = 0.0;
                        for (; x < progress; x += 1.0) {
                            g2.fill(new Rectangle2D.Double(x * ww + 5, d.height - hh - 5, ww - 1, hh));
                        }
                        g2.setColor(jfcDarkBlue);
                        for (; x < num; x += 1.0) {
                            g2.fill(new Rectangle2D.Double(x * ww + 5, d.height - hh - 5, ww - 1, hh));
                        }
                    }
                }
            }
        }

        public void start() {
            pbThread = new Thread(this);
            pbThread.setName("PlaybackMonitor");
            pbThread.start();
        }

        public void stop() {
            if (pbThread != null) {
                pbThread.interrupt();
            }
            pbThread = null;
        }

        public void run() {
            while (pbThread != null) {
                try {
                    pbThread.sleep(99);
                } catch (Exception e) {
                    break;
                }
                repaint();
            }
            pbThread = null;
        }
    } // End PlaybackMonitor


    /**
     * Table to display the name of the sound.
     */
    class CPTable extends JPanel implements ActionListener {

        TableModel dataModel;
        JFrame frame;
        JTextField textField;
        JButton applyB;
        JButton inspecB;
        private String fd_text = "文件或目录";
        private String url_text = "URL";
        private final String name_text = "名称";
        private String applyB_name = "应用";
        private String selc_text = "已选的";
        private String all_text = "全部";
        private final String file_text = "文件或目录";
        private final String addB_name = "添加";
        private final String deleteB_name = "删除";
        private final String inspecB_name = "查看";

        public CPTable() {
            setLayout(new BorderLayout());
            setPreferredSize(new Dimension(260, 300));

            final String[] names = {"#", name_text};

            dataModel = new AbstractTableModel() {
                public int getColumnCount() {
                    return names.length;
                }

                public int getRowCount() {
                    return sounds.size();
                }

                public Object getValueAt(int row, int col) {
                    if (col == 0) {
                        return new Integer(row);
                    } else if (col == 1) {
                        Object object = sounds.get(row);
                        if (object instanceof File) {
                            return ((File) object).getName();
                        } else if (object instanceof URL) {
                            return ((URL) object).getFile();
                        }
                    }
                    return null;
                }

                public String getColumnName(int col) {
                    return names[col];
                }

                public Class getColumnClass(int c) {
                    return getValueAt(0, c).getClass();
                }

                public boolean isCellEditable(int row, int col) {
                    return false;
                }

                public void setValueAt(Object aValue, int row, int col) {
                }
            };

            table = new JTable(dataModel);
            TableColumn col = table.getColumn("#");
            col.setMaxWidth(20);
            table.sizeColumnsToFit(0);

            JScrollPane scrollPane = new JScrollPane(table);
            EmptyBorder eb = new EmptyBorder(5, 5, 2, 5);
            scrollPane.setBorder(new CompoundBorder(eb, new EtchedBorder()));
            add(scrollPane);

            JPanel p1 = new JPanel();
            JMenuBar menuBar = new JMenuBar();
            menuBar.setBorder(new BevelBorder(BevelBorder.RAISED));
            JMenu menu = (JMenu) menuBar.add(new JMenu(addB_name));
            String items[] = {file_text, url_text};
            for (int i = 0; i < items.length; i++) {
                JMenuItem item = menu.add(new JMenuItem(items[i]));
                item.addActionListener(this);
            }
            p1.add(menuBar);

            menuBar = new JMenuBar();
            menuBar.setBorder(new BevelBorder(BevelBorder.RAISED));
            menu = (JMenu) menuBar.add(new JMenu(deleteB_name));
            JMenuItem item = menu.add(new JMenuItem(selc_text));
            item.addActionListener(this);
            item = menu.add(new JMenuItem(all_text));
            item.addActionListener(this);
            p1.add(menuBar);

            inspecB = addButton(inspecB_name, p1);
            inspecB.setBackground(Color.CYAN);

            add("South", p1);
        }


        private JButton addButton(String name, JPanel p) {
            JButton b = new JButton(name);
            b.addActionListener(this);
            p.add(b);
            return b;
        }


        private void doFrame(String titleName) {
            int w = 500;
            int h = 130;
            JPanel panel = new JPanel(new BorderLayout());
            JPanel p1 = new JPanel();
            if (titleName.endsWith(url_text)) {
                p1.add(new JLabel("URL :"));
                textField = new JTextField("http://foo.bar.com/foo.wav");
                textField.addActionListener(this);
            } else {
                p1.add(new JLabel(fd_text));
                String sep = String.valueOf(System.getProperty("file.separator").toCharArray()[0]);
                String text = null;
                try {
                    text = System.getProperty("user.dir") + sep;
                } catch (SecurityException ex) {
                    reportStatus(ex.toString());
                    return;
                }
                textField = new JTextField(text);
                textField.setPreferredSize(new Dimension(w - 100, 30));
                textField.addActionListener(this);
            }
            p1.add(textField);
            panel.add(p1);
            JPanel p2 = new JPanel();
            applyB = addButton(applyB_name, p2);
            addButton("取消", p2);
            panel.add("South", p2);
            frame = new JFrame(titleName);
            frame.getContentPane().add("Center", panel);
            frame.pack();
            Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
            frame.setLocation(d.width / 2 - w / 2, d.height / 2 - h / 2);
            frame.setSize(w, h);
            frame.setVisible(true);
        }


        public void actionPerformed(ActionEvent e) {
            Object object = e.getSource();
            if (object instanceof JTextField) {
                applyB.doClick();
            } else if (object instanceof JMenuItem) {
                JMenuItem mi = (JMenuItem) object;
                if (mi.getText().startsWith("文件")) {
                    doFrame("添加文件或目录");
                } else if (mi.getText().equals(url_text)) {
                    doFrame("添加URL");
                } else if (mi.getText().equals(selc_text)) {
                    int rows[] = table.getSelectedRows();
                    Vector tmp = new Vector();
                    for (int i = 0; i < rows.length; i++) {
                        tmp.add(sounds.get(rows[i]));
                    }
                    sounds.removeAll(tmp);
                    tableChanged();
                } else if (mi.getText().equals(all_text)) {
                    sounds.clear();
                    tableChanged();
                }
            } else if (object instanceof JButton) {
                JButton button = (JButton) e.getSource();
                if (button.getText().equals(applyB_name)) {
                    String name = textField.getText().trim();
                    if (name.startsWith("http") || name.startsWith("file")) {
                        try {
                            sounds.add(new URL(name));
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                        ;
                    } else {
                        loadCP(name);
                    }
                    tableChanged();
                } else if (button.getText().equals("取消")) {
                    frame.dispose();
                    frame = null;
                    errStr = null;
                    playbackMonitor.repaint();
                } else if (button.getText().equals(inspecB_name)) {
                    CPText txt = new CPText();
                    // Locate the text viewer.
                    txt.setLocation(200, 200);
                    // Set content of text viewer.
                    num = table.getSelectedRow();
                    if (num == -1) {
                        JOptionPane.showMessageDialog(this, "请选择在表中一个文件。");
                        return;
                    }
                    String path = ((File) sounds.get(num)).getAbsolutePath();
                    txt.loadCPText(path);

                    txt.setVisible(true);
                }
                startB.setEnabled(sounds.size() != 0);
            }
        }

        public void tableChanged() {
            table.tableChanged(new TableModelEvent(dataModel));
        }
    }  // End CPTable


    /**
     * Animation thread for when an audio file loads.
     */
    class Loading extends Thread {

        double extent;
        int incr;

        public void run() {
            extent = 360.0;
            incr = 10;
            while (true) {
                try {
                    sleep(99);
                } catch (Exception ex) {
                    break;
                }
                playbackMonitor.repaint();
            }
        }

        public void render(Dimension d, Graphics2D g2) {
            if (isAlive()) {
                FontRenderContext frc = g2.getFontRenderContext();
                TextLayout tl = new TextLayout("加载中", g2.getFont(), frc);
                float sw = (float) tl.getBounds().getWidth();
                tl.draw(g2, d.width - sw - 45, d.height - 10);
                double x = d.width - 33, y = d.height - 30, ew = 25, eh = 25;
                g2.draw(new Ellipse2D.Double(x, y, ew, eh));
                g2.fill(new Arc2D.Double(x, y, ew, eh, 90, extent, Arc2D.PIE));
                if ((extent -= incr) < 0) {
                    extent = 350.0;
                }
            }
        }
    }


    /**
     * Animation thread for the contributors of Java Sound.
     */
    class Credits extends Thread {

        int x;
        Font font16 = new Font("serif", Font.PLAIN, 16);
        String contributors = "作者：陈宏葆  单位：西安交通大学软件学院";
        int strWidth = getFontMetrics(font16).stringWidth(contributors);

        public void run() {
            x = -999;
            while (!playbackMonitor.isShowing()) {
                try {
                    sleep(999);
                } catch (Exception e) {
                    return;
                }
            }
            for (int i = 0; i < 100; i++) {
                try {
                    sleep(99);
                } catch (Exception e) {
                    return;
                }
            }
            while (true) {
                if (--x < -strWidth) {
                    x = playbackMonitor.getSize().width;
                }
                playbackMonitor.repaint();
                try {
                    sleep(99);
                } catch (Exception ex) {
                    break;
                }
            }
        }

        public void render(Dimension d, Graphics2D g2) {
            if (isAlive()) {
                g2.setFont(font16);
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_OFF);
                g2.drawString(contributors, x, d.height - 5);
            }
        }
    }

    /**
     * Text file viewer for ControlPanel.
     */
    class CPText extends JFrame implements ActionListener {

        JTextArea text;
        JButton saveB;

        public String text_path = "";

        private final static int WIDTH = 310;
        private final static int HEIGHT = 270;
        private final static int ROWS = 15;
        private final static int COLS = 30;

        private final String saveB_name = "保存";
        private final String DIA_TITLE = "文本查看器";

        public CPText() {
            this.setLayout(new FlowLayout(FlowLayout.CENTER));

            text = new JTextArea();
            text.setColumns(COLS);
            text.setRows(ROWS);
            saveB = new JButton(saveB_name);
            saveB.setBackground(Color.CYAN);
            saveB.addActionListener(this);

            // Add scrolling to JTextArea.
            JScrollPane scrollPane = new JScrollPane(text);
            EmptyBorder eb = new EmptyBorder(5, 5, 2, 5);
            scrollPane.setBorder(new CompoundBorder(eb, new EtchedBorder()));

            this.add(scrollPane);
            this.add(saveB);
            this.setSize(WIDTH + 50, HEIGHT + 50);
            this.setTitle(DIA_TITLE);
            this.setResizable(false);
            this.text.setLineWrap(true);

            this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        }

        public void setCPFile(String p) {
            text_path = p;
        }

        public void addCPText(String t) {
            if (t == null) {
                return;
            }

            text.append(t);
        }

        public boolean loadCPText(String f) {
            setCPFile(f);

            File file = new File(text_path);
            if (file.exists() == false) {
                int res = JOptionPane.showConfirmDialog(this, "文件\'" + text_path + "\'不存在，需要创建吗？");
                if (res == JOptionPane.OK_OPTION) {
                    try {
                        file.createNewFile();
                    } catch (IOException e) {
                        JOptionPane.showMessageDialog(this, "不能创建文件\'" + text_path + "\'。");
                        return false;
                    }
                }
            }

            if (file.canRead() == false) {
                file.setReadable(true);
            }

            try {

                FileInputStream fis = new FileInputStream(file);
                InputStreamReader isr = new InputStreamReader(fis);
                BufferedReader br = new BufferedReader(isr);

                String tmp = null;
                while ((tmp = br.readLine()) != null) {
                    addCPText(tmp + "\n");
                }

                br.close();
                isr.close();
                fis.close();

            } catch (FileNotFoundException e) {
                JOptionPane.showMessageDialog(this, "找不到文件\'" + text_path + "\'。");
                return false;
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "文件\'" + text_path + "\'读取错误。");
                return false;
            }

            return true;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Object src = e.getSource();
            if (src instanceof JButton) {
                JButton bt = (JButton) src;
                if (bt.getText().equals(saveB_name)) {

                    File f = new File(text_path);
                    if (f.exists() == false) {
                        try {
                            f.createNewFile();
                        } catch (IOException e1) {
                            JOptionPane.showMessageDialog(this, "不能创建文件\'" + text_path + "\'。");
                            return;
                        }
                    }
                    f.setWritable(true);

                    FileWriter fw = null;
                    try {
                        fw = new FileWriter(f);
                    } catch (IOException e1) {
                        JOptionPane.showMessageDialog(this, "不能写入文件\'" + text_path + "\'。");
                        return;
                    }
                    PrintWriter pw = new PrintWriter(fw);
                    if (text == null) {
                        JOptionPane.showMessageDialog(this, "内部错误。");
                        return;
                    }

                    String t = text.getText();
                    pw.print(t);
                    pw.flush();
                    pw.close();
                }


            }

        }
    }


    public static void main(String args[]) {
        String media = "./tmp";
        // Make directory if it does not exist or not a directory.
        File dir = new File(media);
        if (dir.exists() == false || dir.isDirectory() == false) {
            JOptionPane.showMessageDialog(null, "目录\'" + media + "\'不存在，已经创建。");
            dir.mkdir();
        }

        final ControlPanel controlPanel = new ControlPanel(media);
        controlPanel.open();
        JFrame f = new JFrame("语音合成系统控制台");
        f.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }

            public void windowIconified(WindowEvent e) {
                if (controlPanel != null && controlPanel.credits != null) {
                    controlPanel.credits.interrupt();
                }
            }
        });
        f.getContentPane().add("Center", controlPanel);
        f.pack();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int w = 750;
        int h = 340;
        f.setLocation(screenSize.width / 2 - w / 2, screenSize.height / 2 - h / 2);
        f.setSize(w, h);
        f.setVisible(true);

        return;
    }
} 
