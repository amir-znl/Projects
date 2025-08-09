/*
 * Author: Amir Zeinali
 * Project : smart gps tracking, Embedded sytems class.
 * This is the class which calls other frames, here main frame and
 * start the program.
 */

package smartGPS;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import com.fasterxml.jackson.databind.JsonNode;
import com.formdev.flatlaf.FlatLightLaf;
import javax.swing.Timer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class launcher {
	static JsonNode jsonNode = null;
	static String altitude;
	static String speed;
	static String numSat;
	static String hdop;
	static String lastUpdate;
	static double lat;
	static double lng;
	static LocalDateTime  lastRecieve_time;
	static String fromattedDate;
	static DateTimeFormatter formatter;
	static boolean is_inside;
	static JPanel situationPanel;
	static  JLabel situationLabel;
	
	static checkPosition checkPos;
	public static void main(String[] args) {

		System.out.println("GETTING DATA....");
		formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		try {
			getServerData gtData = new getServerData("2982071", "J36CJ25QN3KXBKGV");
			jsonNode = gtData.getJsonNode();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println(e.getCause());
			e.printStackTrace();

		}
		JsonNode feedsArray = jsonNode.get("feeds");
		if (jsonNode != null) {
			System.out.println("got Data!!!!!");
			if (feedsArray != null && feedsArray.isArray() && feedsArray.size() > 0) {
			JsonNode lastFeedNode = feedsArray.get(feedsArray.size() - 1);
			lat = lastFeedNode.get("field1").asDouble();
			System.err.println(lat);
			lng = lastFeedNode.get("field2").asDouble();
			System.err.println(lng);
			speed = lastFeedNode.get("field3").asText();
			altitude = lastFeedNode.get("field4").asText();
			System.err.println(altitude);
			lastUpdate = lastFeedNode.get("field5").asText();
			System.err.println(lastUpdate);
			numSat = lastFeedNode.get("field7").asText();
			hdop = lastFeedNode.get("field8").asText();}
			
			
			
			
			
			
		} else {
			System.err.println("not able to get data");
		}
		
		

		try {
			UIManager.setLookAndFeel(new FlatLightLaf());
		} catch (UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}

		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				
				lastRecieve_time = LocalDateTime.now();
			    fromattedDate = lastRecieve_time .format(formatter);
				new mainFrame(altitude,speed, numSat,hdop, lastUpdate);
				check_ifInside();
				mainFrame.mpv.update_location(lat, lng);
				set_safeRegion_frame.draw_the_region();
				
				
				Timer timer = new Timer(21000, new ActionListener() {
					
					@Override
					public void actionPerformed(ActionEvent e) {
						action_loop();
						check_ifInside();
						
					}
				});
				
				timer.start();
				

			}
		});

	}
	
	private static void action_loop() {
		System.out.println("GETTING DATA....");

		try {
			getServerData gtData = new getServerData("2982071", "J36CJ25QN3KXBKGV");
			jsonNode = gtData.getJsonNode();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		JsonNode feedsArray = jsonNode.get("feeds");
		if (jsonNode != null) {
			System.out.println("got Data!!!!!");
			if (feedsArray != null && feedsArray.isArray() && feedsArray.size() > 0) {
			JsonNode lastFeedNode = feedsArray.get(feedsArray.size() - 1);
			lat = lastFeedNode.get("field1").asDouble();
			System.err.println(lat);
			lng = lastFeedNode.get("field2").asDouble();
			System.err.println(lng);
			speed = lastFeedNode.get("field3").asText();
			altitude = lastFeedNode.get("field4").asText();
			System.err.println(altitude);
			lastUpdate = lastFeedNode.get("field5").asText();
			System.err.println(lastUpdate);
			numSat = lastFeedNode.get("field7").asText();
			hdop = lastFeedNode.get("field8").asText();}
			
			
			
		} else {
			System.err.println("not able to get data");
		}
		mainFrame.mainFrame_refresh(altitude,speed, numSat,hdop, lastUpdate);
		mainFrame.mpv.update_location(lat, lng);
		set_safeRegion_frame.draw_the_region();
		lastRecieve_time = LocalDateTime.now();
		fromattedDate = lastRecieve_time .format(formatter);
	}

	public static void check_ifInside() {
		
		try {
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(new File("files/safe_region.dat")));
			@SuppressWarnings("unchecked")
			Map<String, String> dataMap = (Map<String, String>) ois.readObject();
			checkPos = new checkPosition(lat, lng, Double.valueOf(dataMap.get("lat")), Double.valueOf(dataMap.get("lng"))
					, Double.valueOf(dataMap.get("radius")));
			is_inside = checkPos.in_safeRegion();
			 JFrame theFrame =  mainFrame.getFrame();
			 JPanel theBottomPanel = mainFrame.getButtomPanel();
			 
			 if (situationPanel != null) {
				 theBottomPanel.remove(situationPanel);
			 }
			
			 
			situationPanel = new JPanel();
			 situationLabel = new JLabel();
			 if (is_inside) {
				 situationPanel.removeAll();
				 situationPanel.setBackground(Color.GREEN);
				 situationLabel.setText("وسیله شما در محدوده ایمنی قرار دارد.");
				 situationLabel.setIcon(new ImageIcon("files/ok_situation.png"));
				 situationLabel.setVerticalTextPosition(JLabel.CENTER);
				 situationLabel.setHorizontalTextPosition(JLabel.LEFT);
				 situationPanel.add(situationLabel);
			 }else {
				 situationPanel.removeAll();
				 situationPanel.setBackground(Color.RED);
				 situationLabel.setText("وسیله ممکن است در خطر باشد؛ خارج از محدوده ایمنی!");
				 situationLabel.setIcon(new ImageIcon("files/warning.png"));
				 situationLabel.setVerticalTextPosition(JLabel.CENTER);
				 situationLabel.setHorizontalTextPosition(JLabel.LEFT);
				 situationPanel.add(situationLabel);
			 }
			 
			 theBottomPanel.add(situationPanel);
			 theBottomPanel.repaint();
			 theBottomPanel.revalidate();
			 theFrame.revalidate();
			 
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
}
