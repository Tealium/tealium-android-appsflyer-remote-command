package com.tealium.remotecommands.appsflyer

import android.app.Application
import android.util.Log
import com.tealium.internal.tagbridge.RemoteCommand
import org.json.JSONArray
import org.json.JSONObject
import java.lang.Exception
import java.util.*
import kotlin.collections.HashMap

open class AppsFlyerRemoteCommand : RemoteCommand {

    private val TAG = this::class.java.simpleName

    lateinit var tracker: AppsFlyerTrackable
    var application: Application? = null

    @JvmOverloads
    constructor(
        application: Application? = null,
        commandId: String = DEFAULT_COMMAND_ID,
        description: String = DEFAULT_COMMAND_DESCRIPTION,
        appsFlyerDevKey: String? = null,
        configSettings: Map<String, String>? = null
    ) : super(commandId, description) {
        application?.let { app ->
            appsFlyerDevKey?.let { devKey ->
                tracker = AppsFlyerTracker(app, devKey, configSettings)
                this.application = app
            }
        }
    }

    companion object {
        const val DEFAULT_COMMAND_ID = "appsflyer"
        const val DEFAULT_COMMAND_DESCRIPTION = "Tealium-AppsFlyer Remote Command"
    }

    /**
     * Triggered by RemoteCommand response data and parses commands to execute
     *
     * @param response - response containing commands and payload to process
     */
    @Throws(Exception::class)
    public override fun onInvoke(response: Response) {
        val payload = response.requestPayload
        val commands = splitCommands(payload)
        parseCommands(commands, payload)
    }

    /**
     * Process commands and parameter data with each command
     *
     * @param commands - list of command names to be processed
     * @param payload - parameter data to process with command name
     */
    fun parseCommands(commands: Array<String>, payload: JSONObject) {
        commands.forEach { command ->
            when (command) {
                Commands.INITIALIZE -> {
                    initializeAppsFlyer(payload)
                }
                Commands.LAUNCH -> {
                    tracker.trackLaunch()
                }
                Commands.TRACK_LOCATION -> {
                    trackLocation(payload)
                }
                Commands.SET_HOST -> {
                    setHost(payload)
                }
                Commands.SET_USER_EMAILS -> {
                    val emails: JSONArray? = payload.optJSONArray(User.USER_EMAILS)
                    emails?.let {
                        val emailList = toList(emails)
                        tracker.setUserEmails(emailList)
                    }
                }
                Commands.SET_CURRENCY_CODE -> {
                    val currencyCode: String? = payload.optString(Currency.CURRENCY_CODE)

                    currencyCode?.let {
                        tracker.setCurrencyCode(it)
                    } ?: run {
                        Log.e(
                            TAG,
                            "${Currency.CURRENCY_CODE} is required key"
                        )
                    }
                }
                Commands.SET_CUSTOMER_ID -> {
                    val id: String? = payload.optString(User.CUSTOMER_USER_ID, null)
                    id?.let {
                        tracker.setCustomerId(it)
                    } ?: run {
                        Log.e(
                            TAG,
                            "${User.CUSTOMER_USER_ID} is a required key"
                        )
                    }
                }
                Commands.DISABLE_TRACKING -> {
                    val disableTracking: Boolean? =
                        payload.optBoolean(Privacy.DISABLE_TRACKING, false)
                    disableTracking?.let {
                        tracker.disableTracking(it)
                    } ?: run {
                        Log.e(
                            TAG,
                            "${Privacy.DISABLE_TRACKING} is a required key"
                        )
                    }
                }
                Commands.RESOLVE_DEEPLINK_URLS -> {
                    val deeplinks: JSONArray? = payload.optJSONArray(Deeplinking.DEEPLINK_URLS)
                    deeplinks?.let {
                        val deeplinkList = toList(it)
                        tracker.resolveDeeplinksUrls(deeplinkList)
                    } ?: run {
                        Log.e(
                            TAG,
                            "${Deeplinking.DEEPLINK_URLS} is a required key"
                        )
                    }
                }
                else -> {
                    standardEvent(command)?.let { eventType ->
                        val eventParameters: JSONObject? =
                            payload.optJSONObject(StandardEvents.EVENT_PARAMETERS)
                        if (eventParameters != null) {
                            val paramsMap = jsonToMap(eventParameters)
                            tracker.trackEvent(eventType, paramsMap)
                        } else {
                            tracker.trackEvent(eventType)
                        }
                    }
                }
            }
        }
    }

    /**
     * Validate and return standard event name for AppsFlyer events
     *
     * @param commandName - name of Tealium command name
     */
    fun standardEvent(commandName: String): String? {
        return StandardEvents.eventNames[commandName]
    }

    fun initializeAppsFlyer(payload: JSONObject) {
        val devKey: String? = payload.optString(Initialize.AF_DEV_KEY)
        val config: JSONObject? = payload.optJSONObject(Config.SETTINGS)
        val configSettings: Map<String, Any>? = jsonToMap(config)
        application?.let { appContext ->
            devKey?.let { devKey ->
                configSettings?.let {
                    tracker = AppsFlyerTracker(appContext, devKey, it)
                }
                tracker = AppsFlyerTracker(appContext, devKey)
            }
        } ?: run {
            Log.e(
                TAG,
                "${Initialize.AF_DEV_KEY} is a required key"
            )
        }
    }

    fun trackLocation(payload: JSONObject) {
        val latitude: Double? = payload.optDouble(Location.LATITUDE)
        val longitude: Double? = payload.optDouble(Location.LONGITUDE)

        latitude?.let {
            longitude?.let {
                tracker.trackLocation(latitude, longitude)
            }
        } ?: run {
            Log.e(
                TAG,
                "${Location.LATITUDE} and ${Location.LONGITUDE} are required keys"
            )
        }
    }

    fun setHost(payload: JSONObject) {
        val host: String? = payload.optString(Host.HOST)
        val hostPrefix: String? = payload.optString(Host.HOST_PREFIX)

        host?.let { hostName ->
            if (hostPrefix != "") {
                tracker.setHost(hostName, hostPrefix)
            } else {
                tracker.setHost(hostName)
            }
        } ?: run {
            Log.e(
                TAG,
                "${Host.HOST} are required keys"
            )
        }
    }

    fun splitCommands(payload: JSONObject): Array<String> {
        val command = payload.optString(Commands.COMMAND_KEY, "")
        return command.split(Commands.SEPARATOR).map {
            it.trim().toLowerCase(Locale.ROOT)
        }.toTypedArray()
    }

    fun jsonToMap(jsonObject: JSONObject?): Map<String, Any> {
        val map = HashMap<String, Any>()

        jsonObject?.let {
            it.keys().forEach { key ->
                val value = it[key]
                map[key] = value
            }
        }
        return map
    }

    fun toList(jsonArray: JSONArray): List<String> {
        val list = mutableListOf<String>()
        for (i in 0 until jsonArray.length()) {
            val item = jsonArray.getString(i)
            list.add(i, item)
        }
        return list
    }
}