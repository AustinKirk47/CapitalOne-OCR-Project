package com.capitalone.creditocr.model.dao.postgres_impl;

import com.capitalone.creditocr.model.dao.DocumentImageDao;
import com.capitalone.creditocr.model.dto.ImageType;
import com.capitalone.creditocr.model.dto.document_image.DocumentImage;
import com.capitalone.creditocr.model.dto.job.ProcessingJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
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

import static com.capitalone.creditocr.model.dto.document_image.DocumentImage.PAGE_NUM_ENVELOPE;

// Note to team:
// One of the core pieces of Spring is it's dependency injection (DI; https://en.wikipedia.org/wiki/Dependency_injection)
// framework, which makes other classes that this one uses (dependencies) easily swappable with minimal changes. This is
// especially helpful when future changes need to be made, or for testing, where dummy/mock objects can be replaced with the
// real ones. Internally, spring looks at all classes which are registered with it for DI, and creates a "dependency graph",
// where it will create all the required dependencies to create this object when it is requested.
//
// There are several annotations that tell spring that a class should participate in the dependency graph, each with a few
// very minor differences. The best summary I've found of the differences are outlined in this answer on StackOverflow:
// https://stackoverflow.com/a/6897038/1021064
//
// The Autowired annotation tells spring where to look when it needs to check which dependencies this object needs before its
// constructor is called. You _can_ put this on the field, however best-practice dictates that you should use constructor injection
// instead of field-based injection, for when you need to construct an instance of this object outside of spring (i.e. in test cases)
@Repository
public class PgDocumentImageDao implements DocumentImageDao {

    private static final Logger logger = LoggerFactory.getLogger(PgDocumentImageDao.class);

    private final DataSource dataSource;

    private static final RowMapper<DocumentImage> ROW_MAPPER = (rs, rowNum) -> {
        DocumentImage image = DocumentImage.builder()
                .setFileData(rs.getBytes("file_data"))
                .setDocumentId(rs.getInt("document_id"))
                .setImageType(ImageType.valueOf(rs.getString("image_format")))
                .setPageNumber(rs.getInt("page_number"))
                .setIsEnvelope(rs.getBoolean("is_envelope"))
                .build();

        image.setId(rs.getInt("id"));
        return image;
    };

    @Autowired
    public PgDocumentImageDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @SuppressWarnings("Duplicates")
    @Override
    public void addNewImage(DocumentImage image) {

        String sql = "INSERT INTO document_images (file_data, page_number, image_format, is_envelope, document_id) " +
                "      VALUES (:fileData, :pageNum, :format::image_type, :isEnvelope, :docId);";

        MapSqlParameterSource source = new MapSqlParameterSource()
                .addValue("fileData", image.getFileData())
                .addValue("pageNum", image.getPageNumber() == PAGE_NUM_ENVELOPE ? null : image.getPageNumber())
                .addValue("format", image.getImageType().toString())
                .addValue("isEnvelope", image.isEnvelope())
                .addValue("docId", image.getDocumentId());


        NamedParameterJdbcTemplate template = new NamedParameterJdbcTemplate(dataSource);
        KeyHolder holder = new GeneratedKeyHolder();

        template.update(sql, source, holder, new String[] {"id"});
        Map<String, Object> keyMap = holder.getKeys();
        Objects.requireNonNull(keyMap); // Throw a NPE if the value is null, but make the warning go away.

        image.setId((Integer) keyMap.get("id"));

    }

    @Override
    public Optional<DocumentImage> getImageForJob(ProcessingJob job) {
        //language=sql
        String sql = "SELECT images.* FROM document_images as images, jobs, job_assignments " +
                     " WHERE job_assignments.job_id = jobs.id " +
                     "   AND jobs.id = :id " +
                     "   AND jobs.document_image = images.id " +
                     "   AND job_assignments.completed_at IS NULL;" ;

        MapSqlParameterSource source = new MapSqlParameterSource()
                .addValue("id", job.getId());
        NamedParameterJdbcTemplate template = new NamedParameterJdbcTemplate(dataSource);

        List<DocumentImage> resp = template.query(sql, source, ROW_MAPPER);
        if (resp.isEmpty()) {
            return Optional.empty();
        }

        if (resp.size() > 1) {
            logger.warn("Query returned more than 1 result! list = " + resp.toString());
        }

        return Optional.of(resp.get(0));
    }


    @SuppressWarnings("Duplicates")
    @Override
    public Optional<DocumentImage> getImageForDocument(int documentId, int page) {
        //language=sql
        String sql = " select * " +
                     " from document_images " +
                     " where document_id = :docId " +
                     " and page_number = :page;";

        var source = new MapSqlParameterSource()
                .addValue( "docId", documentId )
                .addValue( "page", page );
        var template = new NamedParameterJdbcTemplate( dataSource );

        List<DocumentImage> images = template.query( sql, source, ROW_MAPPER );

        if (images.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of( images.get( 0 ) );
        }
    }

    @SuppressWarnings("Duplicates")
    @Override
    public Optional<DocumentImage> getEnvelopeForDocument(int id) {
        //language=sql
        String sql = " select * " +
                     " from document_images " +
                     " where document_id = :docId " +
                     " and is_envelope = true; ";

        var source = new MapSqlParameterSource()
                .addValue( "docId", id );
        var template = new NamedParameterJdbcTemplate( dataSource );

        List<DocumentImage> images = template.query( sql, source, ROW_MAPPER );

        if (images.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of( images.get( 0 ) );
        }

    }

}
