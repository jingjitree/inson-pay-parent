package top.inson.springboot.common.entity.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CommonPageResult<T> extends CommonResult<T> {

    private Long total;

    private Long totalPage;

    public static <T> CommonResult<T> success(T data, Long total, Long totalPage){
        CommonResult<T> result = new CommonPageResult<>(total, totalPage);
        result.setData(data);
        result.setStatus(SUCCESS_CODE);
        result.setMessage("请求成功");
        return result;
    }

}
