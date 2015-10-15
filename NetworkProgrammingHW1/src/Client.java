import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import org.json.JSONObject;

public class Client {

	static String IP = "140.113.62.101";
	static int port = 9605;
	static String destinationIP = "140.113.87.160";
	static int destinationPort = 5566;
	static DatagramSocket clientSocket;

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {

			clientSocket = new DatagramSocket();

			int head = 3000, tail = 60000;
			int guessPort;
			int count = 1;// 計數器

			System.out.println("#set up");
			System.out.println("UDP Client listening on " + IP + ":" + port);
			System.out.println();

			// s1
			while (true) {

				System.out.println("#" + count);
				count++;

				JSONObject jsn = new JSONObject();
				guessPort = (head + tail) / 2;
				if (head == tail)
					break;

				jsn.put("guess", guessPort);
				System.out.println("send " + jsn.toString());

				String receiveStr = udpConnection(jsn.toString(),
						destinationIP, destinationPort);

				JSONObject resultJsn = new JSONObject(receiveStr);
				String result = resultJsn.getString("result");
				System.out.println("receive " + resultJsn.toString());
				if (result.equals("bingo!")) {
					break;
				} else if (result.equals("smaller")) {
					tail = guessPort - 1;
				} else if (result.equals("larger")) {
					head = guessPort + 1;
				} else {

				}
			}

			System.out.println("#" + count);
			// s2
			JSONObject jsn = new JSONObject();
			jsn.put("student_id", "0440062");
			System.out.println("send " + jsn.toString());

			String receiveStr = udpConnection(jsn.toString(), destinationIP,
					guessPort);

			JSONObject resultJsn = new JSONObject(receiveStr);
			System.out.println("receive " + resultJsn.toString());

			clientSocket.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	static String udpConnection(String sendStr, String ipAddress, int port) {
		String receiveStr = null;
		try {
			byte[] sendData = new byte[1024];
			byte[] receiveData = new byte[1024];
			DatagramPacket sendPacket;
			DatagramPacket receivePacket;

			sendData = sendStr.getBytes();
			sendPacket = new DatagramPacket(sendData, sendData.length,
					InetAddress.getByName(ipAddress), port);
			clientSocket.send(sendPacket);

			receivePacket = new DatagramPacket(receiveData, receiveData.length);
			clientSocket.receive(receivePacket);
			receiveStr = new String(receivePacket.getData());
		} catch (Exception e) {
			e.printStackTrace();
		}

		return receiveStr;

	}

}
