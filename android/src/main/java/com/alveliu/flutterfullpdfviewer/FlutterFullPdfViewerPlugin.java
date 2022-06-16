package com.alveliu.flutterfullpdfviewer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.view.Display;
import android.widget.FrameLayout;

import java.util.Map;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;

/**
 * FlutterFullPdfViewerPlugin
 */
public class FlutterFullPdfViewerPlugin implements FlutterPlugin, MethodCallHandler, ActivityAware {
    static MethodChannel channel;
    private Activity activity;
    private FlutterFullPdfViewerManager flutterFullPdfViewerManager;

    /**
     * Plugin registration.
     */

    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {

        channel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), "flutter_full_pdf_viewer");
        channel.setMethodCallHandler(this);
    }

    @Override
    public void onAttachedToActivity(ActivityPluginBinding activityPluginBinding) {
        // TODO: your plugin is now attached to an Activity
        activity = activityPluginBinding.getActivity();
    }

    @Override
    public void onDetachedFromActivityForConfigChanges() {
        // TODO: the Activity your plugin was attached to was
        // destroyed to change configuration.
        // This call will be followed by onReattachedToActivityForConfigChanges().
    }

    @Override
    public void onReattachedToActivityForConfigChanges(ActivityPluginBinding activityPluginBinding) {
        // TODO: your plugin is now attached to a new Activity
        // after a configuration change.
    }

    @Override
    public void onDetachedFromActivity() {
        // TODO: your plugin is no longer associated with an Activity.
        // Clean up references.
    }

    @Override
    public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
        switch (call.method) {
            case "launch":
                openPDF(call, result);
                break;
            case "resize":
                resize(call, result);
                break;
            case "close":
                close(call, result);
                break;
            default:
                result.notImplemented();
                break;
        }
    }

    private void openPDF(MethodCall call, MethodChannel.Result result) {
        String path = call.argument("path");
        if (flutterFullPdfViewerManager == null || flutterFullPdfViewerManager.closed) {
            flutterFullPdfViewerManager = new FlutterFullPdfViewerManager(activity);
        }
        FrameLayout.LayoutParams params = buildLayoutParams(call);
        activity.addContentView(flutterFullPdfViewerManager.pdfView, params);
        flutterFullPdfViewerManager.openPDF(path);
        result.success(null);
    }

    private void resize(MethodCall call, final MethodChannel.Result result) {
        if (flutterFullPdfViewerManager != null) {
            FrameLayout.LayoutParams params = buildLayoutParams(call);
            flutterFullPdfViewerManager.resize(params);
        }
        result.success(null);
    }

    private void close(MethodCall call, MethodChannel.Result result) {
        if (flutterFullPdfViewerManager != null) {
            flutterFullPdfViewerManager.close(call, result);
            flutterFullPdfViewerManager = null;
        }
    }

    private FrameLayout.LayoutParams buildLayoutParams(MethodCall call) {
        Map<String, Number> rc = call.argument("rect");
        FrameLayout.LayoutParams params;
        if (rc != null) {
            params = new FrameLayout.LayoutParams(dp2px(activity, rc.get("width").intValue()), dp2px(activity, rc.get("height").intValue()));
            params.setMargins(dp2px(activity, rc.get("left").intValue()), dp2px(activity, rc.get("top").intValue()), 0, 0);
        } else {
            Display display = activity.getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            int width = size.x;
            int height = size.y;
            params = new FrameLayout.LayoutParams(width, height);
        }
        return params;
    }

    private int dp2px(Context context, float dp) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
        channel.setMethodCallHandler(null);
    }
}
