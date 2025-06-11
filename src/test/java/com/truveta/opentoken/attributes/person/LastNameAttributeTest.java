/**
 * Copyright (c) Truveta. All rights reserved.
 */
package com.truveta.opentoken.attributes.person;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LastNameAttributeTest {

    private LastNameAttribute lastNameAttribute;

    @BeforeEach
    void setUp() {
        lastNameAttribute = new LastNameAttribute();
    }

    @Test
    void getName_ShouldReturnLastName() {
        assertEquals("LastName", lastNameAttribute.getName());
    }

    @Test
    void getAliases_ShouldReturnLastNameAndSurname() {
        String[] expectedAliases = { "LastName", "Surname" };
        String[] actualAliases = lastNameAttribute.getAliases();
        assertArrayEquals(expectedAliases, actualAliases);
    }

    @Test
    void normalize_ShouldReturnUnchangedValue() {
        String input = "Doe";
        assertEquals(input, lastNameAttribute.normalize(input));
    }

    @Test
    void normalize_Accent() {
        String name1 = "Gómez";
        String name2 = "Gutiérrez";
        String name3 = "Hernández";
        String name4 = "Mäder";
        assertEquals("Gomez", lastNameAttribute.normalize(name1));
        assertEquals("Gutierrez", lastNameAttribute.normalize(name2));
        assertEquals("Hernandez", lastNameAttribute.normalize(name3));
        assertEquals("Mader", lastNameAttribute.normalize(name4));
    }

    @Test
    void validate_ShouldReturnTrueForAnyNonEmptyString() {
        assertTrue(lastNameAttribute.validate("Doe"));
        assertTrue(lastNameAttribute.validate("Smith-Jones"));
        assertTrue(lastNameAttribute.validate("D"));
        assertTrue(lastNameAttribute.validate("test123"));
    }

    @Test
    void validate_ShouldReturnFalseForNullOrEmptyString() {
        assertFalse(lastNameAttribute.validate(null), "Null value should not be allowed");
        assertFalse(lastNameAttribute.validate(""), "Empty value should not be allowed");
        assertTrue(lastNameAttribute.validate("test123"), "Non-empty value should be allowed");
    }

    @Test
    void validate_ShouldReturnFalseForBasicPlaceholderValues() {
        // Test basic placeholder values
        assertFalse(lastNameAttribute.validate("Unknown"), "Unknown should not be allowed");
        assertFalse(lastNameAttribute.validate("N/A"), "N/A should not be allowed");
        assertFalse(lastNameAttribute.validate("None"), "None should not be allowed");
        assertFalse(lastNameAttribute.validate("Test"), "Test should not be allowed");
        assertFalse(lastNameAttribute.validate("Sample"), "Sample should not be allowed");
        assertFalse(lastNameAttribute.validate("Anonymous"), "Anonymous should not be allowed");
    }

    @Test
    void validate_ShouldReturnFalseForMedicalPlaceholderValues() {
        // Test medical/healthcare specific placeholders
        assertFalse(lastNameAttribute.validate("Donor"), "Donor should not be allowed");
        assertFalse(lastNameAttribute.validate("Patient"), "Patient should not be allowed");
        assertFalse(lastNameAttribute.validate("patient not found"), "patient not found should not be allowed");
        assertFalse(lastNameAttribute.validate("patientnotfound"), "patientnotfound should not be allowed");
        assertFalse(lastNameAttribute.validate("<masked>"), "<masked> should not be allowed");
    }

    @Test
    void validate_ShouldReturnFalseForTestingPlaceholderValues() {
        // Test automation/testing specific placeholders
        assertFalse(lastNameAttribute.validate("Automation Test"), "Automation Test should not be allowed");
        assertFalse(lastNameAttribute.validate("Automationtest"), "Automationtest should not be allowed");
        assertFalse(lastNameAttribute.validate("zzztrash"), "zzztrash should not be allowed");
    }

    @Test
    void validate_ShouldReturnFalseForDataAvailabilityPlaceholders() {
        // Test data availability placeholders
        assertFalse(lastNameAttribute.validate("Missing"), "Missing should not be allowed");
        assertFalse(lastNameAttribute.validate("Unavailable"), "Unavailable should not be allowed");
        assertFalse(lastNameAttribute.validate("Not Available"), "Not Available should not be allowed");
        assertFalse(lastNameAttribute.validate("NotAvailable"), "NotAvailable should not be allowed");
    }

    @Test
    void validate_ShouldReturnFalseForCaseInsensitivePlaceholders() {
        // Test case insensitivity (NotInValidator uses equalsIgnoreCase)
        assertFalse(lastNameAttribute.validate("UNKNOWN"), "UNKNOWN (uppercase) should not be allowed");
        assertFalse(lastNameAttribute.validate("unknown"), "unknown (lowercase) should not be allowed");
        assertFalse(lastNameAttribute.validate("UnKnOwN"), "UnKnOwN (mixed case) should not be allowed");
        
        assertFalse(lastNameAttribute.validate("SAMPLE"), "SAMPLE (uppercase) should not be allowed");
        assertFalse(lastNameAttribute.validate("sample"), "sample (lowercase) should not be allowed");
        
        assertFalse(lastNameAttribute.validate("MISSING"), "MISSING (uppercase) should not be allowed");
        assertFalse(lastNameAttribute.validate("missing"), "missing (lowercase) should not be allowed");
    }

    @Test
    void validate_ShouldReturnTrueForValidLastNames() {
        // Test that legitimate last names are still allowed
        assertTrue(lastNameAttribute.validate("Smith"), "Smith should be allowed");
        assertTrue(lastNameAttribute.validate("Johnson"), "Johnson should be allowed");
        assertTrue(lastNameAttribute.validate("García"), "García should be allowed");
        assertTrue(lastNameAttribute.validate("O'Connor"), "O'Connor should be allowed");
        assertTrue(lastNameAttribute.validate("van der Berg"), "van der Berg should be allowed");
        assertTrue(lastNameAttribute.validate("Smith-Jones"), "Smith-Jones should be allowed");
        assertTrue(lastNameAttribute.validate("MacDonald"), "MacDonald should be allowed");
        assertTrue(lastNameAttribute.validate("De La Cruz"), "De La Cruz should be allowed");
    }

    @Test 
    void validate_ShouldReturnTrueForLastNamesCloseToPlaceholders() {
        // Test last names that might be similar to placeholders but are legitimate
        assertTrue(lastNameAttribute.validate("Tester"), "Tester should be allowed (different from Test)");
        assertTrue(lastNameAttribute.validate("Samples"), "Samples should be allowed (different from Sample)");
        assertTrue(lastNameAttribute.validate("Patton"), "Patton should be allowed (different from Patient)");
        assertTrue(lastNameAttribute.validate("Anderson"), "Anderson should be allowed (different from Anonymous)");
    }

    @Test
    void normalize_ThreadSafety() throws InterruptedException {
        final int threadCount = 100;
        final CountDownLatch startLatch = new CountDownLatch(1);
        final CountDownLatch finishLatch = new CountDownLatch(threadCount);
        final CyclicBarrier barrier = new CyclicBarrier(threadCount);
        final List<String> results = Collections.synchronizedList(new ArrayList<>());
        final String testName = "Hernández";

        for (int i = 0; i < threadCount; i++) {
            Thread thread = new Thread(() -> {
                try {
                    startLatch.await(); // Wait for all threads to be ready
                    barrier.await(); // Synchronize to increase contention

                    // Perform normalization
                    String result = lastNameAttribute.normalize(testName);
                    results.add(result);

                    finishLatch.countDown();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            thread.start();
        }

        startLatch.countDown(); // Start all threads
        finishLatch.await(15, TimeUnit.SECONDS); // Wait for all threads to complete

        // Verify all threads got the same result
        assertEquals(threadCount, results.size());
        for (String result : results) {
            assertEquals("Hernandez", result);
        }
    }

    @Test
    void testSerialization() throws Exception {
        // Serialize the attribute
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(lastNameAttribute);
        oos.close();

        // Deserialize the attribute
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        LastNameAttribute deserializedAttribute = (LastNameAttribute) ois.readObject();
        ois.close();

        // Test various values with both original and deserialized attributes
        String[] testValues = {
                "Smith",
                "Johnson",
                "García",
                "Gómez",
                "Mäder",
                "van der Berg",
                "O'Connor",
                "Smith-Jones",
                "McDonald"
        };

        for (String value : testValues) {
            assertEquals(
                    lastNameAttribute.getName(),
                    deserializedAttribute.getName(),
                    "Attribute names should match");

            assertArrayEquals(
                    lastNameAttribute.getAliases(),
                    deserializedAttribute.getAliases(),
                    "Attribute aliases should match");

            assertEquals(
                    lastNameAttribute.normalize(value),
                    deserializedAttribute.normalize(value),
                    "Normalization should be identical for value: " + value);

            assertEquals(
                    lastNameAttribute.validate(value),
                    deserializedAttribute.validate(value),
                    "Validation should be identical for value: " + value);
        }
    }
}
