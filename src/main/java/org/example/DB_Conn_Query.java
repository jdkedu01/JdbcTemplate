package org.example;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import oracle.jdbc.OracleTypes;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class DB_Conn_Query {

    private JdbcTemplate jdbcTemplate;
    private SimpleJdbcCall simpleJdbcCall;

    public DB_Conn_Query() {
        // 데이터 소스 설정
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("oracle.jdbc.OracleDriver");
        dataSource.setUrl("jdbc:oracle:thin:@localhost:1521:XE");
        dataSource.setUsername("hmart");
        dataSource.setPassword("1234");

        // JdbcTemplate 및 SimpleJdbcCall 초기화
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.simpleJdbcCall = new SimpleJdbcCall(jdbcTemplate)
                .withProcedureName("SP_잠재고객")
                .declareParameters(new SqlOutParameter("IDs", OracleTypes.CURSOR));
    }

    private void sqlRun() {
        // Select 검색. 검색결과가 NULL일 때를 고려하지 않음.
        // Primary Key Contraint 때문에 ID의 변경없이 두번실행하면 Error발생
        String query = "SELECT 고객아이디, 고객이름, 적립금 FROM 고객";
        List<Map<String, Object>> results = jdbcTemplate.queryForList(query);

        System.out.println("\t 고객ID \t 고객이름 \t 적립금 ");
        System.out.println("================================ ");
        for (Map<String, Object> row : results) {
            String 고객아이디 = (String) row.get("고객아이디");
            String 고객이름 = (String) row.get("고객이름");
            Integer 적립금 = ((Number) row.get("적립금")).intValue();
            // Integer은 Nullable
            System.out.println("\t" + 고객아이디 + "\t" + 고객이름 + "\t" + 적립금);
        }

        // INSERT 문 실행
//        String insertQuery = "INSERT INTO 고객 VALUES (?, ?, ?, ?, ?, ?)";  //Error. Why?
        String insertQuery = "INSERT INTO 고객 (고객아이디, 고객이름, 나이, 등급, 직업, 적립금) VALUES (?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(insertQuery, "JDKfruits", "김수경", 30, "Silver", "학생", 4500);
//        String Id = "Onion", Name = "홍경환";
//        jdbcTemplate.update(insertQuery, Id, Name, 30, "Silver", "학생", 4500);

        // CallableStatement를 사용한 저장 프로시저 호출 및 CURSOR 처리
        // 검색 결과가 Null일 때를 고려하지 않음
        Map<String, Object> result = simpleJdbcCall.execute();
        List<Map<String, Object>> potentialCustomers = (List<Map<String, Object>>) result.get("IDs");

        System.out.println("====== 잠재고객 명단입니다.======");
        for (Map<String, Object> potentialCustomer : potentialCustomers) {
            System.out.println(potentialCustomer.get("고객아이디") + ",\t" + potentialCustomer.get("고객이름"));
        }
    }

    public static void main(String[] args) {
        DB_Conn_Query dbconquery = new DB_Conn_Query();
        dbconquery.sqlRun();
    }
}


