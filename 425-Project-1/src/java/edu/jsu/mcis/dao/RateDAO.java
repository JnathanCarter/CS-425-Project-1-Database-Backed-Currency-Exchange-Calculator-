package edu.jsu.mcis.dao;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.swing.text.AbstractDocument.Content;
import javax.swing.text.html.HTMLDocument.RunElement;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ContainerFactory;
import org.json.simple.parser.JSONParser;

/**
 *
 * @author johnc
 *         In hindsight this code has become complicated and needs simplifying,
 *         However it seems to work and I need to begin project 2 ASAP,
 *         My Apologies if it seems a mess, I plan to clean it up during
 *         Thanksgiving break for my own benefit.
 */
public class RateDAO {

        private final DAOFactory daoFactory;
        private final String QUERY_SELECT = "SELECT * FROM rate WHERE rate_date = ?";
        private final String QUERY_EXTERNAL_DB = "https://testbed.jaysnellen.com:8443/JSUExchangeRatesServer/rates";
        private final String QUERY_CREATE = "INSERT INTO rate (currencyid, rate_date, rate)"
                        + "VALUES (?,?,?)";

        /* Constructor */
        RateDAO(DAOFactory daoFactory) {
                this.daoFactory = daoFactory;
        }

        /**
         * Queries the database for the rate information
         * If the information does not exist,
         * then it is fetched by an external source using updateInternalDB()
         * 
         * @param date - String for date of the rates
         * @return - JSON String containing rate information
         */
        public String find(String date) {
                JSONArray jsonArray = new JSONArray();
                Connection conn = daoFactory.getConnection();
                PreparedStatement ps = null;
                ResultSet rs = null;
                JSONObject json = new JSONObject();

                try {

                        ps = conn.prepareStatement(QUERY_SELECT);
                        ps.setString(1, date);

                        boolean hasresults = ps.execute();

                        if (hasresults) {
                                rs = ps.getResultSet();
                                if (rs.next()) {
                                        // Dianostic Print
                                        System.err.print(rs);

                                        Map results = new LinkedHashMap<String, String>();

                                        json.put("date", date);
                                        results.put(rs.getString("currencyid"), rs.getDouble("rate"));
                                        while (rs.next()) {
                                                results.put(rs.getString("currencyid"), rs.getDouble("rate"));

                                        }
                                        json.put("rates", results);

                                }
                                // If rates for the date is not found then the DB is update from external source
                                else {
                                        return updateInternalDB(date);
                                }
                        }

                } catch (Exception e) {
                        e.printStackTrace();
                } finally {

                        if (rs != null) {
                                try {
                                        rs.close();
                                        rs = null;
                                } catch (Exception e) {
                                        e.printStackTrace();
                                }
                        }
                        if (ps != null) {
                                try {
                                        ps.close();
                                        ps = null;
                                } catch (Exception e) {
                                        e.printStackTrace();
                                }
                        }
                        if (conn != null) {
                                try {
                                        conn.close();
                                        conn = null;
                                } catch (Exception e) {
                                        e.printStackTrace();
                                }
                        }

                }
                return JSONValue.toJSONString(json);
        }

        /**
         * Queries external source for additional rate information,
         * Then updates the local DB & returns this new rate data in JSON format
         * 
         * @param date - String for date of the rates
         * @return - JSON String of rate data
         */
        public String updateInternalDB(String date) {

                // request from external api
                Connection conn = daoFactory.getConnection();
                PreparedStatement ps = null;
                ResultSet rs = null;

                try {
                        // HTTP GET request to external api for rate data
                        URL url = new URL("https://testbed.jaysnellen.com:8443/JSUExchangeRatesServer/rates");
                        HttpURLConnection con = (HttpURLConnection) url.openConnection();
                        con.setRequestMethod("GET");

                        BufferedReader in = new BufferedReader(new InputStreamReader(
                                        con.getInputStream()));
                        String inputLine;
                        String output = "";
                        while ((inputLine = in.readLine()) != null) {
                                System.out.println(inputLine);
                                output = inputLine;

                        }
                        in.close();

                        // Dianostic Print
                        System.out.println("output-----------" + output);

                        // Parse the output of HTTP Request to external API
                        JSONParser parser = new JSONParser();
                        JSONObject json = (JSONObject) parser.parse(output);

                        // Diagnostic Print
                        Object rates = json.get("rates");
                        System.out.println(JSONValue.toJSONString(rates));

                        // Make map of rates
                        ContainerFactory containerFactory = new ContainerFactory() {
                                @Override
                                public Map createObjectContainer() {
                                        return new LinkedHashMap<>();
                                }

                                @Override
                                public List creatArrayContainer() {
                                        return new LinkedList<>();
                                }
                        };
                        Map map = (Map) parser.parse(JSONValue.toJSONString(json.get("rates")), containerFactory);

                        /*
                         * If the addition to the database is successful,
                         * the find method is called again to return the newly added data
                         */
                        if (addCurrencyData(map, date)) {
                                this.find(date);
                        }

                } catch (Exception e) {
                        e.printStackTrace();
                } finally {

                        if (rs != null) {
                                try {
                                        rs.close();
                                        rs = null;
                                } catch (Exception e) {
                                        e.printStackTrace();
                                }
                        }
                        if (ps != null) {
                                try {
                                        ps.close();
                                        ps = null;
                                } catch (Exception e) {
                                        e.printStackTrace();
                                }
                        }
                        if (conn != null) {
                                try {
                                        conn.close();
                                        conn = null;
                                } catch (Exception e) {
                                        e.printStackTrace();
                                }
                        }

                }

                // Use find method to re-Query database that now includes the new data from
                // extern. API
                return this.find(date);
        }

        /**
         * Accepts Map of rate data and data to add to local DB
         * 
         * @param map
         * @param date
         * @return
         */
        private Boolean addCurrencyData(Map map, String date) {

                PreparedStatement ps = null;
                ResultSet rs = null;

                try {

                        Connection conn = daoFactory.getConnection();
                        ps = conn.prepareStatement(QUERY_CREATE);

                        Iterator<Map.Entry<String, Object>> itr = map.entrySet().iterator();
                        while (itr.hasNext()) {
                                Map.Entry<String, Object> entry = itr.next();
                                System.out.println("Key = " + entry.getKey() +
                                                ", Value = " + entry.getValue());

                                ps.setString(1, entry.getKey());
                                ps.setString(2, date);
                                ps.setDouble(3, (Double) entry.getValue());
                                ps.addBatch();

                        }
                        int[] r = ps.executeBatch();
                        conn.commit();

                        int updateCount = ps.executeUpdate();

                        if (updateCount > 0) {
                                return true;
                        }

                } catch (Exception e) {
                        e.printStackTrace();
                }

                finally {

                        if (rs != null) {
                                try {
                                        rs.close();
                                } catch (Exception e) {
                                        e.printStackTrace();
                                }
                        }
                        if (ps != null) {
                                try {
                                        ps.close();
                                } catch (Exception e) {
                                        e.printStackTrace();
                                }
                        }

                }
                return false;
        }
}

// Descarded code

// class ParameterStringBuilder {
// public static String getParamsString(Map<String, String> params)
// throws UnsupportedEncodingException {
// StringBuilder result = new StringBuilder();

// for (Map.Entry<String, String> entry : params.entrySet()) {
// result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
// result.append("=");
// result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
// result.append("&");
// }

// String resultString = result.toString();
// return resultString.length() > 0
// ? resultString.substring(0, resultString.length() - 1)
// : resultString;
// }
