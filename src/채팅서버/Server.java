
package 채팅서버;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
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

public class Server extends JFrame implements ActionListener {
	// 자동 import 단축키 ctrl + shift + o

	private JPanel contentPane;
	private JTextField port_tf;
	private JTextArea textArea = new JTextArea();
	private JButton start_btn = new JButton("서버실행");
	private JButton stop_btn = new JButton("서버중지");
	
	// Network 자원

	private ServerSocket server_socket;
	private Socket socket;
	private int port;
	private Vector user_vc = new Vector();
	private Vector room_vc = new Vector();

	private StringTokenizer st;
	
	Server() {
		init();// 화면생성 메소드
		start();// 리스너 설정 메소드
	}

	private void start() {
		start_btn.addActionListener(this);
		stop_btn.addActionListener(this);
	}

	private void init()// 화면구성
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
		port_tf.setBounds(81, 235, 210, 20);
		;
		contentPane.add(port_tf);
		port_tf.setColumns(10);

		start_btn.setBounds(12, 280, 138, 23);
		contentPane.add(start_btn);

		stop_btn.setBounds(151, 280, 140, 23);
		contentPane.add(stop_btn);
		stop_btn.setEnabled(false);

		this.setVisible(true); // true = 화면에 보이게 false = 보이지 않게
	}

	private void Server_start() {
		try {
			server_socket = new ServerSocket(port);
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "이미 사용중인 포트번호", "알림", JOptionPane.INFORMATION_MESSAGE);
		} // 포트사용
		if (server_socket != null) {
			Connection();
		}
	}

	private void Connection() {

		// 1가지의 스레드에서는 1가지의 일만 할 수 있다
		Thread th = new Thread(new Runnable() {
			@Override
			public void run() { // 스레드에서 처리할 일을 기재한다.

				while (true) {
					try {
						textArea.append("사용자 접속 대기중\n");
						socket = server_socket.accept(); // 사용자 접속 대기 무한대기
						textArea.append("사용자 접속!!\n");

						UserInfo user = new UserInfo(socket);
						user.start(); // 객체의 스레드 실행

					} catch (IOException e) {
						break;
					}
				} // while 문 끝
			}

		});

		th.start();

	}

	public static void main(String[] args) {

		new Server();

	}

	@Override
	public void actionPerformed(ActionEvent e) {

		if (e.getSource() == start_btn) {
			System.out.println("서버 스타트 버튼 클릭");
			port = Integer.parseInt(port_tf.getText().trim());

			Server_start(); // 소켓 생성 및 사용자 접속 대기

			start_btn.setEnabled(false);
			port_tf.setEnabled(false);
			stop_btn.setEnabled(true);
		} else if (e.getSource() == stop_btn) {
			stop_btn.setEnabled(false);
			start_btn.setEnabled(true);
			port_tf.setEnabled(true);

			try {
				server_socket.close();
				user_vc.removeAllElements();
				room_vc.removeAllElements();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			System.out.println("서버 스탑 버튼 클릭");
		}

	} // 액션 이벤트 끝

	class UserInfo extends Thread {
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

			UserNetwork();

		}
		
//=========================== info.txt에서 정보 읽어와 User값과 비교 수행  =============================
//                         author : 재혁, date : 2016.11.29
		private boolean isUserInfo(String id, String pw) {
			try {
				// Hash Set으로 info.txt의 id와 pw 매칭
				BufferedReader in = new BufferedReader(new FileReader("info.txt"));
				StringTokenizer tk = null;
				StringTokenizer tkIDPW = null;
				HashMap<String, String> list = new HashMap<>();
				String s;
				while ((s = in.readLine()) != null) {
					tk = new StringTokenizer(s, "/");
					String tmp = tk.nextToken();
					System.out.println(tmp);
					tkIDPW = new StringTokenizer(tmp, " ");
					while (tkIDPW.hasMoreTokens()) {
						list.put(tkIDPW.nextToken(), tkIDPW.nextToken());
					}
				}
				System.out.println(list.get("test"));

				String getPw = list.get(id);
				in.close();
				if (getPw == null) {//pw가 null값이면 false 리턴
					return false;
				} else {
					if (getPw.equals(pw))//pw가 같다면  true 리턴
						return true;
				}
			} catch (IOException e) {
				System.err.println(e); // 에러가 있다면 메시지 출력
				System.exit(1);
			}
			return false;
		}
//============================================================================================	

//========================== info.txt에 User값을 저장 (info.txt가 없을 경우 생성) ========================
//				          author : 재혁, date : 2016.11.29
		private boolean setUserInfo(String id, String pw) {
			try {
				System.out.println("체크중 id : " + id);
				System.out.println("체크중 pw : " + pw);
				//filewriter를 이용한 info.txt 파일 생성
				FileWriter fw = new FileWriter("info.txt", true);

				// 파일안에 문자열 쓰기
				fw.write(id+" "+pw+"/\n");
				fw.flush();
				// 객체 닫기
				fw.close();

			} catch (IOException e) {
				System.err.println(e); // 에러가 있다면 메시지 출력
				System.exit(1);
			}
			return false;
		}
//=============================================================================================
		private void UserNetwork() // 네트워크 자원 설정 (Stream 설정)
		{
			try {
				is = user_socket.getInputStream();
				dis = new DataInputStream(is);

				os = user_socket.getOutputStream();
				dos = new DataOutputStream(os);
//=========================== 회원가입 시 Join을 포함한 id,pw 전송 =====================================
//=========================== 회원가입이 아닐시 id, pw 전송하여 id,pw 인증 =============================
//		                  author : 재혁, date : 2016.11.29	
				String inData = dis.readUTF(); // 사용자의 닉네임을 받는다.

				StringTokenizer tk = new StringTokenizer(inData, ":");
				String[] idstr = new String[3];
				int cnt = 0;

				// 첫 데이터 확인 
				while (tk.hasMoreTokens()) {
					idstr[cnt++] = tk.nextToken();
				}
				// Join 포함시, 회원가입으로 간주
				if (idstr[0].equals("Join")) {
					System.out.println("Join 누름");
					String id = idstr[1];
					String pw = idstr[2];
					setUserInfo(id,pw);
				} else { // Join이 아닐경우
					String id = idstr[0];
					String pw = idstr[1];
					System.out.println("id " + id);
					System.out.println("pw " + pw);
					
					if (!isUserInfo(id.trim(), pw.trim())) {
						dos.writeUTF("false");
					} else {
						dos.writeUTF("true");
					}
//============================================================================================
					
					Nickname = id;
					textArea.append(Nickname + ":사용자 접속\n");

					// 기존 사용자들에게 새로운 사용자 알림
					System.out.println("현재 접속된 사용자 수 :" + (user_vc.size() + 1));

					BroadCast("NewUser/" + Nickname); // 기존 사용자에게 자신을 알린다

					// 자신에게 기존 사용자를 받아오는 부분
					for (int i = 0; i < user_vc.size(); i++) {
						UserInfo u = (UserInfo) user_vc.elementAt(i);

						send_Message("OldUser/" + u.Nickname);
					}

					// 자신에게 기존 방 목록은 받아오는 부분
					for (int i = 0; i < room_vc.size(); i++) {
						RoomInfo r = (RoomInfo) room_vc.elementAt(i);

						send_Message("OldRoom/" + r.Room_name);
					}

					user_vc.add(this); // 사용자에게 알린 후 Vector에 자신을 추가
				}

			} catch (IOException e) {
				JOptionPane.showMessageDialog(null, "Stream 설정 에러", "알림", JOptionPane.INFORMATION_MESSAGE);
			}

		}

		public void run() // Thread에서 처리할 내용
		{
			while (true) {
				try {

					String msg = dis.readUTF();

					textArea.append(Nickname + ":사용자로부터 들어온 메세지:" + msg + "\n");
					InMessage(msg);

				} catch (IOException e) {

					textArea.append(Nickname + ":사용자 접속 종료\n");

					try {
						dos.close();
						dis.close();
						user_socket.close();
						user_vc.remove(this);
						BroadCast("User_out/" + Nickname);
					} catch (IOException e1) {

					}
					break;

				} // 메시지 수신
			}

		} // run 메소드 끝

		private void InMessage(String str) // 클라이언트로부터 들어오는 메세지 처리
		{
			st = new StringTokenizer(str, "/");

			String protocol = st.nextToken();
			String message = st.nextToken();

			System.out.println("프로토콜 :" + protocol);
			System.out.println("메세지 :" + message);

			if (protocol.equals("Note")) {
				// protocol = Note
				// message = user
				// note = 받는 내용

				String note = st.nextToken();

				System.out.println("받는 사람:" + message);
				System.out.println("보낼 내용:" + note);

				// 벡터에서 해당 사용자를 찾아서 메세지 전송

				for (int i = 0; i < user_vc.size(); i++) {
					UserInfo u = (UserInfo) user_vc.elementAt(i);

					if (u.Nickname.equals(message)) {
						u.send_Message("Note/" + Nickname + "/" + note);
						// Note/User1/~~~~
					}
				}
			} // if 문 끝
			else if (protocol.equals("CreateRoom")) {
				// 1. 현재 같은 방이 존재 하는지 확인한다.

				for (int i = 0; i < room_vc.size(); i++) {
					RoomInfo r = (RoomInfo) room_vc.elementAt(i);

					if (r.Room_name.equals(message)) // 만들고자 하는 방이 이미 존재 할 때
					{
						send_Message("CreateRoomFail/Fail(이름중복)");
						RoomCh = false;
						break;
					}

				} // for 끝

				if (RoomCh) // 방을 만들 수 있을 때
				{
					RoomInfo new_room = new RoomInfo(message, this);
					room_vc.add(new_room); // 전체 방 벡터에 방을 추가

					send_Message("CreateRoom/" + message);

					new_room.BroadCast_Room("Chatting/< 알림/******* " + message + "방을 개설하였습니다 ******* >");

					BroadCast("New_Room/" + message);
				}

				RoomCh = true;
			} // else if 문 끝

			else if (protocol.equals("Chatting")) {

				String msg = st.nextToken();

				for (int i = 0; i < room_vc.size(); i++) {
					RoomInfo r = (RoomInfo) room_vc.elementAt(i);

					if (r.Room_name.equals(message)) // 해당 방을 찾았을때
					{
						r.BroadCast_Room("Chatting/" + Nickname + "/" + msg);
					}
				}

			} // else if

			else if (protocol.equals("JoinRoom")) {
				for (int i = 0; i < room_vc.size(); i++) {
					RoomInfo r = (RoomInfo) room_vc.elementAt(i);
					if (r.Room_name.equals(message)) {
						// 새로운 사용자를 알림
						r.BroadCast_Room(
								"Chatting/< 알림/******* " + message + "방에 " + Nickname + "님이 입장하셨습니다 ******* >");

						// 사용자 추가
						r.Add_User(this);
						send_Message("JoinRoom/" + message);
					}
				}
			} else if (protocol.equals("Clear")) {
				for (int i = 0; i < room_vc.size(); i++) {
					RoomInfo r = (RoomInfo) room_vc.elementAt(i);
					if (r.Room_name.equals(message)) {
						/* 대화내용삭제 구현 필요 */

						send_Message("Chat_area_Clear/");
					}
				}
			} else if (protocol.equals("Exiting")) {

				for (int i = 0; i < room_vc.size(); i++) {
					RoomInfo r = (RoomInfo) room_vc.elementAt(i);
					if (r.Room_name.equals(message)) {
						/* 방을 나가는 부분 구현 필요 */

						room_vc.remove(message);
						r.Room_user_vc.remove(message);

						r.BroadCast_Room(
								"Chatting/< 알림/******* " + Nickname + "님이 " + message + "에서 나가셨습니다 ******* >\n");
						send_Message("Exiting/" + message);

					}
				}

			}

		}

		private void BroadCast(String str) // 전체 사용자에게 메세지 보내는 부분
		{
			for (int i = 0; i < user_vc.size(); i++) {
				UserInfo u = (UserInfo) user_vc.elementAt(i);

				u.send_Message(str);
			}
		}

		private void send_Message(String str) // 문자열을 받아서 전송
		{
			try {
				dos.writeUTF(str);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	} // UserInfo class 끝

	class RoomInfo {
		private String Room_name;
		private Vector Room_user_vc = new Vector();

		RoomInfo(String str, UserInfo u) {
			this.Room_name = str;
			this.Room_user_vc.add(u);
		}

		public void BroadCast_Room(String str) // 현재 방의 모든 사람에게 알린다
		{
			for (int i = 0; i < Room_user_vc.size(); i++) {
				UserInfo u = (UserInfo) Room_user_vc.elementAt(i);

				u.send_Message(str);
			}
		}

		private void Add_User(UserInfo u) {
			this.Room_user_vc.add(u);
		}
	}

}