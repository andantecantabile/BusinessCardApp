package edu.pdx.ece558_fall15.alex_elizabeth.businesscardcontact;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

public class ContactListFragment extends Fragment {

    private RecyclerView mContactEntryRecyclerView;
    private ContactEntryAdapter mAdapter;
    private Callbacks mCallbacks;

    public interface Callbacks {
        void onContactSelected(ContactEntry ce);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mCallbacks = (Callbacks) context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.contact_list, container, false);

        mContactEntryRecyclerView = (RecyclerView) view.findViewById(R.id.myList);
        mContactEntryRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        updateUI();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUI();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_contact_list, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void updateUI() {
        List<ContactEntry> contactEntries = ContactStore
                .get(getActivity()).getContactEntries();
        if(mAdapter == null) {
            mAdapter = new ContactEntryAdapter(contactEntries);
            mContactEntryRecyclerView.setAdapter(mAdapter);
        }
        mAdapter.notifyDataSetChanged();
    }

    private class ContactEntryHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ContactEntry mContactEntry;

        public ContactEntryHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
        }

        public void bindContactEntry(ContactEntry ce) {
            mContactEntry = ce;
        }

        @Override
        public void onClick(View v) {
            mCallbacks.onContactSelected(mContactEntry);
        }
    }

    private class ContactEntryAdapter extends RecyclerView.Adapter<ContactEntryHolder> {
        private List<ContactEntry> mContactEntries;

        public ContactEntryAdapter(List<ContactEntry> contactEntries) { mContactEntries = contactEntries; }

        @Override
        public ContactEntryHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            View view = layoutInflater
                    .inflate(R.layout.contact_list_item, parent, false);
            return new ContactEntryHolder(view);
        }

        @Override
        public void onBindViewHolder(ContactEntryHolder holder, int position) {
            ContactEntry ce = mContactEntries.get(position);
            holder.bindContactEntry(ce);
        }

        @Override
        public int getItemCount() {
            return mContactEntries.size();
        }

        public void setContactEntries(List<ContactEntry> contactEntries) {
            mContactEntries = contactEntries;
        }
    }
}
