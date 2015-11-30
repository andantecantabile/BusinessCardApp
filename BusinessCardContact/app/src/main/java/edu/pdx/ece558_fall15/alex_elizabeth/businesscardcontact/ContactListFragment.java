package edu.pdx.ece558_fall15.alex_elizabeth.businesscardcontact;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class ContactListFragment extends Fragment
        implements DialogAsyncTask.Callbacks, DialogLoadCEListTask.ListCallbacks {
    private static final String TAG = "ContactListFragment";

    private List<ContactEntry> mContactEntries; // needed to temporarily store the contact entry list before attaching to the adapter

    private RecyclerView mContactEntryRecyclerView;
    private ContactEntryAdapter mAdapter;
    private Callbacks mCallbacks;

    private DialogLoadCEListTask mDlcelt;
    private boolean mNoRefresh = false;

    public interface Callbacks {
        void onContactSelected(ContactEntry ce);
        void onAddBlankContact();
        void onAddNewContactCard();
        /*
        void onDisplaySettings();   // display settings selection
        */
        void onDisplayAbout();      // display about information
    }

    public void setNoRefresh(boolean noRefresh) {
        mNoRefresh = noRefresh;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Log.d(TAG, "onAttach");
        mCallbacks = (Callbacks) activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        View view = inflater.inflate(R.layout.contact_list, container, false);

        mContactEntryRecyclerView = (RecyclerView) view.findViewById(R.id.myList);
        mContactEntryRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        //updateUI();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        if(mNoRefresh) {
            mNoRefresh = false;
        } else {
            // start async task to load the contact list
            mDlcelt = new DialogLoadCEListTask(getActivity(), this, this);
            mDlcelt.execute();
            //updateUI();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(TAG, "onSaveInstanceState");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.d(TAG, "onDetach");
        mCallbacks = null;
    }

    @Override
    public void onDestroyView() {
        Log.d(TAG, "onDestroyView");
        if(mDlcelt != null && mDlcelt.getStatus() != AsyncTask.Status.FINISHED) {
            mDlcelt.cancel(true);
        }
        super.onDestroyView();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        Log.d(TAG, "onCreateOptionsMenu");
        inflater.inflate(R.menu.menu_contact_list, menu);   // add the "Add Contact" button options

        // SETTINGS/ABOUT MENU OPTIONS
        // check if the detail fragment is active.
        FragmentManager fm = getActivity().getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.detail_fragment_container);
        if(fragment == null) {  // if the detail fragment is not active, then display the menu settings.
            inflater.inflate(R.menu.menu_settings, menu);   // add the settings/about button options.
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(TAG, "onOptionsItemSelected");
        switch (item.getItemId()) {
            case R.id.menu_item_new_blank_contact:
                // User chose the "Add Blank Contact" item, perform callback...
                mCallbacks.onAddBlankContact();
                return true;

            case R.id.menu_item_new_contact_bc:
                // User chose the "Add New Contact Business Card" action
                mCallbacks.onAddNewContactCard();
                return true;

            /*
            // SETTINGS/ABOUT MENU OPTIONS
            case R.id.menu_item_settings:
                mCallbacks.onDisplaySettings();
                return true;
                */

            case R.id.menu_item_about:
                mCallbacks.onDisplayAbout();
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * This method provides a way for the contact list activity to start the async task to load the contact list.
     */
    public void refreshUI() {
        // start async task to load the contact list; only if the adapter already exists.
        if (mAdapter != null) {
            mDlcelt = new DialogLoadCEListTask(getActivity(), this, this);
            mDlcelt.execute();
        }
    }

    /**
     * Provide a default updateUI method, that will notify data set changed for an existing adapter.
     */
    public void updateUI() {
        if (mAdapter != null)
            mAdapter.notifyDataSetChanged();
    }

    /**
     * The main updateUI method, which requires a contact entry list with which to populate the adapter.
     * @param contactEntries List of contact entries to be given to the adapter as its dataset.
     */
    public void updateUI(List<ContactEntry> contactEntries) {
        Log.d(TAG, "updateUI");
        if(mAdapter == null) {
            mAdapter = new ContactEntryAdapter(contactEntries);
            mContactEntryRecyclerView.setAdapter(mAdapter);
        }
        else {
            mAdapter.setContactEntries(contactEntries);
        }
        mAdapter.notifyDataSetChanged();
    }

    private class ContactEntryHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ContactEntry mContactEntry;
        private TextView mContactNameView;
        private TextView mContactCompanyView;

        public ContactEntryHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            mContactNameView = (TextView) itemView.findViewById(R.id.contact_list_item_contact_name);
            mContactCompanyView = (TextView) itemView.findViewById(R.id.contact_list_item_company_name);
        }

        public void bindContactEntry(ContactEntry ce) {
            mContactEntry = ce;

            if (mContactNameView != null) {
                String contactName = ce.getName();
                if (contactName != null)
                    mContactNameView.setText(contactName);
            }

            if (mContactCompanyView != null) {
                String contactCompany = ce.getCompany();
                if (contactCompany != null)
                    mContactCompanyView.setText(contactCompany);
            }
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
        public ContactEntryHolder onCreateViewHolder(ViewGroup parent, int position) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            //LayoutInflater layoutInflater = LayoutInflater.from(getContext());
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

    @Override
    public void onAsyncTaskFinished(ContactEntry contactEntry, boolean success, int taskId) {
        Log.d(TAG, "onAsyncTaskFinished");
        // When the async task to save a contact has been completed, need to verify success.
        // First, determine the identity of the async task which has completed.

        /*
        if (taskId == DialogLoadCEListTask.TASK_ID) {
            // if the Load Contact List Task calls this, do nothing here; just wait for the dialog to be dismissed; it will call the onAsyncListTaskDone method next
            if (success) {
                updateUI();
            }
        }
        */
    }

    @Override
    public void onAsyncListTaskDone(List<ContactEntry> contactEntries, boolean success, int taskId) {
        Log.d(TAG, "onAsyncListTaskDone");
        // When the async task to save a contact has been completed, need to verify success.
        // First, determine the identity of the async task which has completed.

        if (taskId == DialogLoadCEListTask.TASK_ID) {
            // if the Load Contact List Task is successful, need to update the UI.
            if (success) {
                updateUI(contactEntries);
            }
        }
    }


}
