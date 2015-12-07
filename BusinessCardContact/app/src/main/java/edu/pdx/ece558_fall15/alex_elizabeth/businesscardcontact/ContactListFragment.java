package edu.pdx.ece558_fall15.alex_elizabeth.businesscardcontact;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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

/**
 * ContactListFragment uses a recycler view to display the contact list entries
 *
 * @author Alex Pearson & Elizabeth Reed
 * Date:   December 6th, 2015
 **/
public class ContactListFragment extends Fragment
        implements DialogAsyncTask.Callbacks, DialogLoadCEListTask.ListCallbacks {
    private static final String TAG = "ContactListFragment";

    private List<ContactEntry> mContactEntries; // needed to temporarily store the contact entry list before attaching to the adapter

    private RecyclerView mContactEntryRecyclerView;
    private ContactEntryAdapter mAdapter;

    private Callbacks mCallbacks;

    private DialogLoadCEListTask mDlcelt;   // async task to handle loading of the contact list
    private boolean mNoRefresh = false;     // flag to bypass refresh of the contact list view on resume

    /**
     * Callbacks to the ContactListActivity.
     **/
    public interface Callbacks {
        void onContactSelected(ContactEntry ce);    // occurs when a contact is selected in the contact list
        void onAddBlankContact();   // menu action item: add blank contact
        void onAddNewContactCard(); // menu action item: add contact from a business card image
        void onDisplaySettings();   // menu action item: display settings selection
        void onDisplayAbout();      // menu action item: display about information
    }

    /**
     * Set flag to skip refresh on resume.
     * @param noRefresh true if refresh should be skipped, false otherwise
     */
    public void setNoRefresh(boolean noRefresh) {
        mNoRefresh = noRefresh;
    }

    /**
     * Attach the fragment
     * @param activity  calling activity
     */
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Log.d(TAG, "onAttach");
        mCallbacks = (Callbacks) activity;
    }

    /**
     * Create the activity
     * @param savedInstanceState    Note that the values in this bundle are not used; because the contact list display will be the same.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        setHasOptionsMenu(true);    // options menu should be displayed
    }

    /**
     * Create the view (uses standard parameters for onCreateView)
     * @param inflater  the LayoutInflater
     * @param container the ViewGroup
     * @param savedInstanceState    The values in this bundle are not used, because the display of the contact list will be the same.
     * @return  The inflated view.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        View view = inflater.inflate(R.layout.contact_list, container, false);

        // Find the recycler view, and assign the layout manager
        mContactEntryRecyclerView = (RecyclerView) view.findViewById(R.id.myList);
        mContactEntryRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        return view;
    }

    /**
     * The contact list should be reloaded on resume.  Note that if the noRefresh flag is set,
     * the reloading of the contact list will be skipped.
     */
    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        if(mNoRefresh) {    // if the noRefresh flag is set, then do not load the contact list
            mNoRefresh = false; // and clear the flag.
        } else {
            // start async task to load the contact list
            mDlcelt = new DialogLoadCEListTask(getActivity(), this, this);
            mDlcelt.execute();
        }
    }

    /**
     * Save the current state
     * @param outState  default bundle
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(TAG, "onSaveInstanceState");
    }

    /**
     * Detach the fragment
     */
    @Override
    public void onDetach() {
        super.onDetach();
        Log.d(TAG, "onDetach");
        mCallbacks = null;  // clear callbacks
    }

    /**
     * Destroy the view
     */
    @Override
    public void onDestroyView() {
        Log.d(TAG, "onDestroyView");
        // if the async task to load the contact list exists and the status of the async task is not finished,
        // then need to cancel the task first
        if(mDlcelt != null && mDlcelt.getStatus() != AsyncTask.Status.FINISHED) {
            mDlcelt.cancel(true);
        }
        super.onDestroyView();
    }

    /**
     * Create the options menu
     * @param menu      the current menu
     * @param inflater  the inflater
     */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        Log.d(TAG, "onCreateOptionsMenu");
        inflater.inflate(R.menu.menu_contact_list, menu);   // add the "Add Contact" button options

        // SETTINGS/ABOUT MENU OPTIONS
        inflater.inflate(R.menu.menu_settings, menu);   // add the settings/about button options.
    }

    /**
     * Method to handle selection of a menu action item.
     * @param item  selected menu item
     * @return  Need to return true to display the menu.
     */
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

            case R.id.menu_item_settings:
                // Display the color theme settings dialog
                mCallbacks.onDisplaySettings();
                return true;

            case R.id.menu_item_about:
                // Display the about info dialog
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

    /**
     * ContactEntryHolder is the viewholder for the recycler view.
     */
    private class ContactEntryHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ContactEntry mContactEntry;     // contact entry for which summary should be displayed in the view holder
        private TextView mContactNameView;      // TextView to display the contact name.
        private TextView mContactCompanyView;   // TextView to display the company name.

        /**
         * Constructor for the viewholder.
         * @param itemView  View for the contact list entry.
         */
        public ContactEntryHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            // Find the contact name and company text view objects.
            mContactNameView = (TextView) itemView.findViewById(R.id.contact_list_item_contact_name);
            mContactCompanyView = (TextView) itemView.findViewById(R.id.contact_list_item_company_name);
        }

        /**
         * Bind the list item (set the item view with the values for the given contact entry).
         * @param ce    contact entry with the data to be displayed in the item view
         */
        public void bindContactEntry(ContactEntry ce) {
            mContactEntry = ce;

            // Set the contact name text
            if (mContactNameView != null) {
                String contactName = ce.getName();
                if (contactName != null)
                    mContactNameView.setText(contactName);
            }

            // Set the contact company name text
            if (mContactCompanyView != null) {
                String contactCompany = ce.getCompany();
                if (contactCompany != null)
                    mContactCompanyView.setText(contactCompany);
            }
        }

        /**
         * Set the onClick method for the itemView.
         * @param v current view
         */
        @Override
        public void onClick(View v) {
            mCallbacks.onContactSelected(mContactEntry);    // perform callback to the list activity, pass the attached contact entry
        }
    }

    /**
     * The adapter for the recycler view.
     */
    private class ContactEntryAdapter extends RecyclerView.Adapter<ContactEntryHolder> {
        private List<ContactEntry> mContactEntries; // the list of contact entries

        /**
         * Constructor for the adapter.
         * @param contactEntries    the list of contact entries
         */
        public ContactEntryAdapter(List<ContactEntry> contactEntries) { mContactEntries = contactEntries; }

        /**
         * Create the view holder for the list item
         * @param parent    the parent ViewGroup
         * @param position  the number of the list item
         * @return  The created view holder.
         */
        @Override
        public ContactEntryHolder onCreateViewHolder(ViewGroup parent, int position) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            //LayoutInflater layoutInflater = LayoutInflater.from(getContext());
            View view = layoutInflater
                    .inflate(R.layout.contact_list_item, parent, false);
            return new ContactEntryHolder(view);
        }

        /**
         * When the viewHolder is bound, need to obtain the contact entry data at the given position in the contact entry list.
         * @param holder    The viewHolder object
         * @param position  The index of the item in the contact entry list.
         */
        @Override
        public void onBindViewHolder(ContactEntryHolder holder, int position) {
            ContactEntry ce = mContactEntries.get(position);
            holder.bindContactEntry(ce);
        }

        /**
         * Returns the number of items in the contact entry list.
         * @return  number of list items
         */
        @Override
        public int getItemCount() {
            return mContactEntries.size();
        }

        /**
         * Sets the contact list data for the view holder.
         * @param contactEntries    the list of ContactEntry objects
         */
        public void setContactEntries(List<ContactEntry> contactEntries) {
            mContactEntries = contactEntries;
        }
    }

    /**
     * Method called when an AsyncTask is finished that returns only a single contact entry.
     * @param contactEntry  the singular ContactEntry object obtained in the async task
     * @param success       flag indicating success of async task
     * @param taskId        the id of the async task that completed
     */
    @Override
    public void onAsyncTaskFinished(ContactEntry contactEntry, boolean success, int taskId) {
        Log.d(TAG, "onAsyncTaskFinished");
    }

    /**
     * Method called when an async task finishes that returns a list of ContactEntry objects (i.e. AsyncListTask).
     * @param contactEntries    the list of ContactEntry objects obtained in the async task
     * @param success           flag indicating success of async task
     * @param taskId            the id of the async task that completed
     */
    @Override
    public void onAsyncListTaskDone(List<ContactEntry> contactEntries, boolean success, int taskId) {
        Log.d(TAG, "onAsyncListTaskDone");
        // When the async task to save a contact has been completed, need to verify success.
        // First, need to determine the identity of the async task which has completed.

        if (taskId == DialogLoadCEListTask.TASK_ID) {
            // if the Load Contact List Task is successful, need to update the UI.
            if (success) {
                updateUI(contactEntries);
            }
        }
    }}
