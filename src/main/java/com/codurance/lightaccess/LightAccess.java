package com.codurance.lightaccess;

import com.codurance.lightaccess.connection.LAConnection;
import com.codurance.lightaccess.executables.*;

import javax.sql.DataSource;
import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.Function;

import static com.codurance.lightaccess.executables.Throwables.executeWithResource;

public class LightAccess {

    private DataSource ds;

    public LightAccess(DataSource connection) {
        this.ds = connection;
    }

    public <T> T executeQuery(SQLQuery<T> sqlQuery) {
        LAConnection conn = pgConnection();
        return executeWithResource(conn, () -> sqlQuery.execute(conn));
    }

    public void executeCommand(SQLCommand sqlCommand) {
        execute(sqlCommand);
    }

    public void executeDDLCommand(DDLCommand ddlCommand) {
        execute(ddlCommand);
    }

    public int nextId(String sequenceName) {
        return nextId(sequenceName, (x) -> x);
    }

    public <T> T nextId(String sequenceName, Function<Integer, T> nextId) {
        LAConnection conn = pgConnection();
        return executeWithResource(conn, () -> nextId.apply(sequenceNextId(sequenceName, conn)));
    }

    private int sequenceNextId(String sequenceName, LAConnection conn) throws SQLException {
        CallableStatement cs = conn.prepareCall("select nextval('" + sequenceName + "')");
        ResultSet resultSet = cs.executeQuery();
        resultSet.next();
        return resultSet.getInt(1);
    }

    private void execute(Command command) {
        LAConnection c = pgConnection();
        executeWithResource(c, () -> command.execute(c));
    }

    private LAConnection pgConnection() {
        return Throwables.executeQuery(() -> new LAConnection(ds.getConnection()));
    }
}
