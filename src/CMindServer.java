import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

import kr.ac.konkuk.ccslab.cm.manager.CMCommManager;
import kr.ac.konkuk.ccslab.cm.manager.CMConfigurator;
import kr.ac.konkuk.ccslab.cm.stub.CMServerStub;


public class CMindServer extends JFrame {
	
	private static final long serialVersionUID = 1L;
	
	CMServerStub m_serverStub;
	CMindServerEventHandler m_eventHandler;
	
	private JTextPane m_outTextPane;
	private JTextField m_inTextField;
	private JButton m_startStopButton;
	
	CMindServer()
	{
	
		MyKeyListener cmKeyListener = new MyKeyListener();
		MyActionListener cmActionListener = new MyActionListener();
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setTitle("CMind Server");
		setSize(400, 400);
		setVisible(true);
		
		setLayout(new BorderLayout());
		
		m_outTextPane = new JTextPane();
		m_outTextPane.setBackground(new Color(245,245,245));
		
		m_outTextPane.setEditable(false);

		StyledDocument doc = m_outTextPane.getStyledDocument();
		addStylesToDocument(doc);

		add(m_outTextPane, BorderLayout.CENTER);
		JScrollPane scroll = new JScrollPane (m_outTextPane, 
		JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		getContentPane().add(scroll, BorderLayout.CENTER);
		
		m_inTextField = new JTextField();
		m_inTextField.addKeyListener(cmKeyListener);
		add(m_inTextField, BorderLayout.SOUTH);
		
		JPanel topButtonPanel = new JPanel();
		topButtonPanel.setLayout(new FlowLayout());
		add(topButtonPanel, BorderLayout.NORTH);
		
		m_startStopButton = new JButton("Start Server CM");
		m_startStopButton.addActionListener(cmActionListener);
		m_startStopButton.setEnabled(false);
		topButtonPanel.add(m_startStopButton);
		
		setVisible(true);
		
		// create CM stub object and set the event handler
		m_serverStub = new CMServerStub();
		m_eventHandler = new CMindServerEventHandler(m_serverStub, this);
		
		
		// start cm
		startCM();

		
	}
	
	public CMServerStub getServerStub()
	{
		return m_serverStub;
	}
	
	public CMindServerEventHandler getServerEventHandler()
	{
		return m_eventHandler;
	}
	
	private void addStylesToDocument(StyledDocument doc)
	{
		Style defStyle = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);

		Style regularStyle = doc.addStyle("regular", defStyle);
		StyleConstants.setFontFamily(regularStyle, "SansSerif");
		
		Style boldStyle = doc.addStyle("bold", defStyle);
		StyleConstants.setBold(boldStyle, true);
	}
	
	public void startCM()
	{
		boolean bRet = false;
		
		// get current server info from the server configuration file
		String strSavedServerAddress = null;
		String strCurServerAddress = null;
		int nSavedServerPort = -1;
		
		strSavedServerAddress = m_serverStub.getServerAddress();
		strCurServerAddress = CMCommManager.getLocalIP();
		nSavedServerPort = m_serverStub.getServerPort();
		
		// ask the user if he/she would like to change the server info
		JTextField serverAddressTextField = new JTextField(strCurServerAddress);
		JTextField serverPortTextField = new JTextField(String.valueOf(nSavedServerPort));
		Object msg[] = {
				"Server Address: ", serverAddressTextField,
				"Server Port: ", serverPortTextField
		};
		int option = JOptionPane.showConfirmDialog(null, msg, "Server Information", JOptionPane.OK_CANCEL_OPTION);

		// update the server info if the user would like to do
		if (option == JOptionPane.OK_OPTION) 
		{
			String strNewServerAddress = serverAddressTextField.getText();
			int nNewServerPort = Integer.parseInt(serverPortTextField.getText());
			if(!strNewServerAddress.equals(strSavedServerAddress) || nNewServerPort != nSavedServerPort)
				m_serverStub.setServerInfo(strNewServerAddress, nNewServerPort);
		}
		
		bRet = m_serverStub.startCM();
		if(!bRet)
		{
			printStyledMessage("CM initialization error!", "bold");
			return;
		}else
		{
			printStyledMessage("Server CM starts.", "bold");	
			printMessage("Welcome to CMind Server.");	
			// change button to "stop CM"
			m_startStopButton.setEnabled(true);
			m_startStopButton.setText("Stop Server CM");				
		}

		m_inTextField.requestFocus();
	}
	
	public void terminateCM()
	{
		m_serverStub.terminateCM();
	}
	
	public void printMessage(String strText)
	{
		StyledDocument doc = m_outTextPane.getStyledDocument();
		try {
			doc.insertString(doc.getLength(), strText+"\n", null);
			m_outTextPane.setCaretPosition(m_outTextPane.getDocument().getLength());
			} catch (BadLocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return;
	}
	
	public void printStyledMessage(String strText, String strStyleName)
	{
		StyledDocument doc = m_outTextPane.getStyledDocument();
		try {
			doc.insertString(doc.getLength(), strText+"\n", doc.getStyle(strStyleName));
			m_outTextPane.setCaretPosition(m_outTextPane.getDocument().getLength());
			} catch (BadLocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return;
	}
	
	public class MyKeyListener implements KeyListener {
		public void keyPressed(KeyEvent e)
		{
			int key = e.getKeyCode();
			if(key == KeyEvent.VK_ENTER)
			{
				JTextField input = (JTextField)e.getSource();
				String strText = input.getText();
				printMessage(strText+"\n");

				input.setText("");
				input.requestFocus();
			}
		}
		
		public void keyReleased(KeyEvent e){}
		public void keyTyped(KeyEvent e){}
	}

	public class MyActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e)
		{
			JButton button = (JButton) e.getSource();
			if(button.getText().equals("Start Server CM"))
			{
				// start cm
				boolean bRet = m_serverStub.startCM();
				if(!bRet)
				{
					printStyledMessage("CM initialization error!", "bold");
				}
				else
				{
					printStyledMessage("Server CM starts.", "bold");				
					// change button to "stop CM"
					button.setText("Stop Server CM");
				}
				// check if default server or not
				if(CMConfigurator.isDServer(m_serverStub.getCMInfo()))
				{
					setTitle("CM Default Server (\"SERVER\")");
				}
				else
				{
					setTitle("CM Additional Server (\"?\")");
				}					
				m_inTextField.requestFocus();
			}
			else if(button.getText().equals("Stop Server CM"))
			{
				// stop cm
				m_serverStub.terminateCM();
				printStyledMessage("Server CM terminates.\n", "bold");	
				// change button to "start CM"
				button.setText("Start Server CM");
			}
		}
	}
	
	public static void main(String[] args) {
		CMindServer server = new CMindServer();
		CMServerStub cmStub = server.getServerStub();
		cmStub.setAppEventHandler(server.getServerEventHandler()); 
	}
}
