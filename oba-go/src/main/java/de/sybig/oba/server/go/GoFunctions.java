/*
 * Created on May 10, 2016
 *
 */
package de.sybig.oba.server.go;

import de.sybig.oba.server.HtmlBase;
import de.sybig.oba.server.OntologyFunction;
import de.sybig.oba.server.OntologyFunctions;
import static de.sybig.oba.server.go.Db.isValidEnsID;
import java.io.IOException;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Vector;
import javax.ws.rs.*;
import org.semanticweb.owlapi.model.OWLClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GoFunctions extends OntologyFunctions implements
        OntologyFunction {

    private Logger log = LoggerFactory.getLogger(GoFunctions.class);
    private Properties goProps;

    /**
     * A class providing ontology functions specific for Go.
     *
     */
    public GoFunctions() {
        super();

        goProps = new Properties();
        try {
            goProps.load(getClass().getResourceAsStream(
                    "/go.properties"));
        } catch (IOException e) {
            log.error("could not load properties for go function class");
            e.printStackTrace();
        }
    }

    @Override
    public String getVersion() {
        return "1.3";
    }

    /**
     * Gets a short documentation of the implemented functions in html.
     */
    @GET
    @Path("/")
    @Produces("text/html")
    @Override
    public String getRoot() {
        StringBuffer out = new StringBuffer();

        out.append("<h1>Available Functions</h1>\n");
        out.append("<dl>");
        out.append("<dt>/GoListForEnsID/{geneEnsID}</dt><dd>Gets a list of Go classes for an ENSEMBL gene ID for different species.</dd>");
        return out.toString();
    }

    /**
     * Returns a list of GO classes for an ENSEMBL ID.
     */
    @GET
    @Path("/GoListForEnsID/{geneEnsID}")
    @Produces("text/plain, application/json, text/html")
    @HtmlBase("../../../cls/")
    public List<OWLClass> GoListForEnsID(@PathParam("geneEnsID") String geneEnsID) throws SQLException {
        StringBuffer out = new StringBuffer();
        List<OWLClass> outList = new LinkedList<OWLClass>();
        Vector<String> list = new Vector<String>();
        String specie = Db.findSpecieFromID(geneEnsID);
        if (specie == null) {
            log.error("INVALID SPECIE");
            return null;
        }

        list = retrieveGeneByID(geneEnsID);
        if (list == null) {
            return null;
        } else {
            out.append("<h1>The list of GO classes for the corresponding ENSEMBL Gene ID " + geneEnsID + "</h1>\n");
            out.append("<dl>");
            for (int i = 0; i < list.size(); i++) {
                OWLClass cls = ontology.getOntologyClass(list.elementAt(i).replace(':', '_'), "http://purl.org/obo/owl/GO");
                if (!outList.contains(cls)) {
                    outList.add(cls);
                }
            }
        }

        return outList;

    }

    public Vector retrieveGeneByID(String geneEnsID) throws SQLException {
        Vector<String> list = new Vector<String>();
        String sp = Db.findSpecieFromID(geneEnsID);
        if (sp == null) {
            log.error("INVALID SPECIE");
            return null;
        }
        Db.connect(goProps.getProperty("urlDB"), goProps.getProperty("driver"));
        if (!isValidEnsID(geneEnsID, sp)) {
            return null;
        } else {
            list = Db.getGOID(geneEnsID, sp);
        }
        Db.close();
        return list;

    }
}
