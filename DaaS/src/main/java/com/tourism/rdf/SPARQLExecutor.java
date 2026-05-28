package com.tourism.rdf;

import com.tourism.models.TourismPlace;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;

import java.util.ArrayList;
import java.util.List;

public class SPARQLExecutor {

    private final String PREFIXES = """
            PREFIX tourism: <http://odws.univaq.it/tourism/>
            PREFIX dbo: <http://dbpedia.org/ontology/>
            PREFIX dbr: <http://dbpedia.org/resource/>
            PREFIX schema: <https://schema.org/>
            PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
            """;

    private final String SELECT_FIELDS = """
            SELECT ?place ?name ?location ?category ?sameAs
                   ?declaredRiskLevel
                   ?wheelchairAccessible ?stepFreeAccess ?accessibleToilets
                   ?publicTransportAvailable ?visitorPressure
                   ?environmentalSensitivity ?localCommunityImpact
                   ?sustainabilityScore
                   ?dataSource ?dataLicense ?lastUpdated
            """;

    private final String COMMON_WHERE = """
            WHERE {
                ?place rdf:type schema:TouristAttraction ;
                       schema:name ?name ;
                       dbo:location ?location ;
                       schema:category ?category ;
                       schema:sameAs ?sameAs ;

                       tourism:declaredRiskLevel ?declaredRiskLevel ;

                       tourism:wheelchairAccessible ?wheelchairAccessible ;
                       tourism:stepFreeAccess ?stepFreeAccess ;
                       tourism:accessibleToilets ?accessibleToilets ;

                       tourism:publicTransportAvailable ?publicTransportAvailable ;
                       tourism:visitorPressure ?visitorPressure ;
                       tourism:environmentalSensitivity ?environmentalSensitivity ;
                       tourism:localCommunityImpact ?localCommunityImpact ;
                       tourism:sustainabilityScore ?sustainabilityScore ;

                       tourism:dataSource ?dataSource ;
                       tourism:dataLicense ?dataLicense ;
                       tourism:lastUpdated ?lastUpdated .

                %s
            }
            ORDER BY ?name
            """;

    private List<TourismPlace> execute(Model model, String filterBlock) {

        String queryString = PREFIXES + SELECT_FIELDS + COMMON_WHERE.formatted(filterBlock);

        Query query = QueryFactory.create(queryString);

        List<TourismPlace> places = new ArrayList<>();

        try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {

            ResultSet results = qexec.execSelect();

            while (results.hasNext()) {

                QuerySolution sol = results.nextSolution();

                String id = getString(sol, "place");
                String name = getString(sol, "name");
                String location = normalizeLocation(getString(sol, "location"));
                String category = getString(sol, "category");
                String sameAs = getString(sol, "sameAs");

                String declaredRiskLevel = getString(sol, "declaredRiskLevel");

                boolean wheelchairAccessible = getBoolean(sol, "wheelchairAccessible");
                boolean stepFreeAccess = getBoolean(sol, "stepFreeAccess");
                boolean accessibleToilets = getBoolean(sol, "accessibleToilets");

                boolean publicTransportAvailable = getBoolean(sol, "publicTransportAvailable");
                String visitorPressure = getString(sol, "visitorPressure");
                String environmentalSensitivity = getString(sol, "environmentalSensitivity");
                String localCommunityImpact = getString(sol, "localCommunityImpact");
                int sustainabilityScore = getInt(sol, "sustainabilityScore");

                String dataSource = getString(sol, "dataSource");
                String dataLicense = getString(sol, "dataLicense");
                String lastUpdated = getString(sol, "lastUpdated");

                TourismPlace place = new TourismPlace(
                        id,
                        name,
                        location,
                        category,
                        sameAs,
                        declaredRiskLevel,
                        wheelchairAccessible,
                        stepFreeAccess,
                        accessibleToilets,
                        publicTransportAvailable,
                        visitorPressure,
                        environmentalSensitivity,
                        localCommunityImpact,
                        sustainabilityScore,
                        dataSource,
                        dataLicense,
                        lastUpdated
                );

                places.add(place);
            }
        }

        return places;
    }

    public List<TourismPlace> getAccessiblePlaces(Model model) {

        String filterBlock = """
                FILTER(?wheelchairAccessible = true)
                """;

        return execute(model, filterBlock);
    }

    public List<TourismPlace> getSustainablePlaces(Model model) {

        String filterBlock = """
                FILTER(?sustainabilityScore >= 7)
                """;

        return execute(model, filterBlock);
    }

    public List<TourismPlace> getPlacesByLocation(Model model, String locationInput) {

        String safeLocation = locationInput
                .trim()
                .replace(" ", "_");

        String filterBlock = """
                FILTER(?location = dbr:%s)
                """.formatted(safeLocation);

        return execute(model, filterBlock);
    }

    public List<TourismPlace> getPlacesByCategory(Model model, String categoryInput) {

        String safeCategory = escapeForSparqlString(
                categoryInput
                        .trim()
                        .toLowerCase()
        );

        String filterBlock = """
                FILTER(
                    CONTAINS(
                        LCASE(STR(?category)),
                        "%s"
                    )
                )
                """.formatted(safeCategory);

        return execute(model, filterBlock);
    }

    public List<TourismPlace> getRecommendedPlaces(Model model) {
    	
        String filterBlock = """
                FILTER(
                    ?wheelchairAccessible = true &&
                    ?publicTransportAvailable = true &&
                    ?sustainabilityScore >= 7
                )
                """;

        return execute(model, filterBlock);
    }

    public TourismPlace getPlaceById(Model model, String id) {

        String placeReference = buildPlaceReference(id);

        String filterBlock = """
                FILTER(?place = %s)
                """.formatted(placeReference);

        List<TourismPlace> places = execute(model, filterBlock);

        if (places.isEmpty()) {
            return null;
        }

        return places.get(0);
    }

    private String getString(QuerySolution sol, String variableName) {

        if (!sol.contains(variableName)) {
            return "";
        }

        RDFNode node = sol.get(variableName);

        if (node == null) {
            return "";
        }

        if (node.isLiteral()) {
            return node.asLiteral().getString();
        }

        return node.toString();
    }

    private boolean getBoolean(QuerySolution sol, String variableName) {

        if (!sol.contains(variableName)) {
            return false;
        }

        RDFNode node = sol.get(variableName);

        if (node == null || !node.isLiteral()) {
            return false;
        }

        return node.asLiteral().getBoolean();
    }

    private int getInt(QuerySolution sol, String variableName) {

        if (!sol.contains(variableName)) {
            return 0;
        }

        RDFNode node = sol.get(variableName);

        if (node == null || !node.isLiteral()) {
            return 0;
        }

        return node.asLiteral().getInt();
    }

    private String normalizeLocation(String location) {

        if (location == null) {
            return "";
        }

        return location
                .replace("http://dbpedia.org/resource/", "")
                .replace("_", " ");
    }

    private String escapeForSparqlString(String value) {

        if (value == null) {
            return "";
        }

        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }

    private String buildPlaceReference(String id) {

        String safeId = id.trim();

        if (safeId.startsWith("http://") || safeId.startsWith("https://")) {
            return "<" + safeId + ">";
        }

        return "tourism:" + safeId;
    }
}