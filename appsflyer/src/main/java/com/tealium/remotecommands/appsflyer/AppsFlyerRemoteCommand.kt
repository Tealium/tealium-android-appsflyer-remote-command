package com.tealium.remotecommands.appsflyer

import android.app.Application
import android.util.Log
import com.tealium.core.Tealium
import com.tealium.remotecommands.RemoteCommand
import org.json.JSONArray
import org.json.JSONObject
import java.lang.Exception
import java.util.*
import kotlin.collections.HashMap

open class AppsFlyerRemoteCommand : RemoteCommand {

    private val TAG = this::class.java.simpleName

    var tracker: AppsFlyerTrackable
    private var application: Application


    constructor(
        application: Application,
        tealium: Tealium,
        af_dev_key: String? = null,
        commandId: String = DEFAULT_COMMAND_ID,
        description: String = DEFAULT_COMMAND_DESCRIPTION,
        tracker: AppsFlyerTrackable = AppsFlyerTracker(
            application,
            tealium,
            af_dev_key
        )
    ) : super(commandId, description) {
        this.tracker = tracker
        this.application = application
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
    override fun onInvoke(response: Response) {
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
                    initialize(payload)
                }
                Commands.TRACK_LOCATION -> {
                    trackLocation(payload)
                }
                Commands.SET_HOST -> {
                    setHost(payload)
                }
                Commands.SET_USER_EMAILS -> {
                    val emails: JSONArray? = payload.optJSONArray(Customer.EMAILS)
                    emails?.let {
                        val emailList = toList(emails)
                        tracker.setUserEmails(emailList)
                    }
                }
                Commands.SET_CURRENCY_CODE -> {
                    val currencyCode: String = payload.optString(Currency.CODE)

                    if (currencyCode.isNotEmpty()) {
                        tracker.setCurrencyCode(currencyCode)
                    } else {
                        Log.e(
                            TAG,
                            "${Currency.CODE} is required key"
                        )
                    }
                }
                Commands.SET_CUSTOMER_ID -> {
                    val id: String = payload.optString(Customer.USER_ID)
                    if (id.isNotEmpty()) {
                        tracker.setCustomerId(id)
                    } else {
                        Log.e(
                            TAG,
                            "${Customer.USER_ID} is a required key"
                        )
                    }
                }
                Commands.DISABLE_DEVICE_TRACKING -> {
                    val disableTracking: Boolean? =
                        payload.optBoolean(Tracking.DISABLE_DEVICE_TRACKING, false)
                    disableTracking?.let {
                        tracker.disableDeviceTracking(it)
                    } ?: run {
                        Log.e(
                            TAG,
                            "${Tracking.DISABLE_DEVICE_TRACKING} is a required key"
                        )
                    }
                }
                Commands.RESOLVE_DEEPLINK_URLS -> {
                    val deepLinkJsonArray: JSONArray? = payload.optJSONArray(DeepLink.URLS)
                    deepLinkJsonArray?.let {
                        val deepLinkList = toList(it)
                        tracker.resolveDeepLinkUrls(deepLinkList)
                    } ?: run {
                        Log.e(
                            TAG,
                            "${DeepLink.URLS} is a required key"
                        )
                    }
                }
                Commands.STOP_TRACKING -> {
                    val stopTracking: Boolean? = payload.optBoolean(Tracking.STOP_TRACKING)
                    stopTracking?.let {
                        tracker.stopTracking(it)
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

    private fun initialize(payload: JSONObject) {
        val devKey: String = payload.optString(Config.DEV_KEY)
        val config: JSONObject? = payload.optJSONObject(Config.SETTINGS)
        val configSettings: Map<String, Any>? = jsonToMap(config)

        tracker.initialize(devKey, configSettings)
    }

    private fun trackLocation(payload: JSONObject) {
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

    private fun setHost(payload: JSONObject) {
        val host: String = payload.optString(Host.HOST)
        val hostPrefix: String = payload.optString(Host.HOST_PREFIX)

        if (host.isNotEmpty()) {
            if (hostPrefix.isNotEmpty()) {
                tracker.setHost(host, hostPrefix)
            } else {
                tracker.setHost(host)
            }
        } else {
            Log.e(
                TAG,
                "${Host.HOST} are required keys"
            )
        }
    }

    internal fun splitCommands(payload: JSONObject): Array<String> {
        val command = payload.optString(Commands.COMMAND_KEY, "")
        return command.split(Commands.SEPARATOR).map {
            it.trim().toLowerCase(Locale.ROOT)
        }.toTypedArray()
    }

    private fun jsonToMap(jsonObject: JSONObject?): Map<String, Any> {
        val map = HashMap<String, Any>()

        jsonObject?.let {
            it.keys().forEach { key ->
                val value = it[key]
                map[key] = value
            }
        }
        return map
    }

    private fun toList(jsonArray: JSONArray): List<String> {
        val list = mutableListOf<String>()
        for (i in 0 until jsonArray.length()) {
            val item = jsonArray.getString(i)
            list.add(i, item)
        }
        return list
    }
}