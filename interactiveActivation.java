import java.io.File;
import java.io.Scanner;
public class interactiveActivation{
  private static final String L_SEG = "placeholder.txt"
  private static Arraylist<Boolean[]> uc;
  public static void main(String[] args){
    File segs = new File(L_SEG);
    Scanner s_segs = new Scanner(segs);

    while(s_segs.hasNextLine()){
      String curLine = s_segs.nextLine();
      String[] temp_segs = curLine.split(",");
      boolean[] thisLetter = new boolean[temp_segs.length];
      for(int i = 0; i<temp_segs.length; i++){
        if(temp_segs[i].equals("0")){
          thisLetter[i] = false;
        }else{
          thisLetter[i] = true;
        }
      }
      uc.add(thisLetter);
    }
  }

  public load
}



public class Unit{
  private ArrayList<Unit> connections;
  private boolean on;
  private Unit(Int level){
    on=False
  }
  public void addConnection(Unit connect){
    connections.append(connect)
  }
  public ArrayList<Unit> getConnections(){
    return connnections;
  }
  public void turnOn(){
    on=True;
  }
  public boolean isOn(){
    return on;
  }

}
