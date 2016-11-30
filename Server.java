
package 채팅서버;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

//ActionListener를 상속받는다. 
public class Server extends JFrame implements ActionListener{
	// 자동 import 단축키 ctrl + shift + o
	
   private JPanel contentPane;
   private JTextField port_tf;
   private JTextArea textArea = new JTextArea();
   private JButton start_btn = new JButton("서버실행");
   private JButton stop_btn = new JButton("서버중지");
   
   //Network 자원
   private ServerSocket server_socket;
   private Socket socket;
   private int port;
 //동적으로 크기가 변하는 배열의 일종으로 크기가 확정되지 않은 배열을 사용하는 Vector 클래스 객제 생성
   private Vector user_vc = new Vector();//class UserInfo에서 사용
   private Vector room_vc = new Vector();//class RoomInfo에서 사용
   
   //문자역을 파싱하여 다양한 형태의 토큰으로 분리
   private StringTokenizer st;
   
   
   Server()
   {
      init();//화면생성 메소드
      start();//리스너 설정 메소드
   }
   private void start()//server생성자
   {
      start_btn.addActionListener(this);//start버튼 액션 리스너
      stop_btn.addActionListener(this);//stop버튼 액션 리스너
   }
   
   private void init()//화면구성 메소드
   {
      setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      setBounds(100, 100, 319, 370);
      contentPane = new JPanel();
      contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
      setContentPane(contentPane);
      contentPane.setLayout(null);
      
      JScrollPane scrollPane = new JScrollPane();
      scrollPane.setBounds(12, 10, 279, 205);
      contentPane.add(scrollPane);
      
          
      scrollPane.setViewportView(textArea);
      textArea.setEditable(false);
      
      JLabel lblNewLabel = new JLabel("포트번호");
      lblNewLabel.setBounds(12, 238, 57, 15);
      contentPane.add(lblNewLabel);
      
      port_tf = new JTextField();
      port_tf.setBounds(81, 235, 210, 20);;
      contentPane.add(port_tf);
      port_tf.setColumns(10);
      
      
      start_btn.setBounds(12, 280, 138, 23);
      contentPane.add(start_btn);
      
      
      stop_btn.setBounds(151, 280, 140, 23);
      contentPane.add(stop_btn);
      stop_btn.setEnabled(false);
      
      this.setVisible(true); // true = 화면에 보이게 false = 보이지 않게
   }
   
   private void Server_start()
   {
      try {
         server_socket = new ServerSocket(port);
      } catch (IOException e) {
    	  JOptionPane.showMessageDialog(null,"이미 사용중인 포트번호","알림",JOptionPane.INFORMATION_MESSAGE);
      } // 포트사용
      if(server_socket !=null)
      {
         Connection();
      }
   }
   
   private void Connection()
   {
      
      //1가지의 스레드에서는 1가지의 일만 할 수 있다
      Thread th = new Thread(new Runnable(){
         @Override
         public void run(){ //스레드에서 처리할 일을 기재한다.
            
          while(true){ 
            try {
               textArea.append("사용자 접속 대기중\n");
               socket = server_socket.accept(); //사용자 접속 대기 무한대기
               textArea.append("사용자 접속!!\n");
               
               UserInfo user = new UserInfo(socket);//UserInfo 객체 생성
             
               user.start(); // 객체의 스레드 실행
               
            } catch (IOException e) {    
            	break;
            }
          } // while 문 끝
         }
         
      });
      
      th.start();//쓰레드 실행
      
   }

   public static void main(String[] args) {//메인
      
      new Server();//Server객체 실행

   }

   @Override
   public void actionPerformed(ActionEvent e) {//액션이벤트 수행
      
      if(e.getSource()==start_btn)//start버튼 눌렀을 때
      {
         System.out.println("서버 스타트 버튼 클릭");
         port = Integer.parseInt(port_tf.getText().trim());
         
         Server_start(); //소켓 생성 및 사용자 접속 대기
         
         start_btn.setEnabled(false);
         port_tf.setEnabled(false);
         stop_btn.setEnabled(true);
      }
      else if(e.getSource()==stop_btn)//stop버튼 눌렀을 때
      {
    	 stop_btn.setEnabled(false);
    	 start_btn.setEnabled(true);
         port_tf.setEnabled(true);
         
    	 try 
    	 {
			server_socket.close();//서버 소켓 닫음
			user_vc.removeAllElements();//user vector에 있는 원소 모두 삭제
			room_vc.removeAllElements();//room vector에 있는 원소 모두 삭제
    	 } 
    	 catch (IOException e1) 
    	 {
			// TODO Auto-generated catch block
			e1.printStackTrace();
    	 }
    	  
         System.out.println("서버 스탑 버튼 클릭");
      }
      
   } // 액션 이벤트 끝
   
   class UserInfo extends Thread//User 원소 관리
   {
	   //stream 객체 생성
	   private OutputStream os;
	   private InputStream is;
	   private DataOutputStream dos;
	   private DataInputStream dis;
	   
	   private Socket user_socket;
	   private String Nickname = "";
   
	   private boolean RoomCh = true;
	   
	   UserInfo(Socket soc) // 생성자 메소드
	   {
		   this.user_socket = soc;   
		   
		   UserNetwork();//네트워크 자원 설정
		   
	   }
	   
	   private void UserNetwork() // 네트워크 자원 설정 (Stream 설정)
	   {
		  try{
		  is = user_socket.getInputStream();
		  dis = new DataInputStream(is);
		  
		  os = user_socket.getOutputStream();
		  dos = new DataOutputStream(os);
		  
		  Nickname = dis.readUTF(); // 사용자의 닉네임을 받는다.
		  textArea.append(Nickname+":사용자 접속\n");
		  
		  // 기존 사용자들에게 새로운 사용자 알림
		  System.out.println("현재 접속된 사용자 수 :"+(user_vc.size()+1));
		 
		  BroadCast("NewUser/"+Nickname); //기존 사용자에게 자신을 알린다
		  
		  // 자신에게 기존 사용자를 받아오는 부분
		  for (int i=0; i<user_vc.size(); i++)
		  {
			  UserInfo u = (UserInfo)user_vc.elementAt(i);
			  
			  send_Message("OldUser/"+u.Nickname);	  
		  }
		  
		  // 자신에게 기존 방 목록은 받아오는 부분
		  for(int i=0; i<room_vc.size(); i++)
		  {
			  RoomInfo r = (RoomInfo)room_vc.elementAt(i);
			  
			  send_Message("OldRoom/"+r.Room_name);
		  }

		  user_vc.add(this); // 사용자에게 알린 후 Vector에 자신을 추가
		  
		  }
		  catch(IOException e){
			  JOptionPane.showMessageDialog(null,"Stream 설정 에러","알림",JOptionPane.INFORMATION_MESSAGE);
		  }
		  
		 
		  
	   }
	   
	   public void run() // Thread에서 처리할 내용
	   {
		   while(true)//프로그램 종료 전까지 계속 실행
		   {
			   try {
				  	   
				   String msg = dis.readUTF(); //메시지수신
				  
				   textArea.append(Nickname+":사용자로부터 들어온 메세지:"+msg+"\n");
				   InMessage(msg);//메시지 처리
				   
			   } catch (IOException e) {//IO예외 발생한 경우
				   
				   textArea.append(Nickname+":사용자 접속 종료\n");
				   //예외 발생한 클라이언트 접속 종료시킴
				   try{
				   dos.close();
				   dis.close();
				   user_socket.close();
				   user_vc.remove(this);
				   BroadCast("User_out/"+Nickname);
				   }
				   catch(IOException e1){
					   
				   }
				   break;
				   
				   
			   } 
		   }
		   
		   
	   } // run 메소드 끝
	   
	   private void InMessage(String str) // 클라이언트로부터 들어오는 메세지 처리 
	   {
		   st = new StringTokenizer(str, "/");//str메시지 파싱하여 토큰(/) 분리
		   
		   String protocol = st.nextToken();//str에서 첫번째/ 이후의 문자열
		   String message = st.nextToken();//str에서 두번째/ 이후의 문자열
		   
		   System.out.println("프로토콜 :"+protocol);
		   System.out.println("메세지 :"+message);
		   
		   if(protocol.equals("Note"))//쪽지 받을 때
		   {
			   //protocol = Note
			   //message = user
			   //note = 받는 내용
			   
			   String note = st.nextToken();//str에서 세번째/ 이후의 문자열
			   
			   System.out.println("받는 사람:"+message);
			   System.out.println("보낼 내용:"+note);
			   
			   //벡터에서 해당 사용자를 찾아서 메세지 전송
			   for(int i=0; i<user_vc.size(); i++)
			   {
				   UserInfo u = (UserInfo)user_vc.elementAt(i);
				   
				   if(u.Nickname.equals(message))
				   {
					   u.send_Message("Note/"+Nickname+"/"+note);
					   // Note/User1/~~~~
				   }
			   }
		   } // if 문 끝
		   else if(protocol.equals("CreateRoom"))//방을 만들었을 때
		   {
			   //1. 현재 같은 방이 존재 하는지 확인한다.
			   
			   for(int i=0; i < room_vc.size(); i++)
			   {
				   RoomInfo r = (RoomInfo)room_vc.elementAt(i);
				   
				   if(r.Room_name.equals(message)) //만들고자 하는 방이 이미 존재 할 때
				   {
					   send_Message("CreateRoomFail/Fail(이름중복)");
					   RoomCh = false;
					   break;
				   }
				   
			   } // for 끝
			   
			   if(RoomCh) //방을 만들 수 있을 때
			   {
				   RoomInfo new_room = new RoomInfo(message,this);
				   room_vc.add(new_room); // 전체 방 벡터에 방을 추가
				   
				   send_Message("CreateRoom/"+message);
				   
				   new_room.BroadCast_Room("Chatting/< 알림/******* "+message+"방을 개설하였습니다 ******* >");
				   
				   BroadCast("New_Room/"+message);
			   }
			   
			   RoomCh=true;
		   } // else if 문 끝
		   
		   else if(protocol.equals("Chatting"))//채팅방에서 대화 주고받을 때
		   {
			   
			   String msg = st.nextToken();//str에서 세번째/ 이후의 문자열
			  
			   for(int i=0; i < room_vc.size(); i++)
			   	{
				   RoomInfo r = (RoomInfo)room_vc.elementAt(i);
				   
				   if(r.Room_name.equals(message)) // 해당 방을 찾았을때
				   {
					  r.BroadCast_Room("Chatting/"+Nickname+"/"+msg);
				   }
			   	}
			   
		   } //else if 
		   
		   else if(protocol.equals("JoinRoom"))//방에 들어갈때
		   {
			   for(int i=0; i<room_vc.size(); i++)
			   {
				   RoomInfo r = (RoomInfo)room_vc.elementAt(i);
				   if(r.Room_name.equals(message))
				   {
					   //새로운 사용자를 알림
					   r.BroadCast_Room("Chatting/< 알림/******* "+message+"방에 "+Nickname+"님이 입장하셨습니다 ******* >");
					   
					   //사용자 추가
					   r.Add_User(this);
					   send_Message("JoinRoom/"+message);
				   }
			   }
		   }
		   else if(protocol.equals("Clear"))//대화내용 삭제할 때
		   {
			   for(int i=0; i<room_vc.size(); i++)
			   {
				   RoomInfo r = (RoomInfo)room_vc.elementAt(i);
				   if(r.Room_name.equals(message))
				   {
					   /*대화내용삭제 구현 필요*/
					   
					   
					   send_Message("Chat_area_Clear/");
				   }
			   }
		   }
		   else if(protocol.equals("Exiting"))//채팅방 나갈 때
		   {
			   
			   for(int i=0; i<room_vc.size(); i++)
			   {
				   RoomInfo r = (RoomInfo)room_vc.elementAt(i);
				   if(r.Room_name.equals(message))
				   {
					   /* 방을 나가는 부분 구현 필요*/
					
					   room_vc.remove(message);
					   r.Room_user_vc.remove(message);
					   
					   
					   r.BroadCast_Room("Chatting/< 알림/******* "+Nickname+"님이 "+message+"에서 나가셨습니다 ******* >\n");
					   send_Message("Exiting/"+message);
					     
				   } 
			   }
			  
		   }
		   
		   
	   } 
	  
	   
	   
	   private void BroadCast(String str) // 전체 사용자에게 메세지 보내는 부분
	   {
		   for(int i=0;i<user_vc.size();i++) 
			  {
				  UserInfo u = (UserInfo)user_vc.elementAt(i);
				  
				  u.send_Message(str);  
			  }
	   }
	   
	   private void send_Message(String str) // 문자열을 받아서 클라이언트에게 전송
	   {
		   try {
			dos.writeUTF(str);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		   
	   }
   } // UserInfo class 끝
   
   class RoomInfo//Room 원소 관리
   {
	   private String Room_name;
	   private Vector Room_user_vc = new Vector();//Room 객체 생성
	   
	   RoomInfo(String str, UserInfo u)//RoomInfo 생성자
	   {
		   this.Room_name = str;
		   this.Room_user_vc.add(u);//Room에 User 추가
	   }
	   
	   public void BroadCast_Room(String str) // 현재 방의 모든 사람에게 알린다
	   {
		   for(int i=0; i < Room_user_vc.size(); i++)
		   {
			   UserInfo u = (UserInfo)Room_user_vc.elementAt(i);
			   
			   u.send_Message(str);
		   }
	   }
	   //Room에 user추가
	   private void Add_User(UserInfo u) 
	   {
		   this.Room_user_vc.add(u);
	   }
   }
   
   
}