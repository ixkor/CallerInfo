/*
 * Copyright 2015 Aleksei Skoriatin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.xkor.callerinfo;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.provider.ContactsContract;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.WindowManager;

import net.xkor.callerinfo.databinding.OverlayBinding;

public class CallReceiver extends BroadcastReceiver {
    private static final String TAG = "CallReceiverTAG";

    private static boolean incomingCall = false;
    private static WindowManager windowManager;
    private static OverlayBinding overlay;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive: " + intent);
        if (intent.getAction().equals(TelephonyManager.ACTION_PHONE_STATE_CHANGED)) {
            String phoneState = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
            if (phoneState.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
                if (!incomingCall) {
                    String phoneNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
                    incomingCall = true;
                    Log.d(TAG, "Incoming call: " + phoneNumber);

                    ContentResolver contentResolver = context.getContentResolver();
                    Uri uri = Uri.withAppendedPath(ContactsContract.CommonDataKinds.Phone.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
                    Cursor cursor = contentResolver.query(uri, null, null, null, null);

                    if (cursor != null && cursor.moveToFirst()) {
                        String contactId = cursor.getString(cursor.getColumnIndex(ContactsContract.Data.CONTACT_ID));
                        String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
//                        CharSequence phoneType = ContactsContract.CommonDataKinds.Phone.getTypeLabel(context.getResources(), cursor.getInt(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE)), "");
                        String photoUri = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.PHOTO_URI));
                        String groups = getGroups(contentResolver, contactId);
                        cursor.close();

                        Preferences preferences = new Preferences(context);
                        if (!preferences.isOnlyIfPhotoExists() || !TextUtils.isEmpty(name) && !TextUtils.isEmpty(photoUri)) {
                            openPanel(context, name, photoUri, groups, preferences);
                        }
                    }
                }
            } else if (phoneState.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
                closePanel();
                Log.d(TAG, "Outgoing call");
            } else if (phoneState.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
                closePanel();
                Log.d(TAG, "End of call");
            }
        }
    }

    private void openPanel(Context context, String name, String photoUri, String groups, Preferences preferences) {
        windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.TYPE_SYSTEM_ALERT |
                WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSPARENT);

        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.format = PixelFormat.TRANSLUCENT;
        params.gravity = Gravity.BOTTOM;

        overlay = DataBindingUtil.inflate(inflater, R.layout.overlay, null, false);
        overlay.setPreferences(preferences);

        overlay.name.setText(name);
        overlay.info.setText(groups);
        if (!TextUtils.isEmpty(photoUri)) {
            overlay.photo.setImageURI(Uri.parse(photoUri));
        }

        windowManager.addView(overlay.getRoot(), params);
    }

    private void closePanel() {
        if (incomingCall && overlay != null) {
            windowManager.removeView(overlay.getRoot());
            overlay.unbind();
            overlay = null;
        }
        incomingCall = false;
    }

    private String getGroups(ContentResolver contentResolver, String contactId) {
        Cursor cursor = contentResolver.query(
                ContactsContract.Data.CONTENT_URI,
                new String[]{
                        ContactsContract.Data.CONTACT_ID,
                        ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID,
                },
                ContactsContract.Data.MIMETYPE + "=? AND " + ContactsContract.Data.CONTACT_ID + "=?",
                new String[]{
                        ContactsContract.CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE,
                        contactId,
                }, null
        );

        if (cursor != null && cursor.moveToFirst()) {
            String[] groupIds = new String[cursor.getCount()];
            String args = "";
            for (int i = 0; i < groupIds.length; i++) {
                groupIds[i] = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID));
                args += "?";
                if (i < groupIds.length - 1) {
                    args += ",";
                }
                cursor.moveToNext();
            }
            cursor.close();

            cursor = contentResolver.query(ContactsContract.Groups.CONTENT_URI,
                    new String[]{
                            ContactsContract.Groups._ID,
                            ContactsContract.Groups.TITLE,
                            ContactsContract.Groups.AUTO_ADD,
                            ContactsContract.Groups.FAVORITES,
                    },
                    ContactsContract.Groups._ID + " IN (" + args + ")",
                    groupIds,
                    null);

            if (cursor != null && cursor.moveToFirst()) {
                String groups = "";
                while (!cursor.isAfterLast()) {
                    if (cursor.getInt(cursor.getColumnIndex(ContactsContract.Groups.AUTO_ADD)) == 1 ||
                            cursor.getInt(cursor.getColumnIndex(ContactsContract.Groups.FAVORITES)) == 1) {
                        cursor.moveToNext();
                        continue;
                    }
                    groups += cursor.getString(cursor.getColumnIndex(ContactsContract.Groups.TITLE));
                    if (cursor.isLast()) {
                        break;
                    }
                    cursor.moveToNext();
                    groups += ", ";
                }
                cursor.close();
                return groups;
            }
        }
        return null;
    }
}