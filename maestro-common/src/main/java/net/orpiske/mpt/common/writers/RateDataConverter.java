package net.orpiske.mpt.common.writers;


/**
 * Provides an interface for converting ETA and ATA data before saving into the report
 */
public interface RateDataConverter {
    String convertEta(final String eta);
    String convertAta(final String ata);
}
