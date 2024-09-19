package org.g0to.conf;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.annotations.SerializedName;
import org.g0to.dictionary.ClassDictionary;
import org.g0to.dictionary.Dictionary;
import org.g0to.wrapper.ClassWrapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DictionarySetting {
    @SerializedName("valueType")
    private String valueType = "preset";

    @SerializedName("value")
    private JsonElement value = new JsonPrimitive("english");

    @SerializedName("baseLength")
    public Integer baseLength = 1;

    @SerializedName("blacklist")
    public List<String> blacklist;

    private transient char[] words;

    public Dictionary newDictionary() {
        final Dictionary dictionary = new Dictionary(getWords(), baseLength);
        if (blacklist != null) {
            dictionary.addToBlacklist(blacklist);
        }

        return dictionary;
    }

    public ClassDictionary newClassDictionary(ClassWrapper classWrapper) {
        final ClassDictionary dictionary = new ClassDictionary(classWrapper, getWords(), baseLength);
        if (blacklist != null) {
            dictionary.addToBlacklist(blacklist);
        }

        return dictionary;
    }

    @SuppressWarnings("UnnecessaryUnicodeEscape")
    public char[] getWords() {
        if (words != null) {
            return words;
        }

        switch (valueType.toLowerCase(Locale.ROOT)) {
            case "chars" -> {
                return this.words = this.value.getAsString().toCharArray();
            }
            case "range" -> {
                final JsonArray array = this.value.getAsJsonArray();
                final String[] ranges = new String[array.size()];

                for (int i = 0; i < array.size(); i++) {
                    ranges[i] = array.get(i).getAsString();
                }

                return this.words = byRange(ranges);
            }
            case "preset" -> {
                switch (this.value.getAsString().toLowerCase(Locale.ROOT)) {
                    case "english" -> {
                        return this.words = byRange(new String[] {
                            "a--z",
                            "A--Z"
                        });
                    }
                    case "arabic" -> {
                        return this.words = byRange(new String[] {
                            "\u0600--\u06FF",
                            "\u0750--\u077F",
                            "\u08A0--\u08FF",
                            "\u0870--\u089F",
                            "\uFE70--\uFEFF"
                        });
                    }
                    case "chinese" -> {
                        return this.words = byRange(new String[] {
                            "\u4E00--\u9FFF",
                            "\u3400--\u4DBF",
                            "\uF900--\uFAFF"
                        });
                    }
                    case "thaiphosym" -> {
                        return this.words = byRange(new String[] {
                            "\u0E31",
                            "\u0E34--\u0E3A",
                            "\u0E47--\u0E4E"
                        });
                    }
                    case "spaces" -> {
                        return this.words = byRange(new String[] {
                            "\u2007",
                            "\u200B--\u200D",
                            "\u202F",
                            "\u2060",
                            "\uFEFF"
                        });
                    }
                    case "number" -> {
                        return this.words = byRange(new String[] {
                            "0--9"
                        });
                    }
                    default -> throw new IllegalStateException(String.format("Preset %s not found", this.value.getAsString()));
                }
            }
        }

        throw new IllegalStateException();
    }

    private static char[] byRange(String[] ranges) {
        final ArrayList<Character> characters = new ArrayList<>();

        for (String v : ranges) {
            if (!v.contains("--")) {
                if (v.length() == 1) {
                    characters.add(v.charAt(0));
                    continue;
                }

                throw new IllegalArgumentException("Illegal range string");
            }

            final String[] split = v.split("--");

            if (split[0].length() != 1 || split[1].length() != 1) {
                throw new IllegalArgumentException("Illegal range string");
            }

            final char c1 = split[0].charAt(0);
            final char c2 = split[1].charAt(0);

            if (c1 > c2) {
                throw new IllegalArgumentException(String.format("c1 '%d' > c2 '%d'", (int) c1, (int) c2));
            } else if (c1 == c2) {
                characters.add(c2);
            } else {
                for (char c = c1; c <= c2; c++) {
                    characters.add(c);
                }
            }
        }

        final char[] copy = new char[characters.size()];
        for (int i = 0; i < characters.size(); i++) {
            copy[i] = characters.get(i);
        }

        return copy;
    }
}
