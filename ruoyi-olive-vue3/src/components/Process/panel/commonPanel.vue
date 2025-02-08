<template>
  <div>
    <el-form :model="bpmnFormData" label-width="80px" :rules="rules" status-icon>
        <el-form-item :label="bpmnFormData.$type === 'bpmn:Process'? '流程标识': '节点ID'" prop="id">
            <el-input v-model="bpmnFormData.id"  @change="updateElementTask('id')"/>
        </el-form-item>
        <el-form-item :label="bpmnFormData.$type === 'bpmn:Process'? '流程名称': '节点名称'" prop="name">
            <el-input v-model="bpmnFormData.name"  @change="updateElementTask('name')"/>
        </el-form-item>
        <!--流程的基础属性-->
        <template v-if="bpmnFormData.$type === 'bpmn:Process'">
          <!-- 默认允许-->
            <el-form-item prop="routineTask" label-width="100px">
                <template #label>
                    <span>
                       常规任务
                       <el-tooltip placement="top">
                          <template #content>
                             <div>
                                第一个任务节点是否默认为流程发起人节点
                             </div>
                          </template>
                          <el-icon><QuestionFilled /></el-icon>
                       </el-tooltip>
                    </span>
                </template>
                <el-switch
                        @change="updateElementTask('routineTask')"
                        v-model="bpmnFormData.routineTask"
                        active-text="是"
                        inactive-text="否">
                </el-switch>
            </el-form-item>
            <el-form-item prop="clearForm" label-width="100px">
                <template #label>
                      <span>
                         清除表单
                         <el-tooltip placement="top">
                            <template #content>
                               <div>
                                 退回, 驳回流程后是否清除表单数据
                               </div>
                            </template>
                            <el-icon><QuestionFilled /></el-icon>
                         </el-tooltip>
                      </span>
                </template>
                <el-switch
                        @change="updateElementTask('clearForm')"
                        v-model="bpmnFormData.clearForm"
                        active-text="是"
                        inactive-text="否">
                </el-switch>
            </el-form-item>
        </template>
        <el-form-item v-if="bpmnFormData.$type === 'bpmn:SubProcess'" label="状态">
            <el-switch v-model="bpmnFormData.isExpanded" active-text="展开" inactive-text="折叠" @change="updateElementTask('isExpanded')" />
        </el-form-item>
        <div>
          <timer-event-panel :id="id" v-if="eventType === 'Timer'"/>
          <message-event-panel :id="id" v-else-if="eventType === 'Message'"/>
          <signal-event-panel :id="id" v-else-if="eventType === 'Signal'"/>
          <error-event-panel :id="id" v-else-if="eventType === 'Error'"/>
          <condition-event-panel :id="id" v-else-if="eventType === 'Condition'"/>
        </div>
        <el-form-item label="节点描述">
            <el-input :rows="2" type="textarea" v-model="bpmnFormData.documentation" @change="updateElementDoc('documentation')"/>
        </el-form-item>
    </el-form>
  </div>
</template>

<script setup name="CommonPanel">
import {StrUtil} from '@/utils/StrUtil'
import useModelerStore from '@/store/modules/modeler'
import { getDocumentValue, setDocumentValue } from "../common/documentationUtil";
import TimerEventPanel from "./eventDefinition/timerEventPanel";
import MessageEventPanel from "./eventDefinition/messageEventPanel";
import SignalEventPanel from "./eventDefinition/signalEventPanel";
import ErrorEventPanel from "./eventDefinition/errorEventPanel";
import ConditionEventPanel from "./eventDefinition/conditionEventPanel";
import {checkEventType} from "../common/bpmnUtils.js";

const modelerStore = useModelerStore()

const { proxy } = getCurrentInstance();
const eventType = ref('');  // 事件类型
const data = reactive({
  rules: {
    id: [
      {required: true, message: '节点Id 不能为空', trigger: 'blur'},
    ],
    name: [
      {required: true, message: '节点名称不能为空', trigger: 'blur'},
    ],
  },
  bpmnFormData: {
    // 勾选后,第一个任务节点默认为流程发起人节点,此时设计流程时,第一个任务节点的审批人信息默认会设置
    routineTask: true,
    // 退回, 驳回流程后是否清除表单数据
    clearForm: false,
    isExpanded: false,
    id: "",
    name: "",
    documentation: "",
    eventDefinitions: []
  },
});

const {rules, bpmnFormData} = toRefs(data);

/** 组件传值  */
const props = defineProps({
    id: {
        type: String,
        required: true
    },
});

/** 传值监听 */
watch(() => props.id, newVal => {
        if (newVal) {
            resetTaskForm();
        }
    },
    {immediate: true}
)

function resetTaskForm() {
  bpmnFormData.value = {
    routineTask: true,
    clearForm: false,
    isExpanded: false,
    id: "",
    name: "",
    documentation: "",
    eventDefinitions:[]
  };
  const formData = JSON.parse(JSON.stringify(modelerStore.getBpmnElement.businessObject));
  delete formData[`dataType`]
  bpmnFormData.value = JSON.parse(JSON.stringify(formData));
  // 设置子流程展开或折叠状态
  if (bpmnFormData.value && bpmnFormData.value.$type === "bpmn:SubProcess") {
    Reflect.set(bpmnFormData.value, "isExpanded", bpmnFormData.value.di?.isExpanded);
  }
  // 处理流程分类
  if (bpmnFormData.value.$type === 'bpmn:Process') {
    if (StrUtil.isBlank(bpmnFormData.value.processCategory)) {
      bpmnFormData.value.processCategory = modelerStore.getCategory;
      updateElementTask('processCategory')
    }
  }
  bpmnFormData.value.documentation = getDocumentValue(modelerStore.getBpmnElement) || "";
  eventType.value = '';
  // 获取事件类型
  if (checkEventType(bpmnFormData.value.$type)) {
    getFlowEventType(bpmnFormData.value, modelerStore.getBpmnElement.businessObject)
  }
}

// 获取事件类型
function getFlowEventType(bpmnFormData, businessObject) {
  if (businessObject.eventDefinitions && businessObject.eventDefinitions.length > 0) {
    if (businessObject.eventDefinitions[0].$type.indexOf("Timer") !== -1) {
      eventType.value = 'Timer';
    } else if (businessObject.eventDefinitions[0].$type.indexOf("Message") !== -1) {
      eventType.value = 'Message';
    } else if (businessObject.eventDefinitions[0].$type.indexOf("Signal") !== -1) {
      eventType.value = 'Signal';
    } else if (businessObject.eventDefinitions[0].$type.indexOf("Error") !== -1) {
      eventType.value = 'Error';
    } else if (businessObject.eventDefinitions[0].$type.indexOf("Condition") !== -1) {
      // flowable 不支持条件事件作为启动事件,保存xml校验会不通过
      if (bpmnFormData.$type === 'bpmn:StartEvent'){
        eventType.value = '';
      } else {
        eventType.value = 'Condition';
      }
    }
  }
}

function updateElementTask(key) {
  if (key === "isExpanded") {
    modelerStore.getModeling.toggleCollapse(modelerStore.getBpmnElement);
    return;
  }
  const taskAttr = Object.create(null);
  taskAttr[key] = bpmnFormData.value[key] || null;
  modelerStore.getModeling.updateProperties(modelerStore.getBpmnElement, taskAttr);
}

function updateElementDoc(key) {
  const taskAttr = Object.create(null);
  taskAttr[key] = bpmnFormData.value[key] || null;
  setDocumentValue(modelerStore, modelerStore.getBpmnElement, taskAttr[key]);
}
</script>
