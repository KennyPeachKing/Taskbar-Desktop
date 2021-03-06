/* Copyright 2016 Braden Farmer
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

package com.farmerbb.taskbar.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import com.farmerbb.taskbar.BuildConfig;
import com.farmerbb.taskbar.backup.BackupUtils;
import com.farmerbb.taskbar.backup.IntentBackupAgent;
import com.farmerbb.taskbar.content.TaskbarIntent;
import com.farmerbb.taskbar.util.U;

import java.io.File;
import java.io.IOException;

public class ReceiveSettingsReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // Ignore this broadcast if this is the free version
        if(context.getPackageName().equals(BuildConfig.PAID_APPLICATION_ID)) {
            BackupUtils.restore(context, new IntentBackupAgent(intent));

            // Get custom start button image
            if(intent.hasExtra("custom_image")) {
                Uri uri = intent.getParcelableExtra("custom_image");
                U.importCustomStartButtonImage(context, uri);
            }

            // Finish import
            try {
                File file = new File(context.getFilesDir() + File.separator + "imported_successfully");
                if(file.createNewFile()) {
                    U.sendBroadcast(context, TaskbarIntent.ACTION_IMPORT_FINISHED);
                }
            } catch (IOException e) { /* Gracefully fail */ }
        }
    }
}
