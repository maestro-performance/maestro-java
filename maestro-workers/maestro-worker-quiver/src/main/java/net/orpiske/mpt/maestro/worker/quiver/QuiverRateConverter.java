package net.orpiske.mpt.maestro.worker.quiver;

import net.orpiske.mpt.common.writers.RateDataConverter;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;

public class QuiverRateConverter implements RateDataConverter {
    private static SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");

    @Override
    public String convertEta(String eta) {
        Instant instant = Instant.ofEpochMilli(Long.parseLong(eta));
        Date date = Date.from(instant);

        return formatter.format(date) + "000";
    }

    @Override
    public String convertAta(String ata) {
        Instant instant = Instant.ofEpochMilli(Long.parseLong(ata));
        Date date = Date.from(instant);

        return formatter.format(date) + "000";
    }
}
