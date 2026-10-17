package com.zhou.goldtask.moment;

/**
 * 解析视频播放器发来的单个 bytes 范围。只取逗号前的第一段。
 */
public final class MomentFileRanges {
    private MomentFileRanges() {
    }

    public static final class ByteRange {
        public final long start;
        public final long end;

        public ByteRange(long start, long end) {
            this.start = start;
            this.end = end;
        }

        public long length() {
            return end - start + 1;
        }
    }

    /**
     * 没有 Range 头时返回 null，表示整个文件。
     * 范围不合法时抛 IllegalArgumentException。
     */
    public static ByteRange parse(String header, long fileLength) {
        if (header == null || header.trim().isEmpty()) {
            return null;
        }
        if (fileLength <= 0) {
            throw new IllegalArgumentException("文件是空的");
        }
        String value = header.trim();
        if (!value.startsWith("bytes=")) {
            throw new IllegalArgumentException("Range 不合法");
        }
        String spec = value.substring("bytes=".length()).split(",")[0].trim();
        int dash = spec.indexOf('-');
        if (dash < 0) {
            throw new IllegalArgumentException("Range 不合法");
        }
        String startText = spec.substring(0, dash).trim();
        String endText = spec.substring(dash + 1).trim();
        long start;
        long end;
        try {
            if (startText.isEmpty()) {
                if (endText.isEmpty()) {
                    throw new IllegalArgumentException("Range 不合法");
                }
                long suffix = Long.parseLong(endText);
                if (suffix <= 0) {
                    throw new IllegalArgumentException("Range 不合法");
                }
                start = Math.max(0, fileLength - suffix);
                end = fileLength - 1;
            } else {
                start = Long.parseLong(startText);
                end = endText.isEmpty() ? fileLength - 1 : Long.parseLong(endText);
                if (end >= fileLength) {
                    end = fileLength - 1;
                }
            }
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Range 不合法");
        }
        if (start < 0 || start >= fileLength || end < start) {
            throw new IllegalArgumentException("Range 不合法");
        }
        return new ByteRange(start, end);
    }
}
