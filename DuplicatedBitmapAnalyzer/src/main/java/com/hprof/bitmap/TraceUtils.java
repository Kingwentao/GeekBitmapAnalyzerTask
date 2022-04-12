package com.hprof.bitmap;

import com.squareup.haha.perflib.Instance;

import java.util.ArrayList;

/**
 * author: WentaoKing
 * created on: 2022/4/12
 * description:
 */
class TraceUtils {

    public static ArrayList<Instance> getTraceFromInstance(Instance instance) {
        ArrayList<Instance> arrayList = new ArrayList<>();
        Instance nextInstance;
        while ((nextInstance = instance.getNextInstanceToGcRoot()) != null) {
            arrayList.add(nextInstance);
            instance = nextInstance;
        }
        return arrayList;
    }
}
