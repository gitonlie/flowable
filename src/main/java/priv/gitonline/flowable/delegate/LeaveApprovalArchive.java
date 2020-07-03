package priv.gitonline.flowable.delegate;

import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;

/**
 * Java服务任务
 * 请假审批归档
 */
public class LeaveApprovalArchive implements JavaDelegate {
    @Override
    public void execute(DelegateExecution delegateExecution) {
        System.out.println("日志归档");
    }
}
