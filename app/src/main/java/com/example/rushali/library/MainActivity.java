package com.example.rushali.library;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import com.example.rushali.library.data.BookContract;
import com.example.rushali.library.data.BookDbHelper;


public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>,SharedPreferences.OnSharedPreferenceChangeListener{

    FloatingActionButton add;
    BookDbHelper mDbHelper;
    BookCursorAdapter mCursorAdapter;
    private static Context context;
    public static final int LOADER_ID = 0;
    String[] PROJECTION = {
            BookContract.BookEntry._ID,
            BookContract.BookEntry.COLUMN_NAME,
            BookContract.BookEntry.COLUMN_BOOKID,
            BookContract.BookEntry.COLUMN_AUTHOR,
            BookContract.BookEntry.COLUMN_PUBLISHER,
            BookContract.BookEntry.COLUMN_TOTALQUANT,
            BookContract.BookEntry.COLUMN_RESQUANT,
            BookContract.BookEntry.COLUMN_RESIDS,
            BookContract.BookEntry.COLUMN_RESERVE
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = getApplicationContext();
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
        setLogin();
        add=(FloatingActionButton)findViewById(R.id.add);

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

               Intent i = new Intent(MainActivity.this,AddBook.class);
               startActivity(i);
               mCursorAdapter.notifyDataSetChanged();
            }
        });

      mDbHelper = new BookDbHelper(this);
      ListView list = (ListView)findViewById(R.id.list);
      mCursorAdapter = new BookCursorAdapter(this,null,0);
      list.setAdapter(mCursorAdapter);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Intent intent = new Intent(MainActivity.this, ProductDetailsActivity.class);
                Log.d("id = ",String.valueOf(id));
                Uri currentProductUri = ContentUris.withAppendedId(BookContract.BookEntry.CONTENT_URI, id);
                intent.setData(currentProductUri);
                startActivity(intent);
            }
        });


        getSupportLoaderManager().initLoader(LOADER_ID,null,this);

    }

    public void change()
    {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(MainActivity.this).edit();
        editor.putBoolean("logout",false);
        editor.apply();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);
    }

    void setLogin()
    {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        if(preferences.getBoolean("logout",true))
        {  change();
        Intent intent = new Intent(this, Login.class);
        startActivity(intent);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if(id==R.id.logout)
        { Intent i = new Intent(MainActivity.this,SettingsActivity.class);
            startActivity(i);
            return true;
        }
        else if(id==R.id.issue)
        {
            Intent intent = new Intent(MainActivity.this,IssueActivity.class);
            startActivity(intent);
        }
        else if(id==R.id.ret)
        {
            Intent intent = new Intent(MainActivity.this,ReturnActivity.class);
            startActivity(intent);
        }
        else if(id==R.id.fine)
        {
            Intent intent = new Intent(MainActivity.this,FineActivity.class);
            startActivity(intent);
        }
        else if(id==R.id.users)
        {

            Intent intent = new Intent(MainActivity.this,UserListActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        return new CursorLoader(this,
                BookContract.BookEntry.CONTENT_URI,
                PROJECTION,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor c) {
        mCursorAdapter.swapCursor(c);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        mCursorAdapter.swapCursor(null);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
       setLogin();
    }
}
