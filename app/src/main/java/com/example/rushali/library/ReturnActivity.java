package com.example.rushali.library;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.rushali.library.data.BookContract;
import com.example.rushali.library.data.BookDbHelper;
import com.example.rushali.library.data.UserContract;
import com.example.rushali.library.data.UserDbHelper;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

public class ReturnActivity extends AppCompatActivity {

    EditText book, student;
    Button returns;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_return);

        book = (EditText) findViewById(R.id.bookid);
        student = (EditText) findViewById(R.id.stid);
        returns = (Button) findViewById(R.id.retbook);

        returns.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                returnbook();
            }
        });
    }

    void returnbook()
    {

        String bid = book.getText().toString();
        String uid = student.getText().toString();
        String name="";

        String sql ="SELECT * FROM "+ BookContract.BookEntry.TABLE_NAME+" WHERE bookid = ?";
        BookDbHelper mDbHelper = new BookDbHelper(getApplicationContext());
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Cursor cursor= db.rawQuery(sql,new String[]{bid});
        ArrayList<String> emails = new ArrayList<>();
        if(cursor!=null&&cursor.moveToFirst()) {

            String issueids = cursor.getString(cursor.getColumnIndex(BookContract.BookEntry.COLUMN_RESIDS));
            String reserve = cursor.getString(cursor.getColumnIndex(BookContract.BookEntry.COLUMN_RESERVE));
            String newidlist = " ";
            ArrayList<String> list = new ArrayList<>(Arrays.asList(issueids.split(" ")));
            ArrayList<String> list2 = new ArrayList<>(Arrays.asList(reserve.split(" ")));
            if (!list.contains(uid)) {
                Toast.makeText(getApplicationContext(), uid + " did not borrow book " + bid, Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Log.d("size=",String.valueOf(list2.size()));
                for (int i = 0; i < list.size(); i++) {

                    if (!list.get(i).equals(uid)) {
                        newidlist += list.get(i) + " ";
                    }
                }
                for(int i=0;i<list2.size();i++)
                {
                    Log.d("reserve","hello"+list2.get(i));
                    String sql1 = "SELECT * FROM " + UserContract.UserEntry.TABLE_NAME + " WHERE userid = ?";
                    UserDbHelper DbHelper = new UserDbHelper(getApplicationContext());
                    SQLiteDatabase db1 = DbHelper.getReadableDatabase();
                    String reg = list2.get(i);
                    Cursor c = db1.rawQuery(sql1, new String[]{reg});
                    if (c != null && c.moveToFirst()) {
                        String email = c.getString(c.getColumnIndex(UserContract.UserEntry.COLUMN_EMAIL));
                        emails.add(email);
                    }
                }
                int stock = cursor.getInt(cursor.getColumnIndex(BookContract.BookEntry.COLUMN_RESQUANT));
                stock++;

                int id = cursor.getInt(cursor.getColumnIndex(BookContract.BookEntry._ID));
               name = cursor.getString(cursor.getColumnIndex(BookContract.BookEntry.COLUMN_NAME));
                Uri currentProductUri = ContentUris.withAppendedId(BookContract.BookEntry.CONTENT_URI, id);

                ContentValues values = new ContentValues();
                values.put(BookContract.BookEntry.COLUMN_RESIDS, newidlist);
                values.put(BookContract.BookEntry.COLUMN_RESQUANT, stock);

                int rowsAffected = getContentResolver().update(currentProductUri, values, null, null);
                if (rowsAffected == 0) {
                    Toast.makeText(getApplicationContext(), "Error with updating ", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Updated", Toast.LENGTH_SHORT).show();
                    finish();
                }

                cursor.close();

                String sql1 = "SELECT * FROM " + UserContract.UserEntry.TABLE_NAME + " WHERE userid = ?";
                UserDbHelper DbHelper = new UserDbHelper(getApplicationContext());
                SQLiteDatabase db1 = DbHelper.getReadableDatabase();
                Cursor c = db1.rawQuery(sql1, new String[]{uid});
                if (c != null && c.moveToFirst()) {

                    String issue = c.getString(c.getColumnIndex(UserContract.UserEntry.COLUMN_ISSUED));
                    ArrayList<String> list1 = new ArrayList<>(Arrays.asList(issue.split(" ")));
                    String x = " ";
                    for (int i = 0; i < list1.size(); i++) {
                        if (!bid.equals(list1.get(i)))
                            x += list1.get(i) + " ";
                    }
           /* if(issue.length()==0)
                x+= bid + " ";
            else
                x+=issue+bid+" ";*/

                    int ids = c.getInt(c.getColumnIndex(UserContract.UserEntry._ID));
                    Uri currentProduct = ContentUris.withAppendedId(UserContract.UserEntry.CONTENT_URI, ids);

                    ContentValues val = new ContentValues();
                    val.put(UserContract.UserEntry.COLUMN_ISSUED, x);

                    int rowsA = getContentResolver().update(currentProduct, val, null, null);
                    if (rowsA == 0) {
                        Toast.makeText(getApplicationContext(), "Error with updating ", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "Updated", Toast.LENGTH_SHORT).show();
                        finish();
                    }

                    String[] to = new String[emails.size()];
                    to=emails.toArray(to);
                    Log.d("emails",String.valueOf(emails.size()));
                    Intent intent = new Intent(Intent.ACTION_SENDTO);
                    intent.setData(Uri.parse("mailto:"));
                    intent.putExtra(android.content.Intent.EXTRA_EMAIL,to);
                    intent.putExtra(Intent.EXTRA_SUBJECT, name+" is available now. Hurry up!");
                    intent.putExtra(Intent.EXTRA_TEXT, (Serializable) new StringBuilder());
                    if (intent.resolveActivity(getPackageManager()) != null) {
                        startActivity(intent);
                    }
                }
                else
                {
                    Toast.makeText(getApplicationContext(), "Unknown user", Toast.LENGTH_SHORT).show();
                    finish();
                }
                c.close();
            }
        }
        else
        {
            Toast.makeText(getApplicationContext(), "Unknown book", Toast.LENGTH_SHORT).show();
            finish();
        }



    }
}
