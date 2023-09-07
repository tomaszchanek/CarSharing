package carsharing.persistance;

import java.sql.ResultSet;
import java.sql.SQLException;

interface RowMapper<T> {
    T mapRow(ResultSet resultSet) throws SQLException;
}
