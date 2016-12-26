package com.abcew.camera.utils;

import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.abcew.camera.ImgSdk;

import java.util.Set;

/**
 * Created by laputan on 16/11/2.
 */
public class PrefManger<T> {
    private enum TYPE {BOOLEAN, INTEGER, LONG, FLOAT, STRING, STRING_SET, ENUM}

    private static final String PREFERENCES_NAME = "imgLyPreferences";

    private static SharedPreferences preferences;

    private PrefManger() {
        if (preferences == null) {
            preferences = ImgSdk.getAppContext().getSharedPreferences(PREFERENCES_NAME, 0);
        }
    }

    @NonNull
    @SuppressWarnings("unchecked")
    private T get(@NonNull PropertyConfig property) {
        final Object result;
        switch (property.type) {
            case BOOLEAN:
                result = preferences.getBoolean(property.name, (Boolean) property.value);
                break;
            case INTEGER:
                result = preferences.getInt(property.name, (Integer) property.value);
                break;
            case LONG:
                result = preferences.getLong(property.name, (Long) property.value);
                break;
            case FLOAT:
                result = preferences.getFloat(property.name, (Float) property.value);
                break;
            case STRING:
                result = preferences.getString(property.name, (String) property.value);
                break;
            case STRING_SET:
                result = preferences.getStringSet(property.name, (Set<String>) property.value);
                break;
            case ENUM:
                result = preferences.getString(property.name, ((Enum) property.value).name());
                break;

            default: throw new RuntimeException("Unsupported Type");
        }

        return (T) result;

    }

    private void set(@NonNull PropertyConfig property, @Nullable Enum value) {
        preferences.edit().putString(property.name, value != null ? value.name() : "").apply();
    }

    private void set(@NonNull PropertyConfig property, int value) {
        preferences.edit().putInt(property.name, value).apply();
    }

    private void set(@NonNull PropertyConfig property, float value) {
        preferences.edit().putFloat(property.name, value).apply();
    }

    private void set(@NonNull PropertyConfig property, long value) {
        preferences.edit().putLong(property.name, value).apply();
    }

    private void set(@NonNull PropertyConfig property, boolean value) {
        preferences.edit().putBoolean(property.name, value).apply();
    }

    private void set(@NonNull PropertyConfig property, String value) {
        preferences.edit().putString(property.name, value).apply();
    }

    private void set(@NonNull PropertyConfig property, Set<String> value) {
        preferences.edit().putStringSet(property.name, value).apply();
    }


    public interface TYPE_PROPERTY {
        PropertyConfig getConfig();
    }

    public static class PropertyConfig {
        final String name;
        final Object value;
        TYPE type;

        public PropertyConfig(String name, Object value) {
            this.name = name;
            this.value = value;
            if (value instanceof Boolean) {
                type = TYPE.BOOLEAN;
            } else if (value instanceof Integer) {
                type = TYPE.INTEGER;
            } else if (value instanceof Long) {
                type = TYPE.LONG;
            } else if (value instanceof Float) {
                type = TYPE.FLOAT;
            } else if (value instanceof Enum) {
                type = TYPE.ENUM;
            } else if (value instanceof String) {
                type = TYPE.STRING;
            } else if (value instanceof Set) {
                type = TYPE.STRING_SET;
            } else {
                throw new RuntimeException("ValueType is not Supported");
            }
        }
    }

    public static abstract class Config<K extends TYPE_PROPERTY> {

        @SuppressWarnings("unused")
        public static class IntegerPref extends TypePreference<Integer> {
            public IntegerPref(@NonNull TYPE_PROPERTY property) {
                this(property.getConfig());
            }

            public IntegerPref(@NonNull PropertyConfig property) {
                super(property, TYPE.INTEGER);
            }

            public void set(int value) {
                manger.set(config, value);
            }

            public int get() {
                return manger.get(config);
            }
        }

        @SuppressWarnings("unused")
        public static class StringPref extends TypePreference<String> {
            public StringPref(@NonNull TYPE_PROPERTY property) {
                this(property.getConfig());
            }

            public StringPref(@NonNull PropertyConfig property) {
                super(property, TYPE.STRING);
            }

            public void set(String value) {
                manger.set(config, value);
            }

            @NonNull
            public String get() {
                return manger.get(config);
            }
        }

        @SuppressWarnings("unused")
        public static class FloatPref extends TypePreference<Float> {
            public FloatPref(@NonNull TYPE_PROPERTY property) {
                this(property.getConfig());
            }

            public FloatPref(@NonNull PropertyConfig property) {
                super(property, TYPE.FLOAT);
            }

            public void set(float value) {
                manger.set(config, value);
            }

            public float get() {
                return manger.get(config);
            }
        }

        @SuppressWarnings("unused")
        public static class LongPref extends TypePreference<Long> {
            public LongPref(@NonNull TYPE_PROPERTY property) {
                this(property.getConfig());
            }

            public LongPref(@NonNull PropertyConfig property) {
                super(property, TYPE.LONG);
            }

            public void set(long value) {
                manger.set(config, value);
            }

            public long get() {
                return manger.get(config);
            }
        }

        @SuppressWarnings("unused")
        public static class BooleanPref extends TypePreference<Boolean> {
            public BooleanPref(@NonNull TYPE_PROPERTY property) {
                this(property.getConfig());
            }

            public BooleanPref(@NonNull PropertyConfig property) {
                super(property, TYPE.BOOLEAN);
            }

            public synchronized void set(boolean value) {
                manger.set(config, value);
            }

            public synchronized boolean get() {
                return manger.get(config);
            }
        }

        @SuppressWarnings("unchecked")
        public static class EnumPref<T extends Enum> extends TypePreference<String> {
            public EnumPref(@NonNull TYPE_PROPERTY property) {
                this(property.getConfig());
            }

            public EnumPref(@NonNull PropertyConfig property) {
                super(property, TYPE.ENUM);
            }

            public void set(T value) {
                manger.set(config, value);
            }

            @NonNull
            public T get() {
                Class<Enum> c = (Class<Enum>) config.value.getClass();

                try {
                    return (T) T.valueOf(c, manger.get(config));
                } catch (IllegalArgumentException ignored) {
                    return (T) config.value;
                }

            }
        }

        @SuppressWarnings("unused")
        public static class StringSetPref extends TypePreference<Set<String>> {
            public StringSetPref(@NonNull TYPE_PROPERTY property) {
                this(property.getConfig());
            }

            public StringSetPref(@NonNull PropertyConfig property) {
                super(property, TYPE.STRING_SET);
            }

            public void set(Set<String> value) {
                manger.set(config, value);
            }

            @NonNull
            public Set<String> get() {
                return manger.get(config);
            }
        }

        private static abstract class TypePreference<T> {
            @NonNull
            protected final PropertyConfig config;
            protected PrefManger<T> manger;

            public TypePreference(@NonNull PropertyConfig config, TYPE matchType) {
                this.config = config;
                if (config.type != matchType) {
                    throw new RuntimeException("Wrong Property Type: " + config.name + " is " + config.type);
                }
                this.manger = new PrefManger<>();
            }
        }
    }


}
