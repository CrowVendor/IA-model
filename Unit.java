import java.util.ArrayList;
public class Unit{
  private ArrayList<Double> connections;
  private int level;
  private String word;

  public Unit(int level){
    level=level;
    connections=new ArrayList<Double>();
  }
  public Unit(int level, String word){
    word=word;
    level=level;
    connections = new ArrayList<Double>();
  }
  public void addConnection(double connect){
    connections.add(connect);
  }
  public ArrayList<Double> getConnections(){
    return connections;
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
