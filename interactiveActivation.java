import java.io.File;
import java.util.Scanner;
import java.util.ArrayList;
public class interactiveActivation{
  private static final String L_SEG = "letter_segmentation.txt";
  private static ArrayList<boolean[]> uc;
  public static void main(String[] args){
    uc = new ArrayList<boolean[]>();
    try{
      File segs = new File(L_SEG);
      Scanner s_segs = new Scanner(segs);

    while(s_segs.hasNextLine()){
      String curLine = s_segs.nextLine();
      String[] temp_segs = curLine.split(",");
      boolean[] thisLetter = new boolean[temp_segs.length];
      for(int i = 0; i<temp_segs.length; i++){
        if(temp_segs[i].equals("0")){
          thisLetter[i] = false;
        }else if(temp_segs[i].equals("1")){
          thisLetter[i] = true;
        }else{
          System.out.println("Problem! Have:" + temp_segs[i]);
        }
      }
      uc.add(thisLetter);
    }
    System.out.println(uc.get(0)[5]);
  }catch(Exception e){
    System.out.println(e);
    System.exit(1);
  }
}

}
