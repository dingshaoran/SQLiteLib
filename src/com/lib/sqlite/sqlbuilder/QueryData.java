package com.lib.sqlite.sqlbuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

public class QueryData {
    private String query;
    private String[] params;

    private QueryData() {
    }

    public String getQuery() {
        return query;
    }

    public String[] getParams() {
        return params;
    }

    public static enum QueryOpera {
        equal(" = "), unEqual(" <> "), upper(" > "), lower(" < "), upEqual(" >= "), lowEqual(" <= "), between(" BETWEEN "), like(" LIKE ");

        private final String opera;

        private QueryOpera(String o) {
            opera = o;
        }

        @Override
        public String toString() {
            return opera;
        }
    }

    public static class QueryBuilder {
        private String orderby;
        private List<Entry<String, String>> whereList;
        private List<Entry<String, String>> orList;
        private int start = -1;
        private int total = -1;
        private boolean desc;

        public QueryBuilder limit(int start, int total) {
            this.start = start;
            this.total = total;
            return this;
        }

        public QueryBuilder where(final String column, final QueryOpera opera, final String data) {
            if (whereList == null) {
                whereList = new ArrayList<Entry<String, String>>();
            }
            whereList.add(new Entry<String, String>() {

                @Override
                public String getKey() {
                    return column + opera + "?";
                }

                @Override
                public String getValue() {
                    return data;
                }

                @Override
                public String setValue(String object) {
                    return null;
                }
            });
            return this;
        }

        public QueryBuilder or(final String column, final QueryOpera opera, final String data) {
            if (orList == null) {
                orList = new ArrayList<Entry<String, String>>();
            }
            orList.add(new Entry<String, String>() {

                @Override
                public String getKey() {
                    return column + opera + "?";
                }

                @Override
                public String getValue() {
                    return data;
                }

                @Override
                public String setValue(String object) {
                    return null;
                }
            });
            return this;
        }

        public QueryBuilder orderBy(String column, boolean desc) {
            this.desc = desc;
            this.orderby = column;
            return this;
        }

        public QueryData build() {
            QueryData qd = new QueryData();
            ArrayList<String> param = new ArrayList<String>();
            StringBuilder builder = new StringBuilder(100);
            if (whereList != null && whereList.size() != 0) {
                builder.append(" WHERE ");
                builder.append(whereList.get(0).getKey());
                param.add(whereList.get(0).getValue());
                for (int i = 1; i < whereList.size(); i++) {
                    builder.append(" AND ");
                    builder.append(whereList.get(i).getKey());
                    param.add(whereList.get(i).getValue());
                }
                if (orList != null) {
                    for (int i = 0; i < orList.size(); i++) {
                        builder.append(" OR ");
                        builder.append(orList.get(i).getKey());
                        param.add(orList.get(i).getValue());
                    }
                }
            }
            if (orderby != null) {
                builder.append(" ORDER BY ");
                builder.append(orderby);
                builder.append(" ");
                if (desc) {
                    builder.append(" desc ");
                }
            }
            if (start != -1 && total != -1) {
                builder.append(" LIMIT ?,? ");
                param.add(String.valueOf(start));
                param.add(String.valueOf(total));
            }
            qd.query = builder.toString();
            qd.params = param.toArray(new String[param.size()]);
            return qd;
        }

    }
}
