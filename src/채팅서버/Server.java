package ä�ü���;

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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
// 11-28 JC �߰� : xor ���� ���� �����ϱ�
import java.util.Random;
/*
 * modified Author : Jeon Jong-Chan
 * Date : 2016-11-27 ~
 * ������ & ����
 * 1. ä�ù� �����Ⱑ �ȵȴ�.
 * 2. ���������ص� �����ʹ� ����� �Ѵ�.
 * 3. ��ȣȭ ������ �ȵ��ִ�.
 */
public class Server extends JFrame implements ActionListener{
	// �ڵ� import ����Ű ctrl + shift + o
	
   private JPanel contentPane;
   private JTextField port_tf;
   private JTextArea textArea = new JTextArea();
   private JButton start_btn = new JButton("��������");
   private JButton stop_btn = new JButton("��������");
   
   //Network �ڿ�
   
   private ServerSocket server_socket;
   private Socket socket;
   private int port;
   private Vector user_vc = new Vector();
   private Vector room_vc = new Vector();
   //�޼��� ��ȣȭ�� ���� �������� ������ xor ��
   private Vector key_vc = new Vector();
   private String key;
   
   
   private StringTokenizer st;
   
   
   Server()
   {
      init();//ȭ����� �޼ҵ�
      start();//������ ���� �޼ҵ�
   }
   private void start()
   {
      start_btn.addActionListener(this);
      stop_btn.addActionListener(this);
   }
   
   private void init()//ȭ�鱸��
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
      
      JLabel lblNewLabel = new JLabel("��Ʈ��ȣ");
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
      
      this.setVisible(true); // true = ȭ�鿡 ���̰� false = ������ �ʰ�
   }
   
   private void Server_start()
   {
      try {
         server_socket = new ServerSocket(port);
      } catch (IOException e) {
    	  JOptionPane.showMessageDialog(null,"�̹� ������� ��Ʈ��ȣ","�˸�",JOptionPane.INFORMATION_MESSAGE);
      } // ��Ʈ���
      if(server_socket !=null)
      {
         Connection();
      }
   }
   
   private void Connection()
   {
      
      //1������ �����忡���� 1������ �ϸ� �� �� �ִ�
      Thread th = new Thread(new Runnable(){
         @Override
         public void run(){ //�����忡�� ó���� ���� �����Ѵ�.
            
          while(true){ 
            try {
               textArea.append("����� ���� �����\n");
               socket = server_socket.accept(); //����� ���� ��� ���Ѵ��
               textArea.append("����� ����!!\n");
               
               UserInfo user = new UserInfo(socket);
             
               user.start(); // ��ü�� ������ ����
               
            } catch (IOException e) {    
            	break;
            }
          } // while �� ��
         }
         
      });
      
      th.start();
      
   }

   public static void main(String[] args) {
      
      new Server();

   }

   @Override
   public void actionPerformed(ActionEvent e) {
      
      if(e.getSource()==start_btn)
      {
         System.out.println("���� ��ŸƮ ��ư Ŭ��");
         port = Integer.parseInt(port_tf.getText().trim());
         
         Server_start(); //���� ���� �� ����� ���� ���
         
         start_btn.setEnabled(false);
         port_tf.setEnabled(false);
         stop_btn.setEnabled(true);
      }
      else if(e.getSource()==stop_btn)
      {
    	 stop_btn.setEnabled(false);
    	 start_btn.setEnabled(true);
         port_tf.setEnabled(true);
         
    	 try 
    	 {
			server_socket.close();
			user_vc.removeAllElements();
			room_vc.removeAllElements();
			key_vc.removeAllElements();
    	 } 
    	 catch (IOException e1) 
    	 {
			// TODO Auto-generated catch block
			e1.printStackTrace();
    	 }
    	  
         System.out.println("���� ��ž ��ư Ŭ��");
      }
      
   } // �׼� �̺�Ʈ ��
   
   class UserInfo extends Thread
   {
	   private OutputStream os;
	   private InputStream is;
	   private DataOutputStream dos;
	   private DataInputStream dis;
	   
	   private Socket user_socket;
	   private String Nickname = "";
	   private String pass = "";
	   
	   int count = 0;
   
	   private boolean RoomCh = true;
	   
	   UserInfo(Socket soc) // ������ �޼ҵ�
	   {
		   this.user_socket = soc;   
		   
		   UserNetwork();
		   
	   }
	   
	   private void UserNetwork() // ��Ʈ��ũ �ڿ� ���� (Stream ����)
	   {
		  try{
		  is = user_socket.getInputStream();
		  dis = new DataInputStream(is);
		  
		  os = user_socket.getOutputStream();
		  dos = new DataOutputStream(os);
		  
		  textArea.append("debug  1 ");
		  
		  Nickname = dis.readUTF(); // ������� �г����� �޴´�.
		  pass = dis.readUTF();//������� �н����带 �޴´�.
		  
		  textArea.append("debug NIckname :"+Nickname+"password : "+pass+"\n");

		  StringTokenizer join_tk = new StringTokenizer(pass, "/");
		  pass = join_tk.nextToken();
		  String join = "";
		  if(join_tk.hasMoreTokens())
		  {
			  join = join_tk.nextToken();
			  textArea.append("debug  2 : "+join+"\n");
			  join = join +"/" + Nickname;
			  InMessage(join);
		  }

		  BufferedReader in = new BufferedReader(new FileReader("info.txt"));
		  String s;
		  
		  while ((s = in.readLine()) != null) {
			  	textArea.append("debug  3 string : " + s +"\n");
			    StringTokenizer tk = new StringTokenizer(s, "/");
				String tmp_id = tk.nextToken();
			    String tmp_pass = tk.nextToken();
				System.out.println("����� id : "+tmp_id+"����� password :"+tmp_pass);
				
				if(tmp_id.equals(Nickname) && tmp_pass.equals(pass))
				{
					count = 0;
					break;
				}
				else
				{
					count++;
				}
		  }
		  if(count != 0)
		  {
			  System.out.println("�߸��� �н�����");
			  dos.close();
			  dis.close();
			  user_socket.close();
		  }
		  textArea.append(Nickname+":����� ����\n");
		  
		  // ���� ����ڵ鿡�� ���ο� ����� �˸�
		  System.out.println("���� ���ӵ� ����� �� :"+(user_vc.size()+1));
		 
		  BroadCast("NewUser/"+Nickname); //���� ����ڿ��� �ڽ��� �˸���
		  
		  // �ڽſ��� ���� ����ڸ� �޾ƿ��� �κ�
		  for (int i=0; i<user_vc.size(); i++)
		  {
			  UserInfo u = (UserInfo)user_vc.elementAt(i);
			  
			  send_Message("OldUser/"+u.Nickname);	  
		  }
		  
		  // �ڽſ��� ���� �� ����� �޾ƿ��� �κ�
		  for(int i=0; i<room_vc.size(); i++)
		  {
			  RoomInfo r = (RoomInfo)room_vc.elementAt(i);
			  
			  send_Message("OldRoom/"+r.Room_name);
		  }

		  user_vc.add(this); // ����ڿ��� �˸� �� Vector�� �ڽ��� �߰�
		  
		  }
		  catch(IOException e){
			  JOptionPane.showMessageDialog(null,"Stream ���� ����","�˸�",JOptionPane.INFORMATION_MESSAGE);
		  }
		  
		 
		  
	   }
	   
	   public void run() // Thread���� ó���� ����
	   {
		   while(true)
		   {
			   try {
				  	   
				   String msg = dis.readUTF(); 
				  
				   textArea.append(Nickname+":����ڷκ��� ���� �޼���:"+msg+"\n");
				   InMessage(msg);
				   
			   } catch (IOException e) {
				   
				   textArea.append(Nickname+":����� ���� ����\n");
				   
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
				   
				   
			   } // �޽��� ����
		   }
		   
		   
	   } // run �޼ҵ� ��
	   
	   private void InMessage(String str) // Ŭ���̾�Ʈ�κ��� ������ �޼��� ó�� 
	   {
		   st = new StringTokenizer(str, "/");
		   
		   String protocol = st.nextToken();
		   String message = st.nextToken();
		   
		   System.out.println("�������� :"+protocol);
		   System.out.println("�޼��� :"+message);
		   
		   if(protocol.equals("Note"))
		   {
			   //protocol = Note
			   //message = user
			   //note = �޴� ����
			   
			   String note = st.nextToken();
			   
			   System.out.println("�޴� ���:"+message);
			   System.out.println("���� ����:"+note);
			   
			   //���Ϳ��� �ش� ����ڸ� ã�Ƽ� �޼��� ����
			   
			   for(int i=0; i<user_vc.size(); i++)
			   {
				   UserInfo u = (UserInfo)user_vc.elementAt(i);
				   
				   if(u.Nickname.equals(message))
				   {
					   u.send_Message("Note/"+Nickname+"/"+note);
					   // Note/User1/~~~~
				   }
			   }
		   } // if �� ��
		   else if(protocol.equals("Join"))
		   {
			   System.out.println("������:"+message);
			   // ���Ͼȿ� ���ڿ� ����
			try {
				FileWriter fw = new FileWriter("info.txt", true);
				fw.write(Nickname+"/"+pass+"\r\n");
				fw.flush();
				// ��ü �ݱ�
				fw.close();
				//�ݾ��ش�.
				dos.close();
				dis.close();
				user_socket.close();
			} catch (IOException e) {
				// TODO �ڵ� ������ catch ���
				e.printStackTrace();
			}
		   }
		   else if(protocol.equals("CreateRoom"))
		   {
			   //1. ���� ���� ���� ���� �ϴ��� Ȯ���Ѵ�.
			   
			   for(int i=0; i < room_vc.size(); i++)
			   {
				   RoomInfo r = (RoomInfo)room_vc.elementAt(i);
				   
				   if(r.Room_name.equals(message)) //������� �ϴ� ���� �̹� ���� �� ��
				   {
					   send_Message("CreateRoomFail/Fail(�̸��ߺ�)");
					   RoomCh = false;
					   break;
				   }
				   
			   } // for ��
			   
			   if(RoomCh) //���� ���� �� ���� ��
			   {
				   Random random = new Random();
				   RoomInfo new_room = new RoomInfo(message,this);
				   room_vc.add(new_room); // ��ü �� ���Ϳ� ���� �߰�
				   // 11-28 JC �߰� : xor ���� ���� �����ϱ�
				   key = key_generate();
				   // 11-28 JC �߰� : xor_vc�� ������� ���������� ���� �� Ű�� �����ش�.
				   key_vc.add(key);
				   
				   send_Message("CreateRoom/"+message);
				   send_Message(key);
				   System.out.println(" new_room.BroadCast_Room");
				   new_room.BroadCast_Room("Notice/< �˸�/******* "+message+"���� �����Ͽ����ϴ� ******* >");
				   
				   BroadCast("New_Room/"+message);
			   }
			   
			   RoomCh=true;
		   } // else if �� ��
		   
		   else if(protocol.equals("Chatting"))
		   {
			   
			   String msg = st.nextToken();
			  
			   for(int i=0; i < room_vc.size(); i++)
			   	{
				   RoomInfo r = (RoomInfo)room_vc.elementAt(i);
				   
				   if(r.Room_name.equals(message)) // �ش� ���� ã������
				   {
					  r.BroadCast_Room("Chatting/"+Nickname+"/"+msg);
				   }
			   	}
			   
		   } //else if 
		   
		   else if(protocol.equals("JoinRoom"))
		   {
			   for(int i=0; i<room_vc.size(); i++)
			   {
				   RoomInfo r = (RoomInfo)room_vc.elementAt(i);
				   if(r.Room_name.equals(message))
				   {
					   //���ο� ����ڸ� �˸�
					   r.BroadCast_Room("Notice/< �˸�/******* "+message+"�濡 "+Nickname+"���� �����ϼ̽��ϴ� ******* >");
					   //����� �߰�
					   r.Add_User(this);
					   send_Message("JoinRoom/"+message);
					   send_Message(key);
				   }
			   }
		   }
		   else if(protocol.equals("Clear"))
		   {
			   for(int i=0; i<room_vc.size(); i++)
			   {
				   RoomInfo r = (RoomInfo)room_vc.elementAt(i);
				   if(r.Room_name.equals(message))
				   {
					   /*��ȭ������� ���� �ʿ�*/
					   
					   
					   send_Message("Chat_area_Clear/");
				   }
			   }
		   }
		   else if(protocol.equals("Exiting"))
		   {
			   
			   for(int i=0; i<room_vc.size(); i++)
			   {
				   RoomInfo r = (RoomInfo)room_vc.elementAt(i);
				   if(r.Room_name.equals(message))
				   {
					   /* ���� ������ �κ� ���� �ʿ�*/
					
					   room_vc.remove(message);
					   r.Room_user_vc.remove(message);
					   
					   
					   r.BroadCast_Room("Notice/< �˸�/******* "+Nickname+"���� "+message+"���� �����̽��ϴ� ******* >\n");
					   send_Message("Exiting/"+message);
					     
				   } 
			   }
			  
		   }
		   
		   
	   } 
	  
	   
	   
	   private void BroadCast(String str) // ��ü ����ڿ��� �޼��� ������ �κ�
	   {
		   for(int i=0;i<user_vc.size();i++) 
			  {
				  UserInfo u = (UserInfo)user_vc.elementAt(i);
				  
				  u.send_Message(str);  
			  }
	   }
	   
	   private void send_Message(String str) // ���ڿ��� �޾Ƽ� ����
	   {
		   try {
			dos.writeUTF(str);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		   
	   }
	   // 11-28 JC �߰�
	   public String key_generate()
	   {
	      //���� �ð��� �������� �� ��ȣȭ Ű ����
	      String time = Long.toString(System.currentTimeMillis( ));

	      System.out.println("debug time : "+ time);
	   
	      return new String(time);
	   
	   	}
   } // UserInfo class ��
   
   class RoomInfo
   {
	   private String Room_name;
	   private Vector Room_user_vc = new Vector();
	   
	   RoomInfo(String str, UserInfo u)
	   {
		   this.Room_name = str;
		   this.Room_user_vc.add(u);
	   }
	   
	   public void BroadCast_Room(String str) // ���� ���� ��� ������� �˸���
	   {
		   for(int i=0; i < Room_user_vc.size(); i++)
		   {
			   UserInfo u = (UserInfo)Room_user_vc.elementAt(i);
			   
			   u.send_Message(str);
		   }
	   }
	   
	   private void Add_User(UserInfo u) 
	   {
		   this.Room_user_vc.add(u);
	   }
   }
   
   
}