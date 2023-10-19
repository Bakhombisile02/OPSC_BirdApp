package com.example.opsc_birdapp

import android.net.Uri
import android.util.Log
import java.net.MalformedURLException
import java.net.URL

private const val EBIRD_BASE_URL = "https://api.ebird.org/v2/data/obs/region/recent?r=ZA&"
private const val LOC_BASE_URL = "https://nominatim.openstreetmap.org/reverse"

private const val PARAM_API_KEY = "key"
private const val LOGGING_TAG = "URLWECREATED"

fun buildURLForBird(): URL? {
    val buildUri: Uri = Uri.parse(EBIRD_BASE_URL).buildUpon()
        .appendQueryParameter(PARAM_API_KEY, BuildConfig.EBIRD_API_KEY)
        .build()

    return runCatching {
        URL(buildUri.toString())
    }.getOrElse {
        Log.e(LOGGING_TAG, "Error building URL for Bird", it)
        null
    }
}

fun buildURLForLocation(lat: String, lon: String): URL? {
    val buildUri: Uri = Uri.parse(LOC_BASE_URL).buildUpon()
        .appendQueryParameter("lat", lat)
        .appendQueryParameter("lon", lon)
        .appendQueryParameter("format", "geocodejson")
        .build()

    return runCatching {
        URL(buildUri.toString())
    }.getOrElse {
        Log.e(LOGGING_TAG, "Error building URL for Location", it)
        null
    }
}
