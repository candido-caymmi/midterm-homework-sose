package com.tourism.rdf;

import com.tourism.models.TourismPlace;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;

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

    private List<TourismPlace> execute(Model model, String queryBody) {

        String queryString = PREFIXES + queryBody;

        Query query = QueryFactory.create(queryString);

        List<TourismPlace> places = new ArrayList<>();

        try (QueryExecution qexec =
                     QueryExecutionFactory.create(query, model)) {

            ResultSet results = qexec.execSelect();

            while (results.hasNext()) {

                QuerySolution sol = results.nextSolution();

                String name = sol.get("name").toString();

                String location = sol.get("location")
                        .toString()
                        .replace("http://dbpedia.org/resource/", "");

                String category = sol.get("category").toString();

                String sustainability = sol.get("sustainability").toString();

                boolean accessible =
                        sol.get("accessible")
                                .asLiteral()
                                .getBoolean();

                String riskLevel = sol.get("riskLevel").toString();

                String sameAs = sol.contains("sameAs")
                        ? sol.get("sameAs").toString()
                        : "";

                TourismPlace place =
                        new TourismPlace(
                                name,
                                location,
                                category,
                                sustainability,
                                accessible,
                                riskLevel,
                                sameAs
                        );

                places.add(place);
            }
        }

        return places;
    }

    public List<TourismPlace> getAccessiblePlaces(Model model) {

        String queryBody = """
                SELECT ?name ?location ?category ?sustainability ?accessible ?riskLevel ?sameAs
                WHERE {
                    ?place rdf:type schema:TouristAttraction ;
                           dbo:name ?name ;
                           dbo:location ?location ;
                           schema:category ?category ;
                           schema:sameAs ?sameAs ;
                           tourism:accessible ?accessible ;
                           tourism:sustainability ?sustainability ;
                           tourism:riskLevel ?riskLevel .

                    FILTER(?accessible = true)
                }
                ORDER BY ?name
                """;

        return execute(model, queryBody);
    }

    public List<TourismPlace> getSustainablePlaces(Model model) {

        String queryBody = """
                SELECT ?name ?location ?category ?sustainability ?accessible ?riskLevel ?sameAs
                WHERE {
                    ?place rdf:type schema:TouristAttraction ;
                           dbo:name ?name ;
                           dbo:location ?location ;
                           schema:category ?category ;
                           schema:sameAs ?sameAs ;
                           tourism:accessible ?accessible ;
                           tourism:sustainability ?sustainability ;
                           tourism:riskLevel ?riskLevel .

                    FILTER(?sustainability = "high")
                }
                ORDER BY ?name
                """;

        return execute(model, queryBody);
    }

    public List<TourismPlace> getPlacesByLocation(Model model, String locationInput) {

        String safeLocation =
                locationInput
                        .trim()
                        .replace(" ", "_");

        String queryBody = """
                SELECT ?name ?location ?category ?sustainability ?accessible ?riskLevel ?sameAs
                WHERE {
                    ?place rdf:type schema:TouristAttraction ;
                           dbo:name ?name ;
                           dbo:location ?location ;
                           schema:category ?category ;
                           schema:sameAs ?sameAs ;
                           tourism:accessible ?accessible ;
                           tourism:sustainability ?sustainability ;
                           tourism:riskLevel ?riskLevel .

                    FILTER(?location = dbr:%s)
                }
                ORDER BY ?name
                """.formatted(safeLocation);

        return execute(model, queryBody);
    }

    public List<TourismPlace> getPlacesByCategory(Model model, String categoryInput) {

        String safeCategory =
                categoryInput
                        .trim()
                        .toLowerCase();

        String queryBody = """
                SELECT ?name ?location ?category ?sustainability ?accessible ?riskLevel ?sameAs
                WHERE {
                    ?place rdf:type schema:TouristAttraction ;
                           dbo:name ?name ;
                           dbo:location ?location ;
                           schema:category ?category ;
                           schema:sameAs ?sameAs ;
                           tourism:accessible ?accessible ;
                           tourism:sustainability ?sustainability ;
                           tourism:riskLevel ?riskLevel .

                    FILTER(
                        CONTAINS(
                            LCASE(STR(?category)),
                            "%s"
                        )
                    )
                }
                ORDER BY ?name
                """.formatted(safeCategory);

        return execute(model, queryBody);
    }

    public List<TourismPlace> getRecommendedPlaces(Model model) {

        String queryBody = """
                SELECT ?name ?location ?category ?sustainability ?accessible ?riskLevel ?sameAs
                WHERE {
                    ?place rdf:type schema:TouristAttraction ;
                           dbo:name ?name ;
                           dbo:location ?location ;
                           schema:category ?category ;
                           schema:sameAs ?sameAs ;
                           tourism:accessible ?accessible ;
                           tourism:sustainability ?sustainability ;
                           tourism:riskLevel ?riskLevel .

                    FILTER(
                        ?accessible = true &&
                        ?sustainability = "high" &&
                        ?riskLevel = "low"
                    )
                }
                ORDER BY ?name
                """;

        return execute(model, queryBody);
    }

    public TourismPlace getPlaceById(Model model, String id) {

        String safeId = id.trim();

        String queryBody = """
                SELECT ?name ?location ?category ?sustainability ?accessible ?riskLevel ?sameAs
                WHERE {
                    tourism:%s rdf:type schema:TouristAttraction ;
                           dbo:name ?name ;
                           dbo:location ?location ;
                           schema:category ?category ;
                           schema:sameAs ?sameAs ;
                           tourism:accessible ?accessible ;
                           tourism:sustainability ?sustainability ;
                           tourism:riskLevel ?riskLevel .
                }
                """.formatted(safeId);

        List<TourismPlace> places = execute(model, queryBody);

        if (places.isEmpty()) {
            return null;
        }

        return places.get(0);
    }
}