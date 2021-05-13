package wooteco.subway.line.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import wooteco.subway.line.domain.Line;
import wooteco.subway.line.domain.Section;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import wooteco.subway.station.domain.Station;

@Repository
public class SectionDao {
    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    private static RowMapper<Section> rowMapper(List<Station> stations) {
        return new RowMapper<Section>() {
            @Override
            public Section mapRow(ResultSet rs, int rowNum) throws SQLException {
                final long id = rs.getLong("id");
                final long upStationId = rs.getLong("up_station_id");
                final long downStationId = rs.getLong("down_station_id");
                final int distance = rs.getInt("distance");
                final Station upStation =
                    stations.stream().filter(station -> station.getId().equals(upStationId))
                        .findAny().orElseThrow(() -> new IllegalArgumentException("없음"));
                final Station downStation =
                    stations.stream().filter(station -> station.getId().equals(downStationId))
                        .findAny().orElseThrow(() -> new IllegalArgumentException("없음"));
                return new Section(id, upStation, downStation, distance);
            }
        };
    }

    public SectionDao(JdbcTemplate jdbcTemplate, DataSource dataSource) {
        this.jdbcTemplate = jdbcTemplate;
        this.simpleJdbcInsert = new SimpleJdbcInsert(dataSource)
                .withTableName("SECTION")
                .usingGeneratedKeyColumns("id");
    }

    public Section insert(Line line, Section section) {
        final Map<String, Object> params = new HashMap<>();
        params.put("line_id", line.getId());
        params.put("up_station_id", section.getUpStation().getId());
        params.put("down_station_id", section.getDownStation().getId());
        params.put("distance", section.getDistance());
        Long sectionId = simpleJdbcInsert.executeAndReturnKey(params).longValue();
        return new Section(sectionId, section.getUpStation(), section.getDownStation(), section.getDistance());
    }

    public void deleteByLineId(Long lineId) {
        jdbcTemplate.update("delete from SECTION s where s.line_id = ?", lineId);
    }

    public void insertSections(Line line) {
        List<Section> sections = line.getSections().getSections();
        List<Map<String, Object>> batchValues = sections.stream()
                .map(section -> {
                    Map<String, Object> params = new HashMap<>();
                    params.put("line_id", line.getId());
                    params.put("up_station_id", section.getUpStation().getId());
                    params.put("down_station_id", section.getDownStation().getId());
                    params.put("distance", section.getDistance());
                    return params;
                })
                .collect(Collectors.toList());

        simpleJdbcInsert.executeBatch(batchValues.toArray(new Map[sections.size()]));
    }

    public List<Section> findAll(final List<Station> stations) {
        String sql = "select s.id, s.line_id, s.up_station_id, s.down_station_id, s.distance from Section s";
        return jdbcTemplate.query(sql, rowMapper(stations));
    }
}
