package org.g0to.conf;

import com.google.gson.annotations.SerializedName;
import org.g0to.conf.transformer.settings.Transformers;

import java.util.Collections;
import java.util.List;

public class Configuration {
    @SerializedName("logLevel")
    public String logLevel = "info";

    @SerializedName("inputPath")
    public String inputPath;

    @SerializedName("outputPath")
    public String outputPath;

    @SerializedName("jdkPath")
    public String jdkPath;

    @SerializedName("javaVersion")
    public Integer javaVersion;

    @SerializedName("libraries")
    public List<String> libraries = Collections.emptyList();

    @SerializedName("skipClasses")
    public String[] skipClasses;

    @SerializedName("libraryClasses")
    public String[] libraryClasses;

    @SerializedName("dictionary")
    public DictionarySetting dictionary;

    @SerializedName("transformers")
    public Transformers transformers = new Transformers();
}
