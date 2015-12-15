import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

public class Main {

	static int PORT = 5566;

	static ArrayList<Account> account = new ArrayList<Account>();

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			@SuppressWarnings("resource")
			ServerSocket serverSocket = new ServerSocket(PORT);
			while (true) {
				Socket s = serverSocket.accept();
				System.out.println("new client");
				new MyThread(s).start();
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}

class MyThread extends Thread {

	Socket s;
	Account account = null;

	public MyThread(Socket s) {
		// TODO Auto-generated constructor stub
		this.s = s;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		try {
			InputStream inStream = s.getInputStream();
			OutputStream outStream = s.getOutputStream();

			Scanner in = new Scanner(inStream);
			PrintWriter out = new PrintWriter(outStream, true);
			while (in.hasNextLine()) {
				String msg = in.nextLine();
				
				System.out.println(msg);

				String[] cmd = msg.split(" ");
				if (cmd[0].equals("exit")) {
					if (account != null) {
						Main.account.remove(account);
						account = null;
					}
					out.print("exit\n");
					break;
				} else if (cmd[0].equals("init")) {
					if (cmd.length < 2 || !cmd[1].equals("-u")) {
						out.print("option error\n");
						continue;
					}
					if (cmd.length < 3) {
						out.print("args error\n");
						continue;
					}

					boolean flag = false;
					for (int i = 0; i < cmd[2].length(); i++) {
						char tmp = cmd[2].charAt(i);
						if (!((tmp >= 'a' && tmp <= 'z')
								|| (tmp >= 'A' && tmp <= 'Z')
								|| (tmp >= '0' && tmp <= '9') || tmp == '_' || tmp == '-')) {
							// account_name名称不合法
							out.print("args error\n");
							flag = true;
							break;
						}
					}
					if (flag)
						continue;

					// account_name名称合法

					int j;
					for (j = 0; j < Main.account.size(); j++) {
						if (cmd[2].equals(Main.account.get(j).getAccountName()))
							break;
					}
					if (j == Main.account.size()) {
						// account_name重复
						out.print("This account has been registered\n");
						continue;
					}

					account = new Account(cmd[2]);
					Main.account.add(account);
					out.print(cmd[2] + "@nctu.edu.tw\n");

				}

			}

			in.close();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

class Account {
	private String account_name;
	private ArrayList<Mail> allMails;

	Account(String account_name) {
		this.account_name = account_name;
		allMails = new ArrayList<Mail>();
	}

	String getAccountName() {
		return account_name;
	}

	void receiveMail(String from, String date, String title, String content) {
		allMails.add(new Mail(from, account_name, date, title, content));
	}

	String sendMail(String to, String date, String title, String content) {
		int index;
		for (index = 0; index < Main.account.size(); index++) {
			if (to.equals(Main.account.get(index))) {
				break;
			}
		}

		if (index < Main.account.size()) {
			Main.account.get(index).receiveMail(account_name, date, title,
					content);
			return "done\n";
		} else {
			return "args error\n";
		}

	}

}

class Mail {
	private String from;
	private String to;
	private String date;
	private String title;
	private String content;
	private boolean isRead;

	Mail(String from, String to, String date, String title, String content) {
		this.from = from;
		this.to = to;
		this.date = date;
		this.title = title;
		this.content = content;
		isRead = false;
	}

	String getFrom() {
		return from;
	}

	String getTo() {
		return to;
	}

	String getDate() {
		return date;
	}

	String getTitle() {
		return title;
	}

	String getContent() {
		return content;
	}

	void read() {
		if (!isRead)
			isRead = true;
	}

}
