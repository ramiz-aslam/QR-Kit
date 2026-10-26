package com.app.qrkit.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.RemoteViews
import com.app.qrkit.MainActivity
import com.app.qrkit.R

class QrKitWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    companion object {
        fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val views = RemoteViews(context.packageName, R.layout.widget_quick_actions)

            // Scan Action PendingIntent
            val scanIntent = Intent(context, MainActivity::class.java).apply {
                action = "com.app.qrkit.ACTION_SCAN_QR"
                data = Uri.parse("qrkit://action/scan")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                putExtra("shortcut_action", "scan")
            }
            val scanPendingIntent = PendingIntent.getActivity(
                context,
                201,
                scanIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.btn_widget_scan, scanPendingIntent)
            views.setOnClickPendingIntent(R.id.iv_widget_scan, scanPendingIntent)
            views.setOnClickPendingIntent(R.id.tv_widget_scan, scanPendingIntent)

            // Create Action PendingIntent
            val createIntent = Intent(context, MainActivity::class.java).apply {
                action = "com.app.qrkit.ACTION_CREATE_QR"
                data = Uri.parse("qrkit://action/create")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                putExtra("shortcut_action", "create")
            }
            val createPendingIntent = PendingIntent.getActivity(
                context,
                202,
                createIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.btn_widget_create, createPendingIntent)
            views.setOnClickPendingIntent(R.id.iv_widget_create, createPendingIntent)
            views.setOnClickPendingIntent(R.id.tv_widget_create, createPendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
