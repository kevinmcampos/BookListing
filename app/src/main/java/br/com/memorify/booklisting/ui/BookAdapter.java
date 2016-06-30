package br.com.memorify.booklisting.ui;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import br.com.memorify.booklisting.R;
import br.com.memorify.booklisting.model.Book;

public class BookAdapter extends RecyclerView.Adapter<BookAdapter.ViewHolder> {

    private List<Book> books;

    public BookAdapter(List<Book> books) {
        this.books = books;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_book, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bind(books.get(position));
    }

    @Override
    public int getItemCount() {
        return books.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView titleTextView;
        TextView authorTextView;
        View inView;
        TextView publishedYearTextView;
        TextView publisherTextView;
        TextView descriptionTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            bindViews(itemView);
        }

        private void bindViews(View itemView) {
            titleTextView = (TextView) itemView.findViewById(R.id.book_title);
            authorTextView = (TextView) itemView.findViewById(R.id.book_author);
            publishedYearTextView = (TextView) itemView.findViewById(R.id.book_published_year);
            inView = itemView.findViewById(R.id.in);
            publisherTextView = (TextView) itemView.findViewById(R.id.book_publisher);
            descriptionTextView = (TextView) itemView.findViewById(R.id.book_description);
        }

        public void bind(Book book) {
            titleTextView.setText(book.title);
            authorTextView.setText(book.authors);
            if (book.publishedYear == null) {
                inView.setVisibility(View.GONE);
                publishedYearTextView.setVisibility(View.GONE);
            } else {
                inView.setVisibility(View.VISIBLE);
                publishedYearTextView.setVisibility(View.VISIBLE);
                publishedYearTextView.setText(book.publishedYear);
            }
            publisherTextView.setText(book.publisher);
            descriptionTextView.setText(book.description);
        }
    }
}
