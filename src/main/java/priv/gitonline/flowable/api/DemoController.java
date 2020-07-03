package priv.gitonline.flowable.api;

import org.apache.commons.io.IOUtils;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.engine.*;
import org.flowable.engine.history.HistoricActivityInstance;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.runtime.Execution;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.image.ProcessDiagramGenerator;
import org.flowable.task.api.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import priv.gitonline.flowable.vo.ResultMap;
import priv.gitonline.flowable.vo.ResultStatus;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * http://www.shareniu.com/flowable6.5_zh_document/bpm/index.html#apiEngine
 */
@RestController
public class DemoController {

    private Logger logger = LoggerFactory.getLogger(DemoController.class);

    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private RepositoryService repositoryService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private ManagementService managementService;
    @Autowired
    private IdentityService identityService;
    @Autowired
    private HistoryService historyService;

    //创建流程
    @GetMapping("/create")
    public ResultMap create(){
        //初始化部分流程图的参数
        Map<String,Object> variables = new HashMap<>();
        variables.put("username","小王");
        variables.put("leader","张经理");
        variables.put("person","人事小刘");
        //流程定义中定义的id启动新流程实例。请注意这个id在Flowable术语中被称作key
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("Leave",variables);

        ResultMap result = ResultMap.ok();
        result.add("instanceId",processInstance.getId());//返回此流程实例的id,后续可查状态
        return result;
    }

    //查询任务
    @GetMapping("/queryTask")
    public ResultMap queryTask(String assignee){
        List<Task> taskList = taskService.createTaskQuery().taskAssignee(assignee).list();
        List<String> list = new ArrayList<>();
        for(Task task:taskList){
            list.add(task.getId());
        }

        ResultMap result = ResultMap.ok();
        result.add("taskIdList",list);//任务列表
        return result;
    }


    //审批任务
    @GetMapping("/approveTask")
    public ResultMap Approve(String taskId,String flag){

        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null) {
            return ResultMap.fail("任务不存在");
        }

        Map<String,Object> variables = new HashMap();
        String assignee = task.getAssignee();
        String app = "Lflag";
        if("人事小刘".equals("")){
            app = "Pflag";
        }
        variables.put(app,flag);

        taskService.complete(taskId,variables);
        return ResultMap.ok("审批通过");
    }

    //查询流程是否完成
    @GetMapping("/finish")
    public ResultMap finish(String instanceId){
        HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery().processInstanceId(instanceId).finished().singleResult();
        if(historicProcessInstance==null){
            return ResultMap.fail("当前实例未完成");
        }
        return ResultMap.ok("当前实例已完成");
    }

    //删除活动实例
    @GetMapping("/delete")
    public ResultMap delete(String instanceId){
        HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery().processInstanceId(instanceId).unfinished().singleResult();
        if(historicProcessInstance!=null){
            runtimeService.deleteProcessInstance(instanceId,"删除");
        }
        return ResultMap.ok();
    }

    //流程图
    @RequestMapping(value = "processDiagram")
    public void genProcessDiagram(HttpServletResponse httpServletResponse, String processId) throws Exception {

        /**
         * 获得当前活动的节点
         */
        String processDefinitionId = "";

        if(ResultStatus.SUCCESS.getCode().equals(this.finish(processId).getCode())){
            HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery().processInstanceId(processId).singleResult();
            processDefinitionId = historicProcessInstance.getProcessDefinitionId();
        }else{
            ProcessInstance pi = runtimeService.createProcessInstanceQuery().processInstanceId(processId).singleResult();
            processDefinitionId = pi.getProcessDefinitionId();
        }

        List<String> highLightedActivitis = new ArrayList<String>();

        /**
         * 获得活动的节点
         */
        List<HistoricActivityInstance> highLightedActivitList =  historyService.createHistoricActivityInstanceQuery().processInstanceId(processId).orderByHistoricActivityInstanceStartTime().asc().list();

        for(HistoricActivityInstance tempActivity : highLightedActivitList){
            String activityId = tempActivity.getActivityId();
            highLightedActivitis.add(activityId);
        }

        List<String> flows = new ArrayList<>();
        //获取流程图
        BpmnModel bpmnModel = repositoryService.getBpmnModel(processDefinitionId);
        ProcessEngineConfiguration engconf = ProcessEngines.getDefaultProcessEngine().getProcessEngineConfiguration();

        ProcessDiagramGenerator diagramGenerator = engconf.getProcessDiagramGenerator();
        InputStream in = diagramGenerator.generateDiagram(bpmnModel, "bmp", highLightedActivitis, flows, engconf.getActivityFontName(),
                engconf.getLabelFontName(), engconf.getAnnotationFontName(), engconf.getClassLoader(), 1.0, true);
        OutputStream out = null;
        byte[] buf = new byte[1024];
        int legth = 0;
        try {
            out = httpServletResponse.getOutputStream();
            while ((legth = in.read(buf)) != -1) {
                out.write(buf, 0, legth);
            }
        } catch (IOException e) {
            logger.error("操作异常",e);
        } finally {
            IOUtils.closeQuietly(out);
            IOUtils.closeQuietly(in);
        }

    }

}
