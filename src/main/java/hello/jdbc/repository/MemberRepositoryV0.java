package hello.jdbc.repository;

import hello.jdbc.connection.DBConnectionUtil;
import hello.jdbc.domain.Member;
import lombok.extern.slf4j.Slf4j;

import java.sql.*;
import java.util.NoSuchElementException;

import static hello.jdbc.connection.DBConnectionUtil.getConnection;

/**
 * JDBC - DriverManager 사용
 */
@Slf4j
public class MemberRepositoryV0 {
    public Member save(Member member) throws SQLException {
        String sql = "insert into member(member_id, money) values(?, ?)";

        Connection con = null;
        PreparedStatement pstmt = null;
        // DB로 쿼리를 날리는 객체. Statement: SQL 그대로 넣음. PreparedStmt: (?,?)로 파라미터 바인딩. Statement를 상속받음.

        try {
            con = getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, member.getMemberId());
            pstmt.setInt(2, member.getMoney());
            pstmt.executeUpdate();
            return member;
        } catch (SQLException e) {
            log.error("db error", e);
            throw e;
        } finally { // close를 위에 두면, 위쪽에서 예외 날 시 close문이 실행 안 됨. 따라서 반드시 실행되는 finally에 담음.
            close(con, pstmt, null);
        }
    }

    public Member findById(String memberId) throws SQLException {
        String sql = "select * from member where member_id=?";

        Connection con = null; // 미리 선언해놓는 이유: try-catch, finally 에서 각각 매번 호출해야 해서.
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            con = getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, memberId );

            rs = pstmt.executeQuery();
            if (rs.next()) {
                Member member = new Member();
                member.setMemberId(rs.getString("member_id"));
                member.setMoney(rs.getInt("money"));
                return member;
            } else {
                throw new NoSuchElementException("member not found memberID =" + memberId);
            }

        } catch (SQLException e){
            log.error("db select error", e);
            throw e;
        } finally {
            close(con, pstmt, rs);
        }
    }


        // 만약 stmt.close()에서 Exception 나면? con.close() 수행 안 됨. -> 전부 try catch로 묶어야 함 (코드 안정성)
        private void close(Connection con, Statement stmt, ResultSet rs) {

            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    log.info("rs close error", e);
                }
            }

            if (stmt != null) {
                try {
                    stmt.close();  // 꼭 닫아준다. 열었던 순서의 역순으로. 안 닫으면 TCP 연결 지속.
                } catch (SQLException e) {
                    log.info("stmt close error", e);
                }
            }

            if (con != null) {
                try {
                    con.close();
                } catch (SQLException e) {
                    log.info("con close error", e);
                }
            }
        }
    private Connection getConnection() {
        return DBConnectionUtil.getConnection();
    }
}
