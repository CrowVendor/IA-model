import java.io.File;
import java.util.Scanner;
import java.util.ArrayList;
public class wordModeling{
     public static void main(String[] args){
          interactiveActivation ia = new interactiveActivation("combined_possibles.txt");
          bilingualInteractiveActivation bia = new bilingualInteractiveActivation("combined_possibles.txt", "combined_dutch_possibles.txt");
          ArrayList<String> testwords = new ArrayList<String>();
          //testwords.add("aunt");
          //testwords.add("blue");
          //testwords.add("knit");
          //testwords.add("farm");
          //testwords.add("left");
          try{
               Scanner testScan = new Scanner(new File("largeEnglish.txt"));
               while(testScan.hasNext()){
                    testwords.add(testScan.next());
               }
          } catch (Exception e){
               System.out.println(e);
               System.exit(1);
          }
          //System.out.println(testwords);
          //System.out.println(ia.responseTimes(testwords, 20));
          //ia.modelWord("left", 20, 5);
          System.out.println(bia.responseTimes(testwords, .9, 1));
          //bia.modelWord("aunt", 50, 10);
     }
}
