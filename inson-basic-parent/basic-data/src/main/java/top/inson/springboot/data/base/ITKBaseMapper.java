package top.inson.springboot.data.base;

import tk.mybatis.mapper.common.BaseMapper;
import tk.mybatis.mapper.common.MySqlMapper;

public interface ITKBaseMapper<T> extends BaseMapper<T>, MySqlMapper<T> {

}
