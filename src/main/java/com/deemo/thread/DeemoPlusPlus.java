package com.deemo.thread;

public class DeemoPlusPlus {
    private volatile int n;

    public void add() {
        /*
            LINENUMBER 7 L0
            ALOAD 0
            DUP
            GETFIELD com/deemo/thread/DeemoPlusPlus.n : I
            ICONST_1 入栈
            IADD
            PUTFIELD com/deemo/thread/DeemoPlusPlus.n : I
         */
        n += 1;
    }
}
