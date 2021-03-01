package dracer.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Objects;

public final class PgConn {
    private static final String url = "jdbc:postgresql://localhost:5432/tracerdb";
    private static final String user = "postgres";
    private static final String password = "DNFWTF4201?!";

    /**
     * @return A SQL connection to the database.
     * @throws SQLException Could not grab a SQL connection.
     */
    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    /**
     * Used to wrap database actions that return a value.
     * @param <X> Type of result to expect from Database.
     */
    @FunctionalInterface
    private interface DatabaseAction<X> {
        X doInConnection(final Connection conn) throws SQLException;
    }

    /**
     * Query the database and return a value.
     * @param action Action to perform.
     * @param <X> Type of result to expect.
     * @return Database's result in given type.
     * @throws SQLException If something went wrong while
     * performing transaction.
     */
    public static <X> X demand(DatabaseAction<X> action) throws SQLException {
        Objects.requireNonNull(action);
        try (final Connection conn = getConnection()) {
            return action.doInConnection(conn);
        }
    }
}
