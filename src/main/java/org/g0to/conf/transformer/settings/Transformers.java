package org.g0to.conf.transformer.settings;

import com.google.gson.annotations.SerializedName;
import org.g0to.transformer.features.*;
import org.g0to.transformer.features.nameobf.NameObfuscation;
import org.g0to.transformer.features.stringencryption.StringEncryption;

public class Transformers {
    @SerializedName("NameObfuscation")
    public NameObfuscation.Setting nameObfuscation;

    @SerializedName("VariableRename")
    public VariableRename.Setting variableRename;

    @SerializedName("StringEncryption")
    public StringEncryption.Setting stringEncryption;

    @SerializedName("InvokeProxy")
    public InvokeProxy.Setting invokeProxy;

    @SerializedName("IndyTransformer")
    public IndyTransformer.Setting indyTransformer;

    @SerializedName("FlowObfuscation")
    public FlowObfuscation.Setting flowObfuscation;

    @SerializedName("GotoReplacer")
    public GotoReplacer.Setting gotoReplacer;

    @SerializedName("NumberEncryption")
    public NumberEncryption.Setting numberEncryption;

    @SerializedName("Debug")
    public Debug.Setting debug;
}
