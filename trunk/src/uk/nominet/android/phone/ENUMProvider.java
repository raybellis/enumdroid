package uk.nominet.android.phone;

import java.util.ArrayList;

import uk.nominet.DDDS.ENUM;
import uk.nominet.DDDS.Rule;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;

public class ENUMProvider extends ContentProvider {

	/* Log tag */
	static private final String TAG = "ENUMProvider";

	/* exported constants */
	static public final Uri CONTENT_URI = Uri.parse("content://enum/");
	static public final String[] COLUMN_NAMES = {
		"_id", "service", "uri",
	};
	
	/* member variables */
	private ENUM mENUM = null;
	
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		return 0;
	}

	@Override
	public String getType(Uri uri) {
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		return null;
	}

	@Override
	public boolean onCreate() {
		Log.d(TAG, "onCreate() called");
        System.setProperty("dns.server", "208.67.222.222,208.67.220.220");
		return true;
	}

	private String getSuffix() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
		String suffix = null;
		if (prefs.getBoolean(ENUMPrefs.ENUM_PREF_CUSTOM, false)) {
			suffix = prefs.getString(ENUMPrefs.ENUM_PREF_SUFFIX, null);
		}
		if (suffix == null || suffix.length() <= 0) {
			suffix = "e164.arpa";
		}
		return suffix;
	}
	
	protected void parseRule(Rule rule, MatrixCursor c)
	{
		// split service field on '+' token
		String[] services = rule.getService().toLowerCase().split("\\+");
		
		// check that resulting fields are valid
		if (services.length < 2) return;	// not x+y
		if (!services[0].equals("e2u")) return; // not E2U+...
		
		String result = rule.getResult();
		
		for (int i = 1; i < services.length; ++i) {
			// record ID is just the current record count
			Integer id = new Integer(c.getCount());
			Object[] row = new Object[] { id, services[i], result };
			Log.v(TAG, services[i] + " -> " + result);
			c.addRow(row);
		}
	}
	
	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder)
	{
		String number = uri.getPath().substring(1);
		String suffix = getSuffix();
		Log.v(TAG, "looking up " + number + " in " + suffix);

		ArrayList<Rule>rules = new ArrayList<Rule>();
		mENUM = new ENUM(suffix);
    	mENUM.lookup(number, rules);
    	
		MatrixCursor c = new MatrixCursor(COLUMN_NAMES, 10);
		for (Rule rule : rules) {
			parseRule(rule, c);
		}
		
		return c;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		return 0;
	}
}
