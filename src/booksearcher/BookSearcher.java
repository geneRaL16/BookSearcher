package booksearcher;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import java.util.ArrayList;
import javax.imageio.ImageIO;

/**
 *
 * @author liam9, Ryan, Spencer
 */
public class BookSearcher {

    /**
     * @param args the command line arguments
     */
    static File bookDB;
    static File bookDB2;
    static File badWords;
    static FileWriter fw;
    static FileWriter fwF;
    static FileWriter fw2;
    static PrintWriter pw;
    static PrintWriter pw2;
    static Scanner s;
    static Scanner s2;
    static Scanner badWordScanner; //Scanner used for bad words file
    static Document book;
    static ArrayList<String> badWordTempList;
    static String[] badWordList; //List of bad words to be checked against
    static Scanner kb;

    /**
     * just a quick test, see if stuff runs
     *
     * @param args
     */
    public static void main(String[] args) {

        try {
            kb = new Scanner(System.in);
            bookDB = new File("bookdb.txt");
            bookDB2 = new File("bookdb2.txt");
            s2 = new Scanner(bookDB2);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(BookSearcher.class.getName()).log(Level.SEVERE, null, ex);
        }

        String isbn = "9780552152679";
        addBook(isbn);
        addBook("9780375753770");
        addBook("0735619670");
        addReview("0735619670", 4, "Could have been better");
    }


    /**
     * Initializes the File Input/Output streams for reading and writing to the
     * local book database files
     */
    public static void initFileIO() {
        bookDB = new File("bookdb.txt");
        bookDB2 = new File("bookdb2.txt");
        badWords = new File("badword.txt");
    }

    /**
     * Loads the entire list of bad words into an Array to be checked against
     * when users enter reviews for books.
     */
    public static void loadBadWords() {
        badWordTempList = new ArrayList<>();
        try {
            badWordScanner = new Scanner(badWords);
            while (badWordScanner.hasNextLine()) {
                badWordTempList.add(badWordScanner.nextLine());
            }
            badWordList = badWordTempList.toArray(new String[badWordTempList.size()]);
        } catch (FileNotFoundException ex) {
            System.out.println("Warning: Bad Word file not found. Profanity may be added to any reviews!");
        }
    }
    /**
     * Checks all of the words in a given String against a file to see if there are any
     * bad words in the String
     * @param phrase String phrase to be compared against bad word file
     * @return Boolean if swear words are in the String
     **/
    public static boolean checkBadWord(String phrase) {
        String[] temp = phrase.toLowerCase().split(" ");
        for (int i = 0; i < temp.length; i++) {
            for (int x = 0; x < badWordList.length; x++) {
                if (temp[i].equals(badWordList[x])) {
                    return false;
                }
            }
        }
        return true;
    }
    
    /**
     * Searches through the book data for the given ISBN, if not found adds book
     * to text file
     *
     * @param ISBN String ISBN number of given book
     * @return Location of book in the text file (linecount)
     */
    public static int searchISBN(String ISBN) {
        try {
            s = new Scanner(bookDB);
            int linecount = 0;
            while (s.hasNextLine()) {
                if (s.nextLine().split(Character.toString((char) 31))[0].equals(ISBN)) {
                    s.close();
                    return linecount;
                }
                linecount++;
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(BookSearcher.class.getName()).log(Level.SEVERE, null, ex);
        }
        s.close();
        return -1;
    }

    /**
     * Gets information about the book from online and returns it as an array of
     * Strings
     *
     * @param ISBN book ISBN to search by
     * @return array of Strings holding all of the relevant information
     */
    public static String[] getBookInfo(String ISBN) throws IndexOutOfBoundsException {
        try {
            book = Jsoup.connect("https://www.googleapis.com/books/v1/volumes?q=isbn:" + ISBN).ignoreContentType(true).get();
        } catch (IOException ex) {
            Logger.getLogger(BookSearcher.class.getName()).log(Level.SEVERE, null, ex);
        }
        String bookString = book.toString();
        String info = "";
        info += bookString.split("\"title\": \"")[1].split("\"")[0] + Character.toString((char) 31); // add title to string info
        try {
            info += bookString.split("\"averageRating\": ")[1].split(",")[0] + Character.toString((char) 31); // add rating to string info
        } catch (ArrayIndexOutOfBoundsException e) { //No rating found for book
            System.out.println("No rating for book " + ISBN);
            info += "N/R" + Character.toString((char) 31);
        }

// TODO MAKE CHANGES FOR MULTIPLE AUTHORS AND CATERGORIES
        info += bookString.split("\"authors\": \\[ \"")[1].split("\"")[0] + Character.toString((char) 31); // add first author's name to string info
        info += bookString.split("\"categories\": \\[ \"")[1].split("\"")[0] + Character.toString((char) 31); // add first category to string info

        info += bookString.split("\"publisher\": \"")[1].split("\"")[0] + Character.toString((char) 31); // add publisher to string info
        info += bookString.split("\"publishedDate\": \"")[1].split("\"")[0] + Character.toString((char) 31); // add publishing date to string info
        info += bookString.split("\"description\": \"")[1].split("\"")[0] + Character.toString((char) 31); // add description to string info
        return info.split(Character.toString((char) 31));
    }

    /**
     * Gets the stored reviews of the book with the given ISBN
     *
     * @param ISBN ISBN of the book
     * @return array of reviews (null if no array)
     */
    public static String[] getReviews(String ISBN) {
        String[] temp = {"no reviews"};
        try {
            Scanner s3 = new Scanner(bookDB);
            if (searchISBN(ISBN) >= 0) {
                for (int i = 0; i < searchISBN(ISBN); i++) {
                    s3.nextLine();
                }
                s3.useDelimiter(Character.toString((char) 31));
                s3.next();
                temp = s3.nextLine().split(Character.toString((char) 31));
            }
            s3.close();
        } catch (Exception e) {
            System.out.println(e);
        }
        return temp;
    }
    

    public static double getAverageRatings (String ISBN) {
        double averageRating = 0;
        double count = 0;
        try {           
            Scanner s4 = new Scanner(bookDB);
            if (searchISBN(ISBN) >= 0) {
                for (int i = 0; i < searchISBN(ISBN) -1 ; i++) {
                    s4.nextLine();
                }              
                String line = s4.nextLine();
                System.out.println(line);
                String[] temp = line.split(Character.toString((char) 31) + "| -");
                for (int i = 1; i < temp.length - 1; i += 2) {
                    System.out.println(temp[i]);
                    averageRating += Integer.parseInt(temp[i]);
                    count++;
                }
                averageRating = averageRating/count; 
                averageRating = Math.round(averageRating);
            }          
            s4.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(BookSearcher.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println(averageRating);       
        return averageRating;
    }
    
    /**
     * Gets the Google thumbnail image of the book
     *
     * @param ISBN String ISBN number of the book
     * @return Google thumbnail image
     */
    public static BufferedImage getBookImage(String ISBN) {
        try {
            book = Jsoup.connect("https://www.googleapis.com/books/v1/volumes?q=isbn:" + ISBN).ignoreContentType(true).get();
        } catch (IOException ex) {
            Logger.getLogger(BookSearcher.class.getName()).log(Level.SEVERE, null, ex);
        }
        String bookString = book.toString();
        try {
            URL bookURL = new URL(bookString.split("\"thumbnail\": \"")[1].split("\"")[0].replaceAll("&amp;", "&"));

            return ImageIO.read(bookURL);
        } catch (ArrayIndexOutOfBoundsException e) { //No image available
            System.out.println("No thumbnail found for book " + ISBN);
            try { //Returns "No Image Available" image
                return ImageIO.read(new File("res" + File.pathSeparator + "Images" + File.pathSeparator + "defaultBookImage.png"));
            } catch (IOException ex) {
                Logger.getLogger(BookSearcher.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (MalformedURLException ex) {
            Logger.getLogger(BookSearcher.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(BookSearcher.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null; //Will never occur (Should this be here?)
    }

    /**
     * Adds book to text file if ISBN corresponds to real book
     *
     * @param ISBN String book ISBN
     */
    public static void addBook(String ISBN) {
        if (searchISBN(ISBN) == -1) {
            try {
                pw = new PrintWriter(new FileOutputStream(bookDB, true));
                pw.println(ISBN + Character.toString((char) 31)); //Delimited by invisible character
                pw.flush();
                pw.close();
            } catch (IOException ex) {
                Logger.getLogger(BookSearcher.class.getName()).log(Level.SEVERE, null, ex);
            } catch (Exception e) {
                System.out.print("book with ISBN " + ISBN + " is not in database");//Book will not be added to the text file
            }
        }
    }

    /**
     * Process for adding a new review for a book
     *
     * @param isbn book's isbn
     * @param rating numerical rating of book between 1 and 5
     * @param review text typed as a review for the book
     */
    public static void addReview(String isbn, int rating, String review) {
        if (searchISBN(isbn) >= 0) {
            try {
                pw2 = new PrintWriter(new FileOutputStream(bookDB2, false));
                String temp;
                int position = searchISBN(isbn); //Location of book in text file for review to be added to
                s = new Scanner(bookDB);
                for (int i = 0; i < position; i++) { //Moves all content to second text file, and modifies line needed
                    pw2.println(s.nextLine());
                }
                temp = s.nextLine();
                temp += rating + " - " + review + Character.toString((char) 31);
                pw2.println(temp);
                while (s.hasNextLine()) {
                    pw2.println(s.nextLine());
                }
                pw2.flush();
                s.close();
                s2 = new Scanner(bookDB2);
                pw = new PrintWriter(new FileOutputStream(bookDB, false));
                while (s2.hasNextLine()) { //Moves all data back to first text file
                    pw.println(s2.nextLine());
                }
                pw.flush();
                pw.close();
                pw2.close();
                s2.close();
            } catch (FileNotFoundException ex) {
                Logger.getLogger(BookSearcher.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
