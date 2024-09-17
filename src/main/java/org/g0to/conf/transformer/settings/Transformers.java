package org.g0to.conf.transformer.settings;

import com.google.gson.annotations.SerializedName;
import org.g0to.transformer.features.*;
import org.g0to.transformer.features.nameobf.NameObfuscation;

public class Transformers {
    @SerializedName("NameObfuscation")
    public NameObfuscation.Setting nameObfuscation;

    @SerializedName("StringEncrypt")
    public StringEncrypt.Setting stringEncrypt;

    @SerializedName("InvokeProxy")
    public InvokeProxy.Setting invokeProxy;

    @SerializedName("FlowObfuscate")
    public FlowObfuscate.Setting flowObfuscate;

    @SerializedName("GotoReplacer")
    public GotoReplacer.Setting gotoReplacer;

    @SerializedName("NumberEncrypt")
    public NumberEncrypt.Setting numberEncrypt;
}
