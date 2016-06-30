package br.com.memorify.booklisting.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

public class Book implements Parcelable {

    public String title;
    public String authors;
    public String publisher;
    public String publishedYear;
    public String description;

    private final static String TITLE_KEY = "title";
    private final static String AUTHORS_KEY = "authors";
    private final static String PUBLISHER_KEY = "publisher";
    private final static String PUBLISHED_DATE_KEY = "publishedDate";
    private final static String DESCRIPTION_KEY = "description";

    public Book() {}

    private Book(Parcel in) {
        super();
        title = in.readString();
        authors = in.readString();
        publisher = in.readString();
        publishedYear = in.readString();
        description = in.readString();
    }

    @NonNull
    public static Book fromJSON(@NonNull JSONObject bookInfo) {
        Book book = new Book();
        book.title = bookInfo.optString(TITLE_KEY);
        book.publisher = bookInfo.optString(PUBLISHER_KEY);
        book.description = bookInfo.optString(DESCRIPTION_KEY);
        JSONArray authorsJsonArray = bookInfo.optJSONArray(AUTHORS_KEY);
        book.authors = formatAuthors(authorsJsonArray);
        String dateUnformatted = bookInfo.optString(PUBLISHED_DATE_KEY);
        book.publishedYear = getYear(dateUnformatted);
        return book;
    }

    private static String formatAuthors(JSONArray authorsJsonArray) {
        String result = "";
        if (authorsJsonArray != null && authorsJsonArray.length() > 0) {
            for (int i = 0; i < authorsJsonArray.length(); i++) {
                result += authorsJsonArray.optString(i) + ", ";
            }
            return result.substring(0, result.length() - 2);
        } else {
            return "";
        }
    }

    private static String getYear(String dateUnformatted) {
        if (dateUnformatted.length() >= 4) {
            return dateUnformatted.substring(0, 4);
        } else {
            return null;
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(authors);
        dest.writeString(publisher);
        dest.writeString(publishedYear);
        dest.writeString(description);
    }

    public static final Parcelable.Creator<Book> CREATOR = new Parcelable.Creator<Book>() {
        public Book createFromParcel(Parcel in) {
            return new Book(in);
        }

        public Book[] newArray(int size) {
            return new Book[size];
        }
    };
}
