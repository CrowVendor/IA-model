import java.io.File;
import java.util.Scanner;
import java.util.ArrayList;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.util.Collections;
public class bilingualInteractiveActivation{
     private static final double alphaFL = 0.005;
     private static final double alphaLW = 0.07;
     private static final double alphaWL = 0.3;
     private static final double alphaWLa = 0.3;
     private static final double gammaFL = 0.15;
     private static final double gammaLW = 0.04;
     private static final double gammaWW = 0.21;
     private static final double gammaLL = 0;
     private static final double gammaLa1W = 0 ;
     private static final double gammaLa2W = 0.03;

     private static final double max = 1.0;
     private static final double min = -0.2;
     private static final double decay = 0.07;
     private static final double oscaleW = 20;
     private static final double oscaleL = 10;

     private String[] lexicon1;
     private String[] lexicon2;
     private String W_FILE1;
     private String W_FILE2;
     private static final String L_SEG = "letter_segmentation.txt";
     private ArrayList<boolean[]> uc;
     private ArrayList<ArrayList<Unit>> featureLevel;
     private ArrayList<ArrayList<Unit>> letterLevel;
     private ArrayList<Unit> wordLevel1;
     private ArrayList<Unit> wordLevel2;
     private ArrayList<Unit> languageLevel;
     private static final int WLEN = 4;

     public bilingualInteractiveActivation(String lex1, String lex2){
          this.W_FILE1 = lex1;
          this.W_FILE2 = lex2;
          uc = new ArrayList<boolean[]>();
          loadSegs();
          instantiateNetwork();
     }

     /* Models the response times for a given list of words, not clearing the
      * network between each word. Returns a list of response times.
      */
     public ArrayList<Integer> modelSequenceResponseTimes(ArrayList<String> words, double threshold, int language){
          clearNetwork();
          ArrayList<Integer> results = new ArrayList<Integer>();
          int i = 0;
          for (String word : words){
               if(language==0){
                    results.add(modelResponseTime(word, threshold, (i % 2)+1));
                    i++;
               } else {
                    results.add(modelResponseTime(word, threshold, language));
               }
          }
          return results;
     }
     /* Models the reponse times for a given list of words, clearing the
      * network after each. Returns an everage response time for the list of words.
      */
     public double responseTimes(ArrayList<String> words, double threshold, int language){
          double totalAverage=0;
          for(String word : words){
               clearNetwork();
               double average = modelResponseTime(word, threshold, language);
               //System.out.println(average);
               totalAverage += average;
          }
          return totalAverage/words.size();
     }
     /* Models response time for a given word by running the model until the
      * response strength of the correct word unit is above a given threshold.
      */
     private int modelResponseTime(String word, double threshold, int language){
          Unit wordUnit=null;
          try{
               if(language == 1){
                    for(Unit wordUnitTemp : wordLevel1){
                         if (wordUnitTemp.getWord().equals(word)){
                              wordUnit=wordUnitTemp;
                         }
                    }
               } else if (language==2){
                    for(Unit wordUnitTemp : wordLevel2){
                         if (wordUnitTemp.getWord().equals(word)){
                              wordUnit=wordUnitTemp;
                         }
                    }
               } else {
                    System.out.println("Exception: invalid language");
                    System.exit(1);
               }
          } catch (Exception e) {
               System.out.println(e);
               System.exit(1);
          }
          if(wordUnit==null){
               System.out.println("Exception: word: "+word+" not in lexicon");
               System.exit(1);
          }
          boolean[][] input = loadWord(word);
          double response=0;
          double probability=0;
          int cycles = 0;
          while(probability<threshold){
               interact(input);
               double totalResponse = 0.0;
               for(Unit wordUnitTemp : wordLevel1){
                    totalResponse+=wordUnitTemp.getResponseStrength(oscaleW);
               }
               for(Unit wordUnitTemp : wordLevel2){
                    totalResponse+=wordUnitTemp.getResponseStrength(oscaleW);
               }
               response = wordUnit.getResponseStrength(oscaleW);
               probability = response/totalResponse;
               cycles++;
          }
          return cycles;
     }
     /* Runs a given word through the model for a set number of cycles, and
      * writes the most activated words' activations over time to an output file.
      */
     public void modelWord(String word, int cycles, int num_results, String outputFileName){
          ArrayList<ArrayList<Double>> results = new ArrayList<ArrayList<Double>>();
          boolean[][] input = loadWord(word);

          for(int i = 0; i<cycles; i++){
               interact(input);
               results.add(getActivationOutput());
               //writeOutput("testresults.txt");
          }
          output(results, num_results, outputFileName);

     }
     /* Takes in a list of activations over time for all the words in the lexicon
      * then writes the top num_results of them to an output file. These top
      * results are the ones with the highest activations at the end of the cycles.
      */
     private void output(ArrayList<ArrayList<Double>> results, int num_results, String outputFileName){
          try{
               BufferedWriter bw = new BufferedWriter(new FileWriter(outputFileName));
               bw.close();
          } catch(Exception e){
               System.out.println(e);
               System.exit(1);
          }
          ArrayList<Double> finalResults = results.get(results.size()-1);
          ArrayList<Integer> indices = new ArrayList<Integer>();
          for(int i =0; i<finalResults.size(); i++){
               indices.add(i);
          }
          Collections.sort(indices, new unitComparator(finalResults));
          ArrayList<ArrayList<Double>> top_results = new ArrayList<ArrayList<Double>>();
          ArrayList<String> top_words = new ArrayList<String>();
          for(int i=0; i < num_results;i++){
               ArrayList<Double> top_result = new ArrayList<Double>();
               int index = indices.get(indices.size()-i-1);
               if(index >= lexicon1.length){
                    top_words.add("DUTCH: "+lexicon2[index - lexicon1.length]);
               } else {
                    top_words.add("ENGLISH: "+lexicon1[index]);
               }
               for(int j=0; j<results.size();j++){
                    top_result.add(results.get(j).get(index));
               }
               top_results.add(top_result);
          }
          writeOutput(top_results, outputFileName, top_words);

     }
     /* Writes a list of activations over time to an output file
     */
     private void writeOutput(ArrayList<ArrayList<Double>> top_results, String filename, ArrayList<String> top_words){
          try{
               BufferedWriter bw = new BufferedWriter(new FileWriter(filename, true));
               for(int i=0;i<top_results.size();i++){
                    bw.append(top_words.get(i));
                    for(double result : top_results.get(i)){
                         bw.append(", "+result);
                    }
                    bw.newLine();
                    bw.flush();
               }
               bw.close();
          } catch(Exception e){
               System.out.println(e);
               System.exit(1);
          }
     }
     /* Returns a list of activations for all words
     */
     private ArrayList<Double> getActivationOutput(){
          ArrayList<Double> results = new ArrayList<Double>();
          for(Unit word : wordLevel1){
               results.add(word.getActivation());
          }
          for(Unit word : wordLevel2){
               results.add(word.getActivation());
          }
          return results;
     }
     /* Returns a list of response strengths calculated from activations and
      * average activations from all words
      */
     private ArrayList<Double> getResponseStrengthOutput(){
          ArrayList<Double> results = new ArrayList<Double>();
          for(Unit word : wordLevel1){
               results.add(word.getResponseStrength(oscaleW));
          }
          for(Unit word : wordLevel2){
               results.add(word.getResponseStrength(oscaleW));
          }
          return results;
     }
     /* Returns a list of activations for all letters for testing purposes.
     */
     private ArrayList<ArrayList<Double>> getLetterActivationOutput(){
          ArrayList<ArrayList<Double>> results = new ArrayList<ArrayList<Double>>();
          for(int i=0; i<4; i++){
               ArrayList<Double> result = new ArrayList<Double>();
               for(Unit letter : letterLevel.get(i)){
                    result.add(letter.getActivation());
               }
               results.add(result);
          }
          return results;
     }

     /* A single cycle of the model, this updates the net inputs caused by
     * every connection, then updates activations based on that.
     */
     private void interact(boolean[][] input){
          featureToLetter(input);
          letterToWord();
          wordToLetter();
          wordToLanguage();
          languageToWord();
          wordToWord();
          letterToLetter();
          update();
     }
     /* Updates all activations
      */
     private void update(){
          for(int i = 0; i<WLEN; i++){
               for(Unit letter : letterLevel.get(i)){
                    updateUnitActivation(letter);
                    letter.setInput(0.0);
               }
          }
          for(Unit word : wordLevel1){
               updateUnitActivation(word);
               word.setInput(0.0);
          }
          for(Unit word : wordLevel2){
               updateUnitActivation(word);
               word.setInput(0.0);
          }
          for(Unit language : languageLevel){
               updateUnitActivation(language);
               language.setInput(0.0);
          }
     }

     // These methods all update the net input based on excitatory or inhibitory
     // connections and the set parameters
     private void featureToLetter(boolean[][] input){
          for(int i = 0; i<WLEN; i++){
               ArrayList<Unit> features = featureLevel.get(i);
               for(int j = 0; j<14; j++){
                    Unit feature = features.get(j);
                    ArrayList<Unit> inhibitedLetters = feature.getIConnections();
                    ArrayList<Unit> excitedLetters = feature.getEConnections();
                    if(input[i][j]){
                         for(Unit excited : excitedLetters){
                              excited.addInput(alphaFL);
                         }
                         for(Unit inhibited : inhibitedLetters){
                              inhibited.addInput(-gammaFL);
                         }
                    } else{
                         for(Unit excited : excitedLetters){
                              excited.addInput(-gammaFL);
                         }
                         for(Unit inhibited : inhibitedLetters){
                              inhibited.addInput(alphaFL);
                         }
                    }
               }
          }
     }
     private void letterToWord(){
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
     private void wordToLetter(){
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
     private void wordToLanguage(){
          for(Unit word : wordLevel1){
               if(word.getActivation()>0.0){
                    languageLevel.get(0).addInput(word.getActivation()*alphaWLa);
               }
          }
          for(Unit word : wordLevel2){
               if(word.getActivation()>0.0){
                    languageLevel.get(1).addInput(word.getActivation()*alphaWLa);
               }
          }
     }
     private void languageToWord(){
          double act1 = languageLevel.get(0).getActivation();
          double act2 = languageLevel.get(1).getActivation();
          if(act2>0.0){
               for(Unit word : wordLevel1){
                    word.addInput(act2 * -gammaLa2W);
               }
          }
          if(act1>0.0){
               for(Unit word : wordLevel2){
                    word.addInput(act1 * -gammaLa1W);
               }
          }
     }
     private void wordToWord(){
          double sum = 0.0;
          for(Unit word : wordLevel1){
               if(word.getActivation() > 0){
                    sum += word.getActivation();
               }
          }
          for(Unit word : wordLevel2){
               if(word.getActivation() > 0){
                    sum += word.getActivation();
               }
          }
          for(Unit word : wordLevel1){
               double total = sum;
               if(word.getActivation()>0){
                    total -= word.getActivation();
               }
               word.addInput(total * -gammaWW);
          }
          for(Unit word : wordLevel2){
               double total = sum;
               if(word.getActivation()>0){
                    total -= word.getActivation();
               }
               word.addInput(total * -gammaWW);
          }
     }
     private void letterToLetter(){
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

     /* Updates the activation of every unit, based off their net input and a
      * variety of parameters. Activations are bounded by a max and min.
      */
     private void updateUnitActivation(Unit a){
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

     // Instantiates a network with feature, letter, word, and language layers,
     // as well as instantiating the connections between them
     private void instantiateNetwork(){
          featureLevel = new ArrayList<ArrayList<Unit>>();
          letterLevel = new ArrayList<ArrayList<Unit>>();
          wordLevel1 = new ArrayList<Unit>();
          wordLevel2 = new ArrayList<Unit>();
          languageLevel = new ArrayList<Unit>();
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
               lexicon1 = loadWords(W_FILE1);
               for(String word : lexicon1){
                    wordLevel1.add(new Unit(2, word));
               }
               lexicon2 = loadWords(W_FILE2);
               for(String word : lexicon2){
                    wordLevel2.add(new Unit(2, word));
               }
          } catch(Exception e){
               System.out.println(e);
               System.exit(1);
          }
          languageLevel.add(new Unit(3));
          languageLevel.add(new Unit(3));
          instantiateFeatureConnections();
          instantiateLetterConnections();
     }
     private void instantiateFeatureConnections(){
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
     private void instantiateLetterConnections(){
          instantiateLetterWordConnections(wordLevel1);
          instantiateLetterWordConnections(wordLevel2);
     }
     private void instantiateLetterWordConnections(ArrayList<Unit> wordLevel){
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

     /* Loads a file of words into a String array
      */
     private String[] loadWords(String filename){
          //NOTE: where to split on? be consistent? currently splits on lines
          try{
               Scanner wordScan = new Scanner(new File(filename));
               int i=0;
               ArrayList<String> words = new ArrayList<String>();
               while(wordScan.hasNextLine()){
                    words.add(wordScan.nextLine());
               }
               //String[] words = wordScan.nextLine().replace(" ", "").split("\n");
               //System.out.println(words[1]);
               return words.toArray(new String[words.size()]);
          } catch (Exception e){
               System.out.println(e);
               System.exit(1);
               return null;
          }
     }
     /* Loads a given string into a boolean array of features for each letter
      */
     private boolean[][] loadWord(String word){
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

     /* Loads the letter_segmentation.txt file of letter features into the
      * uc table, to be used when loading input strings into features and in
      * instantiating feature-letter connections.
      */
     private void loadSegs(){
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

     /* Clears the network by setting all units to their resting activations.
      * For most this is zero, but for the english wordLevel this is -.3
     */
     private void clearNetwork(){
          for(ArrayList<Unit> letters : letterLevel){
               for(Unit letter : letters){
                    letter.setActivation(0.0);
                    letter.setAvgActivation(0.0);
               }
          }
          for(Unit word : wordLevel1){
               word.setActivation(-0.3);
               word.setAvgActivation(-0.3);
          }
          for(Unit word : wordLevel2){
               word.setActivation(0.0);
               word.setAvgActivation(0.0);
          }
          for(Unit language : languageLevel){
               language.setActivation(0.0);
               language.setAvgActivation(0.0);
          }
     }
}
