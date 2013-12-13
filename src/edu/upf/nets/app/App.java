package edu.upf.nets.app;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.table.DefaultTableModel;

import org.apache.log4j.Logger;

import edu.upf.nets.mercury.pojo.TracerouteSession;



public class App extends JFrame {

	
	private static final long serialVersionUID = -8256897075061792286L;
	private static final Logger log = Logger
			.getLogger(App.class.getName());
	
	//variables
	private static final int POOL_MIN = 0;
	private static final int POOL_MAX = 50;
	private static final int POOL_INIT = 10;
	private Timer timer = new Timer();
	private String internetAnalyticsURL = "http://inetanalytics.nets.upf.edu/getUrls";
	private String mercuryUploadTrace = "http://mercury.upf.edu/mercury/api/traceroute/uploadTrace";
	private String mercuryAddSession = "http://mercury.upf.edu/mercury/api/traceroute/addTracerouteSession";
	private String mercuryGetSession = "http://mercury.upf.edu/mercury/api/traceroute/getTracerouteSession/";
	
	//GUI components
	private JMenuBar menuBar;
	private JMenu menu;
	private JMenuItem menuItemAbout;
	private JMenuItem menuItemHelp;
	
	private JTabbedPane tabbedPane;
	
	private JButton bURLs;
	private JLabel lURLsServer;
	private JTextField tfURLsServer;
	private JLabel lLocale;
	private JTextField tfLocale;
	private JTextArea taURLs;
	
	private JButton bTraceroute;
	private JButton bScheduleTraceroute;
	private JButton bStopScheduleTraceroute;
	private JLabel lScheduleTraceroute;
	private JTextField tfScheduleTraceroute;
	private JLabel lMercuryServer;
	private JTextField tfMercuryServer;
	private JTextArea taTraceroute;
	
	private JLabel lParallelTraceroute;
	private JSlider sParallelTraceroute;
	
	
	private DefaultTableModel model;
	
	private JLabel lTracerouteSessionId;
	private JTextField tfTracerouteSessionId;
	private JLabel lTracerouteSessionAuthor;
	private JTextField tfTracerouteSessionAuthor;
	private JTextArea taTracerouteSessionDescription; 
	private JTextArea taTracerouteSessionOutput;
	
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				new App("Internet Analytics - Traceroute App");
			}
		});
	}
	
    public App(String title) {
    	super(title);
    	
		try {
			URL url = ClassLoader.getSystemResource("edu/upf/nets/app/images/GraphBarColor_32.png");
	    	Toolkit kit = Toolkit.getDefaultToolkit();
	    	Image img = kit.createImage(url);
	    	this.setIconImage(img);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

    	//We set the menu bar
    	setJMenuBar(createJMenuBar(this));
    	
    	//We set the tabs
    	tabbedPane = new JTabbedPane();
		tabbedPane.addTab("Main", getMainPanel());
		tabbedPane.addTab("Log", getLogPanel());
		tabbedPane.addTab("Session", getSessionPanel());
	
		//We set the frame
		setLookAndFeel();
		add(tabbedPane);
		setSize(640, 860);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setVisible(true);
    }
    
    protected JPanel getMainPanel(){
		//Here we add the components to a JPanel that will be added to the JFrame
		JPanel myPanel = new JPanel();
		myPanel.add(createGetURLsComponents());
		myPanel.add(createGetURLsTextArea());
		myPanel.add(createExecuteTracerouteComponents());
		myPanel.add(createScheduledTracerouteComponents());
		myPanel.add(createParallelTracerouteComponents());
		
		myPanel.add(createTracerouteTextArea());
		
		return myPanel;
    }
    


    protected JMenuBar createJMenuBar(final JFrame frame) {
    	menuBar = new JMenuBar();
    	menu = new JMenu("Information");
    	
    	menuItemAbout = new JMenuItem("About");
    	menuItemAbout.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ImageIcon ii = new ImageIcon(
						ClassLoader.getSystemResource("edu/upf/nets/app/images/GraphBarColor_64.png"));
				JOptionPane.showMessageDialog(frame, "This tool is done by Manuel Palacin and Alex Bikfalvi\n" +
						"at NeTS Research Group under the Mercury Project.\n " +
					    "Please visit us at http://mercury.upf.edu", "About", JOptionPane.PLAIN_MESSAGE, ii);
				
			}
    	});
    	menuItemHelp = new JMenuItem("Help");
    	menuItemHelp.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
		    	JOptionPane.showMessageDialog(frame,
					    "This tool allows you to traceroute a set of customized or popular web sites.\n" +
					    "Finally all results are sent to http://mercury.upf.edu\n" +
					    "Send feedback to: manuel.palacin@upf.edu");
				
			}
    	});
    	menu.add(menuItemAbout);
    	menu.add(menuItemHelp);
    	
    	menuBar.add(menu);
    	return menuBar;
    }
    
    
    protected JComponent createGetURLsComponents() {
    	JPanel panel = new JPanel(new FlowLayout(FlowLayout.TRAILING));
    	
    	//We set the default locale
    	tfLocale = new JTextField(this.getLocale().getCountry());
    	lLocale = new JLabel("Locale:");
    	//We set the default getURLs server
    	lURLsServer = new JLabel("URLs server:");
    	tfURLsServer = new JTextField(internetAnalyticsURL);
		bURLs = new JButton("get URLs");
		bURLs.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				//We load the text area with urls
				taURLs.setText(
						getURLs(tfLocale.getText(), tfURLsServer.getText())
				);
			}
		});

		panel.add(lLocale);
		panel.add(tfLocale);
		panel.add(lURLsServer);
		panel.add(tfURLsServer);
		panel.add(bURLs);
		
    	return panel;
    }
    
    protected JComponent createGetURLsTextArea() {
    	JPanel panel = new JPanel(new FlowLayout(FlowLayout.TRAILING));
    	
    	//We pre-load the textarea with URLs
    	taURLs = new JTextArea(getURLs(tfLocale.getText(), tfURLsServer.getText()));
    	JScrollPane spURLs = new JScrollPane(taURLs);
		spURLs.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		spURLs.setPreferredSize(new Dimension(480, 160));
		spURLs.setBorder(BorderFactory.createTitledBorder("URLs"));
    	
		panel.add(spURLs);
		
    	return panel;
    }
    
    protected JComponent createExecuteTracerouteComponents() {
    	JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    	
		lMercuryServer = new JLabel("Mercury server:");
		tfMercuryServer = new JTextField(null, mercuryUploadTrace, 30);
		bTraceroute = new JButton("execute Traceroute!");
		bTraceroute.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				executeTraceroute();
			}
		});
		
		panel.add(lMercuryServer);
		panel.add(tfMercuryServer);
		panel.add(bTraceroute);

		return panel;
    }
    
    protected void executeTraceroute(){
    	
    	if(! tfTracerouteSessionId.getText().equals("")){
    		TracerouteSession tracerouteSession = new TracerouteSession();
    		tracerouteSession.setSessionId(tfTracerouteSessionId.getText());
    		tracerouteSession.setAuthor(tfTracerouteSessionAuthor.getText());
    		tracerouteSession.setDescription(taTracerouteSessionDescription.getText());
    		tracerouteSession.setDateStart(new Date());
    		addTracerouteSession(tracerouteSession);
    	}
    	
		//Here we prepare a ThreadPool with X Threads
    	int pool = sParallelTraceroute.getValue();
    	print("Parallel processes: "+pool);
		ExecutorService executorService = Executors.newFixedThreadPool(pool);
		
		String[] destinations = taURLs.getText().split("\n");
		for (String destination : destinations) {
			print(destination);
			TracerouteWorker tw = new TracerouteWorker(
					destination, 
					tfMercuryServer.getText(), 
					taTraceroute, 
					model, 
					tfTracerouteSessionId.getText());
			executorService.submit(tw);
			//tw.execute(); //We don't need this if we use ThreadPool
		}
		

    }
    
    protected JComponent createScheduledTracerouteComponents() {
    	JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    	
		lScheduleTraceroute = new JLabel("Schedule time in seconds:");
		tfScheduleTraceroute = new JTextField(null, "3600", 10);
		
		bScheduleTraceroute = new JButton("Schedule Traceroute!");
		bScheduleTraceroute.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				
				TimerTask timerTask = new TimerTask(){
					@Override
					public void run() {
						executeTraceroute();
					}
				};
				 
				timer.scheduleAtFixedRate(timerTask, 0, 
						Long.parseLong(tfScheduleTraceroute.getText())*1000 ); 
			}
		});
    	
		bStopScheduleTraceroute = new JButton("Stop Scheduler!");
		bStopScheduleTraceroute.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				timer.cancel();
			}
		});
		
		panel.add(lScheduleTraceroute);
		panel.add(tfScheduleTraceroute);
		panel.add(bScheduleTraceroute);
		panel.add(bStopScheduleTraceroute);
		
		return panel;
    }	
    
    protected JComponent createParallelTracerouteComponents() {
    	JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    	
		lParallelTraceroute = new JLabel("Paralel processes:");
		sParallelTraceroute = new JSlider(JSlider.HORIZONTAL,
                POOL_MIN, POOL_MAX, POOL_INIT);
		
		sParallelTraceroute.setMajorTickSpacing(10);
		sParallelTraceroute.setMinorTickSpacing(1);
		sParallelTraceroute.setPaintTicks(true);
		sParallelTraceroute.setPaintLabels(true);
		
		panel.add(lParallelTraceroute);
		panel.add(sParallelTraceroute);
		return panel;
    }
    

    
    
    protected JComponent createTracerouteTextArea() {
    	JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    	
		taTraceroute = new JTextArea();
		//We add a document listener to avoid textArea to run out of memory.
		//We limit to 100 lines
		taTraceroute.getDocument().addDocumentListener( new LimitLinesDocumentListener(100));
		
		JScrollPane spTraceroute = new JScrollPane(taTraceroute);
		spTraceroute.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		spTraceroute.setPreferredSize(new Dimension(480, 320));
		spTraceroute.setBorder(BorderFactory.createTitledBorder("Output"));
    	
		panel.add(spTraceroute);
    	
		return panel;
    }	
    	
    
    protected JPanel getLogPanel(){
		//Here we add the components to a JPanel that will be added to the JFrame
		JPanel myPanel = new JPanel();
		myPanel.add(getLogTable());
		return myPanel;
    }
    
	private JComponent getLogTable(){

		model = new DefaultTableModel();
		model.setColumnIdentifiers(new String[]{"session id","destination","result","timestamp"});
		//model.addRow(new String[]{"hola","adios","hello","bye"});
		JTable table = new JTable(model);
		//tableLatency.update(tableLatency.getGraphics());
		JScrollPane spTable = new JScrollPane(table);
		spTable.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		spTable.setPreferredSize(new Dimension(480, 640));
		spTable.setBorder(BorderFactory.createTitledBorder("Results"));

		JPanel myPanel = new JPanel();
		myPanel.add(spTable);
		
		return myPanel;
	}
	
	
    protected JPanel getSessionPanel(){
		//Here we add the components to a JPanel that will be added to the JFrame
		JPanel myPanel = new JPanel();
		myPanel.add(createTracerouteSessionComponent1());
		myPanel.add(createTracerouteSessionComponent2());
		myPanel.add(createTracerouteSessionComponent3());
		myPanel.add(createTracerouteSessionComponent4());
		myPanel.add(createTracerouteSessionComponent5());
		return myPanel;
    }
	
    protected JComponent createTracerouteSessionComponent1() {
    	
    	
    	JPanel panel1 = new JPanel(new FlowLayout(FlowLayout.TRAILING));
		lTracerouteSessionId = new JLabel("Session Id:");
		tfTracerouteSessionId = new JTextField(null, "", 30);
		JButton bGenTracerouteSessionId = new JButton("Generate Session Id");
		bGenTracerouteSessionId.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				UUID sessionId = UUID.randomUUID();
				tfTracerouteSessionId.setText(sessionId.toString());
			}
		});
		panel1.add(lTracerouteSessionId);
		panel1.add(tfTracerouteSessionId);
		panel1.add(bGenTracerouteSessionId);


		
		return panel1;
    }
    
    protected JComponent createTracerouteSessionComponent2() {
    	JPanel panel2 = new JPanel(new FlowLayout(FlowLayout.TRAILING));
		lTracerouteSessionAuthor = new JLabel("Author:");
		tfTracerouteSessionAuthor = new JTextField(null, "", 20);
		panel2.add(lTracerouteSessionAuthor);
		panel2.add(tfTracerouteSessionAuthor);
		
		return panel2;
    }
    
    protected JComponent createTracerouteSessionComponent3() {
		JPanel panel3 = new JPanel(new FlowLayout(FlowLayout.TRAILING));
		taTracerouteSessionDescription = new JTextArea();
		JScrollPane spTracerouteSessionDescription = new JScrollPane(taTracerouteSessionDescription);
		spTracerouteSessionDescription.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		spTracerouteSessionDescription.setPreferredSize(new Dimension(480, 280));
		spTracerouteSessionDescription.setBorder(BorderFactory.createTitledBorder("Description"));
		panel3.add(spTracerouteSessionDescription);
		
		return panel3;
    }
    
    protected JComponent createTracerouteSessionComponent4() {
		JPanel panel4 = new JPanel(new FlowLayout(FlowLayout.CENTER));
		JButton bTracerouteSessionId = new JButton("Get Session Id");
		bTracerouteSessionId.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
		    	if(! tfTracerouteSessionId.getText().equals("")){
		    		String data = getTracerouteSession(tfTracerouteSessionId.getText());
		    		taTracerouteSessionOutput.append(data);
		    	}
			}
		});
		panel4.add(bTracerouteSessionId);
		
    	return panel4;
    }
    
    protected JComponent createTracerouteSessionComponent5() {
		JPanel panel5 = new JPanel(new FlowLayout(FlowLayout.TRAILING));
		taTracerouteSessionOutput = new JTextArea();
		JScrollPane spTracerouteSessionOutput = new JScrollPane(taTracerouteSessionOutput);
		spTracerouteSessionOutput.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		spTracerouteSessionOutput.setPreferredSize(new Dimension(480, 320));
		spTracerouteSessionOutput.setBorder(BorderFactory.createTitledBorder("Output"));
		panel5.add(spTracerouteSessionOutput);
		
    	return panel5;
    }
    
    
    
    public void setLookAndFeel(){
		try {
		    for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
		        if ("Windows".equals(info.getName())) {
		            UIManager.setLookAndFeel(info.getClassName());
		            break;
		        }
		    }
		} catch (Exception e) {
		    // If Windows is not available, you can set the GUI to another look and feel.
			print("LookAndFeel Windows not found. Using default theme.");
		}
    }

    
    
    
	private String getURLs(String locale, String serverURL){
		
		String URLs = "";
		try{

			URL url = new URL(serverURL+"?countryCode="+locale);
			HttpURLConnection connection = (HttpURLConnection) url
					.openConnection();
			connection.setRequestMethod("GET");
			connection.setDoInput(true);
			connection.setDoOutput(true);
			
			int status = connection.getResponseCode();
			if ((status == 200) || (status == 201)) {
				//read the result from the server
				BufferedReader rd  = new BufferedReader(new InputStreamReader(connection.getInputStream()));
				String line = null;
				StringBuilder sb = new StringBuilder();
		        while (( line = rd.readLine()) != null) {
		        	sb.append('\n' + line.replaceAll("http://", ""));
		        }
		        URLs = sb.toString().replaceFirst("\n", "");
			} else {
				print("Status: " + status);
				URLs = "google.com\nfacebook.com\ntwitter.com\namazon.com\nyahoo.com\n";
			}
			connection.disconnect();
			
		} catch(Exception e){
			URLs = "google.com\nfacebook.com\ntwitter.com\namazon.com\nyahoo.com\n";
		}
		
		return URLs;
	}
    
	
	private int addTracerouteSession(TracerouteSession tracerouteSession){
		int status = 0;
		try{

			URL url = new URL(mercuryAddSession);
			HttpURLConnection connection = (HttpURLConnection) url
					.openConnection();
			connection.setRequestMethod("POST");
			connection.setDoInput(true);
			connection.setDoOutput(true);

			connection.setRequestProperty("Content-Type",
					"application/json;charset=UTF-8");

			DataOutputStream wr = new DataOutputStream(
					connection.getOutputStream());
			String data = tracerouteSession.toJsonString(); 
			wr.write(data.getBytes("UTF-8"));
			wr.flush();
			wr.close();

			
			status = connection.getResponseCode();
			if ((status == 200) || (status == 201)) {
				//read the result from the server
				BufferedReader rd  = new BufferedReader(new InputStreamReader(connection.getInputStream()));
				String line = null;
				StringBuilder sb = new StringBuilder();
		        while (( line = rd.readLine()) != null) {
		        	sb.append(line);
		        }
		        print(sb.toString());
			} else {
				print("Status: " + status);
			}
			connection.disconnect();
			return status;
			
		} catch(Exception e){
			return status;
		}
	}
    
	
	private String getTracerouteSession(String sessionId){
		String data = "";
		try{

			URL url = new URL(mercuryGetSession+sessionId);
			HttpURLConnection connection = (HttpURLConnection) url
					.openConnection();
			connection.setRequestMethod("GET");
			connection.setDoInput(true);
			connection.setDoOutput(true);
			
			int status = connection.getResponseCode();
			if ((status == 200) || (status == 201)) {
				//read the result from the server
				BufferedReader rd  = new BufferedReader(new InputStreamReader(connection.getInputStream()));
				String line = null;
				StringBuilder sb = new StringBuilder();
		        while (( line = rd.readLine()) != null) {
		        	sb.append(line);
		        }
		        data = sb.toString();
			} else {
				print("Status: " + status);
				data = "{\"status\":\""+status+"\"}";
			}
			connection.disconnect();
			
		} catch(Exception e){
			data = "{\"status\":\"0\"}";
		}
		return data;
	}
	
    private void print(String msg, Object... args) {
        log.info(String.format(msg, args));
    }


}
