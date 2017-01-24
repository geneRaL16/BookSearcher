/* test ISBNs:
9780552152679
9780375753770
0735619670
*/
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
    static File categoriesDB;
    static File badWords;
    static File f;
    static Scanner countryScanner;
    static FileWriter fw;
    static FileWriter fwF;
    static FileWriter fw2;
    static PrintWriter pw;
    static PrintWriter pw2;
    static PrintWriter pwCategories;
    static Scanner s;
    static Scanner s2;
    static Scanner catScanner;
    static Scanner badWordScanner; //Scanner used for bad words file
    static ArrayList<String> badWordTempList;
    static String[] badWordList; //List of bad words to be checked against
    static Scanner kb;
    static String[] countryName = new String[249];
    static String[] countryAbbr = new String[249];

    /**
     * method used to initialize some required components
     */
    public static void countrySetup() {
        String[] tempArray;
        try { // puts country conversion things in a two-dimensional array
            //File f = new File("countries.txt");
            s = new Scanner(f);
            for (int i = 0; i < 249; i++) {
                tempArray = s.nextLine().split(",");
                countryAbbr[i] = tempArray[0];
                countryName[i] = tempArray[1];
            }
            s.close();
        } catch (FileNotFoundException e) {
        }
    }

    public static String getBookString(String ISBN) throws IOException {
            return Jsoup.connect("https://www.googleapis.com/books/v1/volumes?q=isbn:" + ISBN).ignoreContentType(true).get().toString();
        //return "";
    }

    public static String[] getCategory(String categories) {
        try {
            catScanner = new Scanner(categoriesDB);
            while (!catScanner.nextLine().equals(categories) && catScanner.hasNextLine()) {
                catScanner.nextLine();
            }
            return catScanner.nextLine().split(Character.toString((char) 31));
        } catch (FileNotFoundException ex) {
            Logger.getLogger(BookSearcher.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    /**
     * Adds given book to category text file based on ISBN given
     *
     * @param ISBN String ISBN number of the given book
     * @throws java.io.IOException
     */
    public static void addToCategory(String ISBN) throws IOException {
        String[] categories = getCategories(ISBN, getBookString(ISBN));
        try {
            boolean catFound = false;
            s2 = new Scanner(bookDB2);
            catScanner = new Scanner(categoriesDB);
            pw2 = new PrintWriter(new FileOutputStream(bookDB2, false));
            String temp = "";
            if (catScanner.hasNextLine()) {
                temp = catScanner.nextLine();
            }
            while (!temp.equals(categories[0]) && catScanner.hasNextLine()) {
                pw2.println(temp);
                System.out.println("Writing " + temp);
                temp = catScanner.nextLine();
            }
            if (temp.equals(categories[0])) {
                pw2.println(temp);
                catFound = true;
                System.out.println("Found category " + temp);
                temp = catScanner.nextLine();
                temp += ISBN + Character.toString((char) 31);
                System.out.println("Adding " + temp);
                pw2.println(temp);
            }
            while (catScanner.hasNextLine()) {
                pw2.println(catScanner.nextLine());
            }
            if (!catFound) {
                pw2.println(temp);
                System.out.println("No category found - creating " + categories[0]);
                pw2.println(categories[0]);
                pw2.print(ISBN + Character.toString((char) 31));
            }
            pw2.close();
            pwCategories = new PrintWriter(new FileOutputStream(categoriesDB, false));
            while (s2.hasNextLine()) {
                pwCategories.println(s2.nextLine());
            }
            pwCategories.close();

        } catch (FileNotFoundException ex) {
            Logger.getLogger(BookSearcher.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Initializes the File Input/Output streams for reading and writing to the
     * local book database files
     */
    public static void initFileIO() {
        bookDB = new File("bookdb.txt");
        bookDB2 = new File("bookdb2.txt");
        badWords = new File("badword.txt");
        categoriesDB = new File("categories.txt");
        f = new File("countries.csv");
    }

    public static String[] loadExistingCategories() {
        ArrayList<String> temp = null;
        try {
            temp = new ArrayList<>();
            catScanner = new Scanner(categoriesDB);
            while (catScanner.hasNextLine()) {
                temp.add(catScanner.nextLine());
                catScanner.nextLine(); //Skips additional line as this is order for category naming
            }

        } catch (FileNotFoundException ex) {
            Logger.getLogger(BookSearcher.class.getName()).log(Level.SEVERE, null, ex);
        }
        return temp.toArray(new String[temp.size()]);
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
     * Checks all of the words in a given String against a file to see if there
     * are any bad words in the String
     *
     * @param phrase String phrase to be compared against bad word file
     * @return Boolean if swear words are in the String
     *
     */
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
        } catch (FileNotFoundException e) {
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
    public static String[] getBookInfo(String ISBN) throws IndexOutOfBoundsException, IOException {
        String bookString = "";
        bookString = getBookString(ISBN);
        String info = "";
        info += getTitle(ISBN, bookString) + Character.toString((char) 31); // add title to string info
        try {
            info += bookString.split("\"averageRating\": ")[1].split(",")[0] + Character.toString((char) 31); // add rating to string info
        } catch (ArrayIndexOutOfBoundsException e) { //No rating found for book
            info += "N/R" + Character.toString((char) 31);
        }

        String[] authors = getAuthors(ISBN, bookString);
        String[] categories = getCategories(ISBN, bookString);
        for (int i = 0; i < authors.length; i++) { // add multiple authors' names to info list
            info += authors[i];
            if (i != authors.length - 1) {
                info += ", ";
            } else {
                info += Character.toString((char) 31);
            }
        }

        for (int i = 0; i < categories.length; i++) { // add multiple categories
            info += categories[i];
            if (i != categories.length - 1) {
                info += ", ";
            } else {
                info += Character.toString((char) 31);
            }
        }

        info += getPublisher(ISBN, bookString) + Character.toString((char) 31); // add publisher to string info
        info += getPublishDate(ISBN, bookString) + Character.toString((char) 31); // add publishing date to string info
        info += getDescription(ISBN, bookString) + Character.toString((char) 31); // add description to string info
        info += MLA(ISBN, bookString) + Character.toString((char) 31); // add MLA citation to string info
        info += APA(ISBN, bookString) + Character.toString((char) 31); // add APA citation to string info
        return info.split(Character.toString((char) 31));
    }

    /**
     * Finds the book's title from within a string
     *
     * @param ISBN ISBN of the book
     * @param bookString the online text that contains the title
     * @return String title
     */
    private static String getTitle(String ISBN, String bookString) {
        return bookString.split("\"title\": \"")[1].split("\"")[0];
    }

    /**
     * Finds the book's publisher from within a string
     *
     * @param ISBN ISBN of the book
     * @param bookString the online text that contains the publisher
     * @return String publisher
     */
    private static String getPublisher(String ISBN, String bookString) {
        return bookString.split("\"publisher\": \"")[1].split("\"")[0];
    }

    private static String getCountry(String ISBN, String bookString) {
        return bookString.split("\"country\": \"")[1].split("\"")[0];
    }

    /**
     * Finds the book's date of publication from within a string
     *
     * @param ISBN ISBN of the book
     * @param bookString the online text that contains the date
     * @return String date
     */
    private static String getPublishDate(String ISBN, String bookString) { // COULD RETURN DATE AS AN ARRAY INSTEAD OF A STRING??
        String date = bookString.split("\"publishedDate\": \"")[1].split("\"")[0];
        String d[] = date.split("-");
        if (d.length == 3) {
            switch (d[1]) {
                case "01":
                    d[1] = "January";
                    break;
                case "02":
                    d[1] = "February";
                    break;
                case "03":
                    d[1] = "March";
                    break;
                case "04":
                    d[1] = "April";
                    break;
                case "05":
                    d[1] = "May";
                    break;
                case "06":
                    d[1] = "June";
                    break;
                case "07":
                    d[1] = "July";
                    break;
                case "08":
                    d[1] = "August";
                    break;
                case "09":
                    d[1] = "September";
                    break;
                case "10":
                    d[1] = "October";
                    break;
                case "11":
                    d[1] = "November";
                    break;
                case "12":
                    d[1] = "December";
                    break;
                default:
                    break;
            }
            return d[1] + " " + d[2] + " " + d[0];
        } else if (d.length > 0) {
            return d[0]; // year is in position 0
        } else {
            return date;
        }
    }

    /**
     * Finds the book's description from within a string
     *
     * @param ISBN ISBN of the book
     * @param bookString the online text that contains the description
     * @return String description
     */
    private static String getDescription(String ISBN, String bookString) {
        return bookString.split("\"description\": \"")[1].split("\"")[0].replaceAll("&amp;", "&");
    }

    /**
     * Finds the names of all authors for a book from within a string
     *
     * @param ISBN ISBN of the book
     * @param bookString the online text to find the authors' names in
     * @return array of author names
     */
    private static String[] getAuthors(String ISBN, String bookString) {
        String[] authors = bookString.split("\"authors\": \\[ \"")[1].split("\" \\],")[0].replaceAll("&amp;", "&").split("\", \"");
        return authors;
    }

    /**
     * Finds all of the book's categories from within a string
     *
     * @param ISBN ISBN of the book
     * @param bookString the online text to find the categories in
     * @return array of categories
     */
    private static String[] getCategories(String ISBN, String bookString) {
        String[] authors = bookString.split("\"categories\": \\[ \"")[1].split("\" \\],")[0].replaceAll("&amp;", "&").split("\", \"");
        return authors;
    }

    /**
     * Returns the MLA citation format of the book to the user as a String. MLA
     * format is: [First author's last name], [rest of first author's name], and
     * [2nd author's first name] [2nd author's last name], [3rd author's first
     * name] [3rd author's last name]... . [Italicized Book Title]. [Publisher],
     * [Publication Date].
     *
     * [Publication Date] is "n.d." if no date is available.
     *
     * @param ISBN book ISBN
     * @return String of book information in MLA format
     */
    public static String MLA(String ISBN, String bookString) {
        String MLAString = "";

        // AUTHOR NAMES
        String[] authors = getAuthors(ISBN, bookString);
        if (authors.length > 0) {
            String[] firstAuthor = authors[0].split(" ");
            MLAString += firstAuthor[firstAuthor.length - 1] + ", ";
            for (int i = 0; i < firstAuthor.length - 1; i++) {
                MLAString += " " + firstAuthor[i];
            }
            if (authors.length > 1) {
                authors[1] = "and " + authors[1];
                for (int i = 1; i < authors.length; i++) {
                    MLAString += ", " + authors[i];
                }
            }
            MLAString += ". ";
        }

        // TITLE
        MLAString += "<i>" + getTitle(ISBN, bookString) + "</i>. ";

        // PUBLISHER
        MLAString += getPublisher(ISBN, bookString) + ", ";

        // PUBLICATION DATE
        String date = getPublishDate(ISBN, bookString);
        if (date.equals("")) {
            MLAString += "n.d.";
        } else {
            MLAString += date + ".";
        }

        return MLAString;
    }

    /**
     * Returns the APA citation format of the book to the user as a String. APA
     * format is: [First author's last name], [rest of first author's name], &
     * [2nd author's first name] [2nd author's last name], [3rd author's first
     * name] [3rd author's last name]... . ([year]). [Italicized Book Title].
     * [Location of publication]: [Publisher].
     *
     * @param ISBN book ISBN
     * @param bookString URL
     * @return String of book information in APA format
     */
    public static String APA(String ISBN, String bookString) {
        String APAString = "", temp, tempArray[];

        // AUTHOR NAMES
        String[] authors = getAuthors(ISBN, bookString);
        if (authors.length > 0) {
            String[] firstAuthor = authors[0].split(" ");
            APAString += firstAuthor[firstAuthor.length - 1] + ",";
            for (int i = 0; i < firstAuthor.length - 1; i++) {
                APAString += " " + firstAuthor[i];
            }
            if (authors.length > 1) {
                authors[1] = ", & " + authors[1];
                for (int i = 1; i < authors.length; i++) {
                    APAString += ", " + authors[i];
                }
            }
            APAString += ". ";
        }

        // YEAR
        temp = getPublishDate(ISBN, bookString);
        APAString += "(";
        if (temp.length() >= 4) {
            APAString += temp.substring(temp.length() - 4, temp.length());
        } else {
            APAString += "n.d.";
        }
        APAString += "). ";

        // TITLE
        temp = getTitle(ISBN, bookString);
        tempArray = temp.split(":");
        APAString += "<i>";
        for (String tempArray1 : tempArray) { // subtitles also begin with a capital letter
            temp = "" + tempArray1.charAt(0);
            APAString += temp + tempArray1.substring(1, tempArray1.length());
        }
        APAString += "</i>. ";

        // LOCATION
        String country = getCountry(ISBN, bookString);
        int count = -1;
        try {
            do {
                count++;
            } while (!((countryAbbr[count]).equalsIgnoreCase(country)));
        } catch (Exception e) {
            count = -1;
        }
        String place;
        if (count >= 0) {
            place = countryName[count];
        } else {
            place = country;
        }
        APAString += place + ": ";

        // PUBLISHER
        APAString += getPublisher(ISBN, bookString) + ".";

        return APAString;
    }

    /**
     * Gets the stored reviews of the book with the given ISBN
     *
     * @param ISBN ISBN of the book
     * @return array of reviews (null if no array)
     */
    public static String[] getReviews(String ISBN) {
        String[] temp = {};
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
        } catch (FileNotFoundException e) {
        }
        return temp;
    }

    /**
     * Gets the average star rating from user reviews (integer value for graphic
     * stars)
     *
     * @param ISBN ISBN of book with ratings of interest
     * @return average user rating, rounded to the nearest integer value
     */
    public static int getAverageRatings(String ISBN) {
        double averageRating = 0;
        double count = 0;

        try {
            Scanner s4 = new Scanner(bookDB);
            if (searchISBN(ISBN) >= 0) {
                for (int i = 0; i < searchISBN(ISBN); i++) {
                    s4.nextLine();
                }
                String line = s4.nextLine();
                String[] temp = line.split(Character.toString((char) 31) + "| -");
                for (int i = 1; i < temp.length - 1; i += 2) {
                    averageRating += Integer.parseInt(temp[i]);
                    count++;
                }
                averageRating = averageRating / count;
                averageRating = Math.round(averageRating);
            }
            s4.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(BookSearcher.class.getName()).log(Level.SEVERE, null, ex);
        }
        return (int) averageRating;
    }

    /**
     * Gets the Google thumbnail image of the book
     *
     * @param ISBN String ISBN number of the book
     * @return Google thumbnail image
     */
    public static BufferedImage getBookImage(String ISBN) throws IOException {
        String bookString = getBookString(ISBN);
        BufferedImage stock = null;
        try {
            stock = ImageIO.read(new File("defaultBookImage.png"));
            URL bookURL = new URL(bookString.split("\"thumbnail\": \"")[1].split("\"")[0].replaceAll("&amp;", "&"));
            return ImageIO.read(bookURL);
        } catch (ArrayIndexOutOfBoundsException e) { //No image available
            System.out.println("No thumbnail found for book " + ISBN);
            return stock;
        } catch (MalformedURLException ex) {
            Logger.getLogger(BookSearcher.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            System.out.println("No Image book file not found '/res/Images/defaultBookImage.png')");
        }
        return null;
    }

    /**
     * Gets the Google string for the image of the book
     *
     * @param ISBN String ISBN number of the book
     * @return String Image URL of the cover of the book
     */
    public static String getBookImageString(String ISBN) throws IOException {
        String bookString = getBookString(ISBN);
        return (bookString.split("\"thumbnail\": \"")[1].split("\"")[0].replaceAll("&amp;", "&"));
    }

    /**
     * Adds book to text file if ISBN corresponds to real book
     *
     * @param ISBN String book ISBN
     */
    public static void addBook(String ISBN) throws org.jsoup.HttpStatusException {
        if (searchISBN(ISBN) == -1) {
            try {
                addToCategory(ISBN);
                pw = new PrintWriter(new FileOutputStream(bookDB, true));
                pw.println(ISBN + Character.toString((char) 31)); //Delimited by invisible character
                pw.flush();
                pw.close();
            } catch (IOException ex) {
                Logger.getLogger(BookSearcher.class.getName()).log(Level.SEVERE, null, ex);
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

    /**
     * Checks if a string is an ISBN
     *
     * @param s string to check
     * @return true if valid ISBN
     */
    public static boolean isISBN(String s) {
        int length = s.length();
        if (length == 13 | length == 10) {
            try { // check all but the last character
                Long.parseLong(s.substring(0, length - 1));
            } catch (NumberFormatException | NullPointerException e) {
                return false;
            }

            // if last character not x
            if (!("" + s.charAt(s.length() - 1)).equalsIgnoreCase("x")) {
                try {
                    Integer.parseInt("" + s.charAt(length - 1));
                } catch (NumberFormatException | NullPointerException e) {
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }
}
