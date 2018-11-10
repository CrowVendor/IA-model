import java.io.File;
import java.util.Scanner;
import java.util.ArrayList;
public class interactiveActivation{
     private static final double alphaFL = 0.005;
     private static final double alphaLW = 0.07;
     private static final double alphaWL = 0.3;
     private static final double gammaFL = 0.15;
     private static final double gammaLW = 0.04;
     private static final double gammaWW = 0.21;
     private static final double gammaLL = 0;
     private static final double max = 1.0;
     private static final double min = -0.2;
     private static final double decay = 0.07;
     private static final double oscaleW = 20;
     private static final double oscaleL = 10;

     private static final String L_SEG = "letter_segmentation.txt";
     private static final String W_FILE = "testwords.txt";
     private static ArrayList<boolean[]> uc;
     private static ArrayList<ArrayList<Unit>> featureLevel;
     private static ArrayList<ArrayList<Unit>> letterLevel;
     private static ArrayList<Unit> wordLevel;
     private static final int WLEN = 4;

     public static void main(String[] args){
          uc = new ArrayList<boolean[]>();
          loadSegs();
          instantiateNetwork();
          String[] words = loadWords(args[0]);
          for (String word : words){
               boolean[][] input = loadWord(word);
               interact(input);
               System.out.println("INPUT: "+word);
               try{
                    for(Unit word_try : wordLevel){
                         System.out.println(word_try.getWord()+": "+word_try.getActivation());
                    }
               } catch(Exception e){
                    System.out.println(e);
                    System.exit(1);
               }
          }
     }

     private static boolean[][] loadWord(String word){
          if (word.length()!=WLEN){
               System.out.println("Incorrect word size, invalid input");
               System.exit(1);
          }
          boolean[][] input = new boolean[WLEN][14];
          for (int i = 0; i<WLEN; i++){
               char letter = word.charAt(i);
               int index = (int)letter - 97;
               input[i]=uc.get(index);
          }
          return input;
     }

     private static void interact(boolean[][] input){
          featureToLetter(input);
          letterToWord();
          wordToLetter();
          wordToWord();
          letterToLetter();
          update();
     }
     private static void update(){
          for(int i = 0; i<WLEN; i++){
               for(Unit letter : letterLevel.get(i)){
                    updateUnitActivation(letter);
                    letter.setInput(0.0);
               }
          }
          for(Unit word : wordLevel){
               updateUnitActivation(word);
               word.setInput(0.0);
          }
     }
     private static void featureToLetter(boolean[][] input){
          for(int i = 0; i<WLEN; i++){
               for(int j = 0; j<14; j++){
                    ArrayList<Unit> features = featureLevel.get(i);
                    Unit feature = features.get(j);
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
          }
     }

     private static void letterToWord(){
          for(int i = 0; i < WLEN; i++){
               for(int j = 0; j < 26; j++){
                    Unit letter = letterLevel.get(i).get(j);
                    if(letter.getActivation() > 0.0){
                         ArrayList<Unit> excitedWords = letter.getEConnections();
                         for(Unit excited : excitedWords){
                              excited.addInput(letter.getActivation() * alphaLW);
                         }
                         ArrayList<Unit> inhibitedWords = letter.getIConnections();
                         for(Unit inhibited : inhibitedWords){
                              inhibited.addInput(letter.getActivation() * -gammaLW);
                         }
                    }
               }
          }


     }
     private static void wordToLetter(){
          for(int i = 0; i < WLEN; i++){
               for(int j = 0; j < 26; j++){
                    Unit letter = letterLevel.get(i).get(j);
                    ArrayList<Unit> excitedWords = letter.getEConnections();
                    for(Unit excited : excitedWords){
                         if(excited.getActivation() > 0.0){
                              letter.addInput(excited.getActivation() * alphaWL);
                         }
                    }
               }
          }
     }
     private static void wordToWord(){
          double sum = 0.0;
          for(Unit word : wordLevel){
               if(word.getActivation() > 0){
                    sum += word.getActivation();
               }
          }
          for(Unit word : wordLevel){
               double total = sum;
               if(word.getActivation()>0){
                    total -= word.getActivation();
               }
               word.addInput(total * -gammaWW);
          }
     }
     private static void letterToLetter(){
          for(int i = 0; i < WLEN; i++){
               double sum = 0.0;
               for(Unit letter : letterLevel.get(i)){
                    if(letter.getActivation() > 0){
                         sum += letter.getActivation();
                    }
               }
               for(Unit letter : letterLevel.get(i)){
                    double total = sum;
                    if(letter.getActivation() > 0){
                         total -= letter.getActivation();
                    }
                    letter.addInput(total * -gammaLL);
               }
          }
     }
     private static void updateUnitActivation(Unit a){
          double net = a.getNet();
          double act = a.getActivation();
          double delta = 0.0;
          if(net > 0.0){
               delta = (max - act) * net - (decay * act);
          }else{
               delta = (act - min) * net - (decay * act);
          }
          a.addActivation(delta);
          if(a.getActivation() > max){
               a.setActivation(max);
          }else if(a.getActivation() < min){
               a.setActivation(min);
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
               }
          }
     }
     private static void instantiateLetterConnections(){
          try{
               for(int i = 0; i<4; i++){
                    ArrayList<Unit> position_letters = letterLevel.get(i);
                    for(int j=0; j<26; j++){
                         Unit letter = position_letters.get(j);
                         for(Unit word : wordLevel){
                              //System.out.println(word);
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
          featureLevel = new ArrayList<ArrayList<Unit>>();
          letterLevel = new ArrayList<ArrayList<Unit>>();
          wordLevel = new ArrayList<Unit>();
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
               String[] lexicon = loadWords(W_FILE);
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

     private static String[] loadWords(String filename){
          //NOTE: where to split on? be consistent?
          try{
               Scanner wordScan = new Scanner(new File(filename));
               String[] words = wordScan.nextLine().replace(" ", "").split(",");
               return words;
          } catch (Exception e){
               System.out.println(e);
               System.exit(1);
               return null;
          }
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
          }catch(Exception e){
               System.out.println(e);
               System.exit(1);
          }
     }

}
