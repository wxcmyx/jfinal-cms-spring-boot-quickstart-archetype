package ${package}.common;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SqlBuilder {

	private static final String AND = ") AND (";
	private static final String OR = ") OR (";
	private static final Joiner JOINER = Joiner.on("").skipNulls();
	private String baseJoin;

	private List params=new ArrayList();

	public SqlBuilder addParams(Object obj){
		params.add(obj);
		return this;
	}
	public Object[] params(){
		return params.toArray();
	}

	public enum StatementType {
		DELETE, INSERT, SELECT, UPDATE, SUB_WHERE
	}

	public enum JoinType {
		INNER, LEFT_OUTER, RIGHT_OUTER, OUTER
	}

	/**
	 * @param table 表名
	 * @param alias 别名
	 * @param idColumn id字段名
	 * @return
	 */
	public SqlBuilder addTable(String table, String alias, String idColumn) {
		return addTable(table, alias, idColumn, JoinType.INNER);
	}

	public SqlBuilder addTable(String table, String alias, String idColumn, JoinType joinType) {
		String fullTableName = JOINER.join(table, " ", alias);
		if (!sql().tables.contains(fullTableName)) {
			if (sql().tables.size() == 0) {
				this.SELECT(alias, ".", idColumn);
				this.FROM(fullTableName);
				baseJoin = JOINER.join(alias, ".", idColumn);
			} else {
				switch (joinType) {
				case INNER:
					this.INNER_JOIN(fullTableName, " ON ", baseJoin, " = ", alias, ".", idColumn);
					break;
				case LEFT_OUTER:
					this.LEFT_OUTER_JOIN(fullTableName, " ON ", baseJoin, " = ", alias, ".", idColumn);
					break;
				case RIGHT_OUTER:
					this.RIGHT_OUTER_JOIN(fullTableName, " ON ", baseJoin, " = ", alias, ".", idColumn);
					break;
				case OUTER:
					this.OUTER_JOIN(fullTableName, " ON ", baseJoin, " = ", alias, ".", idColumn);
					break;
				}
			}
		}
		return this;
	}

	private SQL sql = new SQL();

	private List<SqlBuilder> subConditionBuilders = new ArrayList<SqlBuilder>(0);

	public SqlBuilder subCondition() {
		SqlBuilder result = new SqlBuilder();
		result.sql().statementType = StatementType.SUB_WHERE;
		subConditionBuilders.add(result);
		return result;
	}

	public SqlBuilder UPDATE(String... table) {
		sql().statementType = StatementType.UPDATE;
		sql().tables.add(JOINER.join(table));
		return this;
	}

	public SqlBuilder SET(String... sets) {
		sql().sets.add(JOINER.join(sets));
		return this;
	}

	public SqlBuilder INSERT_INTO(String... tableName) {
		sql().statementType = StatementType.INSERT;
		sql().tables.add(JOINER.join(tableName));
		return this;
	}

	public SqlBuilder VALUES(String columns, String values) {
		sql().columns.add(columns);
		sql().values.add(values);
		return this;
	}

	public SqlBuilder SELECT(String... columns) {
		sql().statementType = StatementType.SELECT;
		sql().select.add(JOINER.join(columns));
		return this;
	}

	public SqlBuilder SELECT_DISTINCT(String... columns) {
		sql().distinct = true;
		SELECT(JOINER.join(columns));
		return this;
	}

	public SqlBuilder DELETE_FROM(String... table) {
		sql().statementType = StatementType.DELETE;
		sql().tables.add(JOINER.join(table));
		return this;
	}

	public SqlBuilder FROM(String... table) {
		sql().tables.add(JOINER.join(table));
		return this;
	}

	public SqlBuilder JOIN(String... join) {
		sql().join.add(JOINER.join(join));
		return this;
	}

	public SqlBuilder INNER_JOIN(String... join) {
		sql().innerJoin.add(JOINER.join(join));
		return this;
	}

	public SqlBuilder LEFT_OUTER_JOIN(String... join) {
		sql().leftOuterJoin.add(JOINER.join(join));
		return this;
	}

	public SqlBuilder RIGHT_OUTER_JOIN(String... join) {
		sql().rightOuterJoin.add(JOINER.join(join));
		return this;
	}

	public SqlBuilder OUTER_JOIN(String... join) {
		sql().outerJoin.add(JOINER.join(join));
		return this;
	}

	public SqlBuilder WHERE(String... conditions) {
		sql().where.add(JOINER.join(conditions));
		sql().lastList = sql().where;
		return this;
	}

	public SqlBuilder OR() {
		if (sql().lastList != null) {
			sql().lastList.add(OR);
		}
		return this;
	}

	public SqlBuilder AND() {
		if (sql().lastList != null) {
			sql().lastList.add(AND);
		}
		return this;
	}

	public SqlBuilder GROUP_BY(String... columns) {
		sql().groupBy.add(JOINER.join(columns));
		return this;
	}

	public SqlBuilder HAVING(String... conditions) {
		sql().having.add(JOINER.join(conditions));
		sql().lastList = sql().having;
		return this;
	}

	public SqlBuilder ORDER_BY(String... columns) {
		sql().orderBy.add(JOINER.join(columns));
		return this;
	}

	private SQL sql() {
		return sql;
	}

	public <A extends Appendable> A SQL(A a) {
		sql().sql(a);
		return a;
	}

	public String SQL() {
		StringBuilder sb = new StringBuilder();
		for (SqlBuilder SqlBuilder : subConditionBuilders) {
			String subConditions = SqlBuilder.build();
			if (!Strings.isNullOrEmpty(subConditions)) {
				this.AND().WHERE(subConditions);
			}
		}
		sql().sql(sb);
		return sb.toString();
	}

	public String build() {
		return SQL();
	}
	public String buildOneline() {
		return StringUtils.replace(SQL(), "\n", " ");
	}

	private static class SafeAppendable {
		private final Appendable a;
		private boolean empty = true;

		public SafeAppendable(Appendable a) {
			super();
			this.a = a;
		}

		public SafeAppendable append(CharSequence s) {
			try {
				if (empty && s.length() > 0)
					empty = false;
				a.append(s);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			return this;
		}

		public boolean isEmpty() {
			return empty;
		}

	}

	private class SQL {

		StatementType statementType;
		List<String> sets = new ArrayList<String>(0);
		List<String> select = new ArrayList<String>(0);
		List<String> tables = new ArrayList<String>(0);
		List<String> join = new ArrayList<String>(0);
		List<String> innerJoin = new ArrayList<String>(0);
		List<String> outerJoin = new ArrayList<String>(0);
		List<String> leftOuterJoin = new ArrayList<String>(0);
		List<String> rightOuterJoin = new ArrayList<String>(0);
		List<String> where = new ArrayList<String>(0);
		List<String> having = new ArrayList<String>(0);
		List<String> groupBy = new ArrayList<String>(0);
		List<String> orderBy = new ArrayList<String>(0);
		List<String> columns = new ArrayList<String>(0);
		List<String> values = new ArrayList<String>(0);
		List<String> lastList = null;
		boolean distinct;

		private void sqlClause(SafeAppendable builder, String keyword,
				List<String> parts, String open, String close,
				String conjunction) {
			if (!parts.isEmpty()) {
				if (!builder.isEmpty())
					builder.append("\n");
				builder.append(keyword);
				builder.append(" ");
				builder.append(open);
				String last = "________";
				for (int i = 0, n = parts.size(); i < n; i++) {
					String part = parts.get(i);
					if (i > 0 && !part.equals(AND) && !part.equals(OR)
							&& !last.equals(AND) && !last.equals(OR)) {
						builder.append(conjunction);
					}
					builder.append(part);
					last = part;
				}
				builder.append(close);
			}
		}

		private String selectSQL(SafeAppendable builder) {
			if (distinct) {
				sqlClause(builder, "SELECT DISTINCT", select, "", "", ", ");
			} else {
				sqlClause(builder, "SELECT", select, "", "", ", ");
			}

			sqlClause(builder, "FROM", tables, "", "", ", ");
			sqlClause(builder, "JOIN", join, "", "", "\nJOIN ");
			sqlClause(builder, "INNER JOIN", innerJoin, "", "", "\nINNER JOIN ");
			sqlClause(builder, "OUTER JOIN", outerJoin, "", "", "\nOUTER JOIN ");
			sqlClause(builder, "LEFT OUTER JOIN", leftOuterJoin, "", "",
					"\nLEFT OUTER JOIN ");
			sqlClause(builder, "RIGHT OUTER JOIN", rightOuterJoin, "", "",
					"\nRIGHT OUTER JOIN ");
			sqlClause(builder, "WHERE", where, "(", ")", " AND ");
			sqlClause(builder, "GROUP BY", groupBy, "", "", ", ");
			sqlClause(builder, "HAVING", having, "(", ")", " AND ");
			sqlClause(builder, "ORDER BY", orderBy, "", "", ", ");
			return builder.toString();
		}

		private String subWhereSQL(SafeAppendable builder) {
			sqlClause(builder, "", where, "(", ")", " AND ");
			return builder.toString();
		}

		private String insertSQL(SafeAppendable builder) {
			sqlClause(builder, "INSERT INTO", tables, "", "", "");
			sqlClause(builder, "", columns, "(", ")", ", ");
			sqlClause(builder, "VALUES", values, "(", ")", ", ");
			return builder.toString();
		}

		private String deleteSQL(SafeAppendable builder) {
			sqlClause(builder, "DELETE FROM", tables, "", "", "");
			sqlClause(builder, "WHERE", where, "(", ")", " AND ");
			return builder.toString();
		}

		private String updateSQL(SafeAppendable builder) {

			sqlClause(builder, "UPDATE", tables, "", "", "");
			sqlClause(builder, "SET", sets, "", "", ", ");
			sqlClause(builder, "WHERE", where, "(", ")", " AND ");
			return builder.toString();
		}

		public String sql(Appendable a) {
			SafeAppendable builder = new SafeAppendable(a);
			if (statementType == null) {
				return null;
			}

			String answer;

			switch (statementType) {
			case DELETE:
				answer = deleteSQL(builder);
				break;

			case INSERT:
				answer = insertSQL(builder);
				break;

			case SELECT:
				answer = selectSQL(builder);
				break;

			case SUB_WHERE:
				answer = subWhereSQL(builder);
				break;

			case UPDATE:
				answer = updateSQL(builder);
				break;

			default:
				answer = null;
			}

			return answer;
		}
	}
}
