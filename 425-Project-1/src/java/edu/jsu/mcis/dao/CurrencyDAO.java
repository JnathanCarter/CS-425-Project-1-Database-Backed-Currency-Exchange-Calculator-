/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package edu.jsu.mcis.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

/**
 *
 * @author johnc
 */
public class CurrencyDAO {
    private final DAOFactory daoFactory;
    private final String QUERY_SELECT = "SELECT * FROM currency" ;
    
    CurrencyDAO(DAOFactory daoFactory){
        this.daoFactory = daoFactory;
    }

    public String find(){
                JSONArray jsonArray = new JSONArray();
                Connection conn = daoFactory.getConnection();
                PreparedStatement ps = null;
                ResultSet rs = null;

                try {

                        ps = conn.prepareStatement(QUERY_SELECT);

                        boolean hasresults = ps.execute();

                        if (hasresults) {

                                rs = ps.getResultSet();

                                while (rs.next()) {

                                        JSONObject json = new JSONObject();
                                        json.put("description", rs.getString("description"));
                                        json.put("id", rs.getString("id"));
                                        jsonArray.add(json);

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
                return JSONValue.toJSONString(jsonArray); 
          }

    

    
}