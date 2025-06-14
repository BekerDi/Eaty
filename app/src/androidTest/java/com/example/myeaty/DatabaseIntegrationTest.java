package com.example.myeaty;

import static org.junit.Assert.*;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class DatabaseIntegrationTest {

    private String dbPath;

    @Before
    public void setUp() {
        Context context = ApplicationProvider.getApplicationContext();
        dbPath = context.getDatabasePath("test_db.db").getAbsolutePath();

        boolean opened = SQLBridge.INSTANCE.nativeOpenDatabase(dbPath);
        assertTrue("Failed to open database", opened);

        SQLBridge.INSTANCE.nativeClearTables();
    }

    @After
    public void tearDown() {
        SQLBridge.INSTANCE.nativeCloseDatabase();
    }

    @Test
    public void testUserOperations() {
        SQLBridge.INSTANCE.nativeSaveUserFullData("testuser", 0, 25, 70, 170, 1, 2, "testpass");

        boolean exists = SQLBridge.INSTANCE.nativeCheckUserExists("testuser");
        assertTrue("User should exist", exists);

        int userId = SQLBridge.INSTANCE.nativeLoginUser("testuser", "testpass");
        assertTrue("Login should return valid userId", userId > 0);

        int lastUserId = SQLBridge.INSTANCE.nativeGetLastUserId();
        assertEquals("Last user id should match inserted user", userId, lastUserId);

        float[] kbju = SQLBridge.INSTANCE.nativeCalculateNutrition(userId, 0, 25, 70, 170, 1, 2);
        assertNotNull("KBJU calculation failed", kbju);
        assertEquals(4, kbju.length);

        float[] fetchedKbju = SQLBridge.INSTANCE.nativeGetKBJUForUser(userId);
        assertNotNull("KBJU fetch failed", fetchedKbju);
        assertEquals(4, fetchedKbju.length);

        UserProfile profile = SQLBridge.INSTANCE.nativeGetUserProfile(userId);
        assertNotNull("User profile should not be null", profile);
        assertEquals("testuser", profile.name);
        assertEquals(25, profile.age);
        assertEquals(70, profile.weight);
        assertEquals(170, profile.height);
    }

    @Test
    public void testProductOperations() {
        SQLBridge.INSTANCE.nativeInitProductDatabase();

        SQLBridge.INSTANCE.nativeInsertProduct("Apple", 52, 0.3f, 0.2f, 14.0f);

        Product[] products = SQLBridge.INSTANCE.nativeGetAllProducts();
        assertNotNull("Products should not be null", products);
        assertTrue("Products should contain at least 1 item", products.length >= 1);

        boolean foundApple = false;
        for (Product p : products) {
            if ("Apple".equals(p.name)) {
                foundApple = true;
                assertEquals(52, (int) p.calories);
                assertEquals(0.3f, p.protein, 0.01f);
                assertEquals(0.2f, p.fat, 0.01f);
                assertEquals(14.0f, p.carbs, 0.01f);
            }
        }
        assertTrue("Apple product should be found", foundApple);
    }

    @Test
    public void testUpdateUserProfile() {
        SQLBridge.INSTANCE.nativeSaveUserFullData("updateuser", 1, 30, 65, 165, 0, 1, "pass123");

        int userId = SQLBridge.INSTANCE.nativeLoginUser("updateuser", "pass123");
        assertTrue("Login should return valid userId", userId > 0);

        SQLBridge.INSTANCE.nativeUpdateUserProfile(userId, 35, 60, 160, 2, 3);

        UserProfile profile = SQLBridge.INSTANCE.nativeGetUserProfile(userId);
        assertNotNull("User profile should not be null", profile);

        assertEquals("updateuser", profile.name);
        assertEquals(35, profile.age);
        assertEquals(60, profile.weight);
        assertEquals(160, profile.height);
        assertEquals(2, profile.goal);
        assertEquals(3, profile.activityLevel);
    }

    @Test
    public void testLoginWithWrongPassword() {
        SQLBridge.INSTANCE.nativeSaveUserFullData("wrongpassuser", 0, 20, 60, 160, 1, 1, "correctpass");

        int userId = SQLBridge.INSTANCE.nativeLoginUser("wrongpassuser", "wrongpass");

        assertEquals("Login with wrong password should fail", -1, userId);
    }

    @Test
    public void testKBJURecalculationAfterProfileUpdate() {
        SQLBridge.INSTANCE.nativeSaveUserFullData("kbjuuser", 0, 28, 75, 175, 0, 2, "pass");

        int userId = SQLBridge.INSTANCE.nativeLoginUser("kbjuuser", "pass");
        assertTrue("Login should return valid userId", userId > 0);

        float[] initialKbju = SQLBridge.INSTANCE.nativeGetKBJUForUser(userId);
        assertNotNull("Initial KBJU should not be null", initialKbju);
        assertEquals(4, initialKbju.length);

        SQLBridge.INSTANCE.nativeUpdateUserProfile(userId, 30, 70, 170, 2, 3);

        float[] updatedKbju = SQLBridge.INSTANCE.nativeGetKBJUForUser(userId);
        assertNotNull("Updated KBJU should not be null", updatedKbju);
        assertEquals(4, updatedKbju.length);

        boolean changed = false;
        for (int i = 0; i < 4; i++) {
            if (initialKbju[i] != updatedKbju[i]) {
                changed = true;
                break;
            }
        }
        assertTrue("KBJU should be recalculated after profile update", changed);
    }
}
