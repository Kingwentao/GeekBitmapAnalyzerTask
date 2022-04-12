package com.hprof.bitmap;

import com.squareup.haha.perflib.ArrayInstance;
import com.squareup.haha.perflib.ClassInstance;
import com.squareup.haha.perflib.ClassObj;
import com.squareup.haha.perflib.Heap;
import com.squareup.haha.perflib.HprofParser;
import com.squareup.haha.perflib.Instance;
import com.squareup.haha.perflib.Snapshot;
import com.squareup.haha.perflib.io.HprofBuffer;
import com.squareup.haha.perflib.io.MemoryMappedFileBuffer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * 简单输出
 */
public class DuplicateBitmapCollection2 {

    public static void main(String[] args) throws IOException {
        // 获取heap dump文件
        File heapDumpFile = getHeapDumpFile();
        // 打开hprof文件
        HprofBuffer buffer = new MemoryMappedFileBuffer(heapDumpFile);
        HprofParser parser = new HprofParser(buffer);
        // 解析获得快照
        Snapshot snapshot = parser.parse();
        snapshot.computeDominators();

        // 获得Bitmap Class
        Collection<ClassObj> bitmapClasses = snapshot.findClasses("android.graphics.Bitmap");
        Collection<Heap> heaps = snapshot.getHeaps();
        System.out.println("bitmapClasses size = " + bitmapClasses.size());
        System.out.println("all heaps size in snapshot = " + heaps.size());

        for (Heap heap : heaps) {
            if (!heap.getName().equals("app")) continue;
            for (ClassObj clazz : bitmapClasses) {
                List<Instance> instances = clazz.getHeapInstances(heap.getId());
                System.out.println("instances size：" + instances.size());
                for (int i = 0; i < instances.size(); i++) {
                    // 从GCRoot开始遍历搜索，Integer.MAX_VALUE代表无法被搜索到，说明对象没被引用可以被回收
                    if (instances.get(i).getDistanceToGcRoot() == Integer.MAX_VALUE) continue;
                    int curHashCode = getHashCodeByInstance(instances.get(i));
                    for (int j = i + 1; j < instances.size(); j++) {
                        int nextHashCode = getHashCodeByInstance(instances.get(j));
                        if (curHashCode == nextHashCode) {
                            System.out.println("***found same file***");
                            AnalyzerResult result = getAnalyzerResult(instances.get(i));
                            getStackInfo(instances.get(i));
                            System.out.println("===========");
                            if (i == instances.size() - 2 && j == instances.size() - 1) {
                                System.out.println("i == instances size - 2");
                            }
                            break;
                        }
                    }
                }
            }
        }
    }

    private static void getStackInfo(Instance instance) {
        if (instance.getDistanceToGcRoot() != 0 && instance.getDistanceToGcRoot() != Integer.MAX_VALUE) {
            getStackInfo(instance.getNextInstanceToGcRoot());
        }
        if (instance.getNextInstanceToGcRoot() != null) {
            System.out.println("instance: " + instance.getNextInstanceToGcRoot());
        }
    }

    private static int getHashCodeByInstance(Instance instance) {
        List<ClassInstance.FieldValue> classInstanceValues = ((ClassInstance) instance).getValues();
        ArrayInstance curBitmapBuffer = fieldValue(classInstanceValues, "mBuffer");
        return Arrays.hashCode(curBitmapBuffer.getValues());
    }

    private static AnalyzerResult getAnalyzerResult(Instance instance) {
        AnalyzerResult result = new AnalyzerResult();
        List<ClassInstance.FieldValue> classInstanceValues = ((ClassInstance) instance).getValues();
        ArrayInstance bitmapBuffer = fieldValue(classInstanceValues, "mBuffer");
        int bitmapHeight = fieldValue(classInstanceValues, "mHeight");
        int bitmapWidth = fieldValue(classInstanceValues, "mWidth");
        result.setHashCode(Arrays.hashCode(bitmapBuffer.getValues()));
        result.setClassInstance(bitmapBuffer.toString());
        result.setBufferSize(bitmapBuffer.getValues().length);
        result.setWidth(bitmapWidth);
        result.setHeight(bitmapHeight);
        return result;
    }

    private static <T> T fieldValue(List<ClassInstance.FieldValue> values, String fieldName) {
        for (ClassInstance.FieldValue fieldValue : values) {
            if (fieldValue.getField().getName().equals(fieldName)) {
                return (T) fieldValue.getValue();
            }
        }
        throw new IllegalArgumentException("Field " + fieldName + " does not exists");
    }

    private static File getHeapDumpFile() {
        File file = new File("/Users/wentaoking/Downloads/test.hprof");
        if (file.exists()) {
            System.out.println("file is exist");
        } else {
            System.out.println("file is not exist");
        }
        return file;
    }

}
