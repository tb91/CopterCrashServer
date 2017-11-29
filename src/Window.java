import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class Window extends JFrame implements ActionListener {
	private Server server;
	public JButton start;
	public JButton stop;
	public JButton clear;
	JList<String> log;
	public DefaultListModel<String> lModel;
	public final String version = "Version 1.5.2";
	private static final long serialVersionUID = 1L;
	public CurrentTimeDate now;
	private JScrollPane scrollPane;
	

	public Window() throws IOException {
		setLocation(100, 100);
		setSize(200, 200);

		setLayout(new BorderLayout());
		setTitle(version);

		setDefaultCloseOperation(DISPOSE_ON_CLOSE);

		this.start = new JButton("start");
		this.start.addActionListener(this);

		this.stop = new JButton("stop");
		this.stop.addActionListener(this);
		this.stop.setEnabled(false);

		this.clear = new JButton("clear");
		this.clear.addActionListener(this);

		this.now = new CurrentTimeDate();

		lModel = new DefaultListModel<String>();
		// this.lModel = new DefaultListModel<String>();
		this.log = new JList<String>(lModel);

		this.log.setVisible(true);

		scrollPane = new JScrollPane(this.log);
		scrollPane.setVerticalScrollBarPolicy(22);

		FlowLayout flow = new FlowLayout();

		JPanel buttons = new JPanel();
		buttons.setLayout(flow);
		buttons.add(this.start);
		buttons.add(this.stop);
		buttons.add(this.clear);

		add(scrollPane, "Center");
		add(buttons, "Last");

		pack();
		setSize(500, 200);
		setVisible(true);
		start.doClick();
	}
	
	@Override
	public void dispose(){
		
		if(server!=null && server.run){
			stop.doClick();
		
			try {
				synchronized (this) {
					this.wait();		//server notifies after closing server
				}

			      
			} catch (InterruptedException e) {
				
				e.printStackTrace();
			}
		}
		super.dispose();
		System.exit(0);
	}
	

		
		
		
	public static void main(String[] args) throws IOException {
		new Window();
	}

	public void actionPerformed(ActionEvent arg) {
		if (arg.getActionCommand().equals("start")) {
			
			try {
				if(this.server==null){
					this.server = new Server(this, 24532);
				}else{
					server.start(24532);
				}
				

				this.stop.setEnabled(true);
				this.start.setEnabled(false);
				putLog("Server started");

			} catch (IOException e) {
				putLog("Server could not be started: " + e.getMessage());
				

			}

		} else if (arg.getActionCommand().equals("stop")) {
			this.stop.setEnabled(false);

			this.server.run = false;
			
			putLog("Server closing requested...");
			
			
			stop.setEnabled(false);

		} else if (arg.getActionCommand().equals("clear")) {
			this.lModel.clear();
		}
	}

	public void putLog(String s) {
		lModel.addElement(now.getcurr() + s);
		log.ensureIndexIsVisible(lModel.size()-1);
		
	}
}