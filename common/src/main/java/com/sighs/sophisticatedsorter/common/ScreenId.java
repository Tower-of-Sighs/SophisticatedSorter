package com.sighs.sophisticatedsorter.common;

/**
 * Identifier of one screen for the per-screen client options (button visibility toggle and button
 * offsets). A screen is identified by {@code "<screen class>@<title translation key>"} so that
 * screens sharing one class (vanilla chests, barrels, shulker boxes and trapped chests all use
 * {@code ChestScreen}) or one translated title stay distinct; screens whose title is not
 * translatable are identified by the class name only.
 * <p>
 * Matching is deliberately loose so old config entries keep working: a stored entry matches when it
 * equals the full id, the bare title key (the pre-{@code class@key} format) or the bare screen
 * class.
 */
public final class ScreenId {
    private ScreenId() {
    }

    /** Builds the id from the screen class and its title key (class only when the key is null). */
    public static String build(String screenClassName, String titleKey) {
        return titleKey == null ? screenClassName : screenClassName + "@" + titleKey;
    }

    /**
     * Loose match of one stored config entry against the current screen id: equal id, equal
     * title-key part or equal class part all match.
     */
    public static boolean matches(String entry, String screenId) {
        if (entry == null || screenId == null) {
            return false;
        }
        if (screenId.equals(entry)) {
            return true;
        }
        String[] parts = split(screenId);
        if (parts[1] != null) {
            return parts[0].equals(entry) || parts[1].equals(entry);
        }
        return parts[0].equals(entry);
    }

    /** Splits an id into {@code {className, titleKey}}; the key is null for class-only ids. */
    public static String[] split(String screenId) {
        int at = screenId.indexOf('@');
        return at < 0
                ? new String[] {screenId, null}
                : new String[] {screenId.substring(0, at), screenId.substring(at + 1)};
    }
}