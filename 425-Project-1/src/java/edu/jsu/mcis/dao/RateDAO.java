/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package edu.jsu.mcis.dao;

import java.lang.reflect.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.LinkedHashMap;
import java.util.Map;

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
                return "this wasnt in db";
        }
}
