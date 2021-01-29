import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.border.Border;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

import kr.ac.konkuk.ccslab.cm.entity.CMUser;
import kr.ac.konkuk.ccslab.cm.event.CMDummyEvent;
import kr.ac.konkuk.ccslab.cm.info.CMInfo;
import kr.ac.konkuk.ccslab.cm.info.CMInteractionInfo;
import kr.ac.konkuk.ccslab.cm.stub.CMClientStub;

public class CMindClient extends JFrame {
	
	private static final long serialVersionUID = 1L;

	String UserName;
	CMClientStub m_clientStub;
	CMindClientEventHandler m_eventHandler;

	CMindClient client;
	CMInteractionInfo interInfo;
	CMUser myself;
	
	private JButton m_startStopButton;
	private JButton m_loginLogoutButton;
	private JButton m_gameStartButton;
	
	JLabel roundInfo;
	JLabel quizInfo;
	JLabel timerInfo;
	JTextPane m_outTextRoundPane;
	JTextPane m_outTextQuizPane;
	JTextPane m_outTextTimerPane;
	private JButton m_turnGiveupButton;
	
	JLabel answerInfo;
	JLabel winnerInfo;
	JTextPane m_outTextAnwserPane;
	JTextPane m_outTextWinnerPane;

	
	JPanel paint_panel; //이벤트핸들러와 공유
	
	private JButton m_paintButton;
	private JButton m_eraseButton;
	private JButton m_clearButton;
	private JButton m_colorSelectButton;
	
	JLabel thicknessInfo;
	JTextField thicknessControl;
	
	Color selectedColor;
	
	Graphics graphics;
	Graphics2D g;
	
	int thickness=10;
		
//	private JTextPane m_outTextInfoPane;
//	private JTextPane m_outTextChatPane;
	
	JLabel loginUsersInfo_head;
	JLabel loginUsersInfo_tail;
	private JTextPane m_outTextloginUsersPane;
	
	
	JTextArea m_outTextArea;
	JTextPane m_outTextPane;
	JScrollPane scrollPane;

	private JLabel m_chatLabel;
	JTextField m_inTextField;
	
	CMcanvas can;
	int xx=0;
	int rgb;

	int yy =0;
	
	public CMindClient()
	{
		MyKeyListener cmKeyListener = new MyKeyListener();
		MyActionListener cmActionListener = new MyActionListener();	
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setTitle("CMind Client");
//		setSize(700,600);
		setBounds(100, 100, 700, 600);
		setResizable(false);
		setVisible(true);
		
		setLayout(new BorderLayout(5,5));
		
		StyledDocument doc;
		
		JPanel topButtonPanel = new JPanel();
		topButtonPanel.setBackground(new Color(250,200,160));
		topButtonPanel.setLayout(new FlowLayout());
		add(topButtonPanel, BorderLayout.NORTH);
		
		m_startStopButton = new JButton("Start Client CM");
		m_startStopButton.addActionListener(cmActionListener);
		m_startStopButton.setBackground(new Color(250,230,190));
		m_startStopButton.setEnabled(false);
		topButtonPanel.add(m_startStopButton);
		
		m_loginLogoutButton = new JButton("Login");
		m_loginLogoutButton.addActionListener(cmActionListener);
		m_loginLogoutButton.setBackground(new Color(250,210,180));
		m_loginLogoutButton.setEnabled(false);
		topButtonPanel.add(m_loginLogoutButton);
		
		m_gameStartButton = new JButton("CMind 게임시작");
		m_gameStartButton.addActionListener(cmActionListener);
		m_gameStartButton.setBackground(new Color(250,235,210));
		m_gameStartButton.setEnabled(false);
		topButtonPanel.add(m_gameStartButton);

		// 그림판 관련 내용 삽입  ~
		JPanel playPanel = new JPanel();
		playPanel.setBackground(new Color(200,220,220));
		playPanel.setPreferredSize(new Dimension(430, 600));
		playPanel.setBounds(0,0,430,600);
		playPanel.setLayout(null);
		add(playPanel, BorderLayout.WEST);		
	
		roundInfo = new JLabel("현재 라운드 :");
		
		m_outTextRoundPane = new JTextPane();
		m_outTextRoundPane.setBackground(new Color(200,220,220));
		m_outTextRoundPane.setEditable(false);
		
		quizInfo = new JLabel("출제자 :");
		
		m_outTextQuizPane = new JTextPane();
		m_outTextQuizPane.setBackground(new Color(200,220,220));
		m_outTextQuizPane.setEditable(false);
		
		timerInfo = new JLabel("남은 시간(초) :");
		
		m_outTextTimerPane = new JTextPane();
		m_outTextTimerPane.setBackground(new Color(200,220,220));
		m_outTextTimerPane.setEditable(false);
		
		m_turnGiveupButton = new JButton("포기");
		m_turnGiveupButton.setBackground(new Color(120,220,220));

		m_turnGiveupButton.addActionListener(cmActionListener);
		m_turnGiveupButton.setEnabled(false);
		
		roundInfo.setBounds(10,4,90,27); // 라운드 라벨 위치 지정
		m_outTextRoundPane.setBounds(85,4,30,27); // 라운드 값 pane 위치 지정
		quizInfo.setBounds(125,4,70,27); // 출제자 라벨 위치 지정
		m_outTextQuizPane.setBounds(175,4,50,27); // 출제자 pane 위치 지정
        timerInfo.setBounds(235,4,90,27); // 타이머 라벨 위치 지정
        m_outTextTimerPane.setBounds(320,4,30,27); // 타이머 pane 위치 지정
        m_turnGiveupButton.setBounds(355,3,60,27); // 포기 버튼 위치 지정
		
        playPanel.add(roundInfo);
        playPanel.add(m_outTextRoundPane);
        playPanel.add(quizInfo);
        playPanel.add(m_outTextQuizPane);
        playPanel.add(timerInfo);
        playPanel.add(m_outTextTimerPane);
        playPanel.add(m_turnGiveupButton);
		
		can = new CMcanvas();
		can.setBackground(new Color(245,245,245));
		can.setBounds(0,34,430,430);
		playPanel.add(can);

		    
		can.addMouseMotionListener(new MouseMotionAdapter() {
	            @Override
	            public void mouseDragged(MouseEvent e) {
	            	if(m_eventHandler.quizflag==1) {
	            		xx = e.getX();
	            		yy = e.getY();
	            		can.repaint();      
	            	}
	            }
	        });
		
		answerInfo = new JLabel("이번 문제 :");
		
		m_outTextAnwserPane = new JTextPane();
		m_outTextAnwserPane.setBackground(new Color(20,220,220));
		m_outTextAnwserPane.setEditable(false);
	        
		winnerInfo = new JLabel("현재 1등 :");
			
		m_outTextWinnerPane = new JTextPane();
		m_outTextWinnerPane.setBackground(new Color(20,220,220));
		m_outTextWinnerPane.setEditable(false);
		        
		answerInfo.setBounds(10,469,80,18); // 정답 라벨 위치 지정
		m_outTextAnwserPane.setBounds(73,464,110,27); // 정답 pane 위치 지정
		winnerInfo.setBounds(197,469,80,18); // 1등 라벨 위치 지정
		m_outTextWinnerPane.setBounds(258,464,170,27); // 1등 pane 위치 지정
		
        playPanel.add(answerInfo);
        playPanel.add(m_outTextAnwserPane);		
        playPanel.add(winnerInfo);
        playPanel.add(m_outTextWinnerPane);
        	
	    JPanel paintButtonPanel = new JPanel();
		paintButtonPanel.setBackground(new Color(230,220,230));
		paintButtonPanel.setLayout(new FlowLayout());
		paintButtonPanel.setBounds(0,490,430,28);
		playPanel.add(paintButtonPanel);
		
		m_paintButton = new JButton("그리기");
		m_paintButton.addActionListener(cmActionListener);
		m_paintButton.setBackground(new Color(220,220,250));
		m_paintButton.setPreferredSize(new Dimension(80, 23));
		m_paintButton.setEnabled(true);
		paintButtonPanel.add(m_paintButton);
		
		m_eraseButton = new JButton("지우개");
		m_eraseButton.addActionListener(cmActionListener);
		m_eraseButton.setBackground(new Color(245,245,245));
		m_eraseButton.setPreferredSize(new Dimension(80, 23));
		m_eraseButton.setEnabled(true);
		paintButtonPanel.add(m_eraseButton);
		
		m_clearButton = new JButton("clear");
		m_clearButton.addActionListener(cmActionListener);
		m_clearButton.setBackground(new Color(200,230,250));
	    m_clearButton.setPreferredSize(new Dimension(63, 23));
		m_clearButton.setEnabled(true);
		paintButtonPanel.add(m_clearButton);
		
		m_colorSelectButton = new JButton("color");
		m_colorSelectButton.addActionListener(cmActionListener);
		m_colorSelectButton.setBackground(new Color(240,200,220));
		m_colorSelectButton.setPreferredSize(new Dimension(63, 23));
		m_colorSelectButton.setEnabled(true);
		paintButtonPanel.add(m_colorSelectButton);
		
		thicknessInfo = new JLabel("도구굵기"); //textfield 역할설명
		
		thicknessControl=new JTextField("10",3); //도구굵기 입력 텍스트 필드생성
		thicknessControl.setHorizontalAlignment(JTextField.CENTER);
		paintButtonPanel.add(thicknessInfo);
		paintButtonPanel.add(thicknessControl);		
		
		graphics = can.getGraphics();
		g=(Graphics2D)graphics;
		g.setColor(selectedColor);
		
		clear();
		
//		JPanel showPanel = new JPanel();
//		showPanel.setBackground(new Color(220,220,220));
//		showPanel.setLayout(new FlowLayout());
//		add(showPanel, BorderLayout.EAST);
//		
//		m_outTextPane = new JTextPane();
//		m_outTextPane.setBackground(new Color(245,245,245));
//		m_outTextPane.setSize(100,500);
//		m_outTextPane.setEditable(false);
//		showPanel.add(m_outTextPane);
//
//		doc = m_outTextPane.getStyledDocument();
//		addStylesToDocument(doc);
//		
////		add(m_outTextPane, BorderLayout.CENTER);
//		JScrollPane centerScroll = new JScrollPane (m_outTextPane, 
//		JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
//		getContentPane().add(centerScroll, BorderLayout.CENTER);
//		
		// ~ 그림판 관련 내용 삽입
		
		JPanel showPanel = new JPanel();
		showPanel.setBackground(new Color(240,220,220));
		showPanel.setPreferredSize(new Dimension(252, 600));
//		showPanel.setBounds(440,5,252,600);
		showPanel.setLayout(null);
		add(showPanel, BorderLayout.EAST);	
		
//		JPanel userInfoPanel = new JPanel();
//		userInfoPanel.setBackground(new Color(240,220,220));
//		userInfoPanel.setLayout(new FlowLayout());
////		userInfoPanel.setPreferredSize(new Dimension(252, 32));
//		userInfoPanel.setBounds(440,0,252,32);
//		showPanel.add(userInfoPanel);
//		
		loginUsersInfo_head = new JLabel("게임방에 입장한 플레이어 : ");
		
		m_outTextloginUsersPane = new JTextPane();
		m_outTextloginUsersPane.setBackground(new Color(240,220,220));
//		m_outTextloginUsersPane.setPreferredSize(new Dimension(25, 28));
		m_outTextloginUsersPane.setEditable(false);
		
		loginUsersInfo_tail = new JLabel(" 명");

		loginUsersInfo_head.setBounds(20,5,170,28); 
		m_outTextloginUsersPane.setBounds(172,5,25,27); 
		loginUsersInfo_tail.setBounds(196,5,30,28);
		
        showPanel.add(loginUsersInfo_head);
        showPanel.add(m_outTextloginUsersPane);
        showPanel.add(loginUsersInfo_tail);

		JPanel textPanel = new JPanel();
		textPanel.setBackground(new Color(20,220,220));
		textPanel.setLayout(new BorderLayout());
//		textPanel.setPreferredSize(new Dimension(252, 32));
		textPanel.setBounds(0,35,252,455);
		showPanel.add(textPanel);
        
        
		m_outTextPane = new JTextPane();
		m_outTextPane.setBackground(new Color(245,245,245));
//		m_outTextPane.setBounds(0,35,252,455);
		m_outTextPane.setPreferredSize(new Dimension(252, 455));
		m_outTextPane.setEditable(false);	
		textPanel.add(m_outTextPane, BorderLayout.CENTER);
		
		doc = m_outTextPane.getStyledDocument();
		addStylesToDocument(doc);
		
		JScrollPane centerScroll = new JScrollPane (m_outTextPane, 
		JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		textPanel.add(centerScroll, BorderLayout.CENTER);	
			
		JPanel bottomChatPanel = new JPanel();
		bottomChatPanel.setBackground(new Color(210,220,200));
		bottomChatPanel.setLayout(new FlowLayout());
		bottomChatPanel.setBounds(0,490,252,29);
//		bottomChatPanel.setPreferredSize(new Dimension(252, 30));
		showPanel.add(bottomChatPanel);
		
		m_chatLabel = new JLabel("채팅 >");
		m_chatLabel.setForeground(Color.darkGray);
		bottomChatPanel.add(m_chatLabel);
		
		m_inTextField = new JTextField(15);
		m_inTextField.addKeyListener(cmKeyListener);
		bottomChatPanel.add(m_inTextField);
		
		setVisible(true);
		
		clear();
		
		// create a CM object and set the event handler
		m_clientStub = new CMClientStub();
		m_eventHandler = new CMindClientEventHandler(m_clientStub, this);
		
		interInfo=m_clientStub.getCMInfo().getInteractionInfo();
		myself=interInfo.getMyself();
		
		// start CM
		testStartCM();
				
		m_inTextField.requestFocus();
		
	}
	
	private void addStylesToDocument(StyledDocument doc)
	{
		Style defStyle = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);

		Style regularStyle = doc.addStyle("regular", defStyle);
		StyleConstants.setFontFamily(regularStyle, "SansSerif");
		
		Style boldStyle = doc.addStyle("bold", defStyle);
		StyleConstants.setBold(boldStyle, true);
		
	}
	
	public CMClientStub getClientStub()
	{
		return m_clientStub;
	}
	
	public CMindClientEventHandler getClientEventHandler()
	{
		return m_eventHandler;
	}

	
	// initialize button titles
		public void initializeButtons()
		{
			m_startStopButton.setText("Start Client CM");
			m_loginLogoutButton.setText("Login");
			revalidate();
			repaint();
		}
		
	
	// set button titles
		public void setButtonsAccordingToClientState()
		{
			int nClientState;
			nClientState = m_clientStub.getCMInfo().getInteractionInfo().getMyself().getState();
			
			// nclientState: CMInfo.CM_INIT, CMInfo.CM_CONNECT, CMInfo.CM_LOGIN, CMInfo.CM_SESSION_JOIN
			switch(nClientState)
			{
			case CMInfo.CM_INIT:
				m_startStopButton.setText("Stop Client CM");
				m_loginLogoutButton.setText("Login");
				break;
				
			case CMInfo.CM_CONNECT:
				m_startStopButton.setText("Stop Client CM");
				m_loginLogoutButton.setText("Login");
				break;
				
			case CMInfo.CM_LOGIN:
				m_startStopButton.setText("Stop Client CM");
				m_loginLogoutButton.setText("Logout");
				break;
				
			case CMInfo.CM_SESSION_JOIN:
				m_startStopButton.setText("Stop Client CM");
				m_loginLogoutButton.setText("Logout");
				break;
				
			default:
				m_startStopButton.setText("Start Client CM");
				m_loginLogoutButton.setText("Login");
				break;
			}
			revalidate();
			repaint();
		}		
			
		public void printMessage(String strText, String type)
		{
			StyledDocument doc;
			SimpleAttributeSet center = new SimpleAttributeSet();
			StyleConstants.setAlignment(center, StyleConstants.ALIGN_CENTER);
			
			switch(type) {
			case "Round": 
				doc = m_outTextRoundPane.getStyledDocument();
				doc.setParagraphAttributes(0, doc.getLength(), center, false);
				try {
					doc.insertString(0, strText+"\n", null);
					m_outTextRoundPane.setCaretPosition(0);
					} catch (BadLocationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
//				g.clearRect(0, 0, 1000, 1000);
				break;
				
			case "Quiz": 
				doc = m_outTextQuizPane.getStyledDocument();
				doc.setParagraphAttributes(0, doc.getLength(), center, false);
				try {
					doc.insertString(0, strText+"\n", null);
					m_outTextQuizPane.setCaretPosition(0);
					} catch (BadLocationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
//				g.clearRect(0, 0, 1000, 1000);
				break;
				
			case "Timer": 
				doc = m_outTextTimerPane.getStyledDocument();
				doc.setParagraphAttributes(0, doc.getLength(), center, false);
				try {
					doc.insertString(0, strText+"\n", null);
					m_outTextTimerPane.setCaretPosition(0);
					} catch (BadLocationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
//				g.clearRect(0, 0, 1000, 1000);
				break;
			case "Answer":
				doc = m_outTextAnwserPane.getStyledDocument();
				doc.setParagraphAttributes(0, doc.getLength(), center, false);
				try {
					doc.insertString(0, strText+"\n", null);
					m_outTextAnwserPane.setCaretPosition(0);
					} catch (BadLocationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
//				g.clearRect(0, 0, 1000, 1000);
				break;
			case "Winner":
				doc = m_outTextWinnerPane.getStyledDocument();
				doc.setParagraphAttributes(0, doc.getLength(), center, false);
				try {
					doc.insertString(0, strText+"\n", null);
					m_outTextWinnerPane.setCaretPosition(0);
					} catch (BadLocationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
//				g.clearRect(0, 0, 1000, 1000);
				break;
			case "Info":
				doc = m_outTextloginUsersPane.getStyledDocument();
				doc.setParagraphAttributes(0, doc.getLength(), center, false);
				try {
					doc.insertString(0, strText+"\n", null);
					m_outTextloginUsersPane.setCaretPosition(0);
					} catch (BadLocationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
//				g.clearRect(0, 0, 1000, 1000);
				break;
			case "Chat": 
				doc = m_outTextPane.getStyledDocument();
				try {
					doc.insertString(doc.getLength(), strText+"\n", null);
					m_outTextPane.setCaretPosition(m_outTextPane.getDocument().getLength());
					} catch (BadLocationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
//				g.clearRect(0, 0, 1000, 1000);
//				System.out.println(strText);
//				System.out.println("왓츠롱!!@!@!@?!@?");
//				m_outTextArea.append(strText+"\n");
//				m_outTextArea.setCaretPosition(m_outTextArea.getDocument().getLength());

				break;
			
			default : 
				return;
			}
			return;
		}
		
		public void printStyledMessage(String strText, String strStyleName, String type)
		{
			StyledDocument doc;
			SimpleAttributeSet center = new SimpleAttributeSet();
			StyleConstants.setAlignment(center, StyleConstants.ALIGN_CENTER);
			
			switch(type) {
			case "Round": 
				doc = m_outTextRoundPane.getStyledDocument();
				doc.setParagraphAttributes(0, doc.getLength(), center, false);
				try {
					doc.insertString(0, strText+"\n", doc.getStyle(strStyleName));
					m_outTextRoundPane.setCaretPosition(m_outTextRoundPane.getDocument().getLength());
					} catch (BadLocationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
//				g.clearRect(0, 0, 1000, 1000);
				break;
				
			case "Quiz": 
				doc = m_outTextQuizPane.getStyledDocument();
				doc.setParagraphAttributes(0, doc.getLength(), center, false);
				try {
					doc.insertString(0, strText+"\n", doc.getStyle(strStyleName));
					m_outTextQuizPane.setCaretPosition(0);
					} catch (BadLocationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
//				g.clearRect(0, 0, 1000, 1000);
				break;
				
			case "Timer": 
				doc = m_outTextTimerPane.getStyledDocument();
				doc.setParagraphAttributes(0, doc.getLength(), center, false);
				try {
					doc.insertString(0, strText+"\n", doc.getStyle(strStyleName));
					m_outTextTimerPane.setCaretPosition(0);
					} catch (BadLocationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
//				g.clearRect(0, 0, 1000, 1000);
				break;
				
			case "Answer":
				doc = m_outTextAnwserPane.getStyledDocument();
				doc.setParagraphAttributes(0, doc.getLength(), center, false);
				try {
					doc.insertString(0, strText+"\n", doc.getStyle(strStyleName));
					m_outTextAnwserPane.setCaretPosition(0);
					} catch (BadLocationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
//				g.clearRect(0, 0, 1000, 1000);
				break;
				
			case "Winner":
				doc = m_outTextWinnerPane.getStyledDocument();
				doc.setParagraphAttributes(0, doc.getLength(), center, false);
				try {
					doc.insertString(0, strText+"\n", doc.getStyle(strStyleName));
					m_outTextWinnerPane.setCaretPosition(0);
					} catch (BadLocationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
//				g.clearRect(0, 0, 1000, 1000);
				break;
				
			case "Info":
				doc = m_outTextloginUsersPane.getStyledDocument();
				doc.setParagraphAttributes(0, doc.getLength(), center, false);
				try {
					doc.insertString(0, strText+"\n", doc.getStyle(strStyleName));
					m_outTextloginUsersPane.setCaretPosition(0);
					} catch (BadLocationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
//				g.clearRect(0, 0, 1000, 1000);
				break;
				
			case "Chat": 
				doc = m_outTextPane.getStyledDocument();
				try {
					doc.insertString(doc.getLength(), strText+"\n", doc.getStyle(strStyleName));
					m_outTextPane.setCaretPosition(m_outTextPane.getDocument().getLength());
					} catch (BadLocationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
//				g.clearRect(0, 0, 1000, 1000);
//				m_outTextArea.append(strText+"\n");
//				m_outTextArea.setCaretPosition(m_outTextArea.getDocument().getLength());

				break;
			
			default : 
				return;
			}
		}
			
		public void testLoginDS()
		{
			String strUserName = null;
			String strPassword = null;
			boolean bRequestResult = false;

			System.out.println("〓〓〓〓〓〓 login to CMind server 〓〓〓〓〓〓");

			JTextField UserNameField = new JTextField();
			JPasswordField passwordField = new JPasswordField();
			Object[] message = {
					"User Name:", UserNameField,
					"Password:", passwordField
			};
			int option = JOptionPane.showConfirmDialog(null, message, "Login Input", JOptionPane.OK_CANCEL_OPTION);
			if (option == JOptionPane.OK_OPTION)
			{
				strUserName = UserNameField.getText();
				strPassword = new String(passwordField.getPassword()); // security problem?
				
				bRequestResult = m_clientStub.loginCM(strUserName, strPassword);
				if(bRequestResult)
				{
					System.out.println("successfully sent the login request.");
					m_gameStartButton.setEnabled(true);

					UserName = m_clientStub.getCMInfo().getInteractionInfo().getMyself().getName();
					System.out.println("My Name is '"+UserName+"'");

				}
				else
				{
					System.out.println("failed the login request!");
				}				
			}
			
		}
		
		public void testLogoutDS()
		{
			boolean bRequestResult = false;
			System.out.println("〓〓〓〓〓〓 logout to CMind server 〓〓〓〓〓");

			bRequestResult = m_clientStub.logoutCM();
			if(bRequestResult) {
				m_eventHandler.hostflag=0;
				m_eventHandler.quizflag=0;
				m_eventHandler.timer1.cancel();
	            m_eventHandler.timer2.cancel();
	            
	            m_eventHandler.count=0;
	            m_eventHandler.round=0;
	            m_eventHandler.game_time=0;
	            m_eventHandler.input_time=0;
	            m_eventHandler.gameflag=0;
	            m_eventHandler.fulltime=0;
	            
	            printStyledMessage("로그아웃하였습니다", "bold", "Chat");
				
				System.out.println("successfully sent the logout request.");
				m_gameStartButton.setEnabled(false);
			}
			else
				System.out.println("failed the logout request!");
			System.out.println("〓〓〓〓〓〓〓〓〓〓〓〓〓〓〓〓〓〓〓〓〓〓〓\n");
			// Change the title of the login button
			setButtonsAccordingToClientState();
			
			setTitle("CMind Client");
		}
		
		public void testStartCM()
		{
			boolean bRet = false;
			
			// get current server info from the server configuration file
			String strCurServerAddress = null;
			int nCurServerPort = -1;
			
			strCurServerAddress = m_clientStub.getServerAddress();
			nCurServerPort = m_clientStub.getServerPort();
			
			// ask the user if he/she would like to change the server info
			JTextField serverAddressTextField = new JTextField(strCurServerAddress);
			JTextField serverPortTextField = new JTextField(String.valueOf(nCurServerPort));
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
				if(!strNewServerAddress.equals(strCurServerAddress) || nNewServerPort != nCurServerPort)
					m_clientStub.setServerInfo(strNewServerAddress, nNewServerPort);
			}
			
			bRet = m_clientStub.startCM();
			if(!bRet)
			{
				printStyledMessage("CM initialization error!", "bold", "Chat");
			}
			else
			{
				m_startStopButton.setEnabled(true);
				m_loginLogoutButton.setEnabled(true);
				
				printStyledMessage("Client CM starts.", "bold", "Chat");
				printMessage("Welcome to CM Time-Request Client.", "Chat");
				printMessage("If you want to play this game, LOGIN first.", "Chat");
				// change the appearance of buttons in the client window frame
				setButtonsAccordingToClientState();
			}
		}
		
		public void testTerminateCM()
		{
			//m_clientStub.disconnectFromServer();
			m_clientStub.terminateCM();
			printStyledMessage("Client CM terminates.", "bold", "Chat");

			// change the appearance of buttons in the client window frame
			initializeButtons();
			setTitle("CMind Client");
		}
	
	public class MyKeyListener implements KeyListener {
		public void keyPressed(KeyEvent e)
		{
			int key = e.getKeyCode();
			if(key == KeyEvent.VK_ENTER)
			{
				JTextField input = (JTextField)e.getSource();
				String chat = input.getText();
				System.out.println("채팅>>"+chat+"\n");

				if(m_eventHandler.quizflag==1) 
				{
					if(chat.contains(m_eventHandler.answer)) 
					{
						printMessage("출제자는 정답을 채팅으로 입력할 수 없습니다.", "Chat");
						
					}
					else {
						m_clientStub.chat("/g",chat);
					}
				}
				else {//출제자가 아닐때 채팅
					System.out.println("chat 내용 확인 "+chat);
				
					if(chat.equals(m_eventHandler.answer)) 
					{
						CMDummyEvent dummy = new CMDummyEvent();
						dummy.setDummyInfo("one_end@"+ m_eventHandler.myself.getName());
						//m_clientStub.send(dummy,"SERVER");
						
						m_clientStub.chat("/g",chat);
						m_clientStub.cast(dummy, m_eventHandler.myself.getCurrentSession(), m_eventHandler.myself.getCurrentGroup());
					
						//m_clientStub.chat("/g",Chat);
						printStyledMessage("정답을 맞췄습니다!", "bold", "Chat");
					
						/*CMDummyEvent dum = new CMDummyEvent();
						dum.setDummyInfo("winner@"+ m_eventhandler.myself.getName());*/			
					}
					else {
						m_clientStub.chat("/g",chat);
					}
				}
				
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
			if(button.getText().equals("Start Client CM"))
			{
				testStartCM();
			}
			else if(button.getText().equals("Stop Client CM"))
			{
				testTerminateCM();
			}
			else if(button.getText().equals("Login"))
			{
				// login to the default cm server
				testLoginDS();
			}
			else if(button.getText().equals("Logout"))
			{
				// logout from the default cm server
				testLogoutDS();
			}
			else if(button.getText().equals("CMind 게임시작"))
			{
				if(m_eventHandler.hostflag==1) {//호스트일때만 시작가능
					if(m_eventHandler.count<3) {
						printStyledMessage("플레이어가 3명 이상 모이지 않아 게임을 시작할 수 없습니다.", "bold", "Chat");
						return;
					}else {
						printStyledMessage("게임 시작", "bold", "Chat");
						CMDummyEvent start=new CMDummyEvent();
						start.setDummyInfo("StartGame");
						m_clientStub.send(start,"SERVER");	
					}
						
				}else {
					printMessage("호스트만이 게임을 시작할 수 있습니다.", "Chat");
				}
			}
			else if(button.getText().equals("포기"))
			{
				m_eventHandler.timer1.cancel();
				CMDummyEvent dummy = new CMDummyEvent();
	            dummy.setDummyInfo("GiveUp");
	            m_clientStub.cast(dummy,myself.getCurrentSession(),myself.getCurrentGroup());
	            dummy = null;

			}
			else if(button.getText().equals("그리기"))
			{
				selectedColor=Color.BLACK;
	            rgb=selectedColor.getRGB();
			}
			else if(button.getText().equals("지우개"))
			{
				selectedColor=new Color(245,245,245); //배경색으로 바뀜
	            rgb=selectedColor.getRGB();
			}
			else if(button.getText().equals("clear"))
			{
				repaint();
				CMDummyEvent dummy = new CMDummyEvent();
		        dummy.setDummyInfo("clear"); // 컬러 두께 추가
		        m_clientStub.cast(dummy,null,null);
			}
			else if(button.getText().equals("color"))
			{
				JColorChooser chooser=new JColorChooser();
				selectedColor = chooser.showDialog(null,"Color",Color.ORANGE);
	            rgb=selectedColor.getRGB();
			}
			m_inTextField.requestFocus();
		}
	}
	
	void setGiveupBtn(boolean value)
	{
		if(value == true) {
			m_turnGiveupButton.setEnabled(true);
		}
		else {
			m_turnGiveupButton.setEnabled(false);
		}
	}
	
	public void clear() {
		g.clearRect(0,0,1000,1000);
	}
	
	
	class CMcanvas extends Canvas{	
		
        //점을 찍는 캔버스 
        @Override
        public void paint(Graphics g) {
            if(m_eventHandler.quizflag==1) {
            //선 굵기 변경
            	thickness=Integer.parseInt(thicknessControl.getText()); 
            	((Graphics2D) g).setStroke(new BasicStroke(thickness));
            	g.setColor(selectedColor);    //변경된 색상변수로 set
            	g.drawLine(xx, yy, xx, yy); //점 찍기
                
                CMDummyEvent dummy = new CMDummyEvent();
                dummy.setDummyInfo("paint@"+xx+"@"+yy+"@"+thickness+"@"+rgb);                m_clientStub.cast(dummy,m_eventHandler.myself.getCurrentSession(),m_eventHandler.myself.getCurrentGroup());
       		 	dummy=null;
            }else {
            	((Graphics2D) g).setStroke(new BasicStroke(thickness));
            	selectedColor=new Color(rgb);   
            	g.setColor(selectedColor);    //변경된 색상변수로 set
                g.drawLine(xx, yy, xx, yy);
            }
        }
        @Override
        public void update(Graphics g) {
            paint(g);
        }
    }
		
	// ~~ 그림판 관련 내용 삽입
	
	public static void main(String[] args) {
		CMindClient client = new CMindClient();
		CMClientStub cmStub = client.getClientStub();
		cmStub.setAppEventHandler(client.getClientEventHandler());

	}
}
