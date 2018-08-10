package com.westagilelabs.durationpicker;

import android.app.Activity;
import android.content.DialogInterface;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeMap;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import javax.annotation.Nullable;

public class RNDurationPickerModule extends ReactContextBaseJavaModule {
    private static final String ERROR_NO_ACTIVITY = "E_NO_ACTIVITY";
    private static final String FRAGMENT_TAG = "RNDurationPicker";
    private static final String ARG_HOUR = "hour";
    private static final String ARG_MINUTE = "minute";
    private static final String ARG_INTERVAL = "interval";
    private static final String ARG_TITLE = "title";
    private static final String ACTION_SET = "setAction";
    private static final String ACTION_CANCEL = "cancelAction";

    RNDurationPickerModule(ReactApplicationContext reactContext) {
        super(reactContext);
    }

    @Override
    public String getName() {
        return "RNDurationPicker";
    }

    private class TimePickerDialogListener implements TimePickerDialog.OnTimeSetListener, DialogInterface.OnCancelListener {
        private final Promise mPromise;
        private boolean mPromiseResolved = false;

        TimePickerDialogListener(Promise promise) {
            mPromise = promise;
        }

        @Override
        public void onTimeSet(TimePickerDialog view, int hour, int minute, int second) {
            if (!mPromiseResolved && getReactApplicationContext().hasActiveCatalystInstance()) {
                WritableMap result = new WritableNativeMap();
                result.putString("action", ACTION_SET);
                result.putInt("hour", hour);
                result.putInt("minute", minute);
                mPromise.resolve(result);
                mPromiseResolved = true;
            }
        }

        @Override
        public void onCancel(DialogInterface dialogInterface) {
            if (!mPromiseResolved && getReactApplicationContext().hasActiveCatalystInstance()) {
                WritableMap result = new WritableNativeMap();
                result.putString("action", ACTION_CANCEL);
                mPromise.resolve(result);
                mPromiseResolved = true;
            }
        }
    }

    @ReactMethod
    public void open(@Nullable final ReadableMap options, Promise promise) {
        Activity activity = getCurrentActivity();

        if (activity == null) {
            promise.reject(
                    ERROR_NO_ACTIVITY,
                    "Tried to open a duration picker but there is no Activity");
            return;
        }

        int hour = 0;
        int minute = 0;
        int interval = 1;
        String title = null;

        if (options != null) {
            if (options.hasKey(ARG_HOUR) && !options.isNull(ARG_HOUR))
                hour = options.getInt(ARG_HOUR);
            if (options.hasKey(ARG_MINUTE) && !options.isNull(ARG_MINUTE))
                minute = options.getInt(ARG_MINUTE);
            if (options.hasKey(ARG_INTERVAL) && !options.isNull(ARG_INTERVAL))
                interval = options.getInt(ARG_INTERVAL);
            if (options.hasKey(ARG_TITLE) && !options.isNull(ARG_TITLE))
                title = options.getString(ARG_TITLE);
        }

        TimePickerDialogListener listener = new TimePickerDialogListener(promise);
        TimePickerDialog tpd = TimePickerDialog.newInstance(listener, hour, minute, true);
        tpd.setTitle(title);
        tpd.enableSeconds(false);
        tpd.setMinTime(0, interval, 0);
        tpd.setTimeInterval(1, interval, 60);
        tpd.show(activity.getFragmentManager(), FRAGMENT_TAG);
    }
}
