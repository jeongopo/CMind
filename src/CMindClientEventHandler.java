import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import kr.ac.konkuk.ccslab.cm.entity.CMUser;
import kr.ac.konkuk.ccslab.cm.event.CMDataEvent;
import kr.ac.konkuk.ccslab.cm.event.CMDummyEvent;
import kr.ac.konkuk.ccslab.cm.event.CMEvent;
import kr.ac.konkuk.ccslab.cm.event.CMInterestEvent;
import kr.ac.konkuk.ccslab.cm.event.CMSessionEvent;
import kr.ac.konkuk.ccslab.cm.event.handler.CMAppEventHandler;
import kr.ac.konkuk.ccslab.cm.info.CMInfo;
import kr.ac.konkuk.ccslab.cm.info.CMInteractionInfo;
import kr.ac.konkuk.ccslab.cm.stub.CMClientStub;

public class CMindClientEventHandler implements CMAppEventHandler {

	private CMindClient m_client;
	private CMClientStub m_clientStub; 
	
	int count;
	CMindClient client;
	CMInteractionInfo interInfo;
	CMUser myself;
	int hostflag;
	int quizflag;
	String Quizplayer = null;
	
	String answer = ""; //����
		//���� ����
		//@ ���� ���� -������,����,�����
	
	int game_time;
	int input_time;
	Timer timer1;
	Timer timer2;
	int gameflag;
	int inputflag;
	int fulltime;
	int x;
	int y;

//	Brush brush;
//	CopyBrush copybrush;
	Color copyColor;
	int copythickness;
	int copyrgb;  
	
	int round; // ���� ����
	String winner = "";
	
	public CMindClientEventHandler(CMClientStub clientStub, CMindClient client)
	{
		m_client = client;
		m_clientStub = clientStub;
		count=0;
		interInfo=m_clientStub.getCMInfo().getInteractionInfo();
		myself=interInfo.getMyself();
		hostflag=0; //ȣ��Ʈ�� 1
		quizflag=0; //�����ڸ� 1
		
		gameflag=0; //�߰��� ���� ������ 1
	    inputflag=0; //Ÿ�̸� �߰��� �Է� ������ 1
	    
	    timer1=new Timer();
	    timer2=new Timer();
	    input_time=0;//Ÿ�̸� �ʱ�ȭ
	    game_time=0;
	    fulltime=120;
	 
	    round = 0;
	 
	    x=0;
	    y=0; //�귯�� ��ġ �ʱ�ȭ
	    
//	    brush = new Brush();
//	    copybrush=new CopyBrush();
	
	}
	
	public void tempTask() {
	      
	    CMDummyEvent de = new CMDummyEvent();
	    TimerTask task = new TimerTask() {
	         
	         
	    	@Override
	    	public void run() {
	      
	    		if(gameflag==0) {
	    			if(game_time<fulltime) {
	    				game_time++;
	    				PrintTime(fulltime,game_time);
	               
	    			}else if(game_time==fulltime) {
	    				printStyledMessage("���� �ð� ����", "bold", "Chat");
	    				game_time++;
	    				gameflag=1;
	               
	    				timer1.cancel();
	               
	    				if(quizflag==1) {
//	       			    	printMessage("�ð� ��", "Chat");
	    					
	    					//m_clientStub.send(de,"SERVER");
	    					m_clientStub.cast(de,myself.getCurrentSession(),myself.getCurrentGroup());
	            		
	    					return;
	    				}
	    				return;
	    			}
	    		}else {//���� ���� ���
	            
	    			timer1.cancel();
	    			
	    			return;
	    		}
	    	}
	  };
	      
	      
	      de.setDummyInfo("TimeOver1");
	      System.out.println("@@���� Ÿ�̸� �ʱ�ȭ@@");
	      game_time=0;
	      gameflag=0;
	      timer1 = new Timer();
	      timer1.schedule(task, 1000, 1000);
	   }
	   
	public void QuizTask() {
		CMDummyEvent de=new CMDummyEvent();
	    de.setDummyInfo("TimeOver2");
	         
	    TimerTask task = new TimerTask() {
	         
	         
	    	@Override
	    	public void run() {
	    		if(inputflag==0) 
	    		{
	    			if(input_time<30) 
	    			{
	    				input_time++;
	    				PrintTime(30,input_time);
	    			}else if(input_time==109) 
	    			{
	    				input_time++;
	    				printStyledMessage("���� �ð� ����", "bold", "Chat");
	    				timer2.cancel();
	    				inputflag=1;
	    				if(quizflag==1) {
	    					
	    					de.setDummyInfo("TimeOver2");
	    					m_clientStub.send(de,"SERVER");
	    					return;     
	    				}
	    			}
	    		
	    		}else if(inputflag==1) {
	            
	    			timer2.cancel();
	    		}
	         }
	      };
	      
	      System.out.println("@@���� Ÿ�̸� �ʱ�ȭ@@");
	      input_time=0;
	      inputflag=0;
	      timer2 = new Timer();
	      timer2.schedule(task, 1000, 1000);
	}
	
	@Override
	public void processEvent(CMEvent cme) {
		switch(cme.getType())
		{
		case CMInfo.CM_DUMMY_EVENT:
			processDummyEvent(cme);
			break;
			
		case CMInfo.CM_SESSION_EVENT:
			LoginCheck(cme);
			break;
			
		case CMInfo.CM_INTEREST_EVENT:
			processInterestEvent(cme);
			break;
			
		case CMInfo.CM_DATA_EVENT:
			processDataEvent(cme);
			break;
		default : return;	
		}
	}
	
	void LoginCheck(CMEvent cme) {
		CMSessionEvent se = (CMSessionEvent) cme;
		
		switch(se.getID()) {
			
		case CMSessionEvent.LOGIN_ACK:
			
			if(se.isValidUser() == 0)
			{
				System.err.println("This client fails authentication by the default server!");
				printMessage("���� ���� ����", "Chat");
			}
			else if(se.isValidUser() == -1)
			{
				System.err.println("This client is already in the login-user list!");
				printMessage("�̹� �α����� ����", "Chat");
			}
			else
			{
				System.err.println("This client successfully logs in to the default server.");
				printMessage("�α��� ����", "Chat");

				CMInteractionInfo interInfo = m_clientStub.getCMInfo().getInteractionInfo();
				
				// Change the title of the client window
				m_client.setTitle("CM Client [UserName: "+interInfo.getMyself().getName()+"]");

				// Set the appearance of buttons in the client frame window
				m_client.setButtonsAccordingToClientState();
			}
				break;
		default : 
			return;
		}
		System.out.println("������������������������\n");
	}
		
	private void processDataEvent(CMEvent cme)
	{
		CMDataEvent de = (CMDataEvent) cme;
		switch(de.getID())
		{
		case CMDataEvent.NEW_USER:
			printMessage("["+de.getUserName()+"] �� �����Ͽ����ϴ�.", "Chat");
			printMessage(count+"", "Info");
			
			break;
		case CMDataEvent.REMOVE_USER:
			//printMessage("["+de.getUserName()+"] leaves group("+de.getHandlerGroup()+") in session("
			//		+de.getHandlerSession()+").");
			printMessage("["+de.getUserName()+"] �������ϴ�.", "Chat");
			printMessage(count+"", "Info");
			break;
		default:
			return;
		}
	}
	
	private void processDummyEvent(CMEvent cme)
	{
		CMDummyEvent de = (CMDummyEvent)cme;
		
		StringTokenizer st=new StringTokenizer(de.getDummyInfo(),"@");
		String token=st.nextToken();
		String Qzplayer="";
		String strtem="";
		
		CMDummyEvent tem=new CMDummyEvent();
		
		switch(token) {
			case "newround" : 
				quizflag=0;
				gameflag=1;
				round++;
				input_time=0;   
				game_time=0;
			
				Qzplayer=st.nextToken();
				printStyledMessage("������ : "+Qzplayer+"\n�� �̸��� : "+myself.getName(), "bold", "Chat");
				if(Qzplayer.equals(myself.getName())) {//�������� ���
					
					inputflag=0;
	                gameflag=0;
	                quizflag=1;
	                QuizTask();
						
					if(answer == "") {
						answer = "ù��° ���� �Դϴ�.";
						round = 1;
					}
					
					JTextField answerCurTextField = new JTextField(answer,10);
					JTextField answerNewTextField = new JTextField();
					Object msg[] = {
								"���� ������ Answer: ", answerCurTextField,
								"������ �ܾ �Է��ϼ���: ", answerNewTextField
					};
					
					boolean answerInput = false;
					
					// ���� ó���ϰ� 
					while(!answerInput) {
						int option = JOptionPane.showConfirmDialog(null, msg, "���� �Է��ϱ� [������: "+Qzplayer+"]", JOptionPane.OK_CANCEL_OPTION);
	
						// update the server info if the user would like to do
						if (option == JOptionPane.OK_OPTION) 
						{
							if(answerNewTextField.getText().equals(answer)) {
								printMessage("���ο� �ܾ ������ �ּ���.\n", "Chat");
								continue;
							}
							if(answerNewTextField.getText().equals("")) {
								printMessage("�ܾ �Է����ּ���.\n", "Chat");
								continue;
							}
							answer = answerNewTextField.getText();
							answerInput = true;
						}
					}
					
					printStyledMessage(round+"", "bold", "Round");	
					printStyledMessage(Qzplayer, "bold", "Quiz");
					printStyledMessage(answer, "bold", "Answer");		

					strtem="Answer@"+answer;
												
					//s.close();
						
					tem.setDummyInfo(strtem);
					m_clientStub.cast(tem,myself.getCurrentSession(),myself.getCurrentGroup());
//					GraphicsDraw();
					
					printMessage("ä���� �����ϼ���\n", "Chat");
					setGiveupBtn(true);
					
				}else { //�����ڰ� �ƴ�
					printStyledMessage(round+"", "bold", "Round");	
					printStyledMessage(Qzplayer, "bold", "Quiz");
					printStyledMessage("???", "bold", "Answer");
					
					printMessage("ä���� �����ϼ���\n", "Chat");
					setGiveupBtn(false);
				}
					break;
					
			case "Answer" : 
				
				clear();
            	inputflag=1;
            	gameflag=0;
            	tempTask();
			
				answer = st.nextToken(); //���� �����س���
				printMessage("�����ڰ� �ܾ �Է��߽��ϴ�.", "Chat");
				printMessage("ä���� ���� ������ ���߼���!\n", "Chat");
					break;
			
			case "paint" :
				if(quizflag==0) {
					x=Integer.parseInt(st.nextToken());
					y=Integer.parseInt(st.nextToken());
					copythickness=Integer.parseInt(st.nextToken());
					copyrgb=Integer.parseInt(st.nextToken());
		            copyColor=new Color(copyrgb);//red, green, blue
					GraphicsCopy();
				}
					break;			
					
			case "Info":
				count = Integer.parseInt(st.nextToken());
				printMessage(count+"", "Info");
				if(count==1) {//ȥ�� ����, ������ ȣ��Ʈ
					hostflag=1;
				}
					break;
				
			case "Host": 
				hostflag=1;
				printStyledMessage("ȣ��Ʈ�Դϴ�", "bold", "Chat");
				m_client.setTitle("(Host) CM Client [UserName: "+interInfo.getMyself().getName()+"]");
					break;
		
			case "one_end": 
				quizflag=0;
		        timer1.cancel();
		        gameflag=1;
		        setGiveupBtn(false);
		        
		        clear();
		        printStyledMessage(st.nextToken()+"���� "+answer+" �� ������ϴ�", "bold", "Chat");
		        
					break;
		
			case "Winner":
				winner = st.nextToken();
				printStyledMessage(winner, "bold", "Winner");

					break;
					
			case "Giveup":
				quizflag=0;
				timer1.cancel();		
				gameflag=1;		
				setGiveupBtn(false);
				clear();
					break;
				
			case "TIMEOVER1" : 
				quizflag=0;
				
				timer1.cancel();
				
				gameflag=1;
				inputflag=1;
				clear();
	         		break;
	      
			case "finish":
	    		gameflag=1;
	    		inputflag=1;
	    		quizflag=0;
	    		round = 0;
	           
	    		clear();
	    		
	    		while(st.hasMoreTokens())
	            {
	                printMessage(st.nextToken(), "Chat");  
	            }
	    		printStyledMessage("������ ��� ����Ǿ����ϴ�.", "bold", "Chat");
	          		break;
	          		
	    	case "clear":
	    		clear();
	          		
		default: break;
		}
		return;
		
		
	}
	
	private void processInterestEvent(CMEvent cme)
	{
		CMInterestEvent ie = (CMInterestEvent) cme;
		switch(ie.getID())
		{
		case CMInterestEvent.USER_TALK:
			//printMessage("("+ie.getHandlerSession()+", "+ie.getHandlerGroup()+")");
			//printMessage("<"+ie.getUserName()+">: "+ie.getTalk());
			
			printMessage("<"+ie.getUserName()+">: "+ie.getTalk(), "Chat");
			//�̺�Ʈ�� ���� ���� ��������
			break;
		default:
			return;
		}
	}
	
	void setGiveupBtn(boolean value)
	{
		m_client.setGiveupBtn(value);
	}
	
	void PrintTime(int full, int time) 
	{	
//		m_client.printMessage("���� �ð�"+(full-time)+"��", "Timer");     
		m_client.printMessage((full-time)+"", "Timer"); 
	}
	
	
	public void printStyledMessage(String strText, String strStyleName, String type)
	{
		m_client.printStyledMessage(strText, strStyleName, type);
		
		return;
	}
	
	private void printMessage(String strText, String type)
	{
		m_client.printMessage(strText, type);
		
		return;
	}
	
	public void GraphicsCopy() {
		if(quizflag==0) {
		 
		 m_client.xx=x;
		 m_client.yy=y;
		 m_client.thickness=copythickness;
		 m_client.rgb=copyrgb;
		 m_client.can.repaint();                            
		 //m_client.can.printAll( m_client.can.getGraphics() );
		}
	}
	
	public void clear() {
		
		m_client.clear();
	}
	
}
