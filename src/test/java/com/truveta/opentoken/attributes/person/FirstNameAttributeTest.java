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

class FirstNameAttributeTest {

    private FirstNameAttribute firstNameAttribute;

    @BeforeEach
    void setUp() {
        firstNameAttribute = new FirstNameAttribute();
    }

    @Test
    void getName_ShouldReturnFirstName() {
        assertEquals("FirstName", firstNameAttribute.getName());
    }

    @Test
    void getAliases_ShouldReturnFirstNameAndGivenName() {
        String[] expectedAliases = { "FirstName", "GivenName" };
        String[] actualAliases = firstNameAttribute.getAliases();
        assertArrayEquals(expectedAliases, actualAliases);
    }

    @Test
    void normalize_ShouldReturnUnchangedValue() {
        String input = "John";
        assertEquals(input, firstNameAttribute.normalize(input));
    }

    @Test
    void normalize_Accent() {
        String name1 = "José";
        String name2 = "Vũ";
        String name3 = "François";
        String name4 = "Renée";
        assertEquals("Jose", firstNameAttribute.normalize(name1));
        assertEquals("Vu", firstNameAttribute.normalize(name2));
        assertEquals("Francois", firstNameAttribute.normalize(name3));
        assertEquals("Renee", firstNameAttribute.normalize(name4));
    }

    @Test
    void validate_ShouldReturnTrueForAnyNonEmptyString() {
        assertTrue(firstNameAttribute.validate("John"));
        assertTrue(firstNameAttribute.validate("Jane Doe"));
        assertTrue(firstNameAttribute.validate("J"));
    }

    @Test
    void validate_ShouldReturnFalseForNullOrEmptyString() {
        assertFalse(firstNameAttribute.validate(null), "Null value should not be allowed");
        assertFalse(firstNameAttribute.validate(""), "Empty value should not be allowed");
        assertTrue(firstNameAttribute.validate("test123"), "Non-empty value should be allowed");
    }

    @Test
    void validate_ShouldReturnFalseForBasicPlaceholderValues() {
        // Test basic placeholder values
        assertFalse(firstNameAttribute.validate("Unknown"), "Unknown should not be allowed");
        assertFalse(firstNameAttribute.validate("N/A"), "N/A should not be allowed");
        assertFalse(firstNameAttribute.validate("None"), "None should not be allowed");
        assertFalse(firstNameAttribute.validate("Test"), "Test should not be allowed");
        assertFalse(firstNameAttribute.validate("Sample"), "Sample should not be allowed");
        assertFalse(firstNameAttribute.validate("Anonymous"), "Anonymous should not be allowed");
    }

    @Test
    void validate_ShouldReturnFalseForMedicalPlaceholderValues() {
        // Test medical/healthcare specific placeholders
        assertFalse(firstNameAttribute.validate("Donor"), "Donor should not be allowed");
        assertFalse(firstNameAttribute.validate("Patient"), "Patient should not be allowed");
        assertFalse(firstNameAttribute.validate("patient not found"), "patient not found should not be allowed");
        assertFalse(firstNameAttribute.validate("patientnotfound"), "patientnotfound should not be allowed");
        assertFalse(firstNameAttribute.validate("<masked>"), "<masked> should not be allowed");
    }

    @Test
    void validate_ShouldReturnFalseForTestingPlaceholderValues() {
        // Test automation/testing specific placeholders
        assertFalse(firstNameAttribute.validate("Automation Test"), "Automation Test should not be allowed");
        assertFalse(firstNameAttribute.validate("Automationtest"), "Automationtest should not be allowed");
        assertFalse(firstNameAttribute.validate("zzztrash"), "zzztrash should not be allowed");
    }

    @Test
    void validate_ShouldReturnFalseForDataAvailabilityPlaceholders() {
        // Test data availability placeholders
        assertFalse(firstNameAttribute.validate("Missing"), "Missing should not be allowed");
        assertFalse(firstNameAttribute.validate("Unavailable"), "Unavailable should not be allowed");
        assertFalse(firstNameAttribute.validate("Not Available"), "Not Available should not be allowed");
        assertFalse(firstNameAttribute.validate("NotAvailable"), "NotAvailable should not be allowed");
    }

    @Test
    void validate_ShouldReturnFalseForCaseInsensitivePlaceholders() {
        // Test case insensitivity (NotInValidator uses equalsIgnoreCase)
        assertFalse(firstNameAttribute.validate("UNKNOWN"), "UNKNOWN (uppercase) should not be allowed");
        assertFalse(firstNameAttribute.validate("unknown"), "unknown (lowercase) should not be allowed");
        assertFalse(firstNameAttribute.validate("UnKnOwN"), "UnKnOwN (mixed case) should not be allowed");
        
        assertFalse(firstNameAttribute.validate("SAMPLE"), "SAMPLE (uppercase) should not be allowed");
        assertFalse(firstNameAttribute.validate("sample"), "sample (lowercase) should not be allowed");
        
        assertFalse(firstNameAttribute.validate("MISSING"), "MISSING (uppercase) should not be allowed");
        assertFalse(firstNameAttribute.validate("missing"), "missing (lowercase) should not be allowed");
    }

    @Test
    void validate_ShouldReturnTrueForValidNames() {
        // Test that legitimate names are still allowed
        assertTrue(firstNameAttribute.validate("John"), "John should be allowed");
        assertTrue(firstNameAttribute.validate("Jane"), "Jane should be allowed");
        assertTrue(firstNameAttribute.validate("Michael"), "Michael should be allowed");
        assertTrue(firstNameAttribute.validate("Sarah"), "Sarah should be allowed");
        assertTrue(firstNameAttribute.validate("José"), "José should be allowed");
        assertTrue(firstNameAttribute.validate("François"), "François should be allowed");
        assertTrue(firstNameAttribute.validate("Mary-Jane"), "Mary-Jane should be allowed");
        assertTrue(firstNameAttribute.validate("Jean-Luc"), "Jean-Luc should be allowed");
    }

    @Test 
    void validate_ShouldReturnTrueForNamesCloseToPlaceholders() {
        // Test names that might be similar to placeholders but are legitimate
        assertTrue(firstNameAttribute.validate("Tester"), "Tester should be allowed (different from Test)");
        assertTrue(firstNameAttribute.validate("Samples"), "Samples should be allowed (different from Sample)");
        assertTrue(firstNameAttribute.validate("Patrice"), "Patrice should be allowed (different from Patient)");
        assertTrue(firstNameAttribute.validate("Ana"), "Ana should be allowed (different from Anonymous)");
    }

    @Test
    void normalize_ThreadSafety() throws InterruptedException {
        final int threadCount = 100;
        final CountDownLatch startLatch = new CountDownLatch(1);
        final CountDownLatch finishLatch = new CountDownLatch(threadCount);
        final CyclicBarrier barrier = new CyclicBarrier(threadCount);
        final List<String> results = Collections.synchronizedList(new ArrayList<>());
        final String testName = "François";

        for (int i = 0; i < threadCount; i++) {
            Thread thread = new Thread(() -> {
                try {
                    startLatch.await(); // Wait for all threads to be ready
                    barrier.await(); // Synchronize to increase contention

                    // Perform normalization
                    String result = firstNameAttribute.normalize(testName);
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
            assertEquals("Francois", result);
        }
    }

    @Test
    void serialization_ShouldPreserveState() throws Exception {
        FirstNameAttribute originalAttribute = new FirstNameAttribute();

        // Serialize the attribute
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(byteOut);
        out.writeObject(originalAttribute);
        out.close();

        // Deserialize the attribute
        ByteArrayInputStream byteIn = new ByteArrayInputStream(byteOut.toByteArray());
        ObjectInputStream in = new ObjectInputStream(byteIn);
        FirstNameAttribute deserializedAttribute = (FirstNameAttribute) in.readObject();
        in.close();

        // Test that both attributes behave identically
        String[] testValues = {
                "John",
                "Mr. John A",
                "Dr. Jane B.",
                "José",
                "François",
                "John-Paul",
                "Mary Jane",
                "Prof. Robert C"
        };

        for (String value : testValues) {
            assertEquals(
                    originalAttribute.getName(),
                    deserializedAttribute.getName(),
                    "Attribute names should match");

            assertArrayEquals(
                    originalAttribute.getAliases(),
                    deserializedAttribute.getAliases(),
                    "Attribute aliases should match");

            assertEquals(
                    originalAttribute.normalize(value),
                    deserializedAttribute.normalize(value),
                    "Normalization should be identical for value: " + value);

            assertEquals(
                    originalAttribute.validate(value),
                    deserializedAttribute.validate(value),
                    "Validation should be identical for value: " + value);
        }
    }
}
