import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Map.Entry;

//import java.util.Iterator;
//import java.util.Vector;
//
//import kr.ac.konkuk.ccslab.cm.entity.CMMember;
//import kr.ac.konkuk.ccslab.cm.entity.CMUser;
import kr.ac.konkuk.ccslab.cm.event.CMDummyEvent;
import kr.ac.konkuk.ccslab.cm.event.CMEvent;
import kr.ac.konkuk.ccslab.cm.event.CMInterestEvent;
import kr.ac.konkuk.ccslab.cm.event.CMSessionEvent;
import kr.ac.konkuk.ccslab.cm.event.handler.CMAppEventHandler;
import kr.ac.konkuk.ccslab.cm.info.CMInfo;
import kr.ac.konkuk.ccslab.cm.stub.CMServerStub;

public class CMindServerEventHandler implements CMAppEventHandler {
	private CMindServer m_server;
	private CMServerStub m_serverStub;
	protected CMInfo m_cmInfo;

	Map<String,Integer> h= new HashMap<>();
	ArrayList<String> endingUserList = new ArrayList<String>(); //마지막에 1등~6등까지의 유저네임 리스트
	ArrayList<Integer> endingScoreList = new ArrayList<Integer>();
	String[] user = {"0","0","0","0","0","0"};	//로그인한 유저 정보를 담고있는 배열

	int host=0; 	//호스트가 누구인지
	int turn=0;		//현재 출제자
	int round=0;	//몇 라운드
	int State=0; 	//게임을 시작했는지  확인 0종료 1시작
	String turnUserName = null;
	int maxuser=6;	//최대 게임인원
	int minuser=3;	//최소 게임인원
	
	public CMindServerEventHandler(CMServerStub serverStub,CMindServer server)
	{
		m_server = server;
		m_serverStub = serverStub;
	}
	
	@Override
	public void processEvent(CMEvent cme) {
		// TODO Auto-generated method stub
		switch(cme.getType())
		{
		case CMInfo.CM_SESSION_EVENT:
			processSessionEvent(cme);
			break;
		case CMInfo.CM_INTEREST_EVENT:
			processInterestEvent(cme);
			break;
		case CMInfo.CM_DUMMY_EVENT:
			processDummyEvent(cme);
			break;
		default:
			return;
		}
	}
	
	private void processSessionEvent(CMEvent cme)
	{
		int j = 0;
		CMSessionEvent se = (CMSessionEvent) cme;
		CMDummyEvent due = new CMDummyEvent();

		switch(se.getID())
		{
		case CMSessionEvent.LOGIN:
			if(m_serverStub.getLoginUsers().getMemberNum()>maxuser)//6명 초과이면 로그인 실패
			{
				printMessage("로그인 실패 "+se.getUserName()+ "현재인원수를 초과하였습니다.");
				m_serverStub.replyEvent(se, 0);
			}
			else if(State==1)
	         {
	            printMessage("로그인 실패 "+se.getUserName()+ "게임이 이미 시작하였습니다.");
	            m_serverStub.replyEvent(se, 0);
	         }
			else
			{
				String name=se.getUserName();
				printMessage(name+" 님이 로그인하였습니다.");
				h.put(name,0);
				m_serverStub.replyEvent(se, 1);
				
				for(j=0;j<6;j++)	//배열에 저장
				{
					if(user[j]=="0")
					{
						user[j]=name;
						break;
					}
				}
				
				System.out.println("내 위치는 :"+j+"번째 값은 "+ user[j]);
				
				if(m_serverStub.getLoginUsers().getMemberNum()==1)	//처음 들어온 사람이 호스트
				{
					host=j;
					due.setDummyInfo("Host");
					m_serverStub.send(due,se.getUserName());
					printMessage("호스트는 "+se.getUserName()+ "입니다");
				}
				
				String msg = "Info@"+m_serverStub.getLoginUsers().getMemberNum();						//info받으면 현재 인원수 알려주기
				due.setDummyInfo(msg);
				m_serverStub.cast(due,null,null);
				due = null;
			}
			
			printMessage("현재 인원 수"+m_serverStub.getLoginUsers().getMemberNum());
			
			
			break;
			
		case CMSessionEvent.LOGOUT:	//로그아웃
			
			String name=se.getUserName();
	        h.remove(name);   //헤쉬에서 지우기
	         
	        for(j=0;j<6;j++) //유저 배열에서 지우기
	        {
	        	if(name.equals(user[j])) 
	            {
	               user[j]="0";
	               break;
	            }
	        }
	         
	        printMessage(name+"님이 로그아웃하였습니다.");
	        printMessage("현재 인원 수"+m_serverStub.getLoginUsers().getMemberNum());         
	         
	        //현재 인원수 알려주기
	        String msg = "Info@"+m_serverStub.getLoginUsers().getMemberNum();   
	        due.setDummyInfo(msg);
	        m_serverStub.cast(due,null,null);
	         
	        //엔딩유저리스트에서도 지우기
	        endingUserList.remove(name);
	                  
	         
	        if(user[host]=="0")      //호스트가 나가버리면 새로운 호스트 지정
	        {
	        	for(j=0;j<6;j++)
	            {
	            	if(user[j]!="0")
	            	{
	            		host=j;
	            		due.setDummyInfo("Host");
	            		m_serverStub.send(due,user[j]);
	            		printMessage("호스트는 "+user[j]+ "입니다");
	            			break;
	            	}
	            }
	         
	         }
	         
	         if(State==1 && m_serverStub.getLoginUsers().getMemberNum()<minuser)   //게임 중간에 플레이어가 3명 미만이 되면 게임 종료
	         {
	            List<Entry<String, Integer>> list_entries = new ArrayList<Entry<String, Integer>>(h.entrySet());
	                
	                Collections.sort(list_entries, new Comparator<Entry<String, Integer>>() {
	                   // compare로 값을 비교
	                   public int compare(Entry<String, Integer> obj1, Entry<String, Integer> obj2) {
	                      // 오름 차순 정렬
	                      return obj2.getValue().compareTo(obj1.getValue());
	                   }
	                });
	                
	                printMessage("정렬하기");
	                for(Entry<String, Integer> entry : list_entries) {   
	                   printMessage(entry.getKey() + " 의 점수는: " + entry.getValue());
	                      endingUserList.add(entry.getKey());   
	                      endingScoreList.add(entry.getValue());
	                }    
	                gameover();         
	         }
	         
	               
	         if(State==1 && user[turn-1]=="0")   //출제자가 나가버리면 새로운 호스트 지정
	           {
	            System.out.println("출제자가 나가버림!!!!");
	               round--;
	               newround();
	               break;               
	           }
	         due = null;
	         
				break;
				
			default:
		}
	}
	
	private void processInterestEvent(CMEvent cme)
	{
		CMInterestEvent ie = (CMInterestEvent) cme;
		switch(ie.getID())
		{
		case CMInterestEvent.USER_ENTER:
			printMessage("["+ie.getUserName()+"] enters group("+ie.getCurrentGroup()+") in session("
					+ie.getHandlerSession()+").");
			printStyledMessage("["+ie.getUserName()+"] 가  CMind 게임에 입장했습니다.", "bold");
			break;
		case CMInterestEvent.USER_LEAVE:
			printMessage("["+ie.getUserName()+"] leaves group("+ie.getHandlerGroup()+") in session("
					+ie.getHandlerSession()+").");
			printStyledMessage("["+ie.getUserName()+"] 가  CMind 게임을 떠났습니다.", "bold");
			break;
		default:
			break;
		}
	}
	
	private void processDummyEvent(CMEvent cme)
	{

		CMDummyEvent due = (CMDummyEvent) cme;
		System.out.println("session("+due.getHandlerSession()+"), group("+due.getHandlerGroup()+")");
		System.out.println("dummy msg: "+due.getDummyInfo());
		
		String recvMessage = null;
		recvMessage = due.getDummyInfo();
		StringTokenizer st = new StringTokenizer(recvMessage,"@");
		
		String token = new String();
		token = st.nextToken();
		System.out.println("토큰 확인용 "+token);
		switch(token)
		{
			case "StartGame":
				printMessage("게임시작 합니다!");
				round=0;
				turn=0;
				State=1;
				newround();
				break;
				
			case "one_end":
				
				token=st.nextToken();
	            int score = h.get(token);
	            score+=20;
	            h.remove(token);
	            h.put(token,score);
	            
	            printMessage(token+" 이 점수를 얻었습니다 (점수는: "+score+" 점)");
	                  
	            //해쉬맵 출력
	            Set<String> keys = h.keySet();
	            Iterator<String> it = keys.iterator();
	               
	            printStyledMessage("점수 출력하기", "bold");
	            while(it.hasNext()) {
	                  String key = it.next();
	                  Integer value = h.get(key);
	                  printMessage("// "+key+"의 점수는 = "+value);      
	               }      
	        
	               List<Entry<String, Integer>> list_entries = new ArrayList<Entry<String, Integer>>(h.entrySet());
	               
	               Collections.sort(list_entries, new Comparator<Entry<String, Integer>>() {
	                  // compare로 값을 비교
	                  public int compare(Entry<String, Integer> obj1, Entry<String, Integer> obj2) {
	                     // 오름 차순 정렬
	                     return obj2.getValue().compareTo(obj1.getValue());
	                  }
	               });
	               
	               printStyledMessage("바뀐 점수로 정렬하기", "bold");
	               for(Entry<String, Integer> entry : list_entries) {   
	            	   	printMessage(entry.getKey() + " 의 점수는: " + entry.getValue()+" 점");
	                    endingUserList.add(entry.getKey());   
	                    endingScoreList.add(entry.getValue());
	               }  
	               
	               
	               printMessage(h.get(endingUserList.get(0))+"");
   
	               	String msg = "Winner@"+endingUserList.get(0)+" ( "+h.get(endingUserList.get(0))+" 점)";	
	               	due.setDummyInfo(msg);
   					m_serverStub.cast(due,null,null);
   					due = null;
	            
   					newround();
	                  				
	               endingUserList.clear();    
	               endingScoreList.clear();

	            break;
	            
			case "GiveUp":
				//포기를 라운드 하나 한걸로 안치면 삭제
				newround();
				break;
				
			case "winner":
				token = st.nextToken();
				printStyledMessage(token+"가 정답을 맞췄습니다.", "bold");
				printMessage("〓〓〓〓〓〓〓〓〓〓〓〓〓〓");
				printMessage("    새로운 라운드를 시작합니다.   ");
				printMessage("〓〓〓〓〓〓〓〓〓〓〓〓〓〓");
				newround();
				break;
				
			case "Answer":
				break;
				
			case "TimeOver1":
				//포기를 라운드 하나 한걸로 안치면 삭제
		        newround();
		        printMessage("아무도 맞히지 못했습니다ㅠㅠ");
		        break;
		            
			case "TimeOver2":
			   	//포기를 라운드 하나 한걸로 안치면 삭제
		        newround();
		        printMessage("인풋 시간초과!");
		        break;      
		        
		   	case "paint":
		   		break;
		        
			default:
				printMessage("Unknown command.");
				break;
		}

		return;
	}
	
	private void newround() {
		round++;
		CMDummyEvent due = new CMDummyEvent();
		
		if(round>5) {
			gameover();
			return;
		}
		printStyledMessage("지금 라운드"+round, "bold");
		
		int j;
		for(j=0;j<6;j++)
			System.out.println("(확인용) newround 속에 " + user[j]);
		
		while(user[turn]=="0")
		{
			turn++;
			turn=turn%6;
		}
		
		printMessage("출제자는  "+ user[turn]);
		String msg = "newround@"+user[turn];
		due.setDummyInfo(msg);
		m_serverStub.cast(due,null,null);
		turn++;
		turn=turn%6;
	}

	private void gameover() {
		printMessage("〓〓〓〓〓〓〓〓〓〓〓〓〓〓");
		printMessage("    CMind 게임을 종료합니다.   ");
		printMessage("〓〓〓〓〓〓〓〓〓〓〓〓〓〓");

		//각유저별 점수 
        Set<String> keys = h.keySet();
        Iterator<String> it = keys.iterator();

        System.err.println("마지막 점수!!");      
        while(it.hasNext()) {
           String key = it.next();
           Integer value = h.get(key);
           printMessage(key+"의 점수는="+value);      
        }
        
        // 1등 2등 3등 정보 모든 클라이언트 한테 보내기 
        // 만약에 2명으로 강제종료 되면? : 이거 클라이언트에서도 처리해야함///////////////////////////******************************
    
        String endingmsg="";
        int[] ranks = new int[endingUserList.size()];
      
       
        if(m_serverStub.getLoginUsers().getMemberNum()==2) {           
        	ranks[0] = 1;
            ranks[1] = endingScoreList.get(1)==endingScoreList.get(0) ? 1: 2;
            endingmsg =  "@"+ "1등은 " + endingUserList.get(0)+ "입니다" +"@" + ranks[1]+"등은 "+endingUserList.get(1)+"입니다";
              
        }else {
              
        	ranks[0] = 1;
        	for (int i = 1; i < ranks.length; i++) {
                ranks[i] = endingScoreList.get(i) == endingScoreList.get(i-1) ? ranks[i - 1] : i + 1;
            }
           
        	for(int j=0; j<ranks.length; j++) {
        		System.out.print(ranks[j] +"등은 " + endingUserList.get(j)+"\n");
        	}
           
        	for(int j=0; j<ranks.length; j++) {            
        		endingmsg =  endingmsg + "@"+ ranks[j] +"등은 " + endingUserList.get(j)+ "입니다";
        	}
        
        }
       
        CMDummyEvent due = new CMDummyEvent();
        due.setDummyInfo("finish"+endingmsg); 
        m_serverStub.cast(due,null,null);      
           
        //초기화
        State=0;   
        turn=0;
        endingUserList.clear();
	}
	
	public void printStyledMessage(String strText, String strStyleName)
	{
		m_server.printStyledMessage(strText, strStyleName);
	}
	
	private void printMessage(String strText)
	{
		m_server.printMessage(strText);
	}
}
