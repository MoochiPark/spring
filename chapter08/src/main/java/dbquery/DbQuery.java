package dbquery;

import javax.sql.DataSource;
import java.sql.*;

public class DbQuery {

  private DataSource dataSource;

  public DbQuery(final DataSource dataSource) {
    this.dataSource = dataSource;
  }

  public int count() {
    Connection conn = null;
    try {
      conn = dataSource.getConnection(); // 풀에서 구함
      String query = "select count(*) from MEMBER";
      try (PreparedStatement ps = conn.prepareStatement(query);
           ResultSet rs = ps.executeQuery()) {
        rs.next();
        return rs.getInt(1);
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    } finally {
      if (conn != null)
        try {
          conn.close(); // 풀에 반환
        } catch (SQLException e) {
          e.printStackTrace();
        }
    }
  }

}
