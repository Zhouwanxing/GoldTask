package com.zhou.goldtask.service;

import cn.dev33.satoken.util.SaResult;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.zhou.goldtask.entity.MomentCreateRequest;
import com.zhou.goldtask.entity.MomentEntity;
import com.zhou.goldtask.entity.MomentMedia;
import com.zhou.goldtask.entity.MomentMediaClaim;
import com.zhou.goldtask.moment.MomentFileRanges;
import com.zhou.goldtask.moment.MomentRules;
import com.zhou.goldtask.moment.MomentThumbs;
import com.zhou.goldtask.repository.MomentMediaClaimRepository;
import com.zhou.goldtask.repository.MomentRepository;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.ByteArrayInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Slf4j
public class MomentService {
    @Resource
    private MomentRepository momentRepository;
    @Resource
    private MomentMediaClaimRepository momentMediaClaimRepository;
    @Resource
    private GridFsTemplate gridFsTemplate;

    public SaResult list(String username, Integer page) {
        int safePage = MomentRules.page(page);
        List<MomentEntity> items = momentRepository.findByUsernameOrderByCreatedAtDesc(
                username, PageRequest.of(safePage - 1, MomentRules.PAGE_SIZE));
        long count = momentRepository.countByUsername(username);
        return SaResult.ok().set("list", items).set("count", count);
    }

    public SaResult upload(String username, MultipartFile file, MultipartFile thumb) {
        if (file == null || file.isEmpty()) {
            return SaResult.error("文件是空的");
        }
        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (IOException ex) {
            log.warn("朋友圈读取上传文件失败", ex);
            return SaResult.error("上传失败");
        }
        String contentType = file.getContentType();
        String error = MomentRules.validateFile(contentType, bytes.length);
        if (error != null) {
            return SaResult.error(error);
        }
        String kind = MomentRules.kindOf(contentType);
        String name = safeName(file.getOriginalFilename(), kind);
        String thumbId = null;
        if ("image".equals(kind) && thumb != null && !thumb.isEmpty()) {
            try {
                thumbId = storeThumb(username, MomentThumbs.accept(thumb.getBytes(), thumb.getContentType()));
            } catch (IOException ex) {
                log.warn("朋友圈读取缩略图失败", ex);
            }
        }
        Document meta = new Document();
        meta.put("username", username);
        meta.put("kind", kind);
        meta.put("contentType", contentType);
        meta.put("name", name);
        if (thumbId != null) {
            meta.put("thumbFileId", thumbId);
        }
        try {
            ObjectId stored = gridFsTemplate.store(new ByteArrayInputStream(bytes), name, contentType, meta);
            log.info("朋友圈附件已保存 fileId={} thumb={}", stored.toHexString(), thumbId);
            return SaResult.data(new MomentMedia(stored.toHexString(), kind, contentType, name, thumbId));
        } catch (RuntimeException ex) {
            deleteStored(thumbId);
            log.warn("朋友圈上传失败", ex);
            return SaResult.error("上传失败");
        }
    }

    public SaResult create(String username, MomentCreateRequest body) {
        String content = body == null ? null : body.getContent();
        List<String> fileIds = body == null || body.getFileIds() == null
                ? new ArrayList<String>() : body.getFileIds();
        String error = MomentRules.validateDraft(content, fileIds.size());
        if (error != null) {
            return SaResult.error(error);
        }
        Set<String> seen = new HashSet<String>();
        List<MomentMedia> media = new ArrayList<MomentMedia>();
        List<MomentMediaClaim> claims = new ArrayList<MomentMediaClaim>();
        for (String fileId : fileIds) {
            if (fileId == null || fileId.trim().isEmpty()) {
                return SaResult.error("附件不存在");
            }
            if (!seen.add(fileId)) {
                return SaResult.error("附件重复");
            }
            GridFSFile file = findOwned(username, fileId);
            if (file == null) {
                return SaResult.error("附件不存在");
            }
            Document metadata = file.getMetadata();
            if (metadata != null && "thumb".equals(metadata.getString("role"))) {
                releaseClaims(claims);
                return SaResult.error("附件不存在");
            }
            MomentMediaClaim claim = new MomentMediaClaim(null, fileId, username);
            try {
                momentMediaClaimRepository.save(claim);
            } catch (DuplicateKeyException ex) {
                releaseClaims(claims);
                log.warn("朋友圈附件被重复使用: {}", fileId);
                return SaResult.error("附件已使用");
            }
            claims.add(claim);
            String kind = metadata == null ? null : metadata.getString("kind");
            String contentType = metadata == null ? null : metadata.getString("contentType");
            String name = metadata == null ? null : metadata.getString("name");
            String thumbFileId = metadata == null ? null : metadata.getString("thumbFileId");
            if (kind == null) {
                kind = "image";
            }
            if (contentType == null) {
                contentType = "application/octet-stream";
            }
            if (name == null) {
                name = "";
            }
            media.add(new MomentMedia(fileId, kind, contentType, name, thumbFileId));
        }
        MomentEntity entity = new MomentEntity();
        entity.setUsername(username);
        entity.setContent(MomentRules.normalizeContent(content));
        entity.setCreatedAt(System.currentTimeMillis());
        entity.setMedia(media);
        try {
            return SaResult.data(momentRepository.save(entity));
        } catch (RuntimeException ex) {
            releaseClaims(claims);
            throw ex;
        }
    }

    public SaResult delete(String username, String id) {
        MomentEntity entity = momentRepository.findById(id).orElse(null);
        if (entity == null || entity.getUsername() == null || !entity.getUsername().equals(username)) {
            return SaResult.error("朋友圈不存在");
        }
        momentRepository.delete(entity);
        List<String> fileIds = new ArrayList<String>();
        if (entity.getMedia() != null) {
            for (MomentMedia item : entity.getMedia()) {
                if (item.getFileId() != null) {
                    fileIds.add(item.getFileId());
                }
            }
        }
        if (!fileIds.isEmpty()) {
            momentMediaClaimRepository.deleteByFileIdIn(fileIds);
            for (MomentMedia item : entity.getMedia()) {
                deleteMediaFiles(item);
            }
        }
        return SaResult.ok();
    }

    public void writeFile(String username, String fileId, HttpServletRequest request, HttpServletResponse response) throws IOException {
        GridFSFile file = findOwned(username, fileId);
        if (file == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        long length = file.getLength();
        Document metadata = file.getMetadata();
        String contentType = metadata == null ? "application/octet-stream" : metadata.getString("contentType");
        if (contentType == null || contentType.isEmpty()) {
            contentType = "application/octet-stream";
        }
        response.setContentType(contentType);
        response.setHeader("Accept-Ranges", "bytes");
        response.setHeader("Content-Disposition", "inline");
        MomentFileRanges.ByteRange range;
        try {
            range = MomentFileRanges.parse(request.getHeader("Range"), length);
        } catch (IllegalArgumentException ex) {
            response.setStatus(416);
            response.setHeader("Content-Range", "bytes */" + length);
            return;
        }
        long start = range == null ? 0 : range.start;
        long end = range == null ? length - 1 : range.end;
        if (range != null) {
            response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
            response.setHeader("Content-Range", "bytes " + start + "-" + end + "/" + length);
        } else {
            response.setStatus(HttpServletResponse.SC_OK);
        }
        long toWrite = end - start + 1;
        response.setContentLengthLong(toWrite);
        InputStream input = gridFsTemplate.getResource(file).getInputStream();
        try {
            skipFully(input, start);
            copyLimited(input, response.getOutputStream(), toWrite);
        } finally {
            input.close();
        }
    }

    private String storeThumb(String username, byte[] jpeg) {
        if (jpeg == null || jpeg.length == 0) {
            return null;
        }
        Document meta = new Document();
        meta.put("username", username);
        meta.put("kind", "image");
        meta.put("contentType", "image/jpeg");
        meta.put("name", "thumb.jpg");
        meta.put("role", "thumb");
        ObjectId stored = gridFsTemplate.store(new ByteArrayInputStream(jpeg), "thumb.jpg", "image/jpeg", meta);
        return stored.toHexString();
    }

    private void deleteMediaFiles(MomentMedia item) {
        if (item == null) {
            return;
        }
        deleteStored(item.getFileId());
        String thumbId = item.getThumbFileId();
        if (hasText(thumbId) && !thumbId.equals(item.getFileId())) {
            deleteStored(thumbId);
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    /** 发帖失败时把已经占用的附件还回去，下一次可再引用。 */
    private void releaseClaims(List<MomentMediaClaim> claims) {
        if (claims == null) {
            return;
        }
        for (MomentMediaClaim claim : claims) {
            if (claim != null && claim.get_id() != null) {
                try {
                    momentMediaClaimRepository.delete(claim);
                } catch (RuntimeException ex) {
                    log.warn("释放朋友圈附件占用失败: {}", claim.getFileId(), ex);
                }
            }
        }
    }

    private GridFSFile findOwned(String username, String fileId) {
        ObjectId objectId;
        try {
            objectId = new ObjectId(fileId);
        } catch (IllegalArgumentException ex) {
            return null;
        }
        GridFSFile file = gridFsTemplate.findOne(Query.query(Criteria.where("_id").is(objectId)));
        if (file == null || file.getMetadata() == null) {
            return null;
        }
        if (!username.equals(file.getMetadata().getString("username"))) {
            return null;
        }
        return file;
    }

    private void deleteStored(String fileId) {
        if (fileId == null) {
            return;
        }
        try {
            gridFsTemplate.delete(Query.query(Criteria.where("_id").is(new ObjectId(fileId))));
        } catch (IllegalArgumentException ex) {
            log.warn("朋友圈附件 id 不合法: {}", fileId);
        }
    }

    private String safeName(String original, String kind) {
        String name = original == null ? "" : original;
        int slash = Math.max(name.lastIndexOf('/'), name.lastIndexOf('\\'));
        if (slash >= 0 && slash + 1 < name.length()) {
            name = name.substring(slash + 1);
        }
        name = name.replace("\r", "").replace("\n", "").replace("\"", "");
        if (name.trim().isEmpty()) {
            return "image".equals(kind) ? "image.jpg" : "video.mp4";
        }
        if (name.length() > 80) {
            name = name.substring(name.length() - 80);
        }
        return name;
    }

    private void skipFully(InputStream input, long count) throws IOException {
        long left = count;
        while (left > 0) {
            long skipped = input.skip(left);
            if (skipped <= 0) {
                if (input.read() < 0) {
                    return;
                }
                skipped = 1;
            }
            left -= skipped;
        }
    }

    private void copyLimited(InputStream input, OutputStream output, long count) throws IOException {
        byte[] buffer = new byte[8192];
        long left = count;
        while (left > 0) {
            int read = input.read(buffer, 0, (int) Math.min(buffer.length, left));
            if (read < 0) {
                return;
            }
            output.write(buffer, 0, read);
            left -= read;
        }
        output.flush();
    }
}
