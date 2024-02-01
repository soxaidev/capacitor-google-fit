package com.adscientiam.capacitor.googlefit;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.util.Log;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import com.getcapacitor.JSArray;
import com.getcapacitor.JSObject;
import com.getcapacitor.NativePlugin;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessActivities;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.Bucket;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.data.Session;
import com.google.android.gms.fitness.data.SleepStages;
import com.google.android.gms.fitness.request.DataDeleteRequest;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.request.SessionInsertRequest;
import com.google.android.gms.fitness.request.SessionReadRequest;
import com.google.android.gms.fitness.result.DataReadResponse;
import com.google.android.gms.fitness.result.SessionReadResponse;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeUnit;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

@CapacitorPlugin(name = "GoogleFit")
@NativePlugin(requestCodes = { GoogleFitPlugin.GOOGLE_FIT_PERMISSIONS_REQUEST_CODE, GoogleFitPlugin.RC_SIGN_IN })
public class GoogleFitPlugin extends Plugin {

    public static final String TAG = "HistoryApi";
    static final int GOOGLE_FIT_PERMISSIONS_REQUEST_CODE = 19849;
    static final int RC_SIGN_IN = 1337;

    private FitnessOptions getFitnessSignInOptions() {
        // FitnessOptions instance, declaring the Fit API data types
        // and access required

        // 変更を加える場合は、connectToGoogleFitも変更する必要がある
        return FitnessOptions
            .builder()
            // .addDataType(DataType.AGGREGATE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
            // .addDataType(DataType.TYPE_DISTANCE_DELTA, FitnessOptions.ACCESS_READ)
            // .addDataType(DataType.AGGREGATE_DISTANCE_DELTA, FitnessOptions.ACCESS_READ)
            // .addDataType(DataType.TYPE_CALORIES_EXPENDED, FitnessOptions.ACCESS_READ) // カロリー
            // .addDataType(DataType.AGGREGATE_CALORIES_EXPENDED, FitnessOptions.ACCESS_READ) // カロリー
            // .addDataType(DataType.TYPE_SPEED, FitnessOptions.ACCESS_READ) // スピード
            // .addDataType(DataType.TYPE_ACTIVITY_SEGMENT, FitnessOptions.ACCESS_READ)
            // .addDataType(DataType.TYPE_HEIGHT, FitnessOptions.ACCESS_READ) // 身長
            // .addDataType(DataType.TYPE_WEIGHT, FitnessOptions.ACCESS_READ) // 体重
            // .addDataType(DataType.TYPE_WEIGHT, FitnessOptions.ACCESS_WRITE) // 体重
            .addDataType(DataType.TYPE_SLEEP_SEGMENT, FitnessOptions.ACCESS_READ) // 睡眠
            .addDataType(DataType.TYPE_SLEEP_SEGMENT, FitnessOptions.ACCESS_WRITE) // 睡眠
            .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ) // 歩数
            .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_WRITE) // 歩数(書き込み)
            .build();
    }

    private GoogleSignInAccount getAccount() {
        return GoogleSignIn.getLastSignedInAccount(getActivity());
    }

    @PluginMethod
    public void disableFit(PluginCall call) {
        Fitness
            .getConfigClient(this.getActivity(), getAccount())
            .disableFit()
            .addOnSuccessListener(task -> call.resolve())
            .addOnFailureListener(e -> call.reject(e.getMessage()));
    }

    @PluginMethod
    public void logoutGoogleFit(PluginCall call) {
        try {
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();
            GoogleSignInClient signInClient = GoogleSignIn.getClient(this.getActivity(), gso);

            signInClient
                .signOut()
                .addOnCompleteListener(
                    this.getActivity(),
                    task -> {
                        if (task.isSuccessful()) {
                            call.resolve();
                        } else {
                            call.reject("Google Fit logout failed");
                        }
                    }
                );
        } catch (Exception e) {
            // 例外発生時のエラーハンドリング
            call.reject("Exception during Google Fit logout: " + e.getMessage());
        }
    }

    private ActivityResultLauncher<Intent> activityResultLauncher;
    private ActivityResultLauncher<Intent> activityResultCallback;

    @Override
    public void load() {
        activityResultLauncher =
            getActivity()
                .registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        int resultCode = result.getResultCode();
                        Intent data = result.getData();

                        GoogleSignInAccount account = getAccount();
                        if (account != null) {
                            if (GoogleSignIn.hasPermissions(account, getFitnessSignInOptions())) {
                                JSObject ret = new JSObject();
                                ret.put("value", "success");
                                notifyListeners("googleFitAllowed", ret);
                            } else {
                                this.requestPermissions();
                            }
                        } else {
                            JSObject ret = new JSObject();
                            ret.put("value", "failure");
                            notifyListeners("googleFitAllowed", ret);
                        }
                    }
                );

        activityResultCallback =
            getActivity()
                .registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        JSObject ret = new JSObject();
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            ret.put("value", "success");
                        } else {
                            ret.put("value", "failure");
                        }
                        notifyListeners("googleFitAllowed", ret);
                    }
                );
    }

    private void requestPermissions() {
        // GoogleSignInOptions を構築
        GoogleSignInOptions signInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .addExtension(getFitnessSignInOptions())
            .build();

        // GoogleSignInClient を取得
        GoogleSignInClient signInClient = GoogleSignIn.getClient(getActivity(), signInOptions);

        // サインインインテントを取得して起動
        Intent signInIntent = signInClient.getSignInIntent();
        activityResultCallback.launch(signInIntent);
    }

    @PluginMethod
    public void connectToGoogleFit(PluginCall call) {
        GoogleSignInAccount account = getAccount();
        if (account == null) {
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();
            GoogleSignInClient signInClient = GoogleSignIn.getClient(this.getActivity(), gso);
            Intent intent = signInClient.getSignInIntent();
            activityResultLauncher.launch(intent);
        } else {
            this.requestPermissions();
        }
        call.resolve();
    }

    @PluginMethod
    public void isGoogleFitInstalled(PluginCall call) {
        Context context = bridge.getActivity().getApplicationContext();
        PackageManager packageManager = context.getPackageManager();
        JSObject result = new JSObject();
        try {
            packageManager.getPackageInfo("com.google.android.apps.fitness", PackageManager.GET_ACTIVITIES);
            result.put("value", true);
        } catch (PackageManager.NameNotFoundException e) {
            result.put("value", false);
        }
        call.resolve(result);
    }

    @PluginMethod
    public void isAllowed(PluginCall call) {
        final JSObject result = new JSObject();
        GoogleSignInAccount account = getAccount();
        if (account != null && GoogleSignIn.hasPermissions(account, getFitnessSignInOptions())) {
            result.put("allowed", true);
        } else {
            result.put("allowed", false);
        }
        call.resolve(result);
    }

    @PluginMethod
    public void openGoogleFit(PluginCall call) {
        Context context = getContext();

        String packageName = "com.google.android.apps.fitness";
        Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(packageName);

        if (launchIntent != null) {
            context.startActivity(launchIntent);
        } else {
            Intent intent = new Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://play.google.com/store/apps/details?id=" + packageName + "&hl=ja")
            );
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            context.startActivity(intent);
        }
    }

    @PluginMethod
    public void isPermissionGranted(PluginCall call) {
        final JSObject result = new JSObject();
        GoogleSignInAccount account = getAccount();
        if (account != null) {
            result.put("allowed", true);
        } else {
            result.put("allowed", false);
        }

        call.resolve(result);
    }

    @PluginMethod
    public Task<DataReadResponse> getHistory(final PluginCall call) throws ParseException {
        GoogleSignInAccount account = getAccount();

        if (account == null) {
            call.reject("No access");
            return null;
        }

        long startTime = dateToTimestamp(call.getString("startTime"));
        long endTime = dateToTimestamp(call.getString("endTime"));

        if (startTime == -1 || endTime == -1) {
            call.reject("Must provide a start time and end time");
            return null;
        }

        DataReadRequest readRequest = new DataReadRequest.Builder()
            .aggregate(DataType.TYPE_STEP_COUNT_DELTA)
            .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
            .bucketByActivitySegment(30, TimeUnit.MINUTES)
            .enableServerQueries()
            .build();

        return Fitness
            .getHistoryClient(getActivity(), account)
            .readData(readRequest)
            .addOnSuccessListener(
                new OnSuccessListener<DataReadResponse>() {
                    @Override
                    public void onSuccess(DataReadResponse dataReadResponse) {
                        List<Bucket> buckets = dataReadResponse.getBuckets();
                        JSONArray days = new JSONArray();
                        for (Bucket bucket : buckets) {
                            JSONObject summary = new JSONObject();
                            try {
                                summary.put("start", timestampToDate(bucket.getStartTime(TimeUnit.MILLISECONDS)));
                                summary.put("end", timestampToDate(bucket.getEndTime(TimeUnit.MILLISECONDS)));
                                List<DataSet> dataSets = bucket.getDataSets();
                                for (DataSet dataSet : dataSets) {
                                    if (dataSet.getDataPoints().size() > 0) {
                                        switch (dataSet.getDataType().getName()) {
                                            case "com.google.distance.delta":
                                                summary.put("distance", dataSet.getDataPoints().get(0).getValue(Field.FIELD_DISTANCE));
                                                break;
                                            case "com.google.speed.summary":
                                                summary.put("speed", dataSet.getDataPoints().get(0).getValue(Field.FIELD_AVERAGE));
                                                break;
                                            case "com.google.calories.expended":
                                                summary.put("calories", dataSet.getDataPoints().get(0).getValue(Field.FIELD_CALORIES));
                                                break;
                                            default:
                                                Log.i(TAG, "need to handle " + dataSet.getDataType().getName());
                                        }
                                    }
                                }
                            } catch (JSONException e) {
                                call.reject(e.getMessage());
                                return;
                            }
                            days.put(summary);
                        }
                        JSObject result = new JSObject();
                        result.put("days", days);
                        call.resolve(result);
                    }
                }
            )
            .addOnFailureListener(
                new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        call.reject(e.getMessage());
                    }
                }
            );
    }

    @PluginMethod
    public Task<DataReadResponse> getHistoryActivity(final PluginCall call) throws ParseException {
        final GoogleSignInAccount account = getAccount();
        if (account == null) {
            call.reject("No access");
            return null;
        }
        long startTime = dateToTimestamp(call.getString("startTime"));
        long endTime = dateToTimestamp(call.getString("endTime"));

        if (startTime == -1 || endTime == -1) {
            call.reject("Must provide a start time and end time");
            return null;
        }

        DataSource stepCountDataSource = new DataSource.Builder()
            .setAppPackageName("com.google.android.gms")
            .setDataType(DataType.TYPE_STEP_COUNT_DELTA)
            .setType(DataSource.TYPE_DERIVED)
            .setStreamName("estimated_steps")
            .build();

        // https://developers.google.com/android/reference/com/google/android/gms/fitness/request/DataReadRequest.Builder
        DataReadRequest readRequest = new DataReadRequest.Builder()
            .aggregate(stepCountDataSource)
            .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
            .enableServerQueries()
            .bucketByTime(30, TimeUnit.MINUTES) // Bucket by 30 minutes interval
            .build();

        return Fitness
            .getHistoryClient(getActivity(), account)
            .readData(readRequest)
            .addOnSuccessListener(
                new OnSuccessListener<DataReadResponse>() {
                    @Override
                    public void onSuccess(DataReadResponse dataReadResponse) {
                        List<Bucket> buckets = dataReadResponse.getBuckets();
                        JSONArray activities = new JSONArray();
                        for (Bucket bucket : buckets) {
                            JSONObject summary = new JSONObject();
                            try {
                                summary.put("start", timestampToDate(bucket.getStartTime(TimeUnit.MILLISECONDS)));
                                summary.put("end", timestampToDate(bucket.getEndTime(TimeUnit.MILLISECONDS)));

                                List<DataSet> dataSets = bucket.getDataSets();

                                for (DataSet dataSet : dataSets) {
                                    if (dataSet.getDataPoints().size() > 0) {
                                        summary.put("steps", dataSet.getDataPoints().get(0).getValue(Field.FIELD_STEPS));
                                    }
                                }
                                summary.put("activity", bucket.getActivity());
                            } catch (JSONException e) {
                                call.reject(e.getMessage());
                                return;
                            }
                            activities.put(summary);
                        }

                        JSObject result = new JSObject();
                        result.put("activities", activities);
                        call.resolve(result);
                    }
                }
            )
            .addOnFailureListener(
                new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        call.reject(e.getMessage());
                    }
                }
            );
    }

    @PluginMethod
    public Task<DataReadResponse> getHistoryActivityPerDay(final PluginCall call) throws ParseException {
        final GoogleSignInAccount account = getAccount();
        if (account == null) {
            call.reject("No access");
            return null;
        }
        long startTime = dateToTimestamp(call.getString("startTime"));
        long endTime = dateToTimestamp(call.getString("endTime"));

        if (startTime == -1 || endTime == -1) {
            call.reject("Must provide a start time and end time");
            return null;
        }

        DataSource stepCountDataSource = new DataSource.Builder()
            .setAppPackageName("com.google.android.gms")
            .setDataType(DataType.TYPE_STEP_COUNT_DELTA)
            .setType(DataSource.TYPE_DERIVED)
            .setStreamName("estimated_steps")
            .build();

        // https://developers.google.com/android/reference/com/google/android/gms/fitness/request/DataReadRequest.Builder
        DataReadRequest readRequest = new DataReadRequest.Builder()
            .aggregate(stepCountDataSource)
            .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
            .bucketByTime(1, TimeUnit.DAYS)
            .build();

        return Fitness
            .getHistoryClient(getActivity(), account)
            .readData(readRequest)
            .addOnSuccessListener(
                new OnSuccessListener<DataReadResponse>() {
                    @Override
                    public void onSuccess(DataReadResponse dataReadResponse) {
                        List<Bucket> buckets = dataReadResponse.getBuckets();
                        JSONArray activities = new JSONArray();
                        for (Bucket bucket : buckets) {
                            JSONObject summary = new JSONObject();
                            try {
                                summary.put("start", timestampToDate(bucket.getStartTime(TimeUnit.MILLISECONDS)));
                                summary.put("end", timestampToDate(bucket.getEndTime(TimeUnit.MILLISECONDS)));

                                List<DataSet> dataSets = bucket.getDataSets();

                                for (DataSet dataSet : dataSets) {
                                    if (dataSet.getDataPoints().size() > 0) {
                                        summary.put("steps", dataSet.getDataPoints().get(0).getValue(Field.FIELD_STEPS));
                                    }
                                }
                                summary.put("activity", bucket.getActivity());
                            } catch (JSONException e) {
                                call.reject(e.getMessage());
                                return;
                            }
                            activities.put(summary);
                        }

                        JSObject result = new JSObject();
                        result.put("activities", activities);
                        call.resolve(result);
                    }
                }
            )
            .addOnFailureListener(
                new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        call.reject(e.getMessage());
                    }
                }
            );
    }

    @PluginMethod
    public Task<DataReadResponse> readSleepData(final PluginCall call) throws ParseException {
        final GoogleSignInAccount account = getAccount();

        if (account == null) {
            call.reject("No access");
            return null;
        }

        long startTime = dateToTimestamp(call.getString("startTime"));
        long endTime = dateToTimestamp(call.getString("endTime"));

        SessionReadRequest request = new SessionReadRequest.Builder()
            .readSessionsFromAllApps()
            .includeSleepSessions()
            .read(DataType.TYPE_SLEEP_SEGMENT)
            .setTimeInterval(startTime, endTime, TimeUnit.MILLISECONDS)
            .build();

        Fitness
            .getSessionsClient(getActivity(), account)
            .readSession(request)
            .addOnSuccessListener(
                response -> {
                    JSObject ret = new JSObject();
                    JSONArray sessions = new JSONArray();

                    for (Session session : response.getSessions()) {
                        JSONObject sessionObj = new JSONObject();

                        long sessionStart = session.getStartTime(TimeUnit.MILLISECONDS);
                        long sessionEnd = session.getEndTime(TimeUnit.MILLISECONDS);
                        String sessionSleepStageVal = session.getActivity();
                        try {
                            sessionObj.put("start", timestampToDate(sessionStart));
                            sessionObj.put("end", timestampToDate(sessionEnd));
                            sessionObj.put("stage", sessionSleepStageVal);
                            sessionObj.put("detail", new JSONArray());
                        } catch (JSONException e) {
                            call.reject(e.getMessage());
                            return;
                        }

                        List<DataSet> dataSets = response.getDataSet(session);
                        for (DataSet dataSet : dataSets) {
                            for (DataPoint point : dataSet.getDataPoints()) {
                                // https://developers.google.com/fit/scenarios/read-sleep-data?hl=ja
                                int sleepStageVal = point.getValue(Field.FIELD_SLEEP_SEGMENT_TYPE).asInt();

                                long segmentStart = point.getStartTime(TimeUnit.MILLISECONDS);
                                long segmentEnd = point.getEndTime(TimeUnit.MILLISECONDS);

                                JSONObject dataSetObj = new JSONObject();
                                try {
                                    dataSetObj.put("start", timestampToDate(segmentStart));
                                    dataSetObj.put("end", timestampToDate(segmentEnd));
                                    dataSetObj.put("stage", sleepStageVal);
                                    sessionObj.put("detail", dataSetObj);
                                } catch (JSONException e) {
                                    call.reject(e.getMessage());
                                    return;
                                }
                            }
                        }
                        sessions.put(sessionObj);
                    }

                    ret.put("value", sessions);
                    call.resolve(ret);
                }
            )
            .addOnFailureListener(
                e -> {
                    call.reject(e.getMessage());
                }
            );

        return null;
    }

    public static class SleepType {

        public long start;
        public long end;
        public int stage;

        // Constructor updated to accept long for start and end
        public SleepType(long start, long end, int stage) {
            this.start = start;
            this.end = end;
            this.stage = stage;
        }
    }

    private SleepType convertJsonObjectToSleepType(JSONObject jsonObject) throws ParseException {
        try {
            long startTime = dateToTimestamp(jsonObject.getString("start"));
            long endTime = dateToTimestamp(jsonObject.getString("end"));
            int stage = jsonObject.getInt("stage");
            return new SleepType(startTime, endTime, stage); // Corrected the class name
        } catch (JSONException e) {
            throw new ParseException(e.getMessage(), 0);
        }
    }

    @PluginMethod
    public Task<DataReadResponse> setWriteSleepData(final PluginCall call) throws ParseException {
        final GoogleSignInAccount account = getAccount();

        if (account == null) {
            call.reject("No access");
            return null;
        }

        long startTime = dateToTimestamp(call.getString("startTime"));
        long endTime = dateToTimestamp(call.getString("endTime"));
        String id = call.getString("id");
        JSArray sleepSegmentJsonList = call.getArray("details");

        List<SleepType> sleepSegmentList = new ArrayList<>();
        for (int i = 0; i < sleepSegmentJsonList.length(); i++) {
            try {
                JSONObject jsonObject = sleepSegmentJsonList.getJSONObject(i);
                sleepSegmentList.add(convertJsonObjectToSleepType(jsonObject));
            } catch (JSONException e) {
                call.reject(e.getMessage());
                return null;
            }
        }

        List<DataPoint> dataPoints = new ArrayList<>();

        sleepSegmentList.forEach(
            sleepSegment -> {
                DataSource sleepDataSource = new DataSource.Builder()
                    .setDataType(DataType.TYPE_SLEEP_SEGMENT)
                    .setType(DataSource.TYPE_RAW)
                    .build();

                int sleep = SleepStages.SLEEP;
                if (sleepSegment.stage == -1) {
                    sleep = SleepStages.AWAKE;
                } else if (sleepSegment.stage == 0) {
                    sleep = SleepStages.SLEEP_REM;
                } else if (sleepSegment.stage == 1 || sleepSegment.stage == 2) {
                    sleep = SleepStages.SLEEP_LIGHT;
                } else if (sleepSegment.stage == 3) {
                    sleep = SleepStages.SLEEP_DEEP;
                } else {
                    sleep = SleepStages.OUT_OF_BED;
                }

                DataPoint singleDataPoint = DataPoint
                    .builder(sleepDataSource)
                    .setTimeInterval(sleepSegment.start, sleepSegment.end, TimeUnit.MILLISECONDS)
                    .setField(Field.FIELD_SLEEP_SEGMENT_TYPE, sleep)
                    .build();

                dataPoints.add(singleDataPoint);
            }
        );

        // int sleep = SleepStages.SLEEP;

        new AlertDialog.Builder(getActivity())
            .setTitle("データの書き込み")
            .setMessage("データを書き込みますか？")
            .setPositiveButton("OK", null)
            .show();

        DataSource sleepDataSource = new DataSource.Builder().setDataType(DataType.TYPE_SLEEP_SEGMENT).setType(DataSource.TYPE_RAW).build();

        // DataPoint singleDataPoint = DataPoint
        //     .builder(sleepDataSource)
        //     .setTimeInterval(startTime, endTime, TimeUnit.MILLISECONDS)
        //     .setField(Field.FIELD_SLEEP_SEGMENT_TYPE, sleep)
        //     .build();

        DataSet dataSet = DataSet.builder(sleepDataSource).addAll(dataPoints).build();

        Session session = new Session.Builder()
            .setIdentifier(id)
            .setStartTime(startTime, TimeUnit.MILLISECONDS) // From first segment
            .setEndTime(endTime, TimeUnit.MILLISECONDS) // From last segment
            .setActivity(FitnessActivities.SLEEP)
            .build();

        SessionInsertRequest request = new SessionInsertRequest.Builder().setSession(session).addDataSet(dataSet).build();

        Fitness
            .getSessionsClient(getActivity(), account)
            .insertSession(request)
            .addOnSuccessListener(
                unused -> {
                    JSObject ret = new JSObject();
                    ret.put("value", "success");
                    call.resolve(ret);
                }
            )
            .addOnFailureListener(
                e -> {
                    call.reject(e.getMessage());
                }
            );

        return null;
    }

    private String timestampToDate(long timestamp) {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timestamp);
        return df.format(cal.getTime());
    }

    private long dateToTimestamp(String date) {
        if (date.isEmpty()) {
            return -1;
        }
        SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        try {
            return f.parse(date).getTime();
        } catch (ParseException e) {
            return -1;
        }
    }

    @PluginMethod
    public Task<DataReadResponse> writeStepCountData(final PluginCall call) throws ParseException {
        final GoogleSignInAccount account = getAccount();

        if (account == null) {
            call.reject("No access");
            return null;
        }

        long startTime = dateToTimestamp(call.getString("startTime"));
        long endTime = dateToTimestamp(call.getString("endTime"));
        int stepCount = call.getInt("value");

        DataSource stepCountDataSource = new DataSource.Builder()
            .setDataType(DataType.TYPE_STEP_COUNT_DELTA)
            .setType(DataSource.TYPE_RAW)
            .setAppPackageName(getActivity())
            .build();

        DataPoint stepCountDataPoint = DataPoint
            .builder(stepCountDataSource)
            .setTimeInterval(startTime, endTime, TimeUnit.MILLISECONDS)
            .setField(Field.FIELD_STEPS, stepCount)
            .build();

        DataSet stepCountDataSet = DataSet.builder(stepCountDataSource).add(stepCountDataPoint).build();

        JSObject ret = new JSObject();
        ret.put("value", "success");

        DataDeleteRequest deleteRequest = new DataDeleteRequest.Builder()
            .setTimeInterval(startTime, endTime, TimeUnit.MILLISECONDS)
            .addDataType(DataType.TYPE_STEP_COUNT_DELTA)
            .build();

        Fitness
            .getHistoryClient(getActivity(), account)
            .deleteData(deleteRequest)
            .addOnSuccessListener(
                unused -> {
                    Fitness
                        .getHistoryClient(getActivity(), account)
                        .insertData(stepCountDataSet)
                        .addOnSuccessListener(
                            session1 -> {
                                call.resolve(ret);
                            }
                        )
                        .addOnFailureListener(e -> call.reject(e.getMessage()));
                }
            )
            .addOnFailureListener(
                e -> {
                    call.reject(e.getMessage());
                }
            );

        return null;
    }
}
