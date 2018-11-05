package com.capitalone.creditocr.model.dao.postgres_impl;

import com.capitalone.creditocr.model.dao.DocumentTextDao;
import com.capitalone.creditocr.model.dto.document.DocumentText;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Repository
public class PgDocumentTextDao implements DocumentTextDao {

    private final DataSource dataSource;

    @Autowired
    public PgDocumentTextDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }


    @Override
    public Optional<String> getDocumentTextById(int id) {
        //language=sql
        String sql = "SELECT original_text AS text FROM document_text, document_images " +
                     " WHERE document_text.image_id = images.id " +
                     "   AND document_images.document_id = :docId;";

        MapSqlParameterSource source = new MapSqlParameterSource()
                .addValue("docId",  id);

        NamedParameterJdbcTemplate template = new NamedParameterJdbcTemplate(dataSource);
        List<String> result = template.query(sql, source, (rSet, rNum) -> rSet.getString("text"));

        if (result.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(result.get(0));
        }
    }

    @Override
    public void addDocumentText(DocumentText documentText) {
        //language=sql
        String sql = "INSERT INTO document_text (original_text, fingerprint, image_id) " +
                     "     VALUES (:docText, :fingerprint, :imageId);";

        MapSqlParameterSource source = new MapSqlParameterSource()
                .addValue("docText", documentText.getDocumentText())
                .addValue("fingerprint", documentText.getFingerprint())
                .addValue("imageId", documentText.getImageId());

        NamedParameterJdbcTemplate template = new NamedParameterJdbcTemplate(dataSource);
        KeyHolder holder = new GeneratedKeyHolder();

        template.update(sql, source, holder);
        Map<String, Object> keys = holder.getKeys();
        Objects.requireNonNull(keys);

        documentText.setId((Integer) keys.get("id"));
    }
}