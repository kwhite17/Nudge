package com.ksp.nudge;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.provider.ContactsContract.CommonDataKinds.Phone;


/**
 * Created by kevin on 11/20/17.
 */

public class AutocompleteCursorAdapter extends SimpleCursorAdapter implements Filterable {
    private LayoutInflater cursorInflater;
    private ContentResolver resolver;

    public AutocompleteCursorAdapter(Context context, int layout, Cursor c, String[] from,
                              int[] to, int flags){
        super(context,layout,c,from,to,flags);
        cursorInflater = LayoutInflater.from(context);
        resolver = context.getContentResolver();
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return cursorInflater.inflate(R.layout.autocomplete_contact_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView nameView = view.findViewById(R.id.autocomplete_item_name_id);
        TextView phoneView = view.findViewById(R.id.autocomplete_item_number_id);
        TextView typeView = view.findViewById(R.id.autocomplete_item_type_id);
        ImageView contactImageView = view.findViewById(R.id.autocompleteContactImageId);
        int nameCol = cursor.getColumnIndexOrThrow(Phone.DISPLAY_NAME);
        int typeCol = cursor.getColumnIndex(Phone.TYPE);
        int phoneCol = cursor.getColumnIndex(Phone.NUMBER);
        int imageCol = cursor.getColumnIndex(Phone.PHOTO_THUMBNAIL_URI);
        CharSequence type = Phone.getTypeLabel(view.getResources(), cursor.getInt(typeCol), "");
        String name = cursor.getString(nameCol);
        String phone = cursor.getString(phoneCol);
        String imageUri = cursor.getString(imageCol);

        nameView.setText(name);
        phoneView.setText(phone);
        typeView.setText(type);
        if (imageUri != null) {
            contactImageView.setImageURI(Uri.parse(imageUri));
        }
    }

    @Override
    public Cursor runQueryOnBackgroundThread(CharSequence constraint) {
        // this is how you query for suggestions
        // notice it is just a StringBuilder building the WHERE clause of a cursor which is the used to query for results
        if (getFilterQueryProvider() != null) { return getFilterQueryProvider().runQuery(constraint); }

        StringBuilder buffer = null;
        String[] args = null;
        if (constraint != null) {
            buffer = new StringBuilder();
            buffer.append(Phone.DISPLAY_NAME + " IS NOT NULL AND " + Phone.NORMALIZED_NUMBER + " IS NOT NULL AND ");
            buffer.append("UPPER(");
            buffer.append(Phone.DISPLAY_NAME);
            buffer.append(") GLOB ?");
            args = new String[] { constraint.toString().toUpperCase() + "*" };
        }

        return resolver.query(Phone.CONTENT_URI, null, buffer == null ? null : buffer
                .toString(), args, "display_name ASC");
    }
}
