package subway.domain.path;

import static java.util.stream.Collectors.*;

import java.util.List;

import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.WeightedMultigraph;

import subway.domain.Sections;
import subway.domain.Station;

public class JGraphTPathFinder implements PathFinder {

    private final List<Station> pathVerticies;
    private final Sections pathEdges;

    public JGraphTPathFinder(Sections sections, Station source, Station target) {
        GraphPath<Station, SectionWeightedEdge> shortestPath;
        shortestPath = makeGraphPath(sections, source, target);

        pathVerticies = setPathVerticies(shortestPath);
        pathEdges = new Sections(shortestPath.getEdgeList().stream()
            .map(SectionWeightedEdge::toSection)
            .collect(toList()));
    }

    private GraphPath<Station, SectionWeightedEdge> makeGraphPath(Sections sections,
        Station source, Station target) {
        GraphPath<Station, SectionWeightedEdge> shortestPath;
        try {
            shortestPath = getPath(sections, source, target);
        } catch (IllegalArgumentException exception) {
            throw new PathException("노선과 연결되지 않은 역이 입력되었습니다.");
        }
        return shortestPath;
    }

    private List<Station> setPathVerticies(GraphPath<Station, SectionWeightedEdge> shortestPath) {
        List<Station> pathVerticies;
        try {
            pathVerticies = shortestPath.getVertexList();
        } catch (NullPointerException exception) {
            throw new PathException("두 역이 연결되지 않았습니다.");
        }
        return pathVerticies;
    }

    private GraphPath<Station, SectionWeightedEdge> getPath(Sections sections, Station source, Station target) {
        WeightedMultigraph<Station, SectionWeightedEdge> graph = makeGraph(sections);
        DijkstraShortestPath<Station, SectionWeightedEdge> shortestPath = new DijkstraShortestPath<>(graph);
        return shortestPath.getPath(source, target);
    }

    private WeightedMultigraph<Station, SectionWeightedEdge> makeGraph(Sections sections) {
        WeightedMultigraph<Station, SectionWeightedEdge> graph = new WeightedMultigraph<>(SectionWeightedEdge.class);

        List<Station> stations = sections.getDistinctStations();
        List<SectionWeightedEdge> sectionWeightedEdges = SectionWeightedEdge.toSectionWeightedEdges(sections);

        stations.forEach(graph::addVertex);
        sectionWeightedEdges.forEach(edge -> graph.addEdge(edge.getSource(), edge.getTarget(), edge));
        sectionWeightedEdges.forEach(edge -> graph.setEdgeWeight(edge, edge.getWeight()));
        return graph;
    }

    @Override
    public List<Station> getPathVerticies() {
        return pathVerticies;
    }

    @Override
    public Sections getPathEdges() {
        return pathEdges;
    }
}
