package com.drools.rule_management.helper;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * DRLDocument: Lưu trữ và thao tác nội dung DRL ở dạng memory (không cần file
 * path).
 * Giống ý tưởng DomDocument của PHP nhưng cho text DRL.
 */
public class DRLDocument {
    private final StringBuilder content;

    public DRLDocument() {
        this.content = new StringBuilder();
    }

    /** Thêm nội dung vào cuối */
    public DRLDocument append(String str) {
        content.append(str);
        return this;
    }

    /** Thêm một dòng vào cuối */
    public DRLDocument appendLine(String line) {
        content.append(line).append(System.lineSeparator());
        return this;
    }

    /** Chèn nội dung vào vị trí bất kỳ */
    public DRLDocument insert(int offset, String str) {
        content.insert(offset, str);
        return this;
    }

    /** Xóa toàn bộ nội dung */
    public void clear() {
        content.setLength(0);
    }

    /** Lấy nội dung DRL dạng String */
    public String getContent() {
        return content.toString();
    }

    /** Trả về độ dài nội dung */
    public int length() {
        return content.length();
    }

    @Override
    public String toString() {
        return getContent();
    }

    /**
     * Lưu nội dung DRL ra file.
     * 
     * @param filePath Đường dẫn file để lưu.
     * @throws IOException nếu có lỗi IO khi ghi file.
     */
    public void save(String filePath) throws IOException {
        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write(getContent());
        }
    }

    /**
     * Lưu nội dung DRL ra file dạng Path.
     * 
     * @param path Đối tượng Path đại diện file.
     * @throws IOException nếu có lỗi IO khi ghi file.
     */
    public void save(Path path) throws IOException {
        Files.write(path, getContent().getBytes());
    }
}