/**
 * Copyright (c) Truveta. All rights reserved.
 */
package com.truveta.opentoken.attributes.person;

import java.util.List;
import java.util.Set;

import com.truveta.opentoken.attributes.BaseAttribute;
import com.truveta.opentoken.attributes.utilities.AttributeUtilities;
import com.truveta.opentoken.attributes.validation.NotInValidator;

/**
 * Represents the first name of a person.
 * 
 * This class extends BaseAttribute and provides functionality for working with
 * first name fields. It recognizes "FirstName" and "GivenName" as valid aliases
 * for this attribute type.
 * 
 * The attribute performs no normalization on input values, returning them
 * unchanged.
 */
public class FirstNameAttribute extends BaseAttribute {

    private static final String NAME = "FirstName";
    private static final String[] ALIASES = new String[] { NAME, "GivenName" };

    public FirstNameAttribute() {
        super(List.of(
                new NotInValidator(
                        Set.of(
                                "Unknown", // Placeholder for unknown first names
                                "N/A", // Not applicable
                                "None", // No first name provided
                                "Test", // Commonly used in testing scenarios
                                "Sample", // Sample data placeholder
                                "Donor", // Placeholder for donor records
                                "Patient", // Placeholder for patient records
                                "Automation Test", // Placeholder for automation tests
                                "Automationtest", // Another variation of automation test
                                "patient not found", // Placeholder for cases where patient data is not found
                                "patientnotfound", // Another variation of patient not found
                                "<masked>", // Placeholder for masked data
                                "Anonymous", // Placeholder for anonymous records
                                "zzztrash", // Placeholder for test or trash data
                                "Missing", // Placeholder for missing data
                                "Unavailable", // Placeholder for unavailable data
                                "Not Available", // Placeholder for data not available
                                "NotAvailable" // Placeholder for data not available (no spaces)
                        ))));
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String[] getAliases() {
        return ALIASES;
    }

    @Override
    public String normalize(String value) {
        return AttributeUtilities.normalize(value);
    }
}
