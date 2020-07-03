package priv.gitonline.flowable.vo;

public enum  ResultStatus{

    SUCCESS("0000","调用成功"),
    FAILED("0009","调用失败");

    private String code;
    private String msg;

    ResultStatus(String code,String msg){
        this.code = code;
        this.msg = msg;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
