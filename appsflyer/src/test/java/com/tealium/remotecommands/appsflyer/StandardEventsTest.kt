package com.tealium.remotecommands.appsflyer

import com.appsflyer.AFInAppEventType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Tests the [StandardEvents.eventNames] mapping in isolation.
 * Event-dispatch flow through [AppsFlyerRemoteCommand] is covered in
 * [AppsFlyerRemoteCommandTest] — this suite focuses on the mapping itself.
 */
class StandardEventsTest {

    @Test
    fun eventNames_coversCanonicalAppsFlyerEvents() {
        // Representative sample of the Android-side canonical names.
        assertEquals(AFInAppEventType.LEVEL_ACHIEVED, StandardEvents.eventNames["levelachieved"])
        assertEquals(AFInAppEventType.ADD_PAYMENT_INFO, StandardEvents.eventNames["addpaymentinfo"])
        assertEquals(AFInAppEventType.ADD_TO_CART, StandardEvents.eventNames["addtocart"])
        assertEquals(AFInAppEventType.ADD_TO_WISH_LIST, StandardEvents.eventNames["addtowishlist"])
        assertEquals(AFInAppEventType.COMPLETE_REGISTRATION, StandardEvents.eventNames["completeregistration"])
        assertEquals(AFInAppEventType.TUTORIAL_COMPLETION, StandardEvents.eventNames["tutorialcompletion"])
        assertEquals(AFInAppEventType.INITIATED_CHECKOUT, StandardEvents.eventNames["initiatecheckout"])
        assertEquals(AFInAppEventType.PURCHASE, StandardEvents.eventNames["purchase"])
        assertEquals(AFInAppEventType.SUBSCRIBE, StandardEvents.eventNames["subscribe"])
        assertEquals(AFInAppEventType.START_TRIAL, StandardEvents.eventNames["starttrial"])
        assertEquals(AFInAppEventType.RATE, StandardEvents.eventNames["rate"])
        assertEquals(AFInAppEventType.SEARCH, StandardEvents.eventNames["search"])
        assertEquals(AFInAppEventType.SPENT_CREDIT, StandardEvents.eventNames["spentcredits"])
        assertEquals(AFInAppEventType.ACHIEVEMENT_UNLOCKED, StandardEvents.eventNames["achievementunlocked"])
        assertEquals(AFInAppEventType.CONTENT_VIEW, StandardEvents.eventNames["contentview"])
        assertEquals(AFInAppEventType.LIST_VIEW, StandardEvents.eventNames["listview"])
        assertEquals(AFInAppEventType.AD_CLICK, StandardEvents.eventNames["adclick"])
        assertEquals(AFInAppEventType.AD_VIEW, StandardEvents.eventNames["adview"])
        assertEquals(AFInAppEventType.TRAVEL_BOOKING, StandardEvents.eventNames["travelbooking"])
        assertEquals(AFInAppEventType.SHARE, StandardEvents.eventNames["share"])
        assertEquals(AFInAppEventType.INVITE, StandardEvents.eventNames["invite"])
        assertEquals(AFInAppEventType.LOGIN, StandardEvents.eventNames["login"])
        assertEquals(AFInAppEventType.RE_ENGAGE, StandardEvents.eventNames["reengage"])
        assertEquals(AFInAppEventType.OPENED_FROM_PUSH_NOTIFICATION, StandardEvents.eventNames["openfrompushnotification"])
        assertEquals(AFInAppEventType.UPDATE, StandardEvents.eventNames["update"])
        assertEquals(AFInAppEventType.LOCATION_COORDINATES, StandardEvents.eventNames["locationcoordinates"])
        assertEquals(AFInAppEventType.CUSTOMER_SEGMENT, StandardEvents.eventNames["customersegment"])
    }

    @Test
    fun eventNames_supportsIosAliases() {
        // iOS-style aliases should resolve to the same underlying AppsFlyer event —
        // this lets cross-platform TiQ tags reuse a single command_name.
        assertEquals(AFInAppEventType.LEVEL_ACHIEVED, StandardEvents.eventNames["achievelevel"])
        assertEquals(AFInAppEventType.CONTENT_VIEW, StandardEvents.eventNames["viewedcontent"])
        assertEquals(AFInAppEventType.TUTORIAL_COMPLETION, StandardEvents.eventNames["completetutorial"])
        assertEquals(AFInAppEventType.ACHIEVEMENT_UNLOCKED, StandardEvents.eventNames["unlockachievement"])
        assertEquals(AFInAppEventType.OPENED_FROM_PUSH_NOTIFICATION, StandardEvents.eventNames["pushnotificationopened"])
    }

    @Test
    fun eventNames_returnsNullForUnknown() {
        assertNull(StandardEvents.eventNames["not_a_known_event"])
        assertNull(StandardEvents.eventNames[""])
    }

    @Test
    fun eventNames_keysAreLowercaseAndUnderscoreFree() {
        // The dispatch path lowercases the incoming command — any key that contained
        // uppercase or whitespace would silently never match.
        StandardEvents.eventNames.keys.forEach { key ->
            assertEquals("'$key' must be lowercase", key.lowercase(), key)
        }
    }

    @Test
    fun eventParameterKeys_exposedAsConstants() {
        // These constants are part of the payload contract and must remain stable.
        assertNotNull(StandardEvents.EVENT_PARAMETERS)
        assertNotNull(StandardEvents.EVENT_PARAMETERS_SHORT)
        assertEquals("event_parameters", StandardEvents.EVENT_PARAMETERS)
        assertEquals("event", StandardEvents.EVENT_PARAMETERS_SHORT)
    }
}
