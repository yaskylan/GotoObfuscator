package org.g0to.conf.transformer.settings;

import com.google.gson.annotations.SerializedName;
import org.g0to.transformer.features.*;
import org.g0to.transformer.features.classrename.ClassRename;

public class Transformers {
    @SerializedName("ClassRename")
    public ClassRename.Setting classRename;

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
