package com.joymeter.mapper;

import java.util.HashMap;
import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.annotations.Update;

import com.joymeter.entity.Record;
import com.joymeter.provider.RecordProvider;

public interface RecordMapper {
	@Select("SELECT * FROM test_record")
    @Results({
        @Result(property = "status", column = "status")
    })
    List<Record> getAll();
	
	@Select("SELECT * FROM test_record WHERE projectName = #{projectName}")
    @Results({
        @Result(property = "status", column = "status")
    })
    List<Record> getByProject(String projectName);
	
	@SelectProvider(type = RecordProvider.class,method="selectoffline")
    @Results({
        @Result(property = "offline", column = "offline")
    })
    List<HashMap<String, Object>> getofflineCount(Record record);

    @Select("SELECT * FROM test_record WHERE deviceId = #{deviceId}")
    @Results({
        @Result(property = "status", column = "status")
    })
    Record getOne(Long deviceId);

    @Update("UPDATE test_record SET status=#{status} WHERE deviceId =#{deviceId}")
    void update(Record record);

    @Delete("DELETE FROM test_record WHERE deviceId =#{deviceId}")
    void delete(Long deviceId);
}
