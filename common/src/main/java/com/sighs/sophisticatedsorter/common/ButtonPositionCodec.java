package com.sighs.sophisticatedsorter.common;

/**
 * Parses and formats the per-screen button offset records persisted in each
 * target's config. A record is "screenId|sort X|sort Y|transfer X|transfer Y",
 * where screenId is the screen identifier from {@link ScreenId} ({@code class@title key},
 * loose-matched against the current screen so old title-key-only records keep working).
 */
public final class ButtonPositionCodec {
    private ButtonPositionCodec() {
    }

    /**
     * Parses one record for the requested screen (id or null to only validate the record). Returns
     * null when the record is malformed or does not match the requested screen, which makes
     * hand-edited config files harmless.
     */
    public static ButtonPositions parse(String record, String screenType) {
        if (record == null) {
            return null;
        }
        String[] fields = record.split("\\|", -1);
        if (fields.length != 5 || fields[0].isEmpty()
                || (screenType != null && !ScreenId.matches(fields[0], screenType))) {
            return null;
        }
        try {
            return new ButtonPositions(
                    Integer.parseInt(fields[1]),
                    Integer.parseInt(fields[2]),
                    Integer.parseInt(fields[3]),
                    Integer.parseInt(fields[4]));
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    public static String format(String screenType, ButtonPositions positions) {
        return screenType + "|" + positions.sortX() + "|" + positions.sortY()
                + "|" + positions.transferX() + "|" + positions.transferY();
    }
}
