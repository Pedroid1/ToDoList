package com.example.todolistkotlin.notifications.local

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.todolistkotlin.R

class LocalNotificationBroadcast : BroadcastReceiver() {

    companion object {
        const val notificationId = 1
        const val channelId = "channel1"
        const val titleExtra = "titleExtra"
        const val messageExtra = "messageExtra"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if(context != null && intent != null) {
            val notification = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(intent.getStringExtra(titleExtra))
                .setContentText(intent.getStringExtra(messageExtra))
                .build()

            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.notify(notificationId, notification)
        }
    }
}