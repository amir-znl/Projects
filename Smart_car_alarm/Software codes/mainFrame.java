
/*
 * Author: Amir Zeinali
 * Project : smart gps tracking, Embedded sytems class.
 * This is the main frame where everything actually exists. The right panel,
 * the map and safe Area.
 */


package smartGPS;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import javafx.geometry.Side;
import net.miginfocom.swing.MigLayout;

public class mainFrame {

	static JFrame mFrame;
	static JPanel mainPanel;
	static JPanel sidePanel;
	static JPanel bottomPanel;

	static JLabel altitude_label;
	static JLabel altitude_title_label;
	static JLabel speed_label;
	static JLabel speed_title_label;
	static JLabel numSat_label;
	static JLabel numSat_title_label;
	static JLabel hdop_label;
	static JLabel hdop_title_label;
	static JLabel lastupdate_label;
	static JLabel lastupdate_title_label;
	static JLabel lastServerInteraction_Label;
	static JLabel timeJLabel;
	
	static mapViewer mpv;
	public static ImageIcon gpsIcon = new ImageIcon("files//gps_world.png");
	
	static JButton go_locationButton;
	static JButton setSafeRegion_Button;

	public mainFrame(String altitude, String speed, String numSat, String hdop, String lastUpdate) {
		mpv = new mapViewer();
		mFrame = mpv.getFrame();
		initialize(altitude, speed, numSat, hdop, lastUpdate);
		mFrame.setVisible(true);

	}
	
	public static  JFrame getFrame() {
		return mFrame;
	}
	
	public static  JPanel getButtomPanel() {
		return bottomPanel;
	}

	private void initialize(String altitude, String speed, String numSat, String hdop, String lastUpdate) {

		sidePanel = new JPanel();
		BoxLayout bxLayout = new BoxLayout(sidePanel, BoxLayout.Y_AXIS);
		
		sidePanel.setLayout(bxLayout);

		altitude_title_label = new JLabel("ارتفاع از سطح دریا: ");
		altitude_title_label.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
		sidePanel.add(altitude_title_label);
		
		altitude_label = new JLabel(altitude + " متر");
		altitude_label.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
		sidePanel.add(altitude_label);
		
		speed_title_label = new JLabel("سرعت:  ");
		speed_title_label.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
		sidePanel.add(speed_title_label);
		
		speed_label = new JLabel(speed + " کیلومتر بر ساعت");
		speed_label.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
		sidePanel.add(speed_label);
		
		lastupdate_title_label = new JLabel("زمان آخرین دریافت به GMT (Tehran +3:30): ");
		lastupdate_title_label.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
		sidePanel.add(lastupdate_title_label);
		
		lastupdate_label = new JLabel(lastUpdate);
		lastupdate_label.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
		sidePanel.add(lastupdate_label);
		
		numSat_title_label = new JLabel("تعداد ماهوارها: ");
		numSat_title_label.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
		sidePanel.add(numSat_title_label);
		
		numSat_label = new JLabel(numSat);
		numSat_label.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
		sidePanel.add(numSat_label);
		
		hdop_title_label = new JLabel("نمره دقت: ");
		hdop_title_label.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
		sidePanel.add(hdop_title_label);
		
		hdop_label = new JLabel(hdop);
		hdop_label.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
		sidePanel.add(hdop_label);

		go_locationButton = new JButton("برو به مکان وسیله");
		go_locationButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				mpv.goto_location();

			}
		});
		// go_locationButton.setForeground(Color.WHITE);
		// go_locationButton.setBackground(Color.BLUE);

		sidePanel.add(go_locationButton);
		
		JLabel emptyJLabel  = new JLabel("    "); 
		emptyJLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 2, 0));
		sidePanel.add(emptyJLabel);
		
		setSafeRegion_Button = new JButton("تنظیم نقطه امن");
		setSafeRegion_Button.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				new set_safeRegion_frame(mpv.getMapViewer(), mpv.getWaypointPainter()); 
				
			}
		});
		
		sidePanel.add(setSafeRegion_Button);
		sidePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
		
		bottomPanel = new JPanel();
		BoxLayout bbLayout = new BoxLayout(bottomPanel, BoxLayout.Y_AXIS);
		bottomPanel.setLayout(bbLayout);
		JPanel labelJPanel = new JPanel();
		JPanel labelJPanel2 = new JPanel();
		lastServerInteraction_Label = new JLabel("آخرین دریافتی از سرور در زمان زیر بوده است: ");
		lastServerInteraction_Label.setAlignmentX(JLabel.LEFT_ALIGNMENT);
		timeJLabel = new JLabel(launcher.fromattedDate);
		timeJLabel.setAlignmentX(JLabel.LEFT_ALIGNMENT);
		labelJPanel.add(lastServerInteraction_Label);
		labelJPanel2.add(timeJLabel);
		bottomPanel.add(labelJPanel);
		bottomPanel.add(labelJPanel2);
		
		
		mainPanel = new JPanel(new BorderLayout());
		mainPanel.add(sidePanel, BorderLayout.CENTER);
		mainPanel.add(bottomPanel, BorderLayout.SOUTH);
		mFrame.add(mainPanel, BorderLayout.CENTER);
		mFrame.setIconImage(gpsIcon.getImage());

	}
	
	public static void mainFrame_refresh(String altitude2, String speed2, String numSat2, String hdop2, String lastUpdate2) {
		altitude_label.setText(altitude2 + " متر");
		speed_label.setText(speed2 + " کیلومتر بر ساعت");
		lastupdate_label.setText(lastUpdate2);
		numSat_label.setText(numSat2);
		hdop_label .setText(hdop2);
		timeJLabel.setText(launcher.fromattedDate);
		
		mFrame.revalidate();
		
	}

}
