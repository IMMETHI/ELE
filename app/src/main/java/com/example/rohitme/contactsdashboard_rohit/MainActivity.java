package com.example.rohitme.contactsdashboard_rohit;

import android.Manifest;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.GridView;
import android.widget.ProgressBar;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity {
    private static final int CONTACT_PERMISSION_REQ_CODE = 111;
    private List<ContactModel> tempContactList = new ArrayList<>();
    private List<ContactModel> dataList = new ArrayList<>();
    private GridView gridView;
    private GridViewAdapter gridViewAdapter;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        gridView = (GridView) findViewById(R.id.gridview);
        gridViewAdapter = new GridViewAdapter(dataList, this);
        gridView.setAdapter(gridViewAdapter);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS, Manifest.permission.READ_CALL_LOG}, CONTACT_PERMISSION_REQ_CODE);
        else
            new ContactFetchTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == CONTACT_PERMISSION_REQ_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            new ContactFetchTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public void getCallDetails() {
        @SuppressWarnings("deprecation")
        String projection[] = new String[]{
                CallLog.Calls.NUMBER
                , CallLog.Calls.DURATION
        };
        Cursor managedCursor = managedQuery(CallLog.Calls.CONTENT_URI, projection, null, null, CallLog.Calls.NUMBER + " ASC");

        while (managedCursor.moveToNext()) {
            long callDuration = Long.parseLong(managedCursor.getString(1));
            if (callDuration <= 0) continue;
            String phoneNumber = managedCursor.getString(0);
            getContactDetails(phoneNumber, callDuration);
        }
        try {
            mergeDuplicateContact();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        Collections.sort(dataList);
    }

    private void mergeDuplicateContact() throws CloneNotSupportedException {
        long totalTalkTime = 0;
        for (int i = 0; i < tempContactList.size(); i++) {
            totalTalkTime += tempContactList.get(i).totalTalkTime;
            if (i < tempContactList.size() - 1 && !tempContactList.get(i).phoneNumber.equals(tempContactList.get(i + 1).phoneNumber)) {
                ContactModel contactModel = (ContactModel) tempContactList.get(i).clone();
                contactModel.totalTalkTime = totalTalkTime;
                totalTalkTime = 0;
                dataList.add(contactModel);
            }
        }
    }

    public void getContactDetails(String phoneNumber, long callDuration) {
        ContactModel contactModel = new ContactModel();
        ContentResolver cr = getContentResolver();
        String contactNumber = Uri.encode(phoneNumber);
        String projection[] = new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME
                , ContactsContract.PhoneLookup._ID
                , ContactsContract.PhoneLookup.PHOTO_THUMBNAIL_URI
                , ContactsContract.PhoneLookup.LAST_TIME_CONTACTED};
        Cursor contactCursor = cr.query(Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, contactNumber), projection, null, null, null);
        int phoneContactID = -1;
        if (contactCursor != null && contactCursor.getCount() > 0 && contactCursor.moveToFirst())
            phoneContactID = phoneContactID = contactCursor.getInt(contactCursor.getColumnIndexOrThrow(ContactsContract.PhoneLookup._ID));
        if (phoneContactID == -1) return;
        contactModel.name = contactCursor.getString(0);
        contactModel.phoneNumber = phoneNumber;
        contactModel.image_uri = contactCursor.getString(2);
        contactModel.lastContactTime = contactCursor.getString(3);
        contactModel.totalTalkTime = callDuration;
        contactModel.lastContactTime = getFormattedTime(contactModel.lastContactTime);

        Cursor emailCur = cr.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, null, ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?", new String[]{String.valueOf(phoneContactID)}, null);
        String email = "";
        if (emailCur != null && emailCur.getCount() > 0 && emailCur.moveToFirst())
            email = emailCur.getString(emailCur.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
        contactModel.email = email;
        tempContactList.add(contactModel);
        emailCur.close();
        contactCursor.close();
    }

    private String getFormattedTime(String lastContactTime) {
        Calendar calendar = Calendar.getInstance();
        TimeZone tz = TimeZone.getDefault();
        calendar.add(Calendar.MILLISECOND, tz.getOffset(calendar.getTimeInMillis()));
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM HH:mm");
        Date formattedDate = new Date(Long.parseLong(lastContactTime));
        return sdf.format(formattedDate);
    }

    private class ContactFetchTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... params) {
            getCallDetails();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            progressBar.setVisibility(View.GONE);
            gridViewAdapter.notifyDataSetChanged();
        }
    }
}