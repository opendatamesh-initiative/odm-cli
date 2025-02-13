package org.opendatamesh.cli.usecases.importschema.referencehandler;

/**
 * Enum defining different descriptor formats for reference resolution.
 * <p>
 * Available formats:
 * <ul>
 *   <li>{@code CANONICAL} - All references are resolved and incorporated directly within the data product descriptor.</li>
 *   <li>{@code NORMALIZED} - Elements containing references store their full content within the referenced file, keeping only the ref field, while all other fields are set to null.</li>
 * </ul>
 * </p>
 */

public enum DescriptorFormat {
    /**
     * All references are resolved and incorporated directly within the data product descriptor.
     */
    CANONICAL,

    /**
     * Elements containing references store their full content within the referenced file,
     * keeping only their reference field while all other fields are set to null.
     */
    NORMALIZED
}
