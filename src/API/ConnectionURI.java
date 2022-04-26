package API;

import java.net.URI;

/**
 * Creates a URI for use by Connection.
 * Instances should be created once for each hostname to be accessed
 */
public class ConnectionURI {

    public static final int HTTPS_PORT=443;

    public static final String ON_DEMAND ="https://ondemand.rit.edu";
    public static final String SITE_DOWN = "/sitedown";
    public static final String LOGIN = "/api/login/anonymous";
    public static final String CONFIG = "/api/config";

    public String locations;
    public String locationMain;
    public String locationConcepts;
    public String menuAddon;

    public final String businessID;
    public final String tenantID;


    public ConnectionURI(String businessID, String tenantID) {
        this.businessID=businessID;
        this.tenantID=tenantID;
        populateURIs();
    }

    /**
     * Returns a URI for the API to use
     * Path should be the string immediately following the domain,
     * ex: "/foo/bar". Do not specify the port number.
     *
     * @param path path after the hostname
     * @return URI
     */
    public static URI getURI(String path) {
        return URI.create(ON_DEMAND+
                //":"+HTTPS_PORT+ //port number
                path);
    }

    private void populateURIs() {
        locations = "/api/sites/" + businessID; //tenantID can actually be replaced with any string
        //
        locationMain = locations; //add the vendor id
        locationConcepts = locations + "/" + businessID + "/concepts/"; //add the vendor id
        menuAddon = "/menus/";
    }
}
