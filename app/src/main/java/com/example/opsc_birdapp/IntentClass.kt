package com.example.opsc_birdapp

import android.content.Context
import android.content.Intent



fun openIntent(context: Context, order: String, activityToOpen: Class<*>, radius: String, pref: String, lanPref: String)
{
    val intent = Intent(context, activityToOpen)
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    intent.putExtra("radius",radius)
    intent.putExtra("prefer",pref)
    intent.putExtra("lanpref",lanPref)
    context.startActivity(intent)

}