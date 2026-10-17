package com.zhou.goldtask.moment;

/**
 * 朋友圈草稿和附件的纯校验，不碰数据库。
 */
public final class MomentRules {
    public static final int PAGE_SIZE = 10;
    public static final int MAX_CONTENT = 2000;
    public static final int MAX_MEDIA = 9;
    public static final long MAX_IMAGE_BYTES = 15L * 1024 * 1024;
    public static final long MAX_VIDEO_BYTES = 80L * 1024 * 1024;

    private MomentRules() {
    }

    public static String normalizeContent(String content) {
        if (content == null) {
            return "";
        }
        return content.trim();
    }

    /** 通过返回 null；否则返回可直接展示的原因。 */
    public static String validateDraft(String content, int mediaCount) {
        String text = normalizeContent(content);
        if (text.length() > MAX_CONTENT) {
            return "文字超过 2000 字";
        }
        if (mediaCount > MAX_MEDIA) {
            return "附件最多 9 个";
        }
        if (text.isEmpty() && mediaCount == 0) {
            return "请输入内容或上传图片、视频";
        }
        return null;
    }

    /** image、video，或无法识别时 null。 */
    public static String kindOf(String contentType) {
        if (contentType == null) {
            return null;
        }
        String lower = contentType.toLowerCase();
        if (lower.startsWith("image/")) {
            return "image";
        }
        if (lower.startsWith("video/")) {
            return "video";
        }
        return null;
    }

    /** 通过返回 null。 */
    public static String validateFile(String contentType, long size) {
        String kind = kindOf(contentType);
        if (kind == null) {
            return "只能上传图片或视频";
        }
        if (size <= 0) {
            return "文件是空的";
        }
        long max = "image".equals(kind) ? MAX_IMAGE_BYTES : MAX_VIDEO_BYTES;
        if (size > max) {
            return "image".equals(kind) ? "图片不能超过 15MB" : "视频不能超过 80MB";
        }
        return null;
    }

    public static int page(Integer page) {
        if (page == null || page < 1) {
            return 1;
        }
        return page;
    }
}
