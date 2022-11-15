/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
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
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.text.AbstractDocument.Content;
import javax.swing.text.html.HTMLDocument.RunElement;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

/**
 *
 * @author johnc
 */
public class RateDAO {

        private final DAOFactory daoFactory;
        private final String QUERY_SELECT = "SELECT * FROM rate WHERE rate_date = ?";
        private final String QUERY_EXTERNAL_DB = "https://testbed.jaysnellen.com:8443/JSUExchangeRatesServer/rates";
        private final String QUERY_CREATE = "INSERT INTO rate (currencyid, rate_date, rate)"
                        + "VALUES (?,?,?)";

        RateDAO(DAOFactory daoFactory) {
                this.daoFactory = daoFactory;
        }

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

                                        System.err.print(rs);

                                        Map results = new LinkedHashMap<String, String>();

                                        json.put("date", date);
                                        results.put(rs.getString("currencyid"), rs.getDouble("rate"));
                                        while (rs.next()) {
                                                results.put(rs.getString("currencyid"), rs.getDouble("rate"));

                                        }
                                        json.put("rates", results);

                                } else {
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

        public String updateInternalDB(String date) {

                // request from external api
                Connection conn = daoFactory.getConnection();
                PreparedStatement ps = null;
                ResultSet rs = null;

                try {
                    // get information from external api
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
                        System.out.println("output-----------"+output);
                        in.close();
                        
                    //parse the output 
                    
                    //add to internal db



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
                // instead of adding to json object add each entry to the database

                // call find again
                // return this.find(date);
                return "";
        }

        private void addCurrencyData(String date, String currencyId, Double rate) {

                PreparedStatement ps = null;
                ResultSet rs = null;

                try {
                        Connection conn = daoFactory.getConnection();

                        ps = conn.prepareStatement(QUERY_CREATE);
                        ps.setString(1, currencyId);
                        ps.setString(2, date);
                        ps.setDouble(3, rate);

                        int updateCount = ps.executeUpdate();

                        if (updateCount > 0) {
                                return;
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
        }
}

class ParameterStringBuilder {
        public static String getParamsString(Map<String, String> params)
                        throws UnsupportedEncodingException {
                StringBuilder result = new StringBuilder();

                for (Map.Entry<String, String> entry : params.entrySet()) {
                        result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
                        result.append("=");
                        result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
                        result.append("&");
                }

                String resultString = result.toString();
                return resultString.length() > 0
                                ? resultString.substring(0, resultString.length() - 1)
                                : resultString;
        }
}
