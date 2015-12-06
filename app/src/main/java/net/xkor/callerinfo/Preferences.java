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

import android.content.Context;
import android.content.SharedPreferences;

public class Preferences {
    private static final String KEY_VERTICAL_ORIENTATION = "KEY_VERTICAL_ORIENTATION";
    private static final String KEY_SHOW_NAME = "KEY_SHOW_NAME";
    private static final String KEY_SHOW_GROUPS = "KEY_SHOW_GROUPS";
    private static final String KEY_ONLY_IF_PHOTO_EXISTS = "KEY_ONLY_IF_PHOTO_EXISTS";
    private static final String KEY_PHOTO_SIZE = "KEY_PHOTO_SIZE";

    private SharedPreferences sharedPreferences;

    public Preferences(Context context) {
        sharedPreferences = context.getSharedPreferences(Preferences.class.getSimpleName(), Context.MODE_PRIVATE);
    }

    public boolean isVerticalOrientation() {
        return sharedPreferences.getBoolean(KEY_VERTICAL_ORIENTATION, false);
    }

    public void setVerticalOrientation(boolean value) {
        sharedPreferences.edit().putBoolean(KEY_VERTICAL_ORIENTATION, value).apply();
    }

    public boolean isShowName() {
        return sharedPreferences.getBoolean(KEY_SHOW_NAME, true);
    }

    public void setShowName(boolean value) {
        sharedPreferences.edit().putBoolean(KEY_SHOW_NAME, value).apply();
    }

    public boolean isShowGroups() {
        return sharedPreferences.getBoolean(KEY_SHOW_GROUPS, true);
    }

    public void setShowGroups(boolean value) {
        sharedPreferences.edit().putBoolean(KEY_SHOW_GROUPS, value).apply();
    }

    public boolean isOnlyIfPhotoExists() {
        return sharedPreferences.getBoolean(KEY_ONLY_IF_PHOTO_EXISTS, false);
    }

    public void setOnlyIfPhotoExists(boolean value) {
        sharedPreferences.edit().putBoolean(KEY_ONLY_IF_PHOTO_EXISTS, value).apply();
    }

    public int getPhotoSize() {
        return sharedPreferences.getInt(KEY_PHOTO_SIZE, 5);
    }

    public void setPhotoSize(int value) {
        sharedPreferences.edit().putInt(KEY_PHOTO_SIZE, value).apply();
    }
}
