package com.joymeter.provider;

import com.joymeter.entity.Record;

public class RecordProvider {
	public String selectoffline(Record record) {
		StringBuilder sql = new StringBuilder();
		StringBuilder sqlb = new StringBuilder();
		String column = null;
		sql.append("SELECT ");
		sqlb.append(",COUNT(*) as offline from test_record WHERE status = 'offline' ");
		if(record.getProjectName()!=null) {
			sqlb.append("and projectName = #{projectName} ");
			column = "province";
		}else {
			column = "projectName";
		}
		if(record.getProvince()!=null) {
			sqlb.append("and province = #{province} ");
			column = "city";
		}
		if(record.getCity()!=null) {
			sqlb.append("and city = #{city} ");
			column = "district";
		}
		if(record.getDistrict()!=null) {
			sqlb.append("and district = #{district} ");
			column = "community";
		}
		return sql.append(column).append(sqlb.append(" Group By "+column)).toString();
	}
}
