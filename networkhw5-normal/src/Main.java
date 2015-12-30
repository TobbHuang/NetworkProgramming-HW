import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

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
				// System.out.println("new client");
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

			byte[] buffer = new byte[1024];
			int length = 0;

			while (true) {
				length = inStream.read(buffer);

				String msg = new String(buffer, 0, length);

				// System.out.println(msg);

				// String[] cmd = msg.split(" ");

				// 切割
				ArrayList<String> cmd = new ArrayList<String>();
				for (int i = 0; i < msg.length(); i++) {

					if (msg.charAt(i) != ' ') {
						if (msg.charAt(i) == '"') {
							int j = i + 1;
							while (j < msg.length()) {
								if (msg.charAt(j) == '"')
									break;
								j++;
							}

							cmd.add(msg.substring(i, j + 1));

							i = j;

						} else {
							int j = i + 1;
							while (j < msg.length()) {
								if (msg.charAt(j) == ' ')
									break;
								j++;
							}

							cmd.add(msg.substring(i, j));

							i = j;
						}
					}

				}

				// 指令处理
				if (cmd.get(0).equals("exit")) {
					if (account != null) {
						Main.account.remove(account);
						account = null;
					}
					outStream.write("exit\n".getBytes());
					break;
				} else if (cmd.get(0).equals("init")) {
					if (cmd.size() < 2 || !cmd.get(1).equals("-u")) {
						outStream.write("option error\n".getBytes());
						continue;
					}
					if (cmd.size() != 3) {
						outStream.write("args error\n".getBytes());
						continue;
					}

					// 过滤不合法字符
					String tmpStr = cmd.get(2);
					for (int i = 0; i < tmpStr.length();) {
						char tmp = tmpStr.charAt(i);
						if (!((tmp >= 'a' && tmp <= 'z')
								|| (tmp >= 'A' && tmp <= 'Z')
								|| (tmp >= '0' && tmp <= '9') || tmp == '_'
								|| tmp == '-' || tmp == ' ')) {

							tmpStr = tmpStr.substring(0, i)
									+ tmpStr.substring(i + 1);

						} else {
							i++;
						}
					}

					int j;
					for (j = 0; j < Main.account.size(); j++) {
						if (tmpStr.equals(Main.account.get(j).getAccountName()))
							break;
					}

					if (Main.account.size() == 0 || j == Main.account.size()) {
						account = new Account(tmpStr);
						Main.account.add(account);
						outStream.write((tmpStr + "@nctu.edu.tw\n").getBytes());
					} else {
						// account_name重复
						outStream.write("This account has been registered\n"
								.getBytes());
					}

				} else if (cmd.get(0).equals("ls")) {
					if (cmd.size() != 2) {
						outStream.write("args error\n".getBytes());
						continue;
					}

					if (cmd.get(1).equals("-u")) {
						String tmp = "";
						if (Main.account.size() == 0) {
							tmp = "no accounts\n";
						} else {
							for (int i = 0; i < Main.account.size(); i++) {
								tmp += (Main.account.get(i).getAccountName() + "@nctu.edu.tw\n");
							}
						}
						outStream.write(tmp.getBytes());
					} else if (cmd.get(1).equals("-l")) {
						if (account == null) {
							outStream.write("init first\n".getBytes());
						} else {
							outStream.write(account.getAllMails().getBytes());
						}
					} else if (cmd.get(1).equals("-a")) {
						if (account == null) {
							outStream.write("init first\n".getBytes());
						} else {
							outStream.write(account.getInfo().getBytes());
						}
					} else {
						outStream.write("option error\n".getBytes());
						continue;
					}

				} else if (cmd.get(0).equals("rm")) {
					if (cmd.size() < 2) {
						outStream.write("option error\n".getBytes());
						continue;
					}

					if (cmd.get(1).equals("-d")) {
						if (cmd.size() != 3) {
							outStream.write("args error\n".getBytes());
							continue;
						}

						if (isDigit(cmd.get(2))) {
							outStream.write(account.deleteMail(
									Integer.parseInt(cmd.get(2)) - 1)
									.getBytes());
						} else {
							outStream.write("args error\n".getBytes());
						}

					} else if (cmd.get(1).equals("-D")) {

						if (cmd.size() != 2) {
							outStream.write("args error\n".getBytes());
							continue;
						}

						outStream.write(account.deleteMail(-1).getBytes());
					} else {
						outStream.write("option error\n".getBytes());
					}

				} else if (cmd.get(0).equals("rd")) {
					if (cmd.size() < 2 || !cmd.get(1).equals("-r")) {
						outStream.write("option error\n".getBytes());
						continue;
					}

					if (cmd.size() != 3 || !isDigit(cmd.get(2))) {
						outStream.write("args error\n".getBytes());
						continue;
					}

					outStream.write(account.read(
							Integer.parseInt(cmd.get(2)) - 1).getBytes());
					continue;

				} else if (cmd.get(0).equals("wt")) {
					if (cmd.size() < 2
							|| (!cmd.get(1).equals("-d")
									&& !cmd.get(1).equals("-t") && !cmd.get(1)
									.equals("-c"))) {
						outStream.write("option error\n".getBytes());
						continue;
					}

					if (cmd.size() < 3) {
						outStream.write("args error\n".getBytes());
						continue;
					}

					if (cmd.size() < 4
							|| (!cmd.get(3).equals("-d")
									&& !cmd.get(3).equals("-t") && !cmd.get(3)
									.equals("-c"))) {
						outStream.write("option error\n".getBytes());
						continue;
					}

					if (cmd.size() < 5) {
						outStream.write("args error\n".getBytes());
						continue;
					}

					if (cmd.size() < 6
							|| (!cmd.get(5).equals("-d")
									&& !cmd.get(5).equals("-t") && !cmd.get(5)
									.equals("-c"))) {
						outStream.write("option error\n".getBytes());
						continue;
					}
					
					if (cmd.size() < 7) {
						outStream.write("args error\n".getBytes());
						continue;
					}

					if (cmd.size() != 7) {
						outStream.write("option error\n".getBytes());
						continue;
					}

					Date now = new Date();
					SimpleDateFormat dateFormat = new SimpleDateFormat(
							"yyyy-MM-dd HH:mm:ss");

					String str1 = "";
					String str2 = "";
					String str3 = "";

					if (cmd.get(1).equals("-d")) {
						str1 = cmd.get(2);
					} else if (cmd.get(1).equals("-t")) {
						str2 = cmd.get(2);
					} else if (cmd.get(1).equals("-c")) {
						str3 = cmd.get(2);
					}

					if (cmd.get(3).equals("-d")) {
						str1 = cmd.get(4);
					} else if (cmd.get(3).equals("-t")) {
						str2 = cmd.get(4);
					} else if (cmd.get(3).equals("-c")) {
						str3 = cmd.get(4);
					}

					if (cmd.get(5).equals("-d")) {
						str1 = cmd.get(6);
					} else if (cmd.get(5).equals("-t")) {
						str2 = cmd.get(6);
					} else if (cmd.get(5).equals("-c")) {
						str3 = cmd.get(6);
					}

					if (str1 == "" || str2 == "" || str3 == "") {
						outStream.write("option error\n".getBytes());
						continue;
					}

					str1 = isStrLegal(str1);
					str2 = isStrLegal(str2);
					str3 = isStrLegal(str3);
					outStream.write(account.sendMail(str1,
							dateFormat.format(now), str2, str3).getBytes());

				} else if (cmd.get(0).equals("re")) {
					if (cmd.size() < 2
							|| (!cmd.get(1).equals("-c") && !cmd.get(1).equals(
									"-n"))) {
						outStream.write("option error\n".getBytes());
						continue;
					}

					if (cmd.size() < 3) {
						outStream.write("args error\n".getBytes());
						continue;
					}

					if (cmd.size() < 4
							|| (!cmd.get(3).equals("-c") && !cmd.get(3).equals(
									"-n"))) {
						outStream.write("option error\n".getBytes());
						continue;
					}

					if (cmd.size() < 5) {
						outStream.write("args error\n".getBytes());
						continue;
					}
					
					if (cmd.size() != 5) {
						outStream.write("option error\n".getBytes());
						continue;
					}

					Date now = new Date();
					SimpleDateFormat dateFormat = new SimpleDateFormat(
							"yyyy-MM-dd HH:mm:ss");

					String str1 = "";
					String str2 = "";

					if (cmd.get(1).equals("-c")) {
						str1 = cmd.get(2);
					} else if (cmd.get(1).equals("-n")) {
						str2 = cmd.get(2);
					}

					if (cmd.get(3).equals("-c")) {
						str1 = cmd.get(4);
					} else if (cmd.get(3).equals("-n")) {
						str2 = cmd.get(4);
					}

					if (str1 == "" || str2 == "") {
						outStream.write("option error\n".getBytes());
						continue;
					}

					if (!isDigit(str2)) {
						outStream.write("args error\n".getBytes());
						continue;
					}

					str1 = isStrLegal(str1);
					outStream.write(account.reply(str1,
							Integer.parseInt(str2) - 1, dateFormat.format(now))
							.getBytes());

				} else if (cmd.get(0).equals("fwd")) {
					if (cmd.size() < 2
							|| (!cmd.get(1).equals("-d")
									&& !cmd.get(1).equals("-c") && !cmd.get(1)
									.equals("-n"))) {
						outStream.write("option error\n".getBytes());
						continue;
					}

					if (cmd.size() < 3) {
						outStream.write("args error\n".getBytes());
						continue;
					}

					if (cmd.size() < 4
							|| (!cmd.get(3).equals("-d")
									&& !cmd.get(3).equals("-c") && !cmd.get(3)
									.equals("-n"))) {
						outStream.write("option error\n".getBytes());
						continue;
					}

					if (cmd.size() < 5) {
						outStream.write("args error\n".getBytes());
						continue;
					}

					if (cmd.size() < 6
							|| (!cmd.get(5).equals("-d")
									&& !cmd.get(5).equals("-c") && !cmd.get(5)
									.equals("-n"))) {
						outStream.write("option error\n".getBytes());
						continue;
					}

					if (cmd.size() < 7) {
						outStream.write("args error\n".getBytes());
						continue;
					}
					
					if (cmd.size() != 7) {
						outStream.write("option error\n".getBytes());
						continue;
					}

					Date now = new Date();
					SimpleDateFormat dateFormat = new SimpleDateFormat(
							"yyyy-MM-dd HH:mm:ss");

					String str1 = "";
					String str2 = "";
					String str3 = "";

					if (cmd.get(1).equals("-d")) {
						str1 = cmd.get(2);
					} else if (cmd.get(1).equals("-c")) {
						str2 = cmd.get(2);
					} else if (cmd.get(1).equals("-n")) {
						str3 = cmd.get(2);
					}

					if (cmd.get(3).equals("-d")) {
						str1 = cmd.get(4);
					} else if (cmd.get(3).equals("-c")) {
						str2 = cmd.get(4);
					} else if (cmd.get(3).equals("-n")) {
						str3 = cmd.get(4);
					}

					if (cmd.get(5).equals("-d")) {
						str1 = cmd.get(6);
					} else if (cmd.get(5).equals("-c")) {
						str2 = cmd.get(6);
					} else if (cmd.get(5).equals("-n")) {
						str3 = cmd.get(6);
					}

					if (str1 == "" || str2 == "" || str3 == "") {
						outStream.write("option error\n".getBytes());
						continue;
					}

					if (!isDigit(str3)) {
						outStream.write("args error\n".getBytes());
						continue;
					}

					str1 = isStrLegal(str1);
					str2 = isStrLegal(str2);
					outStream.write(account.forward(str2, str1,
							Integer.parseInt(str3) - 1, dateFormat.format(now))
							.getBytes());

				} else {
					outStream.write("command error\n".getBytes());
					continue;
				}

			}

			inStream.close();
			outStream.close();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	boolean isDigit(String str) {
		for (int i = 0; i < str.length(); i++) {
			if (str.charAt(i) < '0' || str.charAt(i) > '9') {
				return false;
			}
		}
		return true;
	}

	String isStrLegal(String str) {
		for (int i = 0; i < str.length();) {
			char ch = str.charAt(i);

			if ((ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z')
					|| (ch >= '0' && ch <= '9') || ch == '_' || ch == '-'
					|| ch == ':' || ch == '.' || ch == '@' || ch == ' ') {
				i++;
			} else {
				str = str.substring(0, i) + str.substring(i + 1);
			}

		}
		return str;
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

	void receiveMail(String from, String date, String title, String content,
			String head) {
		allMails.add(new Mail(from, account_name, date, title, content, head));
	}

	String sendMail(String to, String date, String title, String content) {
		int index;
		for (index = 0; index < Main.account.size(); index++) {
			if (to.equals(Main.account.get(index).getAccountName()
					+ "@nctu.edu.tw")) {
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

	String sendMail(String to, String date, String title, String content,
			String head) {
		int index;
		for (index = 0; index < Main.account.size(); index++) {
			if (to.equals(Main.account.get(index).getAccountName()
					+ "@nctu.edu.tw")) {
				break;
			}
		}

		if (index < Main.account.size()) {

			Main.account.get(index).receiveMail(account_name, date, title,
					content, head);
			return "done\n";
		} else {
			return "args error\n";
		}

	}

	String getAllMails() {
		String tmp = "";

		if (allMails.size() == 0) {
			tmp = "no mail\n";
		} else {
			for (int i = 0; i < allMails.size(); i++) {
				tmp += ((i + 1) + ". " + allMails.get(i).getHead() + allMails
						.get(i).getTitle());
				if (!allMails.get(i).isRead()) {
					tmp += "(new)";
				}
				tmp += "\n";
			}
		}

		return tmp;

	}

	String deleteMail(int index) {
		if (index == -1) {
			allMails.clear();
			return "done\n";
		} else {
			if (index >= allMails.size()) {
				return "args error\n";
			} else {
				allMails.remove(index);
				return "done\n";
			}
		}
	}

	String read(int index) {
		if (index >= allMails.size()) {
			return "args error\n";
		} else {
			Mail mail = allMails.get(index);
			mail.read();
			return "From: " + mail.getFrom() + "@nctu.edu.tw\nTo: "
					+ mail.getTo() + "@nctu.edu.tw\nDate: " + mail.getDate()
					+ "\nTitle: " + mail.getHead() + mail.getTitle()
					+ "\nContent: " + mail.getContent() + "\n";
		}

	}

	String getInfo() {
		return "Account: " + account_name + "\nMail address: " + account_name
				+ "@nctu.edu.tw\nNumber of mails: " + allMails.size() + "\n";
	}

	String reply(String content, int index, String date) {
		if (index >= allMails.size()) {
			return "args error\n";
		} else {
			Mail mail = allMails.get(index);

			sendMail(mail.getFrom() + "@nctu.edu.tw", date, mail.getTitle(),
					content + "\n----\nFrom: " + mail.getFrom()
							+ "@nctu.edu.tw\nTo: " + mail.getTo()
							+ "@nctu.edu.tw\nDate: " + mail.getDate()
							+ "\nTitle: " + mail.getHead() + mail.getTitle()
							+ "\nContent: " + mail.getContent(), "re:");

			return "done\n";
		}
	}

	String forward(String content, String reveiver, int index, String date) {

		System.out.println(getAccountName());
		System.out.println(getAllMails());

		if (index >= allMails.size()) {
			return "args error\n";
		}

		int i;
		for (i = 0; i < Main.account.size(); i++) {
			if ((Main.account.get(i).getAccountName() + "@nctu.edu.tw")
					.equals(reveiver))
				break;
		}
		if (i >= Main.account.size()) {
			return "args error\n";
		}

		Mail mail = allMails.get(index);

		sendMail(reveiver, date, mail.getTitle(),
				content + "\n----\nFrom: " + mail.getFrom()
						+ "@nctu.edu.tw\nTo: " + mail.getTo()
						+ "@nctu.edu.tw\nDate: " + mail.getDate() + "\nTitle: "
						+ mail.getHead() + mail.getTitle() + "\nContent: "
						+ mail.getContent(), "fwd:");

		return "done\n";

	}

}

class Mail {
	private String from;
	private String to;
	private String date;
	private String title;
	private String content;
	private boolean isRead;
	private String head = "";

	Mail(String from, String to, String date, String title, String content) {
		this.from = from;
		this.to = to;
		this.date = date;
		this.title = title;
		this.content = content;
		isRead = false;
	}

	Mail(String from, String to, String date, String title, String content,
			String head) {
		this.from = from;
		this.to = to;
		this.date = date;
		this.title = title;
		this.content = content;
		isRead = false;
		this.head = head;
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

	boolean isRead() {
		return isRead;
	}

	void read() {
		if (!isRead)
			isRead = true;
	}

	String getHead() {
		return head;
	}

}
