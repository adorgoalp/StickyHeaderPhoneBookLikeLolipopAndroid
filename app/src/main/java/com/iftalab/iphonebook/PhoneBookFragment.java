package com.iftalab.iphonebook;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by Ifta Noor Mahmood on 1/24/2017.
 */

public class PhoneBookFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{
    public static final String TAG = "PhoneBookFragmentTag";
    private static final int LOADER_ID = 50;
    private static final String[] PROJECTION = {ContactsContract.Contacts.DISPLAY_NAME, ContactsContract.Contacts.PHOTO_URI};
    RecyclerView rv;
    RecyclerView.LayoutManager layoutManager;
    LinearLayoutManager llm;
    PhoneBookAdapter adapter;
    TextView tvSticky;

    public PhoneBookFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.phone_book_layout,null);
        rv = (RecyclerView) v.findViewById(R.id.rv);
        tvSticky = (TextView) v.findViewById(R.id.tvStickyTop);
        layoutManager = new LinearLayoutManager(getActivity());
        rv.setLayoutManager(layoutManager);
        llm = (LinearLayoutManager) rv.getLayoutManager();
        adapter = new PhoneBookAdapter(getActivity());
        rv.setAdapter(adapter);
//        tvStickey.setTranslationY(-10);
        rv.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int position = firstVisibleItemPosition();
                if (isSectionStartPosition.get(position)) {
                    tvSticky.setText(sectionCharMap.get(position).toUpperCase());
                }
                int secondItemPosition = secondVisibleItemPosition();
                if(isSectionStartPosition.get(secondItemPosition))
                {
                    View v = rv.getLayoutManager().getChildAt(2);
                    int translationY = tvSticky.getHeight()*2-v.getTop();
                    tvSticky.setTranslationY(translationY*-1);
                }else {
                    tvSticky.setTranslationY(0);
                }
            }
        });
        getLoaderManager().initLoader(LOADER_ID, null, this);
        return v;
    }
    int firstVisibleItemPosition() {
        return llm.findFirstVisibleItemPosition();
    }

    int secondVisibleItemPosition() {
        return llm.findFirstCompletelyVisibleItemPosition();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (id == LOADER_ID) {
            return new CursorLoader(getContext(),
                    ContactsContract.Contacts.CONTENT_URI,
                    PROJECTION,
                    ContactsContract.Contacts.DISPLAY_NAME + "<> ?",
                    new String[]{"null"},
                    "UPPER(" + ContactsContract.Contacts.DISPLAY_NAME + ") ASC");
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (loader.getId() == LOADER_ID) {
            if (data != null && data.getCount() != 0) {
                if (data.moveToFirst()) {
                    if (adapter != null) {
                        adapter.swapCursor(data);
                    }
                }
            }
        }
    }
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        getLoaderManager().destroyLoader(LOADER_ID);
    }

    SparseBooleanArray isSectionStartPosition = new SparseBooleanArray();
    SparseArray<String> sectionCharMap = new SparseArray<>();

    class PhoneBookAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        class PhoneBuddy {
            String name, imageUri, sectionChar = null;

            public PhoneBuddy(Cursor cursor) {
                if (cursor == null) {
                    return;
                }
                this.name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                this.imageUri = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.PHOTO_URI));

                if (isSectionStart(cursor)) {
                    this.sectionChar = this.name.substring(0, 1);
                } else {
                    this.sectionChar = " ";
                }

            }

            public boolean isSectionStart() {
                if (sectionChar != null) {
                    if (sectionChar.equals(" ")) {
                        return false;
                    }
                    return true;
                }
                return false;
            }

            private boolean isSectionStart(Cursor cursor) {
                if (cursor.getPosition() == 0) {
                    return true;
                } else {
                    String currentName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                    cursor.moveToPrevious();
                    String previousName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

                    if (currentName.toUpperCase().substring(0, 1).equals(previousName.toUpperCase().substring(0, 1))) {
                        return false;
                    }
                    return true;
                }
            }
        }

        private static final int ITEM_VIEW = 5000;
        Cursor dataCursor = null;
        Context context;

        public PhoneBookAdapter(Context context) {
            this.context = context;
        }

        protected void swapCursor(Cursor cursor) {
            dataCursor = cursor;
            notifyDataSetChanged();
            rv.smoothScrollToPosition(0);
        }

        class ViewHolderQA extends RecyclerView.ViewHolder {
            TextView tvSectionLetter, tvName;

            private ViewHolderQA(View itemView) {
                super(itemView);
                tvSectionLetter = (TextView) itemView.findViewById(R.id.tvSectionLetter);
                tvName = (TextView) itemView.findViewById(R.id.tvName);
            }

            public void bindView() {
                if (dataCursor == null) {
                    return;
                }
                try {
                    int position = getAdapterPosition();
                    dataCursor.moveToPosition(position);
                    PhoneBuddy phoneBuddy = new PhoneBuddy(dataCursor);
                    isSectionStartPosition.put(position, phoneBuddy.isSectionStart());
                    sectionCharMap.put(position, phoneBuddy.sectionChar);
                    tvSectionLetter.setText(phoneBuddy.sectionChar.toUpperCase());
                    tvName.setText(phoneBuddy.name);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v;
            if (viewType == ITEM_VIEW) {
                v = LayoutInflater.from(context).inflate(R.layout.single_phonebook_entry, parent, false);
                return new ViewHolderQA(v);
            }
            return null;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            ViewHolderQA vh = (ViewHolderQA) holder;
            vh.bindView();
        }

        @Override
        public int getItemCount() {
            if (dataCursor == null) {
                return 0;
            }
            return dataCursor.getCount();
        }

        @Override
        public int getItemViewType(int position) {
            return ITEM_VIEW;
        }
    }
}
