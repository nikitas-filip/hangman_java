import java.io.*;
import java.util.*;
import java.lang.Math;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.stream.Collectors;

import org.json.JSONException;
import org.json.JSONObject;

public class Main {
    private static String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }

    public static JSONObject readJsonFromUrl(String url) throws IOException, JSONException {
        InputStream is = new URL(url).openStream();
        try {
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
            String jsonText = readAll(rd);
            JSONObject json = new JSONObject(jsonText);
            return json;
        } finally {
            is.close();
        }
    }


    public static void main(String[] args) throws IOException, ErrorHandling.InvalidCountException ,ErrorHandling.InvalidRangeException,ErrorHandling.UndersizeException,ErrorHandling.UnbalancedException {

        System.out.print("Enter the OPEN LIBRARY ID : ");
        Scanner scanner = new Scanner(System. in);
        String Open_Library_ID = scanner. nextLine();
        JSONObject json = readJsonFromUrl("https://openlibrary.org/works/" + Open_Library_ID + ".json"); //read the json from the open library
        JSONObject desc = (JSONObject) json.get("description");

        String description = (String) desc.get("value"); //get the description
        String result_desc = description.replaceAll("\\p{Punct}", " "); //remove unnecessary punctuation


            String words[] = result_desc.split(" "); //save the words of the description in an array

            //variable declarations
            int j=0;
            int long_words_count = 0;
            int set_size = 0;
            Set<String> dictionary = null;
            int really_long_words = 0;
            double percentage = 0;
            int random_number;
            int wrong_guesses = 0;
            String hangman_word_str = null;
            char [] hangman_word;
            char current_letter ;
            int points = 0;
            Set<String> hidden_words;
            Character known_part [] ;
            double letter_probability [] = new double[26];

            for (int i = 0; i < words.length; i++) { //count the long words
                if (words[i].length() >= 6)
                    long_words_count++;
            }


            String final_words[] = new String[long_words_count]; //create our dictionary that will contain only the long words

        try {
            if (long_words_count < words.length) //if there were words with less than 6 letters in the description
                throw(new ErrorHandling.InvalidRangeException());
        }
        catch(Exception e) {
            e.printStackTrace(); //catch the InvalidRange Exception
        }

        for (int i = 0; i < words.length; i++) {
            if (words[i].length() >= 6) {
                final_words[j] = words[i];
                j++;
            }

        }


        try {

            List<String> final_words_list = Arrays.asList(final_words);
            dictionary = new HashSet<String>(final_words_list); //create a Set so that we remove any duplicate words


            int total_words = final_words.length;
            set_size = dictionary.size();

            if (set_size < total_words) {
                throw(new ErrorHandling.InvalidCountException()); //throw an exception because we found duplicate words
            }

        }
        catch(ErrorHandling.InvalidCountException e) {
            e.printStackTrace();
        }


        try{
            if (set_size < 20) //if the dictionary is under 20 words throw an exception
                throw(new ErrorHandling.UndersizeException());

            for (String word : dictionary){
                if (word.length() >= 9)
                    really_long_words++;
            }

            percentage =  ( (double) really_long_words / dictionary.size()) * 100;

            if (percentage < 20 ){ //if the percentage of words with more than 8 letters is lower than 20% then throw an exception
                throw(new ErrorHandling.UnbalancedException());
            }

            String destination = System.getProperty("user.home") + "\\medialab";
            new File(destination).mkdirs(); //create the destination directory if it doesn't exist
            PrintWriter out = new PrintWriter(destination + "\\hangman_DICTIONARY-1.txt");

            for (String word : dictionary){ //Copy the words to a txt file , 1 word per line
                out.println(word);
            }
            out.close();

        }
        catch(ErrorHandling.UndersizeException e){
            e.printStackTrace();
            return;
        }
        catch(ErrorHandling.UnbalancedException e){
            e.printStackTrace();
            return;
        }

        random_number = new Random().nextInt(dictionary.size());
        j = 0;

        for(String word : dictionary) { //pick a random word from the dictionary
            if (j == random_number)
                hangman_word_str = word;
            j++;
        }


        hangman_word_str = hangman_word_str.toLowerCase(Locale.ROOT); //Transform the word to lowercase
        hangman_word = hangman_word_str.toCharArray();
        known_part = new Character[hangman_word.length];
        for (int i=0; i<hangman_word.length; i++)
            known_part[i] = '_';
        j = 0;

        //remove all the words that are not of equal size with the hangman word
        dictionary.removeIf(word -> word.length() != hangman_word.length);
        hidden_words = dictionary ; // the subset of hidden words

        List<Character> potential_letters = "abcdefghijklmnopqrstuvwxyz".chars().mapToObj(c -> (char) c).collect(Collectors.toList()); //all the potential letters aka the alphabet
        points = 15; // the player starts the game with 15 points
        int i =0;
        HashMap<Character, Double> letter_probabilities  = new HashMap<Character, Double>();

        for (Character letter: potential_letters)
            letter_probabilities.put(letter,(double)0);

        for (Character letter:potential_letters){
            for (String hidden_word:hidden_words){
                if (hidden_word.charAt(0) == letter)
                    letter_probabilities.put(letter,letter_probabilities.get(letter)+1); //increase the propability
            }

            letter_probabilities.put(letter,letter_probabilities.get(letter)/ (double) hidden_words.size()); //increase the propability
        }
        int letters_found = 0;
        while (wrong_guesses < 6 && (letters_found < hangman_word.length && points > 0 )  ) { //keep playing until you guess wrong 6 times in total or find the WORD winner winner

            letters_found = 0;
            scanner = new Scanner(System.in);
            System.out.println("The letters you can pick from are the following: ");
            System.out.print("[ ");
            for (char letter : potential_letters) {

                if (potential_letters.indexOf(letter) == (potential_letters.size() -1)) // if it's the last character then don't put a comma
                    System.out.println(letter + " ]");
                else
                    System.out.print(letter + ", ");
            }
            System.out.println("Please enter a letter:");
            String guess = scanner.nextLine();
            Character letter_guess = guess.charAt(0);
            System.out.println(letter_guess);
            boolean found_first = false;

            for (j=0; j< hangman_word.length; j++) {

                if (hangman_word[j] == letter_guess) {       //if he guess correctly

                    if (found_first == false) {
                        System.out.println(letter_guess + " was a correct guess! ");
                        found_first = true;
                    }

                    known_part[j] = letter_guess;

                    i++;
                    potential_letters.remove(letter_guess); //remove the letter  that was guessed from the "alphabet"

                    //CALCULATE THE NEW LETTER PROPABILITIES
                    /*for (Character letter : potential_letters) {
                        for (String hidden_word : hidden_words) {
                            if (hidden_word.charAt(0) == letter)
                                letter_probabilities.put(letter, letter_probabilities.get(letter) + 1); //increase the propability
                        }

                        letter_probabilities.put(letter, letter_probabilities.get(letter) / (double) hidden_words.size()); //increase the propability
                    }
                    for (String word : hidden_words) {

                    }*/
                }
            }
            if (found_first ) { //IF HE FOUND THE LETTER
                //POINT SYSTEM
                if (letter_probabilities.get(letter_guess) > 0.6)
                    points = points + 5; //5 POINTS FOR GRYFFINDOR
                else if (letter_probabilities.get(letter_guess) >= 0.4 && letter_probabilities.get(letter_guess) < 0.6)
                    points = points + 10;
                else if (letter_probabilities.get(letter_guess) >= 0.25 && letter_probabilities.get(letter_guess) < 0.4)
                    points = points + 15;
                else
                    points = points + 30;
            }
            else{
                wrong_guesses++;
                points = points - 15; // 15 points deducted from GRYFFINDOR
                System.out.println("You guessed wrong ! You have guessed wrong " + wrong_guesses + " times. " + (6 - wrong_guesses) + " more and its game over!");
                potential_letters.remove(letter_guess); //remove the letter  that was guessed from the "alphabet"
            }

            System.out.print("The word until now is: ");

            for (int x = 0; x < known_part.length; x++) {
                System.out.print(known_part[x]);
                if (known_part[x]!='_')
                 letters_found ++;
            }
            System.out.println(" ");


        }

        if (wrong_guesses == 6) {
            System.out.println("GAME OVER . You guessed wrong too many times .");
            System.out.println("The correct word was " + hangman_word_str);
        }
        else if( points < 0){
            System.out.println("GAME OVER . Your points are negative.");
            System.out.println("The correct word was " + hangman_word_str);
        }
        else{
            System.out.println("Congrats , you found the word ! Winner winner ! The word was: " + hangman_word_str);
            System.out.println("You gathered " + points + " points in total!");
        }

    }
}
