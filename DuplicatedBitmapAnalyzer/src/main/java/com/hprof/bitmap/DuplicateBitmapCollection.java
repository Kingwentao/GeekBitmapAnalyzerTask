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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 重复Bitmap收集
 * 使用leakcanary打印调用链
 */
public class DuplicateBitmapCollection {

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
        ClassObj bitmapClasses = snapshot.findClass("android.graphics.Bitmap");

        // 只分析default和app
        Heap defaultHeap = snapshot.getHeap("default");
        Heap appHeap = snapshot.getHeap("app");
        List<Instance> defaultAndAppBmInstance = bitmapClasses.getHeapInstances(defaultHeap.getId());
        List<Instance> appBmInstance = bitmapClasses.getHeapInstances(appHeap.getId());
        defaultAndAppBmInstance.addAll(appBmInstance);
        System.out.println("defaultAndAppBmInstance heaps size in snapshot = " + defaultAndAppBmInstance.size());

        // 收集所有
        Map<String, List<Instance>> collectSameMap = new HashMap<>();
        ArrayList<DuplicatedCollectInfo> duplicatedCollectInfos = new ArrayList<>();
        for (Instance instance : defaultAndAppBmInstance) {
            List<ClassInstance.FieldValue> classFieldList = HahaHelper.classInstanceValues(instance);
            ArrayInstance arrayInstance = HahaHelper.fieldValue(classFieldList, "mBuffer");
            byte[] mBufferByte = HahaHelper.getByteArray(arrayInstance);
            int mBufferHashCode = Arrays.hashCode(mBufferByte);
            String hashKey = String.valueOf(mBufferHashCode);
            if (collectSameMap.containsKey(hashKey)) {
                collectSameMap.get(hashKey).add(instance);
            } else {
                List<Instance> bmList = new ArrayList<>();
                bmList.add(instance);
                collectSameMap.put(hashKey, bmList);
            }
        }

        // 移除少于1个的
        collectSameMap.entrySet().removeIf(entry -> entry.getValue().size() <= 1);

        // 收集重复的图片对象
        for (Map.Entry<String, List<Instance>> entry : collectSameMap.entrySet()) {
            DuplicatedCollectInfo info = new DuplicatedCollectInfo(entry.getKey());
            for (Instance instance : entry.getValue()) {
                info.addBitmapInstance(new BitmapInstance(snapshot, entry.getKey(), instance));
            }
            info.internalSetValue();
            System.out.println("add duplicated info: " + info.string());
            duplicatedCollectInfos.add(info);
        }

        System.out.println("duplicated size: " + duplicatedCollectInfos.size());
    }

    private static File getHeapDumpFile() {
        File file = new File("/Users/wentaoking/Downloads/test2.hprof");
        if (file.exists()) {
            System.out.println("file is exist");
        } else {
            System.out.println("file is not exist");
        }
        return file;
    }

}
