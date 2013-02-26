/**
 *
 */
package chb.test;

import chb.Utility;
import chb.database.Corpus;
import chb.database.DataSource;
import chb.math.Numerics;
import chb.plot.CData;
import chb.plot.CPlot;
import chb.segment.*;
import chb.synthesis.VoiceSynther;
import chb.template.TInfo;
import chb.template.metatemplate.MTEngine;
import chb.wave.Wave;
import org.math.plot.Plot2DPanel;

import javax.sound.sampled.AudioFormat;
import javax.swing.*;
import java.io.*;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;


/**
 * @author Administrator
 */
public class Program {

    /**
     * @param args
     */
    public static void main(String[] args) {
        //testWave();
        //testHash();
        //testUnicode();
        //testJDBC();
        //testText();
        //testSegment();
        //testGui();
        try {
            testTemplate();
            System.out.print("[INFO]Main function exits.\n");
        } catch (Exception e) {

            e.printStackTrace();
        }
        //testPlot();
        //testPlot2();
        //AsciiChineseNumber.test();
        //testNumerics();
        //testSynth();
        //testCorpus();

    }

    static void testTemplate() {
        /* Test MTEngine. */
        MTEngine mte = new MTEngine();
        mte.loadTemplate("./JavaSoundRun/tmpl/meta.xml");

        String[] mte_test = new String[]{"。", "，", "：", "’", "中国", "我", "中华人民", "美利坚合众国"};
        for (String s : mte_test) {
            TInfo inf = mte.query(s);
            Utility.Log("Content:" + inf.content + "\nType:" + inf.type);
            Set<Map.Entry<String, String>> set = inf.info.entrySet();
            for (Map.Entry<String, String> e : set) {
                Utility.Log("Info:" + e.getKey() + " --> " + e.getValue() + "\n");
            }
        }
    }


    static void testGui() {
        chb.gui.ControlPanel.main(new String[0]);
    }

    static void testCorpus() {
        DataSource ds = DataSource.CreateConnection("any", "wordbase", "microcore", "19871013", "UTF8");
        if (ds == null) {
            System.out.print("ds is Null.\n");
            return;
        }
        if (ds.State != DataSource.ConnectionState.Opened)
            ds.Open();

        boolean res = Corpus.IsPhraseWithConn("中国人们", ds);
        System.out.print(res + "\n");

        res = Corpus.IsAuxWithConn("哈", ds);
        System.out.print(res + "\n");

    }

    static void testSynth() throws Exception {
        Wave wave = Wave.CreateWave(Wave.DefaultFormat);
        wave.ReadFrom("wav/wo_01.wav");
        short[] data = wave.Get16Bits();

        VoiceSynther vs = new VoiceSynther();
        int[] peakx = vs.GetPeak(data);
        List<short[]> segs = vs.GetSeg(data, peakx);
        short[] z = null;
        for (short[] s : segs) {
            //CData cdata = new CData(null, s, null);
            //CPlot.Plot(cdata, true);
            z = vs.GetConnect(z, s);
        }

        CData cdata = new CData(null, z, null);
        CPlot.Plot(cdata, true);

        short[] data1 = vs.ChangeLength(1.0D, 0.5D, 0.5D, data, vs.GetPeak(data));

        cdata.setY(data);
        CPlot plot = CPlot.Plot("origin", CPlot.EAST, cdata, false);
        cdata.setY(data1);
        CPlot.Plot(plot, "narrow", cdata, true);

        short[] data2 = vs.GetLoudness(2.0, 1.5, 0.5, data, vs.GetPeak(data));
        cdata.setY(data2);
        CPlot.Plot("emotion", CPlot.EAST, cdata, true);

        wave.ReadFrom("wav/shi_01.wav");
        data = wave.Get16Bits();
        cdata.setY(data);
        CPlot.Plot("shi", CPlot.EAST, cdata, true);

        short[] data3 = vs.GetFusion(data2, vs.GetPeak(data2), data, vs.GetPeak(data), 0.2);
        cdata.setY(data3);
        CPlot.Plot("fusion", CPlot.EAST, cdata, true);

        short[] data4 = vs.TrimBefore(data, vs.GetPeak(data), 0.7, 0.3);
        cdata.setY(data4);
        CPlot.Plot("trimbefore-shi", CPlot.EAST, cdata, true);

        short[] data5 = vs.TrimAfter(data4, vs.GetPeak(data4), 0.5, 0.3);
        cdata.setY(data5);
        CPlot.Plot("trimafter-shi", CPlot.EAST, cdata, true);
    }

    static void testNumerics() {
        double[] H = Numerics.GaussWin(64, 3.0D);
        System.out.println(H.length);
        for (int i = 0; i < H.length; ++i) {
            System.out.print(i + 1);
            System.out.print("\t:\t");
            System.out.println(H[i]);
        }

        int a = 9;
        int b = 5;
        System.out.println(1.0D * b / a);

        Random rd = new Random();
        System.out.println(rd.nextInt());
        System.out.println(rd.nextDouble());

    }

    class SortPack {
        public double X = 0;
        public double Y = 0;

        SortPack() {
            X = Math.random();
            Y = Math.random();
        }
    }


    @SuppressWarnings("unused")
    private static void printX(SortPack[] x) {
        for (int i = 0; i < x.length; ++i)
            System.out.println(x[i].X + "-" + x[i].Y);
    }

    @SuppressWarnings("unused")
    private static void printX(double[] x) {
        for (int i = 0; i < x.length; ++i)
            System.out.println(x[i]);
    }


    @SuppressWarnings("unused")
    private static void testPlot2() {

        double[] x = CData.GetRange(0.0D, 1.0D, 50.0D);
        double[] y = new double[x.length];

        for (int i = 0; i < y.length; ++i) {
            y[i] = x[i] * x[i];
        }

        double[][] z = new double[y.length][x.length];

        for (int i = 0; i < y.length; ++i)
            for (int j = 0; j < x.length; ++j)
                z[j][i] = x[j] * y[i];
        Plot2DPanel panel = new Plot2DPanel();
        panel.addHistogramPlot("histogram", new double[]{0.1, 0.30, 0.5, 0.6, 0.65, 0.8, 0.9}, 50);
        //panel.addScatterPlot("Scatter", x, y);

        //panel.addLinePlot("Line", x, y);

        JFrame frame = new JFrame("Histogram");
        frame.setContentPane(panel);
        frame.setSize(500, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);


    }

    public static void testPlot() {
        double[] x = CData.GetRange(0.0D, 1.0D, 50.0D);
        double[] y = new double[x.length];

        for (int i = 0; i < y.length; ++i) {
            y[i] = x[i] * x[i];
        }

        double[][] z = new double[y.length][x.length];

        for (int i = 0; i < y.length; ++i)
            for (int j = 0; j < x.length; ++j)
                z[j][i] = x[j] * y[i];

        CData data = new CData(x, y, null);
        CPlot plot = CPlot.Plot("Test Plot-0", CPlot.SOUTH, data, false);

        data = new CData(x, y, z);
        CPlot.Plot("Test Plot-1", CPlot.SOUTH, data, true);

        for (int i = 0; i < y.length; ++i) {
            y[i] = -x[i] * x[i];
        }

        data = new CData(x, y, null);
        CPlot.Plot(plot, "Test Plot-2", data, true);

        data.setX((short[]) null);
        data.setZ((short[][]) null);
        data.setY(new double[]{1.D, 4.0D, 8.0D, 16.0D, 46.0D, 78.0D});

        CPlot.Plot("Test Plot X", CPlot.EAST, data, true);

    }

    public static void testText() throws Exception {
        File file = new File("E:\\labdata\\segtest.txt");
        if (file.canRead() == false)
            file.setReadable(true);

        String text = "";

        //FileReader reader = new FileReader(file);
        InputStreamReader reader = new InputStreamReader(new FileInputStream("E:\\labdata\\segtest_utf8.txt"), "UTF8");
        System.out.println(reader.getEncoding());

        int n = 0;
        char[] buffer = new char[1000];
        n = reader.read(buffer);
        while (n > 0) {
            for (int i = 0; i < n; ++i)
                text += buffer[i];

            n = reader.read(buffer);
        }

        reader.close();

        System.out.println(text);

    }

    public static void testSegment() throws Exception {
        DataSource conn = new DataSource();
        conn.DbAddress = "any";
        conn.DataBase = "wordbase";
        conn.User = "microcore";
        conn.Password = "19871013";
        conn.Encoding = "UTF8";

        conn.Open();

        System.out.println("Start to segmentate.");

        CWSEngine engine = new CWSEngine();
        engine.DataSrc = conn;

        String text = "";

        File file = new File("E:\\labdata\\segtest.txt");
        if (file.canRead() == false)
            file.setReadable(true);

        InputStreamReader reader = new InputStreamReader(new FileInputStream("E:\\labdata\\segtest_utf8.txt"), "UTF8");

        int n = 0;
        char[] buffer = new char[1000];
        n = reader.read(buffer);
        while (n > 0) {
            for (int i = 0; i < n; ++i)
                text += buffer[i];

            n = reader.read(buffer);
        }

        reader.close();


        File fileout = new File("E:\\labdata\\segtest_split.txt");
        fileout.createNewFile();
        if (fileout.canWrite() == false)
            fileout.setWritable(true);

        FileWriter writer = new FileWriter(fileout);

        text = Utility.CleanText(text);

        engine.SetText(text);

        engine.Split(9);

        for (CWPoint pt : engine.Points) {
            for (CWSegment sg : pt.Segments) {
                writer.write(sg.GetString() + " | ");
            }
            writer.write("\n\r******\n\r");
        }

        writer.flush();
        writer.close();
        System.out.println("结束切词。");

        File file2 = new File("E:\\labdata\\segtest_seg.txt");
        FileWriter writer2 = new FileWriter(file2);

        engine.Segmentate(CWSolverDP.CreateSolver());
        List<CWWord> words = engine.GetSegmentResult(CWWordComparater.CreateComparater());

        for (int i = 0; i < words.size(); ++i) {
            writer2.write(words.get(i).Word);
            writer2.write("\n\r*************\n\r");
        }
        writer2.flush();
        writer2.close();
        System.out.println("结束分词。");

        File file3 = new File("E:\\labdata\\segtest_merge.txt");
        FileWriter writer3 = new FileWriter(file3);

        engine.Merge(new CWMergerRules(conn));
        words = engine.GetMergeResult(CWWordComparater.CreateComparater());

        for (int i = 0; i < words.size(); ++i) {
            writer3.write(words.get(i).Word);
            writer3.write("\n\r*************\n\r");
        }

        writer3.flush();
        writer3.close();
        System.out.println("结束合并。");

    }

    public static void testJDBC() throws SQLException {

        DataSource conn = new DataSource();
        conn.DbAddress = "202.117.15.72";
        conn.DataBase = "wordbase";
        conn.User = "microcore";
        conn.Password = "19871013";
        conn.Encoding = "utf8";

        conn.Open();

        boolean res = Corpus.IsPhraseWithConn("中国你好啊", conn);
        System.out.print(res);

    }


    public static void testUnicode() {
        char c = '中';
        Character cc = Character.valueOf(c);

        System.out.println(c);
        System.out.println(cc);
    }

    public static void testHash() throws UnsupportedEncodingException {
        String s = "yī";
        long hash = Utility.Hash_01(s.getBytes("UTF-16LE"));

        System.out.println(hash);

        hash = Utility.Hash_02(s.getBytes("UTF-16LE"));
        System.out.println(hash);
    }

    public static void testWave() {

        System.out.println("Start recording.");
        AudioFormat format = new AudioFormat(Wave.DefaultEncoding,
                44100.0f, 16, 1, 2, 44100.0f, false);
        Wave wave = Wave.CreateWave(format);
        wave.BufferSize = 1024 * 128;

        try {
            wave.Record(1.0f);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        short[] shortbuffer = wave.Get16Bits();

        File file = new File("E:\\labdata\\wo_short.txt");
        try {
            file.createNewFile();
        } catch (IOException e) {

            e.printStackTrace();
        }
        PrintWriter fileout = null;
        try {
            fileout = new PrintWriter(file);
        } catch (FileNotFoundException e) {
            System.out.print(e.getStackTrace());
            e.printStackTrace();
        }

        for (short i : shortbuffer) {
            fileout.write(i + "\n");
        }
        fileout.flush();
        fileout.close();
        System.out.println("Stop recording.");

        try {
            wave.WriteTo("E:\\labdata\\wo_short.wav", Wave.DefaultFileType);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Write to file.");
        System.out.println("Number of bytes: "
                + wave.ByteArrayOut.toByteArray().length
                + "Number of shorts: " + shortbuffer.length);

        System.out.print(wave.Format.toString());
        try {
            System.out.print("\nGo to check out the wave file before it is appended.");
            System.in.read();
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        try {
            wave.Append(wave);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            wave.WriteTo("E:\\labdata\\wo_short.wav", Wave.Type.WAVE);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            wave.ReadFrom("./tmp/canon.txt.wav");
            wave.AsynPlay();
            System.out.println("AsynPay().");
            wave.AsynJoin();

            wave.SetBytes(shortbuffer);
            wave.Play();

            URL url = new URL("http://localhost:8080/waves/sent.wav");
            wave.ReadFrom(url);
            wave.Play();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}



