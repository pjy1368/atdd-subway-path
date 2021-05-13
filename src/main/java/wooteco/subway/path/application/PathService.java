package wooteco.subway.path.application;

import org.springframework.stereotype.Service;

import wooteco.subway.line.dao.SectionDao;
import wooteco.subway.station.dao.StationDao;

@Service
public class PathService {

    private final StationDao stationDao;
    private final SectionDao sectionDao;

    public PathService(StationDao stationDao, SectionDao sectionDao) {
        this.stationDao = stationDao;
        this.sectionDao = sectionDao;
    }
}
