package priv.gitonline.flowable.vo;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class ResultMap{

    private String code;
    private String msg;
    private Map<String,Object> data = new HashMap<>();

    public ResultMap() {
    }

    public ResultMap(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public void add(String key,Object value){
        data.put(key,value);
    }

    public static ResultMap ok(){
        ResultMap resultMap = new ResultMap();
        resultMap.setCode(ResultStatus.SUCCESS.getCode());
        resultMap.setMsg(ResultStatus.SUCCESS.getMsg());
        return resultMap;
    }

    public static ResultMap ok(String msg){
        ResultMap resultMap = new ResultMap();
        resultMap.setCode(ResultStatus.SUCCESS.getCode());
        resultMap.setMsg(msg);
        return resultMap;
    }

    public static ResultMap fail(){
        ResultMap resultMap = new ResultMap();
        resultMap.setCode(ResultStatus.FAILED.getCode());
        resultMap.setMsg(ResultStatus.FAILED.getMsg());
        return resultMap;
    }

    public static ResultMap fail(String msg){
        ResultMap resultMap = new ResultMap();
        resultMap.setCode(ResultStatus.FAILED.getCode());
        resultMap.setMsg(msg);
        return resultMap;
    }
}
