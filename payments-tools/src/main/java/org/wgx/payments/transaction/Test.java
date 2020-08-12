package org.wgx.payments.transaction;

public class Test {

    public static void main(String[] arg) {
        Long longq = Long.valueOf(1l);
        Long longq2 = Long.valueOf(1l);

        System.out.print(longq.hashCode() + "_" + longq2.hashCode());
    }
}
