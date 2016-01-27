package org.fruct.oss.tsp.search;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.support.annotation.Nullable;

import org.fruct.oss.tsp.events.HistoryEvent;
import org.fruct.oss.tsp.events.HistoryAppendEvent;

import java.util.ArrayList;

import de.greenrobot.event.EventBus;

public class SearchContentProvider extends ContentProvider {
	public SearchContentProvider() {
	}

	private ArrayList<String> searchHistory = new ArrayList<>();

	@Override
	public boolean onCreate() {
		EventBus.getDefault().register(this);
		return true;
	}

	@Nullable
	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		MatrixCursor matrixCursor = new MatrixCursor(
				new String[] {BaseColumns._ID, SearchManager.SUGGEST_COLUMN_TEXT_1,
						SearchManager.SUGGEST_COLUMN_INTENT_EXTRA_DATA}, 1);

		for (String s : searchHistory) {
			matrixCursor.addRow(new Object[] {1, s, s});
		}

		return matrixCursor;
	}

	public void onEvent(HistoryEvent event) {
		searchHistory = new ArrayList<>(event.getPatterns());
	}

	public void onEvent(HistoryAppendEvent event) {
		searchHistory.add(event.getPattern());
	}

	@Nullable
	@Override
	public String getType(Uri uri) {
		return null;
	}

	@Nullable
	@Override
	public Uri insert(Uri uri, ContentValues values) {
		return null;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		return 0;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		return 0;
	}
}
