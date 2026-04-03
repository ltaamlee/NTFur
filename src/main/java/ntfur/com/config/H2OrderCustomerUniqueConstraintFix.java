package ntfur.com.config;

import java.sql.Connection;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Hibernate used to map Order→Customer as @OneToOne, which adds a UNIQUE index on
 * customer_id. That blocks a second order for the same customer. After switching to
 * @ManyToOne, existing H2 databases still keep the old constraint; this removes it.
 */
@Component
@Order(100)
@RequiredArgsConstructor
@Slf4j
public class H2OrderCustomerUniqueConstraintFix implements ApplicationRunner {

    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) {
        try (Connection c = dataSource.getConnection()) {
            if (!"H2".equalsIgnoreCase(c.getMetaData().getDatabaseProductName())) {
                return;
            }
        } catch (Exception e) {
            return;
        }

        try {
            List<String> names = jdbcTemplate.query(
                    """
                            SELECT CONSTRAINT_NAME FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS
                            WHERE TABLE_SCHEMA = 'PUBLIC' AND TABLE_NAME = 'ORDERS'
                            AND CONSTRAINT_TYPE = 'UNIQUE'
                            """,
                    (rs, row) -> rs.getString(1));

            for (String constraintName : names) {
                List<String> cols = jdbcTemplate.query(
                        """
                                SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
                                WHERE TABLE_SCHEMA = 'PUBLIC' AND TABLE_NAME = 'ORDERS'
                                AND CONSTRAINT_NAME = ?
                                ORDER BY ORDINAL_POSITION
                                """,
                        (rs, row) -> rs.getString(1),
                        constraintName);

                if (cols.size() == 1 && "CUSTOMER_ID".equalsIgnoreCase(cols.get(0))) {
                    jdbcTemplate.execute("ALTER TABLE ORDERS DROP CONSTRAINT \"" + constraintName + "\"");
                    log.info("Removed obsolete UNIQUE on orders.customer_id ({})", constraintName);
                }
            }
        } catch (Exception ex) {
            log.debug("Order customer_id unique fix skipped: {}", ex.getMessage());
        }
    }
}
