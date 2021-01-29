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
	ArrayList<String> endingUserList = new ArrayList<String>(); //�������� 1��~6������� �������� ����Ʈ
	ArrayList<Integer> endingScoreList = new ArrayList<Integer>();
	String[] user = {"0","0","0","0","0","0"};	//�α����� ���� ������ ����ִ� �迭

	int host=0; 	//ȣ��Ʈ�� ��������
	int turn=0;		//���� ������
	int round=0;	//�� ����
	int State=0; 	//������ �����ߴ���  Ȯ�� 0���� 1����
	String turnUserName = null;
	int maxuser=6;	//�ִ� �����ο�
	int minuser=3;	//�ּ� �����ο�
	
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
			if(m_serverStub.getLoginUsers().getMemberNum()>maxuser)//6�� �ʰ��̸� �α��� ����
			{
				printMessage("�α��� ���� "+se.getUserName()+ "�����ο����� �ʰ��Ͽ����ϴ�.");
				m_serverStub.replyEvent(se, 0);
			}
			else if(State==1)
	         {
	            printMessage("�α��� ���� "+se.getUserName()+ "������ �̹� �����Ͽ����ϴ�.");
	            m_serverStub.replyEvent(se, 0);
	         }
			else
			{
				String name=se.getUserName();
				printMessage(name+" ���� �α����Ͽ����ϴ�.");
				h.put(name,0);
				m_serverStub.replyEvent(se, 1);
				
				for(j=0;j<6;j++)	//�迭�� ����
				{
					if(user[j]=="0")
					{
						user[j]=name;
						break;
					}
				}
				
				System.out.println("�� ��ġ�� :"+j+"��° ���� "+ user[j]);
				
				if(m_serverStub.getLoginUsers().getMemberNum()==1)	//ó�� ���� ����� ȣ��Ʈ
				{
					host=j;
					due.setDummyInfo("Host");
					m_serverStub.send(due,se.getUserName());
					printMessage("ȣ��Ʈ�� "+se.getUserName()+ "�Դϴ�");
				}
				
				String msg = "Info@"+m_serverStub.getLoginUsers().getMemberNum();						//info������ ���� �ο��� �˷��ֱ�
				due.setDummyInfo(msg);
				m_serverStub.cast(due,null,null);
				due = null;
			}
			
			printMessage("���� �ο� ��"+m_serverStub.getLoginUsers().getMemberNum());
			
			
			break;
			
		case CMSessionEvent.LOGOUT:	//�α׾ƿ�
			
			String name=se.getUserName();
	        h.remove(name);   //�콬���� �����
	         
	        for(j=0;j<6;j++) //���� �迭���� �����
	        {
	        	if(name.equals(user[j])) 
	            {
	               user[j]="0";
	               break;
	            }
	        }
	         
	        printMessage(name+"���� �α׾ƿ��Ͽ����ϴ�.");
	        printMessage("���� �ο� ��"+m_serverStub.getLoginUsers().getMemberNum());         
	         
	        //���� �ο��� �˷��ֱ�
	        String msg = "Info@"+m_serverStub.getLoginUsers().getMemberNum();   
	        due.setDummyInfo(msg);
	        m_serverStub.cast(due,null,null);
	         
	        //������������Ʈ������ �����
	        endingUserList.remove(name);
	                  
	         
	        if(user[host]=="0")      //ȣ��Ʈ�� ���������� ���ο� ȣ��Ʈ ����
	        {
	        	for(j=0;j<6;j++)
	            {
	            	if(user[j]!="0")
	            	{
	            		host=j;
	            		due.setDummyInfo("Host");
	            		m_serverStub.send(due,user[j]);
	            		printMessage("ȣ��Ʈ�� "+user[j]+ "�Դϴ�");
	            			break;
	            	}
	            }
	         
	         }
	         
	         if(State==1 && m_serverStub.getLoginUsers().getMemberNum()<minuser)   //���� �߰��� �÷��̾ 3�� �̸��� �Ǹ� ���� ����
	         {
	            List<Entry<String, Integer>> list_entries = new ArrayList<Entry<String, Integer>>(h.entrySet());
	                
	                Collections.sort(list_entries, new Comparator<Entry<String, Integer>>() {
	                   // compare�� ���� ��
	                   public int compare(Entry<String, Integer> obj1, Entry<String, Integer> obj2) {
	                      // ���� ���� ����
	                      return obj2.getValue().compareTo(obj1.getValue());
	                   }
	                });
	                
	                printMessage("�����ϱ�");
	                for(Entry<String, Integer> entry : list_entries) {   
	                   printMessage(entry.getKey() + " �� ������: " + entry.getValue());
	                      endingUserList.add(entry.getKey());   
	                      endingScoreList.add(entry.getValue());
	                }    
	                gameover();         
	         }
	         
	               
	         if(State==1 && user[turn-1]=="0")   //�����ڰ� ���������� ���ο� ȣ��Ʈ ����
	           {
	            System.out.println("�����ڰ� ��������!!!!");
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
			printStyledMessage("["+ie.getUserName()+"] ��  CMind ���ӿ� �����߽��ϴ�.", "bold");
			break;
		case CMInterestEvent.USER_LEAVE:
			printMessage("["+ie.getUserName()+"] leaves group("+ie.getHandlerGroup()+") in session("
					+ie.getHandlerSession()+").");
			printStyledMessage("["+ie.getUserName()+"] ��  CMind ������ �������ϴ�.", "bold");
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
		System.out.println("��ū Ȯ�ο� "+token);
		switch(token)
		{
			case "StartGame":
				printMessage("���ӽ��� �մϴ�!");
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
	            
	            printMessage(token+" �� ������ ������ϴ� (������: "+score+" ��)");
	                  
	            //�ؽ��� ���
	            Set<String> keys = h.keySet();
	            Iterator<String> it = keys.iterator();
	               
	            printStyledMessage("���� ����ϱ�", "bold");
	            while(it.hasNext()) {
	                  String key = it.next();
	                  Integer value = h.get(key);
	                  printMessage("// "+key+"�� ������ = "+value);      
	               }      
	        
	               List<Entry<String, Integer>> list_entries = new ArrayList<Entry<String, Integer>>(h.entrySet());
	               
	               Collections.sort(list_entries, new Comparator<Entry<String, Integer>>() {
	                  // compare�� ���� ��
	                  public int compare(Entry<String, Integer> obj1, Entry<String, Integer> obj2) {
	                     // ���� ���� ����
	                     return obj2.getValue().compareTo(obj1.getValue());
	                  }
	               });
	               
	               printStyledMessage("�ٲ� ������ �����ϱ�", "bold");
	               for(Entry<String, Integer> entry : list_entries) {   
	            	   	printMessage(entry.getKey() + " �� ������: " + entry.getValue()+" ��");
	                    endingUserList.add(entry.getKey());   
	                    endingScoreList.add(entry.getValue());
	               }  
	               
	               
	               printMessage(h.get(endingUserList.get(0))+"");
   
	               	String msg = "Winner@"+endingUserList.get(0)+" ( "+h.get(endingUserList.get(0))+" ��)";	
	               	due.setDummyInfo(msg);
   					m_serverStub.cast(due,null,null);
   					due = null;
	            
   					newround();
	                  				
	               endingUserList.clear();    
	               endingScoreList.clear();

	            break;
	            
			case "GiveUp":
				//���⸦ ���� �ϳ� �Ѱɷ� ��ġ�� ����
				newround();
				break;
				
			case "winner":
				token = st.nextToken();
				printStyledMessage(token+"�� ������ ������ϴ�.", "bold");
				printMessage("���������������");
				printMessage("    ���ο� ���带 �����մϴ�.   ");
				printMessage("���������������");
				newround();
				break;
				
			case "Answer":
				break;
				
			case "TimeOver1":
				//���⸦ ���� �ϳ� �Ѱɷ� ��ġ�� ����
		        newround();
		        printMessage("�ƹ��� ������ ���߽��ϴ٤Ф�");
		        break;
		            
			case "TimeOver2":
			   	//���⸦ ���� �ϳ� �Ѱɷ� ��ġ�� ����
		        newround();
		        printMessage("��ǲ �ð��ʰ�!");
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
		printStyledMessage("���� ����"+round, "bold");
		
		int j;
		for(j=0;j<6;j++)
			System.out.println("(Ȯ�ο�) newround �ӿ� " + user[j]);
		
		while(user[turn]=="0")
		{
			turn++;
			turn=turn%6;
		}
		
		printMessage("�����ڴ�  "+ user[turn]);
		String msg = "newround@"+user[turn];
		due.setDummyInfo(msg);
		m_serverStub.cast(due,null,null);
		turn++;
		turn=turn%6;
	}

	private void gameover() {
		printMessage("���������������");
		printMessage("    CMind ������ �����մϴ�.   ");
		printMessage("���������������");

		//�������� ���� 
        Set<String> keys = h.keySet();
        Iterator<String> it = keys.iterator();

        System.err.println("������ ����!!");      
        while(it.hasNext()) {
           String key = it.next();
           Integer value = h.get(key);
           printMessage(key+"�� ������="+value);      
        }
        
        // 1�� 2�� 3�� ���� ��� Ŭ���̾�Ʈ ���� ������ 
        // ���࿡ 2������ �������� �Ǹ�? : �̰� Ŭ���̾�Ʈ������ ó���ؾ���///////////////////////////******************************
    
        String endingmsg="";
        int[] ranks = new int[endingUserList.size()];
      
       
        if(m_serverStub.getLoginUsers().getMemberNum()==2) {           
        	ranks[0] = 1;
            ranks[1] = endingScoreList.get(1)==endingScoreList.get(0) ? 1: 2;
            endingmsg =  "@"+ "1���� " + endingUserList.get(0)+ "�Դϴ�" +"@" + ranks[1]+"���� "+endingUserList.get(1)+"�Դϴ�";
              
        }else {
              
        	ranks[0] = 1;
        	for (int i = 1; i < ranks.length; i++) {
                ranks[i] = endingScoreList.get(i) == endingScoreList.get(i-1) ? ranks[i - 1] : i + 1;
            }
           
        	for(int j=0; j<ranks.length; j++) {
        		System.out.print(ranks[j] +"���� " + endingUserList.get(j)+"\n");
        	}
           
        	for(int j=0; j<ranks.length; j++) {            
        		endingmsg =  endingmsg + "@"+ ranks[j] +"���� " + endingUserList.get(j)+ "�Դϴ�";
        	}
        
        }
       
        CMDummyEvent due = new CMDummyEvent();
        due.setDummyInfo("finish"+endingmsg); 
        m_serverStub.cast(due,null,null);      
           
        //�ʱ�ȭ
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
