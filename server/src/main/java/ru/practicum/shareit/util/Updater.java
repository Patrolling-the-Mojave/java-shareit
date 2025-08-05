package ru.practicum.shareit.util;

public class Updater {
    public static void runIfNotNull(final Object o, final Runnable runnable) {
        if (o != null) {
            runnable.run();
        }
    }
}

