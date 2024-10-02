package origin;

import java.util.*;
import java.math.*;
import java.sql.*;

public class Calculation {
    private Map<String,BigDecimal> fxRates = new HashMap<String,BigDecimal>();

    // @Autowired
    Connection conn;

    public Calculation(Connection conn) {
        this.conn = conn;
    }

    public void calculate() {
        List<Map<String,Object>> result = new ArrayList<>();
        
        try {
            // Establish database connection
            
            // Create SQL query
            String query = "SELECT TRD_SPOT_AMT, TRD_SPOT_ACCT, TRD_SPOT_CCY, TRD_SPOT_TRD_DT FROM TRADERX_SPOTS_VW";
            
            // Execute query
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            
            // Process query results
            while (rs.next()) {
                Map<String,Object> resultMap = new HashMap<>();
                resultMap.put("TRD_SPOT_ACCT", rs.getString("TRD_SPOT_ACCT"));
                resultMap.put("TRD_SPOT_AMT", new BigInteger(rs.getString("TRD_SPOT_AMT")));
                resultMap.put("TRD_SPOT_PRC", new BigDecimal(rs.getString("TRD_SPOT_PRC")));
                resultMap.put("TRD_SPOT_CCY", rs.getString("TRD_SPOT_CCY"));
                resultMap.put("TRD_SPOT_TRD_DT", rs.getObject("TRD_SPOT_TRD_DT"));
                result.add(resultMap);
            }
            
            // Close database connection
            rs.close();
            stmt.close();

            // Do the FX rates
            for(Map<String,Object> resultMap : result) {
                BigDecimal amount = resultMap.get("TRD_SPOT_AMT") instanceof BigInteger ?
                        BigDecimal.valueOf( ((BigInteger)resultMap.get("TRD_SPOT_AMT")).longValue() )
                        : BigDecimal.ZERO;
                BigDecimal price = resultMap.get("TRD_SPOT_PRC") instanceof BigDecimal ?
                        (BigDecimal)resultMap.get("TRD_SPOT_PRC")
                        : BigDecimal.ZERO;
                BigDecimal value = amount.multiply(price);
                resultMap.put("TRD_SPOT_VAL", value);


                // OK, now save the results back to the TRADERX_SPOTS_ENRICHED table
                String insertQuery = "INSERT INTO TRADERX_SPOTS_ENRICHED (TRD_SPOT_ACCT, TRD_SPOT_AMT, TRD_SPOT_PRC, TRD_SPOT_CCY, TRD_SPOT_TRD_DT, TRD_SPOT_VAL) VALUES (?, ?, ?, ?, ?, ?)";
                PreparedStatement insertStmt = conn.prepareStatement(insertQuery);
                insertStmt.setString(1, (String)resultMap.get("TRD_SPOT_ACCT"));
                insertStmt.setBigDecimal(2, (BigDecimal)resultMap.get("TRD_SPOT_AMT"));
                insertStmt.setBigDecimal(3, (BigDecimal)resultMap.get("TRD_SPOT_PRC"));
                insertStmt.setString(4, (String)resultMap.get("TRD_SPOT_CCY"));
                insertStmt.setObject(5, resultMap.get("TRD_SPOT_TRD_DT"));
                insertStmt.setBigDecimal(6, (BigDecimal)resultMap.get("TRD_SPOT_VAL"));
                insertStmt.executeUpdate();

                insertStmt.close();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
