package org.g0to.conf;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.annotations.SerializedName;
import org.g0to.dictionary.ClassDictionary;
import org.g0to.dictionary.Dictionary;
import org.g0to.wrapper.ClassWrapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;

public class DictionarySetting {
    @SerializedName("valueType")
    private String valueType = "range";

    @SerializedName("value")
    private JsonElement value;

    @SerializedName("baseLength")
    public Integer baseLength = 5;

    @SerializedName("blacklist")
    public List<String> blacklist;

    private transient char[] words;

    public DictionarySetting() {
        final JsonArray defaultValue = new JsonArray();
        defaultValue.add("a--z");
        defaultValue.add("A--Z");

        this.value = defaultValue;
    }

    public Dictionary newDictionary() {
        return new Dictionary(getWords(), baseLength);
    }

    public ClassDictionary newClassDictionary(ClassWrapper classWrapper) {
        return new ClassDictionary(classWrapper, getWords(), baseLength);
    }

    public char[] getWords() {
        if (words != null) {
            return words;
        }

        switch (valueType.toLowerCase(Locale.ROOT)) {
            case "chars" -> {
                return this.words = this.value.getAsString().toCharArray();
            }
            case "range" -> {
                final ArrayList<Character> characters = new ArrayList<>();
                final JsonArray array = this.value.getAsJsonArray();

                for (JsonElement je : array) {
                    final String v = je.getAsString();

                    if (!v.contains("--")) {
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
                    }

                    for (char c = c1; c <= c2; c++) {
                        characters.add(c);
                    }
                }

                final char[] copy = new char[characters.size()];
                for (int i = 0; i < characters.size(); i++) {
                    copy[i] = characters.get(i);
                }

                return this.words = copy;
            }
        }

        throw new IllegalStateException();
    }
}
