package com.cpbpc.comms;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ThreadStorage {
    private static Map<String, ThreadLocal<AbbreIntf>> abbreIntfStorage = new ConcurrentHashMap<>();
    private static Map<String, ThreadLocal<PhoneticIntf>> phoneticStorage = new ConcurrentHashMap<>();
    private static Map<String, ThreadLocal<VerseIntf>> verseStorage = new ConcurrentHashMap<>();

    public static AbbreIntf getAbbreviation() {
        String language = AppProperties.getConfig().getProperty("language", "zh");
        ThreadLocal<AbbreIntf> storage = abbreIntfStorage.getOrDefault(language, createAbbre(language));
        return storage.get();
    }
    public static PhoneticIntf getPhonetics() {
        String language = AppProperties.getConfig().getProperty("language", "zh");
        ThreadLocal<PhoneticIntf> storage = phoneticStorage.getOrDefault(language, createPhonetics(language));
        return storage.get();
    }

    public static VerseIntf getVerse() {
        String language = AppProperties.getConfig().getProperty("language", "zh");
        ThreadLocal<VerseIntf> storage = verseStorage.getOrDefault(language, createVerse(language));
        return storage.get();
    }

    private static Object createObj(String language, String className) {
        String packageName = getRootPackageName() + language;
        try {
            Class<?> clazz = Class.forName(packageName + "."+className);
            Object obj = clazz.newInstance();
            return obj;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static ThreadLocal<AbbreIntf> createAbbre(String language) {
        ThreadLocal storage = new ThreadLocal();
        try {
            Object obj = createObj(language, "Abbreviation");
            if (obj instanceof AbbreIntf) {
                AbbreIntf abbreIntf = (AbbreIntf) obj;

                storage.set(abbreIntf);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return storage;
    }

    private static ThreadLocal<PhoneticIntf> createPhonetics(String language) {
        ThreadLocal storage = new ThreadLocal();
        try {
            Object obj = createObj(language, "Phonetics");
            if (obj instanceof PhoneticIntf) {
                PhoneticIntf phoneticIntf = (PhoneticIntf) obj;

                storage.set(phoneticIntf);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return storage;
    }

    private static ThreadLocal<VerseIntf> createVerse(String language) {
        ThreadLocal storage = new ThreadLocal();
        try {
            Object obj = createObj(language, "VerseRegExp");
            if (obj instanceof VerseIntf) {
                VerseIntf verseIntf = (VerseIntf) obj;

                storage.set(verseIntf);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return storage;
    }

    private static String getRootPackageName() {
        return "com.cpbpc.rpgv2.";
    }

}
