package br.com.memorify.booklisting.ui;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import br.com.memorify.booklisting.R;
import br.com.memorify.booklisting.model.Book;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private final String BOOKS_RESULT_KEY = "BOOKS_RESULT_KEY";

    private EditText searchEditText;
    private Button searchButton;
    private RecyclerView bookListView;
    private View loadingView;
    private TextView emptyView;

    private BookAdapter bookAdapter;

    private ArrayList<Book> books = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState != null && savedInstanceState.containsKey(BOOKS_RESULT_KEY)) {
            books = savedInstanceState.getParcelableArrayList(BOOKS_RESULT_KEY);
        }

        bindViews();
        setupViews();
        if (savedInstanceState == null) {
            showMessage(R.string.welcome);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(BOOKS_RESULT_KEY, books);
        super.onSaveInstanceState(outState);
    }

    private void bindViews() {
        searchEditText = (EditText) findViewById(R.id.search_text);
        searchButton = (Button) findViewById(R.id.search_button);
        bookListView = (RecyclerView) findViewById(R.id.book_list);
        loadingView = findViewById(R.id.loading_progress);
        emptyView = (TextView) findViewById(R.id.empty_view);
    }

    private void setupViews() {
        setupList();
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String searchText = searchEditText.getText().toString().trim();
                if (!searchText.isEmpty()) {
                    if (isOnline()) {
                        new SearchBooksTask(searchText).execute();
                    } else {
                        showMessage(R.string.no_internet_connection);
                    }
                }
            }
        });
    }

    private void setupList() {
        bookListView.setHasFixedSize(true);
        bookListView.setLayoutManager(new LinearLayoutManager(getBaseContext()));
        bookAdapter = new BookAdapter(books);
        bookListView.setAdapter(bookAdapter);
    }

    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    public class SearchBooksTask extends AsyncTask<Void, Void, Void> {

        private String searchText;
        private List<Book> result;
        private int errorResId;

        public SearchBooksTask(@NonNull String searchText) {
            if (searchText.isEmpty()) {
                throw new IllegalArgumentException();
            }
            this.searchText = searchText;
        }

        @Override
        protected void onPreExecute() {
            showProgress(true);
        }

        @Override
        protected Void doInBackground(Void... params) {
            HttpURLConnection urlConnection = null;

            final String BOOKS_BASE_URL = "https://www.googleapis.com/books/v1/volumes";
            final String QUERY_PARAM = "q";
            final String MAX_RESULT_PARAM = "maxResults";

            String query = searchText;
            final int maxResult = 10;

            try {
                Uri builtUri = Uri.parse(BOOKS_BASE_URL).buildUpon()
                        .appendQueryParameter(QUERY_PARAM, query)
                        .appendQueryParameter(MAX_RESULT_PARAM, String.valueOf(maxResult))
                        .build();
                URL url = new URL(builtUri.toString());

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                switch (urlConnection.getResponseCode()) {
                    case 200:
                        String resultJsonStr = getStringFromInputStream(urlConnection.getInputStream());
                        result = getBooksDataFromJSON(resultJsonStr);
                        break;
                    default:
                        errorResId = R.string.internal_error;
                        Log.e(TAG, "URL: " + url);
                        Log.e(TAG, "responseCode: " + urlConnection.getResponseCode());
                        Log.e(TAG, "responseMessage: " + urlConnection.getResponseMessage());
                }

            } catch (UnknownHostException e) {
                e.printStackTrace();
                errorResId = R.string.no_internet_connection;
            } catch (IOException | JSONException e) {
                errorResId = R.string.internal_error;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }

            return null;
        }

        private String getStringFromInputStream(InputStream inputStream) throws IOException {
            StringBuilder buffer = new StringBuilder();
            if (inputStream == null) {
                // do nothing
                return null;
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line).append("\n");
            }

            if (buffer.length() == 0) {
                return null;
            }

            try {
                reader.close();
            } catch (final IOException e) {
                Log.e(TAG, "Error closing stream", e);
            }

            return buffer.toString();
        }

        private List<Book> getBooksDataFromJSON(String resultJsonStr) throws JSONException {
            final String BOOK_ITEM_LIST_KEY = "items";
            final String BOOK_VOLUME_INFO_KEY = "volumeInfo";

            List<Book> books = new ArrayList<>();

            JSONObject resultJson = new JSONObject(resultJsonStr);
            JSONArray items = resultJson.optJSONArray(BOOK_ITEM_LIST_KEY);
            if ( items != null ){
                for (int i = 0; i < items.length(); i++) {
                    JSONObject bookInfo = items.getJSONObject(i);
                    Book book = Book.fromJSON(bookInfo.optJSONObject(BOOK_VOLUME_INFO_KEY));
                    books.add(book);
                }
            }
            return books;
        }

        @Override
        protected void onCancelled() {
            loadingView.setVisibility(View.GONE);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            showProgress(false);
            boolean hasError = errorResId != 0;
            if (hasError) {
                showMessage(errorResId);
            } else {
                if (result != null) {
                    books.clear();
                    books.addAll(result);
                    bookAdapter.notifyDataSetChanged();

                    if (result.size() == 0) {
                        showMessage(R.string.no_result);
                    } else {
                        showList();
                    }
                }
            }
        }
    }

    private void showMessage(@StringRes int message) {
        bookListView.setVisibility(View.GONE);
        emptyView.setVisibility(View.VISIBLE);
        emptyView.setText(message);
    }

    private void showList() {
        bookListView.setVisibility(View.VISIBLE);
        emptyView.setVisibility(View.GONE);
    }

    private void showProgress(boolean shouldShowProgress) {
        loadingView.setVisibility(shouldShowProgress ? View.VISIBLE : View.INVISIBLE);
        searchEditText.setEnabled(!shouldShowProgress);
        searchButton.setEnabled(!shouldShowProgress);
    }
}
