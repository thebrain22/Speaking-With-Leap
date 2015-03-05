import com.leapmotion.leap.*;

import static java.lang.Math.*;

import com.leapmotion.leap.Vector;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

import static java.lang.System.*;

public class Leap{
	public static void main(String[]args) throws Exception{
		out.println("Program started.");

		Scanner yolo;



		//out.println(bestMatch(new int[][]))
		yolo=new Scanner(new File("hands.txt"));
		Map<String,List<Double>>perfectLetters=new HashMap<>();

		  while(yolo.hasNext()){

			String key=yolo.next();
			String[]nums=yolo.nextLine().substring(1).replaceAll("[\\[\\] ]","").split(",");
			//out.println(key+" "+Arrays.toString(nums));
			perfectLetters.put(key, new ArrayList<Double>());
			for(String x:nums){
				//out.println(x);
				perfectLetters.get(key).add(Double.parseDouble(x));
			}
		}

		Map<String,List<Double>>perfectNums=new HashMap<>();
		yolo=new Scanner(new File("numberHands.txt"));
		while(yolo.hasNext()){

			String key=yolo.next();
			String[]nums=yolo.nextLine().substring(1).replaceAll("[\\[\\] ]","").split(",");
			//out.println(key+" "+Arrays.toString(nums));
			perfectNums.put(key, new ArrayList<Double>());
			for(String x:nums){
				perfectNums.get(key).add(Double.parseDouble(x));
			}
		}


		//out.println(perfectLetters);
		//out.println(perfectNums);

		Controller controller = new Controller();
		controller.enableGesture(Gesture.Type.TYPE_CIRCLE);
		new LeapListener(controller);
		assert (controller != null);
		yolo=new Scanner(in);
		Thread.sleep(100);
		boolean innumbermode=false;
		while(true){
			while((in.available()==0&&controller.frame().hands().count()<2)||innumbermode==true){
				Thread.sleep(3);
				if(controller.frame().hands().count()<2)
					innumbermode=false;
			}
			Frame curr=controller.frame();
			if(in.available()>0){
				while(in.available()>0)
					in.read();
			}
			innumbermode=curr.hands().count()>=2;
			//out.println("running");
			//yolo.nextLine();
			if(!curr.hands().isEmpty()){
				Hand currenthand=curr.hands().get(0);
				for(Hand temp:curr.hands()){
					if(temp.isRight())
						currenthand=temp;
				}
				double[]gesture=new double[21];
				Vector palm=currenthand.direction();
				//out.println(currenthand.palmNormal().roll());
				gesture[20]=(abs(currenthand.palmNormal().roll())>PI/4?1:0);

				//palm.setX(0);
				//palm.setY(0);
				//palm.setZ(0);
			//	out.println(curr.hands().get(0).arm().direction());
				int fingernum=0;
				for(Finger x:currenthand.fingers()){
					//out.println(x.type());
					//out.println(x.tipPosition());
					//out.println(x.isExtended());
					gesture[15+fingernum]=x.isExtended()?1:0;
					for(int u=0;u<3;u++){
						gesture[fingernum*3+u]=x.direction().get(u)-palm.get(u);
						//out.println(x.direction().get(u)-palm.get(u)+" ");
					}

					fingernum++;
				}

				out.println(Arrays.toString(gesture));
				//Map<String, List<Double>> etentest = extension(perfectNums,gesture);
				//out.println(etentest.keySet());
				String match;
				if(innumbermode)
					match=bestMatchArithmetic(perfectNums,gesture);
				else
					match=bestMatchArithmetic(perfectLetters,gesture);
				//out.println(match);
				addtoword(match);
			}
			//Thread.sleep(1);

		}
		//yolo.close();
	}
	public static Map<String, List<Double>> extension(Map<String,List<Double>> set, double[] match)
	{
		Map<String, List<Double>> newvals = new HashMap<>();
		for(String a: set.keySet()){
			boolean good = true;
			for(int i = 16; i<20; i++)
			{
				if(match[i]!=set.get(a).get(i))
				{
					good = false;
				}

			}
			if(good)
				newvals.put(a, set.get(a));

		}
		return newvals;


	}
	public static String bestMatchArithmetic(Map<String,List<Double>>set,double[]match){
		String bestmatch="";
		double[]overalfingerdiffs=new double[5];
		double bestDiff=Long.MAX_VALUE;
		double overalworstfinger=-1;
		for(String key:set.keySet()){
			if(set.get(key).get(20)!=match[20]) continue;
			double []fingerdiffs=new double[5];
			double highestFingerDiff=0;
			double worstFinger=-1;
			for(int x=0;x<5;x++){
				double diff=0;
				for(int i=0;i<3;i++){
					if(set.get(key).get(i)*match[i]>0)
						diff+=pow(abs(set.get(key).get(x*3+i)-match[x*3+i]),5);
					else
						diff+=pow(abs(set.get(key).get(x*3+i)+match[x*3+i]),5);
				}
				fingerdiffs[x]=diff;
				if(diff>highestFingerDiff){
					highestFingerDiff=diff;
					worstFinger=x;
				}
			}
			if(highestFingerDiff<bestDiff){
				bestDiff=highestFingerDiff;

				bestmatch=key;
				overalworstfinger=worstFinger;
				overalfingerdiffs=fingerdiffs;
			}
			//out.println("Finger: "+key+" "+Arrays.toString(overalfingerdiffs));
		}
		//out.println(bestDiff);
		//out.println("Finger "+overalworstfinger);
		return bestmatch;
	}
	public static volatile String currentWord="";
	public static synchronized void addtoword(String in){
		currentWord+=in.replaceAll("@"," ");
		out.println(currentWord);
	}
	public static synchronized void backspace(){
		if(currentWord.length()>0)
			currentWord=currentWord.substring(0,currentWord.length()-1);
		if(currentWord.length()==0)
			out.println("String Empty");
		else
			out.println(currentWord);
	}
	public static synchronized void enter(){
		//call audio

		out.println(currentWord);
		try {
			TextToSpeech.sayToGoogle(currentWord);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		out.println("String emptied");
		currentWord="";
	}
	/*public static Set<String> dictionary=new HashSet<>();
	static {
		try {
			Scanner yolo=new Scanner(new File("dictionarywords.txt"));
			while(yolo.hasNextLine()){
				dictionary.add(yolo.nextLine());
			}
			yolo=new Scanner(new File("dictionarywordstwo.txt"));
			while(yolo.hasNextLine()){
				dictionary.add(yolo.nextLine());
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}*/
}
