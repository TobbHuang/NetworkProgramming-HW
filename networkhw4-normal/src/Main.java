import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;

import org.json.JSONObject;

public class Main {

	static ArrayList<Account> list = new ArrayList<Account>();

	static int PORT = 5568;

	static DatagramSocket socket;

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			socket = new DatagramSocket(PORT);

			while (true) {
				byte[] receiveByte = new byte[1024];
				DatagramPacket dataPacket = new DatagramPacket(receiveByte,
						receiveByte.length);
				socket.receive(dataPacket);
				new MyThread(dataPacket).start();
			}

			// socket.close();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}

class Account {
	private int port;
	private String account_name;
	private String account_id;

	private long money = 0;

	Account(int port, String account_name, String account_id) {
		this.port = port;
		this.account_name = account_name;
		this.account_id = account_id;
	}

	public int getPort() {
		return port;
	}

	public String getAccountName() {
		return account_name;
	}

	public String getAccountId() {
		return account_id;
	}

	public long getMoney() {
		return money;
	}

	public boolean save(long money) {
		this.money += money;
		return true;
	}

	public boolean withdraw(long money) {
		if (this.money >= money) {
			this.money -= money;
			return true;
		} else {
			return false;
		}
	}

	public boolean remit(long money, String destination_name) {
		if (this.money < money)
			return false;

		if (getAccountName().equals(destination_name))
			return false;

		int index;
		for (index = 0; index < Main.list.size(); index++) {
			if (destination_name.equals(Main.list.get(index).getAccountName())) {
				break;
			}
		}
		if (index == Main.list.size()) {
			return false;
		}

		this.money -= money;
		Main.list.get(index).save(money);

		return true;
	}

	public boolean bomb() {
		this.money = 0;
		return true;
	}

}

class MyThread extends Thread {

	DatagramPacket dataPacket;

	MyThread(DatagramPacket dataPacket) {
		this.dataPacket = dataPacket;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		try {
			String str = new String(dataPacket.getData(), 0,
					dataPacket.getLength());
			System.out.println(str);

			JSONObject rcvJsn = new JSONObject(str);

			String action = rcvJsn.getString("action");

			JSONObject sendJsn = new JSONObject();
			byte[] sendBuf;
			DatagramPacket sendPacket;

			if (action.equals("init")) {

				String account_id = rcvJsn.getString("account_id");
				for (int i = 0; i < Main.list.size(); i++) {
					// 查询account是否已被注册
					if (Main.list.get(i).getAccountId().equals(account_id)) {
						String sendStr = "account_id has been registered";
						sendJsn.put("message", sendStr);
						sendBuf = sendJsn.toString().getBytes();
						sendPacket = new DatagramPacket(sendBuf,
								sendBuf.length, dataPacket.getAddress(),
								dataPacket.getPort());
						Main.socket.send(sendPacket);
						return;
					}
				}

				Main.list.add(new Account(dataPacket.getPort(), rcvJsn
						.getString("account_name"), rcvJsn
						.getString("account_id")));

				sendJsn.put("message", "ok");
				sendBuf = sendJsn.toString().getBytes();
				sendPacket = new DatagramPacket(sendBuf, sendBuf.length,
						dataPacket.getAddress(), dataPacket.getPort());
				Main.socket.send(sendPacket);

			} else if (action.equals("save")) {
				long money = rcvJsn.getInt("money");
				int port = dataPacket.getPort();

				// 根据port找index
				int index;
				for (index = 0; index < Main.list.size(); index++) {
					if (port == Main.list.get(index).getPort())
						break;
				}
				Main.list.get(index).save(money);
				sendJsn.put("message", "ok");
				sendBuf = sendJsn.toString().getBytes();
				sendPacket = new DatagramPacket(sendBuf, sendBuf.length,
						dataPacket.getAddress(), port);
				Main.socket.send(sendPacket);

			} else if (action.equals("withdraw")) {
				long money = rcvJsn.getInt("money");
				int port = dataPacket.getPort();

				// 根据port找index
				int index;
				for (index = 0; index < Main.list.size(); index++) {
					if (port == Main.list.get(index).getPort())
						break;
				}
				boolean result = Main.list.get(index).withdraw(money);
				if (result) {
					sendJsn.put("message", "ok");
				} else {
					sendJsn.put("message", "invalid transaction");
				}
				sendBuf = sendJsn.toString().getBytes();
				sendPacket = new DatagramPacket(sendBuf, sendBuf.length,
						dataPacket.getAddress(), port);
				Main.socket.send(sendPacket);

			} else if (action.equals("remit")) {
				long money = rcvJsn.getInt("money");
				int port = dataPacket.getPort();

				// 根据port找index
				int index;
				for (index = 0; index < Main.list.size(); index++) {
					if (port == Main.list.get(index).getPort())
						break;
				}

				boolean result = Main.list.get(index).remit(money,
						rcvJsn.getString("destination_name"));
				if (result) {
					sendJsn.put("message", "ok");
				} else {
					sendJsn.put("message", "invalid transaction");
				}
				sendBuf = sendJsn.toString().getBytes();
				sendPacket = new DatagramPacket(sendBuf, sendBuf.length,
						dataPacket.getAddress(), port);
				Main.socket.send(sendPacket);

			} else if (action.equals("show")) {
				int port = dataPacket.getPort();

				// 根据port找index
				int index;
				for (index = 0; index < Main.list.size(); index++) {
					if (port == Main.list.get(index).getPort())
						break;
				}
				if (index == Main.list.size()) {
					sendJsn.put("message", "account not find");
				} else {
					sendJsn.put("message", Main.list.get(index).getMoney());
				}
				sendBuf = sendJsn.toString().getBytes();
				sendPacket = new DatagramPacket(sendBuf, sendBuf.length,
						dataPacket.getAddress(), port);
				Main.socket.send(sendPacket);

			} else if (action.equals("bomb")) {
				for (int i = 0; i < Main.list.size(); i++) {
					Main.list.get(i).bomb();
				}
				sendJsn.put("message", "ok");
				sendBuf = sendJsn.toString().getBytes();
				sendPacket = new DatagramPacket(sendBuf, sendBuf.length,
						dataPacket.getAddress(), dataPacket.getPort());
				Main.socket.send(sendPacket);

			} else if (action.equals("end")) {

				Main.list.clear();

				sendJsn.put("message", "end");
				sendBuf = sendJsn.toString().getBytes();
				sendPacket = new DatagramPacket(sendBuf, sendBuf.length,
						dataPacket.getAddress(), dataPacket.getPort());
				Main.socket.send(sendPacket);
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
