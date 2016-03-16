package tk.ty3uk.miuinotificationsound;

import de.robv.android.xposed.XposedHelpers;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

import de.robv.android.xposed.XposedBridge;

import android.os.Bundle;
import android.view.View;
import android.os.Build;

import java.util.Arrays;

public class Main implements IXposedHookLoadPackage {
    final static int STREAM_NOTIFICATION = 5;

    final static String SETTINGS_PACKAGE = "com.android.settings";
    final static String SETTINGS_RINGER_VOLUME_FRAGMENT_CLASS = "com.android.settings.sound.RingerVolumeFragment";
    final static String SETTINGS_RINGER_VOLUME_FRAGMENT_METHOD = "onViewCreated";

    final static String AUDIO_SERVICE_CLASS = "android.media.AudioService";
    final static String AUDIO_SERVICE_CLASS_MM = "com.android.server.audio.AudioService";
    final static String AUDIO_SERVICE_METHOD = "updateStreamVolumeAlias";
    final static String AUDIO_SERVICE_STREAM_VOLUME_ALIAS = "mStreamVolumeAlias";

    public void handleLoadPackage(final LoadPackageParam lpparam) throws Throwable {
        if (lpparam.packageName.equals(SETTINGS_PACKAGE)) {
            final Class<?> RingerVolumeFragment = XposedHelpers.findClass(SETTINGS_RINGER_VOLUME_FRAGMENT_CLASS, lpparam.classLoader);

            XposedHelpers.findAndHookMethod(RingerVolumeFragment, SETTINGS_RINGER_VOLUME_FRAGMENT_METHOD, View.class, Bundle.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    View view = (View) param.args[0];
                    int notificationSection = view.getResources().getIdentifier(SETTINGS_PACKAGE.concat(":id/notification_section"), null, null);

                    XposedBridge.log("notification_section id: " + notificationSection);

                    if (notificationSection > 0) {
                        XposedBridge.log("notification_section visibility: " + view.findViewById(notificationSection).getVisibility());
                        view.findViewById(notificationSection).setVisibility(View.VISIBLE);
                    }
                }
            });
        }

        if (lpparam.packageName.equals("android")) {
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
                final Class<?> AudioService = XposedHelpers.findClass(AUDIO_SERVICE_CLASS_MM, lpparam.classLoader);

                XposedHelpers.findAndHookMethod(AudioService, AUDIO_SERVICE_METHOD, boolean.class, String.class, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        int[] mStreamVolumeAlias = (int[]) XposedHelpers.getObjectField(param.thisObject, AUDIO_SERVICE_STREAM_VOLUME_ALIAS);

                        XposedBridge.log("mStreamVolumeAlias levels BEFORE: " + Arrays.toString(mStreamVolumeAlias));

                        mStreamVolumeAlias[STREAM_NOTIFICATION] = STREAM_NOTIFICATION;

                        XposedBridge.log("mStreamVolumeAlias levels after: " + Arrays.toString(mStreamVolumeAlias));
                    }
                });
            } else {
                final Class<?> AudioService = XposedHelpers.findClass(AUDIO_SERVICE_CLASS, lpparam.classLoader);

                XposedHelpers.findAndHookMethod(AudioService, AUDIO_SERVICE_METHOD, boolean.class, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        int[] mStreamVolumeAlias = (int[]) XposedHelpers.getObjectField(param.thisObject, AUDIO_SERVICE_STREAM_VOLUME_ALIAS);

                        XposedBridge.log("mStreamVolumeAlias levels BEFORE: " + Arrays.toString(mStreamVolumeAlias));

                        mStreamVolumeAlias[STREAM_NOTIFICATION] = STREAM_NOTIFICATION;

                        XposedBridge.log("mStreamVolumeAlias levels after: " + Arrays.toString(mStreamVolumeAlias));
                    }
                });
            }
        }
    }
}