import java.io.File;
import java.util.Scanner;
import java.util.ArrayList;
public class interactiveActivation{
     private static final double alphaFL, alphaLW, alphaWL, gammaFL, gammaLW, gammaWW, gammaLL = 0.005, 0.07, 0.3, 0.15, 0.04, 0.21, 0;
     private static final double max = 1.0;
     private static final double min = -0.2;
     private static final double decay = 0.07;
     private static final double oscaleW, oscaleL = 20, 10;

     private static final String L_SEG = "letter_segmentation.txt";
     private static ArrayList<boolean[]> uc;
     private static ArrayList<ArrayList<Unit>> featureLevel;
     private static ArrayList<ArrayList<Unit>> letterLevel;
     private static ArrayList<Unit> wordLevel;
     private static String input;
     private static final int WLEN = 4;

     public static void main(String[] args){
          uc = new ArrayList<boolean[]>();
          loadSegs();
          input = args[0].toLowerCase();
          //boolean[][] featureLevel = new boolean[4][28];
          for(int i =0; i<input.length(); i++){
               char letter = input.charAt(i);
               int index = (int)letter - 97;
               boolean[] segs = uc.get(index);
               //featureLevel[i] = segs;
          }


     }

     private static void interact(boolean[][] input){

     }
     private static void featureToLetter(boolean[][] input){
          for(int i = 0; i<WLEN; i++){
               for(int j = 0; j<14; j++){
                    Unit feature = featureLevel.get(i).get(j);
                    if(input[i][j]){
                         ArrayList<Unit> excitedLetters = feature.getEConnections();
                         for(Unit excited : excitedLetters){
                              excited.addInput(alphaFL);
                         }
                         ArrayList<Unit> inhibitedLetters = feature.getIConnections();
                         for(Unit inhibited : inhibitedLetters){
                              inhibited.addInput(-gammaFL);
                         }
                    }
               }
               for(Unit letter : letterLevel.get(i)){
                    double net = letter.getNet();
                    double act = letter.getActivation();
                    double delta = 0.0;
                    if(net > 0.0){
                         delta = (max - act) * net - (decay * act);
                    }else{
                         delta = (act - min) * net - (decay * act);
                    }
                    letter.addActivation(delta);
                    if(letter.getActivation() > max){
                         letter.setActivation(max);
                    }else if(letter.getActivation() < min){
                         letter.setActivation(min);
                    }
               }
          }
     }
     private static void instantiateFeatureConnections(){
          for(int i = 0; i<4; i++){
               ArrayList<Unit> position_features = featureLevel.get(i);
               ArrayList<Unit> position_letters = letterLevel.get(i);
               for (int j = 0; j<14; j++){
                    Unit feature = position_features.get(j);
                    for(int k = 0; k<26; k++){
                         if (uc.get(k)[j]){
                              feature.addConnection(position_letters.get(k), true);
                         } else {
                              feature.addConnection(position_letters.get(k), false);
                         }
                    }
                    //position_features.add(feature);
               }
               //featureLevel.add(position_features);
          }
     }
     private static void instantiateLetterConnections(){
       try{
         for(int i = 0; i<4; i++){
           ArrayList<Unit> position_letters = letterLevel.get(i);
           for(int j=0; j<26; j++){
             Unit letter = position_letters.get(j);
             for(Unit word : wordLevel){
               if(word.getLetter(i)==(char)(j+97)){
                 letter.addConnection(word, true);
               } else {
                 letter.addConnection(word, false);
               }
             }
           }
         }
       } catch(Exception e){
         System.out.println(e);
         System.exit(1);
       }
     }
     private static void instantiateNetwork(){
          //currently makes features and letters, and connections between
          for(int i = 0; i<4; i++){
               ArrayList<Unit> position_features = new ArrayList<Unit>();
               for (int j = 0; j<14; j++){
                    Unit feature = new Unit(0);
                    position_features.add(feature);
               }
               featureLevel.add(position_features);
          }
          for(int i = 0; i<4; i++){
               ArrayList<Unit> position_letters = new ArrayList<Unit>();
               for (int j = 0; j<26; j++){
                    Unit letter = new Unit(1);
                    position_letters.add(letter);
               }
               letterLevel.add(position_letters);
          }
          try{
            Scanner wordScan = new Scanner(new File("testwords.txt"));
            String[] lexicon = wordScan.nextLine().split(",");
            for(String word : lexicon){
              wordLevel.add(new Unit(2, word));
            }
          } catch(Exception e){
            System.out.println(e);
            System.exit(1);
          }
          instantiateFeatureConnections();
          instantiateLetterConnections();
     }

     private static void loadSegs(){
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
