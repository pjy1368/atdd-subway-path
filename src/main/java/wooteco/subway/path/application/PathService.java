package wooteco.subway.path.application;

import java.util.List;
import java.util.stream.Collectors;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.WeightedMultigraph;
import org.springframework.stereotype.Service;

import wooteco.subway.line.dao.SectionDao;
import wooteco.subway.line.domain.Section;
import wooteco.subway.path.dto.PathResponse;
import wooteco.subway.station.dao.StationDao;
import wooteco.subway.station.domain.Station;
import wooteco.subway.station.dto.StationResponse;

@Service
public class PathService {

    private final StationDao stationDao;
    private final SectionDao sectionDao;

    public PathService(StationDao stationDao, SectionDao sectionDao) {
        this.stationDao = stationDao;
        this.sectionDao = sectionDao;
    }

    public PathResponse shortenPath(final Long sourceId, final Long targetId) {
        final WeightedMultigraph<Station, DefaultWeightedEdge> graph = drawGraph();
        final DijkstraShortestPath<Station, DefaultWeightedEdge> dijkstraShortestPath = new DijkstraShortestPath<>(graph);

        final Station source = stationDao.findById(sourceId);
        final Station target = stationDao.findById(targetId);
        final List<StationResponse> shortestPath = makeShortestPath(dijkstraShortestPath, source, target);
        int distance = (int) dijkstraShortestPath.getPathWeight(source, target);
        return new PathResponse(shortestPath, distance);
    }

    private List<StationResponse> makeShortestPath(DijkstraShortestPath<Station, DefaultWeightedEdge> dijkstraShortestPath,
        Station source, Station target) {
        return dijkstraShortestPath.getPath(source, target).getVertexList()
            .stream()
            .map(StationResponse::of)
            .collect(Collectors.toList());
    }

    private WeightedMultigraph<Station, DefaultWeightedEdge> drawGraph() {
        final WeightedMultigraph<Station, DefaultWeightedEdge> graph = new WeightedMultigraph<>(DefaultWeightedEdge.class);
        final List<Station> stations = addVertices(graph);
        setEdgeWeights(graph, stations);
        return graph;
    }

    private List<Station> addVertices(WeightedMultigraph<Station, DefaultWeightedEdge> graph) {
        final List<Station> stations = stationDao.findAll();
        for (final Station station : stations) {
            graph.addVertex(station);
        }
        return stations;
    }

    private void setEdgeWeights(WeightedMultigraph<Station, DefaultWeightedEdge> graph, List<Station> stations) {
        final List<Section> sections = sectionDao.findAll(stations);
        for (final Section section : sections) {
            graph.setEdgeWeight(graph.addEdge(section.getUpStation(), section.getDownStation()), section.getDistance());
        }
    }
}
