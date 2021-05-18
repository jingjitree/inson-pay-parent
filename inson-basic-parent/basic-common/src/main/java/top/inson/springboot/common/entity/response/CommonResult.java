package top.inson.springboot.common.entity.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CommonResult<T> {
    static final Integer SUCCESS_CODE = 1;

    private Integer status;

    private String message;

    private T data;

    public static <T> CommonResult<T> success(){
        return new CommonResult<>(SUCCESS_CODE, "请求成功", null);
    }

    public static <T> CommonResult<T> success(T data){
        return new CommonResult<>(SUCCESS_CODE, "请求成功", data);
    }

    public static <T> CommonResult<T> fail(){
        return new CommonResult<>(0, "请求失败", null);
    }

    public static <T> CommonResult<T> fail(String message){
        return new CommonResult<>(0, message, null);
    }

    public static <T> CommonResult<T> fail(Integer status, String message){
        return new CommonResult<>(status, message, null);
    }

}
