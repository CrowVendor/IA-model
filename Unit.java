import java.util.ArrayList;
public class Unit{
  private ArrayList<Unit> excitatoryConnections;
  private ArrayList<Unit> inhibitoryConnections;
  private int level;
  private String word;
  private double activation;
  private double netInput;

  public Unit(int level){
    level=level;
    activation = 0.0;
    netInput = 0.0;
    excitatoryConnections=new ArrayList<Unit>();
    inhibitoryConnections=new ArrayList<Unit>();
  }
  public Unit(int level, String word){
    word=word;
    level=level;
    activation = 0.0;
    netInput = 0.0;
    excitatoryConnections=new ArrayList<Unit>();
    inhibitoryConnections=new ArrayList<Unit>();
  }
  public void addConnection(Unit connect, boolean exc){
    if(exc){
         excitatoryConnections.add(connect);
    }else{
         inhibitoryConnections.add(connect);
    }
  }
  public ArrayList<Unit> getEConnections(){
    return excitatoryConnections;
  }
  public ArrayList<Unit> getIConnections(){
    return inhibitoryConnections;
  }
  public double getActivation(){
       return activation;
 }
 public void addActivation(double a){
      activation += a;
}
public void setActivation(double a){
     activation = a;
}
public void addInput(double i){
     netInput += i;
}
public double getNet(){
     return netInput;
}
  public Character getLetter(int position) throws Exception{
    if (level!=2){
      throw new Exception("Not on the word level");
    }
    if (position>3){
      throw new IndexOutOfBoundsException();
    }
    return word.charAt(position);
  }

}
