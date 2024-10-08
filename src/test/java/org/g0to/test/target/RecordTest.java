package org.g0to.test.target;

public record RecordTest(
   String data1,
   String data2
) {
    public String sum() {
        return data1 + data2;
    }
}