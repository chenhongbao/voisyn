/**
 * 
 */
package chb.main;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import javax.sound.sampled.AudioFormat;
import javax.swing.JFrame;

import org.math.plot.Plot2DPanel;


import chb.Utility;
import chb.database.Corpus;
import chb.database.DataSource;
import chb.segment.CWMergerRules;
import chb.segment.CWPoint;
import chb.segment.CWSEngine;
import chb.segment.CWSegment;
import chb.segment.CWSolverDP;
import chb.segment.CWWord;
import chb.segment.CWWordComparater;
import chb.wave.Wave;
import chb.plot.*;


/**
 * @author Administrator
 *
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
//		try {
//			testSegment();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
		
		//testPlot();
		//testPlot2();
		//AsciiChineseNumber.test();
		
		System.out.println(Short.MAX_VALUE);
		System.out.println(Short.MIN_VALUE);
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
		for(int i=0; i<x.length;++i)
			System.out.println(x[i].X+"-"+x[i].Y);
	}
	
	@SuppressWarnings("unused")
	private static void printX(double[] x) {
		for(int i=0; i<x.length;++i)
			System.out.println(x[i]);
	}



	@SuppressWarnings("unused")
	private static void testPlot2() {
		
		double[] x = CData.GetRange(0.0D, 1.0D, 50.0D);
		double[] y = new double[x.length];
		
		for(int i =0; i<y.length;++i) {
			y[i] = x[i]*x[i];
		}
		
		double[][] z = new double[y.length][x.length];
		
		for(int i =0; i<y.length; ++i)
			for(int j=0; j<x.length; ++j)
				z[j][i] = x[j]*y[i];
		Plot2DPanel panel = new Plot2DPanel();
		panel.addHistogramPlot("histogram", new double[]{0.1, 0.30, 0.5, 0.6, 0.65, 0.8, 0.9} , 50);
		//panel.addScatterPlot("Scatter", x, y);
		
		//panel.addLinePlot("Line", x, y);
		
		JFrame  frame= new JFrame("Histogram");
		frame.setContentPane(panel);
		frame.setSize(500, 600);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
		
		
	}

	public static void testPlot() {
		double[] x = CData.GetRange(0.0D, 1.0D, 50.0D);
		double[] y = new double[x.length];
		
		for(int i =0; i<y.length;++i) {
			y[i] = x[i]*x[i];
		}
		
		double[][] z = new double[y.length][x.length];
		
		for(int i =0; i<y.length; ++i)
			for(int j=0; j<x.length; ++j)
				z[j][i] = x[j]*y[i];
		
		CData data = new CData(x, y, null);
		CPlot plot = CPlot.Plot("Test Plot", CPlot.EAST, data, false);
		
		data = new CData(x, y, z);
		CPlot.Plot("Test Plot", CPlot.EAST, data, true);
		
		for(int i =0; i<y.length;++i) {
			y[i] = -x[i]*x[i];
		}
		
		data = new CData(x, y, null);
		CPlot.Plot(plot, "Test Plot-2", data, true);
		
		
	}
	
	public static void testText() throws Exception {
        File file = new File("E:\\labdata\\segtest.txt");
        if(file.canRead() == false)
        	file.setReadable(true);
        
        String text = "";
        
        //FileReader reader = new FileReader(file);
        InputStreamReader  reader = new InputStreamReader(new FileInputStream("E:\\labdata\\segtest_utf8.txt"), "UTF8");
        System.out.println(reader.getEncoding());
        
        int n = 0;
        char[] buffer = new char[1000];
        n = reader.read(buffer);
        while(n>0) {
        	for(int i=0; i<n; ++i) 
        		text += buffer[i];
        	
        	n = reader.read(buffer);
        }
        
        reader.close();
        
        System.out.println(text);
        
	}
	
	public static void testSegment() throws Exception {
		DataSource conn = new DataSource();
		conn.DbAddress="202.117.15.72";
		conn.DataBase="wordbase";
		conn.User="microcore";
		conn.Password="19871013";
		conn.Encoding="utf8";
		
		conn.Open();
		//conn.Close();
		
		System.out.println("Start to segmentate.");
		
        CWSEngine engine = new CWSEngine();
        engine.ConnectionString = conn.ConnectionString;
       
        String text = "";
        
        File file = new File("E:\\labdata\\segtest.txt");
        if(file.canRead() == false)
        	file.setReadable(true);
        
        InputStreamReader  reader = new InputStreamReader(new FileInputStream("E:\\labdata\\segtest_utf8.txt"), "UTF8");
        
        int n = 0;
        char[] buffer = new char[1000];
        n = reader.read(buffer);
        while(n>0) {
        	for(int i=0; i<n; ++i) 
        		text += buffer[i];
        	
        	n = reader.read(buffer);
        }
        
        reader.close();
        
        
        File fileout = new File("E:\\labdata\\segtest_split.txt");
        fileout.createNewFile();
        if(fileout.canWrite() == false)
        	fileout.setWritable(true);
        
        FileWriter writer = new FileWriter(fileout);
        
        text = Utility.CleanText(text);
        
        engine.SetText(text);

        engine.Split(9);

        for(CWPoint pt: engine.Points)
        {
            for(CWSegment sg: pt.Segments)
            {
                writer.write(sg.GetString()+" | ");
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

        for (int i = 0; i < words.size(); ++i)
        {
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

        for (int i = 0; i < words.size(); ++i)
        {
            writer3.write(words.get(i).Word);
            writer3.write("\n\r*************\n\r");
        }

        writer3.flush();
        writer3.close();
        System.out.println("结束合并。");

	}
	
	public static void testJDBC() throws SQLException {
		
		DataSource conn = new DataSource();
		conn.DbAddress="202.117.15.72";
		conn.DataBase="wordbase";
		conn.User="microcore";
		conn.Password="19871013";
		conn.Encoding="utf8";
		
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
		String s ="yī";
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
		wave.BufferSize = 1024*128;
		
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
			System.out.print(e.getMessage());
			e.printStackTrace();
		}
		
		for(short i: shortbuffer) {
			fileout.write(i+"\n");
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
		+wave.ByteArrayOut.toByteArray().length 
		+ "Number of shorts: "+ shortbuffer.length);
		
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
			wave.ReadFrom("E:\\labdata\\wo_short.wav");
			wave.AsynPlay();
			System.out.println("AsynPay().");
			wave.AsynJoin();
			
			wave.SetBytes(shortbuffer);
			wave.Play();
			
			URL  url = new URL("http://localhost:8080/waves/sent.wav");
			wave.ReadFrom(url);
			wave.Play();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

}



