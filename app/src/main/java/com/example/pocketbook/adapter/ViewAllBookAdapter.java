package com.example.pocketbook.adapter;

import android.os.Bundle;

import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pocketbook.GlideApp;
import com.example.pocketbook.R;
import com.example.pocketbook.fragment.ViewBookFragment;
import com.example.pocketbook.fragment.ViewMyBookFragment;
import com.example.pocketbook.model.Book;
import com.example.pocketbook.model.User;
import com.example.pocketbook.util.FirebaseIntegrity;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Objects;

public class ViewAllBookAdapter
        extends FirestoreRecyclerAdapter<Book, ViewAllBookAdapter.BookHolder> {

    private User currentUser;
    private User bookOwner;
    private FragmentActivity activity;
    private boolean isOwnerTab;

    public ViewAllBookAdapter(@NonNull FirestoreRecyclerOptions<Book> options, User currentUser,
                              FragmentActivity activity, boolean isOwnerTab) {
        super(options);
        this.currentUser = currentUser;
        this.activity = activity;
        this.isOwnerTab = isOwnerTab;
    }

    static class BookHolder extends RecyclerView.ViewHolder {
        TextView bookTitle;
        TextView bookAuthor;
        TextView otherUser;
        ImageView bookCoverImageView;
        CardView bookCoverCard;
        ImageView statusImageView;

        public BookHolder(@NonNull View itemView) {
            super(itemView);
            bookTitle = itemView.findViewById(R.id.itemBookV2Title);
            bookAuthor = itemView.findViewById(R.id.itemBookV2Author);
            bookCoverImageView = itemView.findViewById(R.id.itemBookV2BookCoverImageView);
            bookCoverCard = itemView.findViewById(R.id.itemBookV2Card);
            statusImageView = itemView.findViewById(R.id.itemBookV2Status);
            otherUser = itemView.findViewById(R.id.itemBookV2OtherUser);

        }
    }

    @NonNull
    @Override
    public BookHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new BookHolder(inflater.inflate(R.layout.item_book_v2, parent, false));
    }

    @Override
    protected void onBindViewHolder(@NonNull BookHolder bookHolder,
                                    int position, @NonNull Book book) {

        bookHolder.otherUser.setVisibility(View.INVISIBLE);

        bookHolder.bookTitle.setText(book.getTitle());
        bookHolder.bookAuthor.setText(book.getAuthor());

        GlideApp.with(Objects.requireNonNull(activity.getBaseContext()))
                .load(FirebaseIntegrity.getBookCover(book))
                .into(bookHolder.bookCoverImageView);

        switch (book.getStatus().toUpperCase()) {

            // if the book is borrowed or accepted, it is not available for requesting
            case "BORROWED":

                if (isOwnerTab) {
                    setOwnerTabUser(bookHolder, book.getId(), R.string.borrowed_book_text);
                } else {
                    setBorrowerTabUser(bookHolder, book.getOwner(), R.string.owned_book_text);
                }

                bookHolder.statusImageView.setImageResource(R.drawable.ic_borrowed);
                bookHolder.statusImageView.setColorFilter(ContextCompat
                                .getColor(activity.getBaseContext(), R.color.colorBorrowed),
                        android.graphics.PorterDuff.Mode.SRC_IN);
                break;

            case "ACCEPTED":

                if (isOwnerTab) {
                    setOwnerTabUser(bookHolder, book.getId(), R.string.accepted_book_text);
                } else {
                    setBorrowerTabUser(bookHolder, book.getOwner(), R.string.owned_book_text);
                }

                bookHolder.statusImageView.setImageResource(R.drawable.ic_accepted);
                bookHolder.statusImageView.setColorFilter(ContextCompat
                                .getColor(activity.getBaseContext(), R.color.colorAccepted),
                        android.graphics.PorterDuff.Mode.SRC_IN);
                break;

            case "REQUESTED":

                if (isOwnerTab) {
                    setOwnerTabUser(bookHolder, book.getId(), R.string.requested_book_text);
                } else {
                    setBorrowerTabUser(bookHolder, book.getOwner(), R.string.owned_book_text);
                }

                bookHolder.statusImageView.setImageResource(R.drawable.ic_requested);
                bookHolder.statusImageView.setColorFilter(ContextCompat
                                .getColor(activity.getBaseContext(), R.color.colorRequested),
                        android.graphics.PorterDuff.Mode.SRC_IN);

                break;

            // default case is that the book is available
            default:
                bookHolder.statusImageView.setImageResource(R.drawable.ic_available);
                bookHolder.statusImageView.setColorFilter(ContextCompat
                                .getColor(activity.getBaseContext(), R.color.colorAvailable),
                        android.graphics.PorterDuff.Mode.SRC_IN);
        }

        bookHolder.itemView.setOnClickListener(v -> {
            if (book.getOwner().equals(currentUser.getEmail())) {
                Log.e("OWNERIN", book.getOwner());
                ViewMyBookFragment nextFrag = ViewMyBookFragment
                        .newInstance(currentUser, book);
                Bundle bundle = new Bundle();
                bundle.putSerializable("VMBF_USER", currentUser);
                bundle.putSerializable("VMBF_BOOK", book);
                nextFrag.setArguments(bundle);
                activity.getSupportFragmentManager().beginTransaction()
                        .replace(activity.findViewById(R.id.container).getId(), nextFrag)
                        .addToBackStack(null).commit();
            } else {
                FirebaseFirestore.getInstance().collection("users")
                        .document(book.getOwner())
                        .get().addOnCompleteListener(task -> {
                    DocumentSnapshot document = task.getResult();
                    if ((document != null) && (document.exists())) {
                        bookOwner = FirebaseIntegrity.getUserFromFirestore(document);
                        ViewBookFragment nextFrag = ViewBookFragment
                                .newInstance(currentUser, bookOwner, book);
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("BA_CURRENTUSER", currentUser);
                        bundle.putSerializable("BA_BOOK", book);
                        bundle.putSerializable("BA_BOOKOWNER", bookOwner);
                        nextFrag.setArguments(bundle);
                        activity.getSupportFragmentManager().beginTransaction()
                                .replace(activity.findViewById(R.id.container).getId(), nextFrag)
                                .addToBackStack(null).commit();
                    }
                });
            }

        });

    }

    /**
     * sets the owner tab user
     * @param bookHolder The bookHolder
     * @param documentId The documentId
     * @param stringId The stringId
     */
    protected void setOwnerTabUser(BookHolder bookHolder, String documentId, int stringId) {
        FirebaseFirestore.getInstance()
                .collection("catalogue")
                .document(documentId)
                .get()
                .addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful()) {

                        DocumentSnapshot document = task1.getResult();

                        // if the document exists
                        if ((document != null) && (document.exists())) {

                            ArrayList<String> requesters =
                                    (ArrayList<String>) document.get("requesters");

                            if ((requesters != null) && (requesters.get(0) != null)) {

                                FirebaseFirestore.getInstance()
                                        .collection("users")
                                        .document(requesters.get(0))
                                        .get()
                                        .addOnCompleteListener(task2 -> {
                                            if (task2.isSuccessful()) {

                                                DocumentSnapshot document1
                                                        = task2.getResult();

                                                // if the document exists
                                                if ((document1 != null)
                                                        && (document1.exists())) {

                                                    User user = FirebaseIntegrity
                                                            .getUserFromFirestore(document1);

                                                    if (user != null) {

                                                        bookHolder.otherUser
                                                                .setText(activity
                                                                        .getResources()
                                                                        .getString(stringId,
                                                                                user.getUsername()
                                                                        ));

                                                        bookHolder.otherUser
                                                                .setVisibility(View
                                                                        .VISIBLE);
                                                    }

                                                }
                                            }
                                        });
                            }

                        }
                    }
                });

    }

    /**
     * sets the borrower tab user
     * @param bookHolder The bookHolder
     * @param documentId The documentId
     * @param stringId The stringId
     */
    protected void setBorrowerTabUser(BookHolder bookHolder, String documentId, int stringId) {
        FirebaseFirestore.getInstance()
                .collection("users")
                .document(documentId)
                .get()
                .addOnCompleteListener(task2 -> {
                    if (task2.isSuccessful()) {

                        DocumentSnapshot document1
                                = task2.getResult();

                        // if the document exists
                        if ((document1 != null)
                                && (document1.exists())) {

                            User user = FirebaseIntegrity.getUserFromFirestore(
                                    document1);

                            if (user != null) {

                                bookHolder.otherUser
                                        .setText(activity
                                                .getResources()
                                                .getString(stringId,
                                                        user.getUsername()));

                                bookHolder.otherUser.setVisibility(View.VISIBLE);
                            }

                        }
                    }
                });
    }
}

