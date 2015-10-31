import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Main {

	static String dir = "/Users/Tobb_Huang/Desktop/";
	static int count[]=new int[4];// 四个矿工的统计数据
	static int finishCount=0;// 记录已完成统计人数
	
	static Lock lock=new ReentrantLock();

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			if (args.length <= 0) {
				System.out.println("Map name is wrong!!!");
				return;
			}

			ArrayList<String> mine = new ArrayList<String>();

			// 读取文件中的内容
			File file = new File(dir + args[0]);
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String tmpStr;
			while ((tmpStr = reader.readLine()) != null) {
				mine.add(tmpStr);
			}
			reader.close();

			// 打印地图、宽度和高度
			int mineHeight = mine.size();
			int mineWidth = mine.get(0).length();
			for (int i = 0; i < mineWidth; i++) {
				if (i == 0) {
					System.out.print(" ");
				}
				System.out.print("-");
				if (i == mineWidth - 1) {
					System.out.println(" ");
				}
			}

			for (int i = 0; i < mineHeight; i++) {
				String str = mine.get(i);
				for (int j = 0; j < mineWidth; j++) {
					if (j == 0) {
						System.out.print("|");
					}
					if (str.charAt(j) != '#') {
						System.out.print(str.charAt(j));
					} else {
						if(str.charAt(0)=='#'){
							System.out.print("-");
						} else{
							System.out.print("|");
						}
					}
					if(j==mineWidth-1){
						System.out.println("|");
					}
				}
			}
			
			for (int i = 0; i < mineWidth; i++) {
				if (i == 0) {
					System.out.print(" ");
				}
				System.out.print("-");
				if (i == mineWidth - 1) {
					System.out.println(" ");
				}
			}
			
			System.out.println("map size: "+mineWidth+"*"+mineHeight);
			
			// 把地图分成四份
			ArrayList<String> list1=new ArrayList<String>();
			ArrayList<String> list2=new ArrayList<String>();
			ArrayList<String> list3=new ArrayList<String>();
			ArrayList<String> list4=new ArrayList<String>();
			
			int m=0;// 标记是1、2矿还是3、4矿
			for(int i=0;i<mineHeight;i++){
				String strTmp=mine.get(i);
				if(strTmp.charAt(0)!='#'){
					String str[]=strTmp.split("#");
					if(m==0){
						list1.add(str[0]);
						list2.add(str[1]);
					} else{
						list3.add(str[0]);
						list4.add(str[1]);
					}
				} else{
					m++;
				}
			}
			
			// run四个线程
			new MyThread(list1,0).start();
			new MyThread(list2,1).start();
			new MyThread(list3,2).start();
			new MyThread(list4,3).start();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	static void printResult(){
		lock.lock();
		finishCount++;
		if(finishCount<4){
			lock.unlock();
			return;
		}
		lock.unlock();
		
		int maxCount=0;
		for(int i=0;i<4;i++){
			if(count[i]>maxCount)
				maxCount=count[i];
		}
		
		int tmp=0;
		for(int i=0;i<4;i++){
			if(count[i]==maxCount)
				tmp++;
		}
		String strTmp;
		if(tmp==1){
			strTmp=" (win)";
		} else{
			strTmp=" (draw)";
		}
		
		for(int i=0;i<4;i++){
			if(count[i]==maxCount){
				System.out.println("Miner#"+(i+1)+": "+count[i]+strTmp);
			} else{
				System.out.println("Miner#"+(i+1)+": "+count[i]);
			}
		}
		
	}

}

class MyThread extends Thread{
	
	ArrayList<String> list;
	int m;// 序号
	
	MyThread(ArrayList<String> list,int i){
		this.list=list;
		m=i;
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		for(int i=0;i<list.size();i++){
			String str=list.get(i);
			for(int j=0;j<str.length();j++){
				if(str.charAt(j)=='*'){
					Main.count[m]++;
				}
			}
		}
		
		Main.printResult();
	}
	
}
