package com.hprof.bitmap;

/**
 * author: WentaoKing
 * created on: 2022/4/12
 * description:
 */
class AnalyzerResult {
    int hashCode;
    String classInstance;
    int width;
    int height;
    int bufferSize;

    @Override
    public String toString() {
        return "bufferHashCode:" + this.hashCode + "\n"
                + "width:" + this.width + "\n"
                + "height:" + this.height + "\n"
                + "bufferSize:" + this.bufferSize;
    }

    public int getBufferSize() {
        return bufferSize;
    }

    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    public int getHashCode() {
        return hashCode;
    }

    public void setHashCode(int hashCode) {
        this.hashCode = hashCode;
    }

    public String getClassInstance() {
        return classInstance;
    }

    public void setClassInstance(String classInstance) {
        this.classInstance = classInstance;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }
}
