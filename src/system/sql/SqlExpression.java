package system.sql;

import java.util.Collections;

public class SqlExpression {
	private final String sql;
	private final Iterable<Object> parameters;
	
	public SqlExpression(String sql) {
		this(sql, Collections.emptyList());
	}
	
	public SqlExpression(String sql, Iterable<Object> params) {
		this.sql = sql;
		this.parameters = params;
	}

	public String getSql() {
		return sql;
	}

	public Iterable<Object> getParameters() {
		return parameters;
	}
}
