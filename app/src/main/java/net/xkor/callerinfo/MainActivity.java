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

import android.animation.LayoutTransition;
import android.app.Activity;
import android.databinding.BindingAdapter;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.SeekBar;

import net.xkor.callerinfo.databinding.ActivityMainBinding;

public class MainActivity extends Activity {

    private ActivityMainBinding binding;
    private Preferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences = new Preferences(this);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        binding.setPreferences(preferences);

        binding.example.name.setText(R.string.example_name);
        binding.example.info.setText(R.string.example_groups);
        binding.example.photo.setImageDrawable(null);
        binding.example.photo.setBackgroundResource(R.drawable.example_photo);
        binding.example.overlay.getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);
        binding.example.infoContainer.getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);
        binding.content.getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);

        binding.verticalOrientation.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (preferences.isVerticalOrientation() != isChecked) {
                    preferences.setVerticalOrientation(isChecked);
                    binding.invalidateAll();
                }
            }
        });

        binding.showName.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (preferences.isShowName() != isChecked) {
                    preferences.setShowName(isChecked);
                    binding.invalidateAll();
                }
            }
        });

        binding.showGroups.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (preferences.isShowGroups() != isChecked) {
                    preferences.setShowGroups(isChecked);
                    binding.invalidateAll();
                }
            }
        });

        binding.onlyIfPhotoExists.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (preferences.isOnlyIfPhotoExists() != isChecked) {
                    preferences.setOnlyIfPhotoExists(isChecked);
                }
            }
        });

        binding.photoSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && preferences.getPhotoSize() != progress) {
                    preferences.setPhotoSize(progress);
                    binding.invalidateAll();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    @BindingAdapter("app:size")
    public static void setPhotoSize(View view, int size) {
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.width = size;
        layoutParams.height = size;
        view.setLayoutParams(layoutParams);
    }
}
