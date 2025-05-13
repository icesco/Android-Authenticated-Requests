package net.aliaslab.authenticatedrequests.demo

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.serialization.Serializable
import net.aliaslab.authenticatedrequests.tokenpersistence.SecurePreferencesManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertSame
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class SecurePreferencesManagerTest {

    private lateinit var context: Context
    private val clientId = "testClient"

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        SecurePreferencesManager.clearAll() // pulizia prima di ogni test
    }

    @Test
    fun testHelperStoresAndRetrievesValue() {
        val helper = SecurePreferencesManager.getHelper(context, clientId)
        helper.putString("token", "12345")

        val retrieved = helper.getString("token")
        assertEquals("12345", retrieved)

        // Test rimozione della chiave
        helper.remove("token")
        val afterRemoval = helper.getString("token")
        assertEquals(null, afterRemoval)
    }

    @Test
    fun testHelpersAreCachedByClientId() {
        val helper1 = SecurePreferencesManager.getHelper(context, clientId)
        val helper2 = SecurePreferencesManager.getHelper(context, clientId)

        assertSame(helper1, helper2)
    }

    @Test
    fun testClearHelperRemovesSpecificInstance() {
        val helper = SecurePreferencesManager.getHelper(context, clientId)
        helper.putString("token", "toBeCleared")

        SecurePreferencesManager.clearHelper(clientId)

        val newHelper = SecurePreferencesManager.getHelper(context, clientId)
        val result = newHelper.getString("token") // dovrebbe essere null se il file è nuovo

        assertNotNull(result)
    }

    @Test
    fun testClearAllRemovesAllInstances() {
        val helper1 = SecurePreferencesManager.getHelper(context, "client1")
        val helper2 = SecurePreferencesManager.getHelper(context, "client2")

        helper1.putString("data", "123")
        helper2.putString("data", "456")

        SecurePreferencesManager.clearAll()

        val new1 = SecurePreferencesManager.getHelper(context, "client1")
        val new2 = SecurePreferencesManager.getHelper(context, "client2")

        assertNotNull(new1.getString("data"))
        assertNotNull(new2.getString("data"))
    }

    @Test
    fun testClearAllRemovesAllInstancesButNotData() {
        val helper1 = SecurePreferencesManager.getHelper(context, "client1")
        val helper2 = SecurePreferencesManager.getHelper(context, "client2")

        helper1.putString("data", "123")
        helper2.putString("data", "456")

        SecurePreferencesManager.clearAll()

        val new1 = SecurePreferencesManager.getHelper(context, "client1")
        val new2 = SecurePreferencesManager.getHelper(context, "client2")

        // Gli helper sono nuove istanze
        assert(new1 !== helper1)
        assert(new2 !== helper2)

        // Ma i dati persistono, perché non abbiamo chiamato clear() sui file
        assertEquals("123", new1.getString("data"))
        assertEquals("456", new2.getString("data"))
    }

    @Test
    fun testStoringVariousDataTypes() {
        val helper = SecurePreferencesManager.getHelper(context, clientId)

        // Boolean
        helper.putBoolean("flag", true)
        assertEquals(true, helper.getBoolean("flag"))

        // Int
        helper.putInt("number", 42)
        assertEquals(42, helper.getInt("number"))

        // Oggetto serializzato
        @Serializable
        data class SampleData(val id: Int, val name: String)

        val data = SampleData(1, "Test")
        helper.putObject("sample", data, SampleData.serializer())

        val retrieved = helper.getObject("sample", SampleData.serializer())
        assertEquals(data, retrieved)
    }
}