import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;

import org.json.JSONObject;

public class Main {

	static String IP;
	static int PORT;
	static String DESTINATION_IP = "210.41.96.40";
	static int DESTINATION_PORT = 5566;

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {

			Scanner sc = new Scanner(System.in);

			Socket s = null;

			InputStream is = null;
			OutputStream os = null;

			System.out.println("Welcome to Game 2048!");
			System.out.println("enter 'help' to get more information\n");
			System.out.print(">");

			if (sc.nextLine().equals("help")) {
				System.out.println("Enter keyboard:\n"
						+ "'connect' - connect to game server\n"
						+ "'disconnect' - disconnect from game server\n"
						+ "'new' - new a game round\n"
						+ "'end' - close the game\n" + "'w' - move bricks up\n"
						+ "'s' - move bricks down\n"
						+ "'a' - move bricks left\n"
						+ "'d' - move bricks right\n"
						+ "'u' - undo the last move");
				System.out.print(">");

				while (true) {

					// connect
					while (true) {
						String cmd = sc.nextLine();
						if (cmd.equals("connect")) {
							if (s == null) {
								s = new Socket(
										InetAddress.getByName(DESTINATION_IP),
										DESTINATION_PORT);

								is = s.getInputStream();
								os = s.getOutputStream();

								System.out.println("connect to game server");
								System.out.print(">");
							} else {
								System.out
										.println("Have already connectted to server");
								System.out.print(">");
							}
						} else {
							if (s == null) {
								System.out
										.println("Please connect to server first");
								System.out.print(">");
							} else {
								if (cmd.equals("disconnect")) {
									is.close();
									os.close();
									s.close();
									s = null;

									System.out
											.println("disconnect from game server");
									System.out.print(">");
								} else if (cmd.equals("new")) {

									JSONObject jsn = new JSONObject();
									jsn.put("action", "New");
									String tmp = tcpConnect(s, is, os,
											jsn.toString());
									if (tmp != null) {

										JSONObject rcvJsn = new JSONObject(tmp);
										if (rcvJsn.getInt("status") == 1) {

											String[] data = rcvJsn.getString(
													"message").split(",");

											for (int i = 0; i < 4; i++) {
												System.out
														.println("---------------------");
												for (int j = 0; j < 4; j++) {
													System.out.print("|");
													System.out
															.printf("%4d",
																	Integer.parseInt(data[4
																			* i
																			+ j]));
												}
												System.out.println("|");
											}
											System.out
													.println("---------------------");
											System.out.print("move>");

											break;
										} else {
											// 出错信息
											System.out.println(rcvJsn
													.getString("message"));
											System.out.print(">");
										}
									} else {
										System.out.println("Fail to connect...");
										System.out.print(">");
									}

								} else {
									System.out
											.println("Please new a game round first");
									System.out.print(">");
								}
							}
						}
					}

					// move
					while (true) {
						String cmd = sc.nextLine();
						if (cmd.equals("end")) {
							// 结束游戏
							System.out.println("The game has closed");
							System.out.print(">");
							break;
						} else {
							JSONObject jsn = new JSONObject();
							if (cmd.equals("w")) {
								jsn.put("action", "moveUp");
							} else if (cmd.equals("s")) {
								jsn.put("action", "moveDown");
							} else if (cmd.equals("a")) {
								jsn.put("action", "moveLeft");
							} else if (cmd.equals("d")) {
								jsn.put("action", "moveRight");
							} else if (cmd.equals("u")) {
								jsn.put("action", "unDo");
							}
							JSONObject rcvJsn = new JSONObject(tcpConnect(s,
									is, os, jsn.toString()));
							//System.out.println(rcvJsn.toString());
							// 这个地方有点坑，status突然变成boolean了
							if (rcvJsn.getBoolean("status") == true) {
								// print
								String[] data = rcvJsn.getString("message")
										.split(",");

								for (int i = 0; i < 4; i++) {
									System.out.println("---------------------");
									for (int j = 0; j < 4; j++) {
										System.out.print("|");
										System.out.printf("%4d", Integer
												.parseInt(data[4 * i + j]));
									}
									System.out.println("|");
								}
								System.out.println("---------------------");

								// 判断游戏是否胜利
								int i = 0;
								for (i = 0; i < 16; i++) {
									if (data[i].equals("2048")) {
										System.out.println();
										System.out
												.println("Congrats! You win the game!");
										break;
									}
								}

								if (i == 16) {
									System.out.print("move>");
								} else {
									System.out.print(">");
									break;
								}
							} else {
								// 出错
								System.out.println(rcvJsn.getString("message"));
								System.out.print("move>");
							}
						}
					}

				}

			} else {
				// 如果没输help
				// 不过好像没定义这里要输出什么
			}

			sc.close();

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static String tcpConnect(Socket s, InputStream is, OutputStream os,
			String msg) {
		try {
			byte[] data = new byte[1024];

			os.write(msg.getBytes());

			int len = is.read(data);
			return new String(data, 0, len);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;

	}

}
