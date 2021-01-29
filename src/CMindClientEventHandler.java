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
	
	String answer = ""; //정답
		//구분 문자
		//@ 다음 문자 -출제자,정답,맞춘애
	
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
	
	int round; // 현재 라운드
	String winner = "";
	
	public CMindClientEventHandler(CMClientStub clientStub, CMindClient client)
	{
		m_client = client;
		m_clientStub = clientStub;
		count=0;
		interInfo=m_clientStub.getCMInfo().getInteractionInfo();
		myself=interInfo.getMyself();
		hostflag=0; //호스트면 1
		quizflag=0; //출제자면 1
		
		gameflag=0; //중간에 게임 끝나면 1
	    inputflag=0; //타이머 중간에 입력 끝나면 1
	    
	    timer1=new Timer();
	    timer2=new Timer();
	    input_time=0;//타이머 초기화
	    game_time=0;
	    fulltime=120;
	 
	    round = 0;
	 
	    x=0;
	    y=0; //브러시 위치 초기화
	    
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
	    				printStyledMessage("게임 시간 종료", "bold", "Chat");
	    				game_time++;
	    				gameflag=1;
	               
	    				timer1.cancel();
	               
	    				if(quizflag==1) {
//	       			    	printMessage("시간 끝", "Chat");
	    					
	    					//m_clientStub.send(de,"SERVER");
	    					m_clientStub.cast(de,myself.getCurrentSession(),myself.getCurrentGroup());
	            		
	    					return;
	    				}
	    				return;
	    			}
	    		}else {//게임 끝난 경우
	            
	    			timer1.cancel();
	    			
	    			return;
	    		}
	    	}
	  };
	      
	      
	      de.setDummyInfo("TimeOver1");
	      System.out.println("@@게임 타이머 초기화@@");
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
	    				printStyledMessage("출제 시간 종료", "bold", "Chat");
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
	      
	      System.out.println("@@퀴즈 타이머 초기화@@");
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
				printMessage("서버 연결 실패", "Chat");
			}
			else if(se.isValidUser() == -1)
			{
				System.err.println("This client is already in the login-user list!");
				printMessage("이미 로그인한 유저", "Chat");
			}
			else
			{
				System.err.println("This client successfully logs in to the default server.");
				printMessage("로그인 성공", "Chat");

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
		System.out.println("〓〓〓〓〓〓〓〓〓〓〓〓〓〓〓〓〓〓〓〓〓〓〓\n");
	}
		
	private void processDataEvent(CMEvent cme)
	{
		CMDataEvent de = (CMDataEvent) cme;
		switch(de.getID())
		{
		case CMDataEvent.NEW_USER:
			printMessage("["+de.getUserName()+"] 이 입장하였습니다.", "Chat");
			printMessage(count+"", "Info");
			
			break;
		case CMDataEvent.REMOVE_USER:
			//printMessage("["+de.getUserName()+"] leaves group("+de.getHandlerGroup()+") in session("
			//		+de.getHandlerSession()+").");
			printMessage("["+de.getUserName()+"] 나갔습니다.", "Chat");
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
				printStyledMessage("출제자 : "+Qzplayer+"\n내 이름은 : "+myself.getName(), "bold", "Chat");
				if(Qzplayer.equals(myself.getName())) {//출제자인 경우
					
					inputflag=0;
	                gameflag=0;
	                quizflag=1;
	                QuizTask();
						
					if(answer == "") {
						answer = "첫번째 라운드 입니다.";
						round = 1;
					}
					
					JTextField answerCurTextField = new JTextField(answer,10);
					JTextField answerNewTextField = new JTextField();
					Object msg[] = {
								"이전 라운드의 Answer: ", answerCurTextField,
								"출제할 단어를 입력하세요: ", answerNewTextField
					};
					
					boolean answerInput = false;
					
					// 공백 처리하고 
					while(!answerInput) {
						int option = JOptionPane.showConfirmDialog(null, msg, "문제 입력하기 [출제자: "+Qzplayer+"]", JOptionPane.OK_CANCEL_OPTION);
	
						// update the server info if the user would like to do
						if (option == JOptionPane.OK_OPTION) 
						{
							if(answerNewTextField.getText().equals(answer)) {
								printMessage("새로운 단어를 출제해 주세요.\n", "Chat");
								continue;
							}
							if(answerNewTextField.getText().equals("")) {
								printMessage("단어를 입력해주세요.\n", "Chat");
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
					
					printMessage("채팅을 시작하세요\n", "Chat");
					setGiveupBtn(true);
					
				}else { //출제자가 아님
					printStyledMessage(round+"", "bold", "Round");	
					printStyledMessage(Qzplayer, "bold", "Quiz");
					printStyledMessage("???", "bold", "Answer");
					
					printMessage("채팅을 시작하세요\n", "Chat");
					setGiveupBtn(false);
				}
					break;
					
			case "Answer" : 
				
				clear();
            	inputflag=1;
            	gameflag=0;
            	tempTask();
			
				answer = st.nextToken(); //정답 저장해놓기
				printMessage("출제자가 단어를 입력했습니다.", "Chat");
				printMessage("채팅을 통해 문제를 맞추세요!\n", "Chat");
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
				if(count==1) {//혼자 있음, 무조건 호스트
					hostflag=1;
				}
					break;
				
			case "Host": 
				hostflag=1;
				printStyledMessage("호스트입니다", "bold", "Chat");
				m_client.setTitle("(Host) CM Client [UserName: "+interInfo.getMyself().getName()+"]");
					break;
		
			case "one_end": 
				quizflag=0;
		        timer1.cancel();
		        gameflag=1;
		        setGiveupBtn(false);
		        
		        clear();
		        printStyledMessage(st.nextToken()+"님이 "+answer+" 을 맞췄습니다", "bold", "Chat");
		        
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
	    		printStyledMessage("게임이 모두 종료되었습니다.", "bold", "Chat");
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
			//이벤트를 보낸 애의 유저네임
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
//		m_client.printMessage("남은 시간"+(full-time)+"초", "Timer");     
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
