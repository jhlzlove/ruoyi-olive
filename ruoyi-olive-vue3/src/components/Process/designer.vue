<template>
  <div>
    <el-scrollbar class="designer-scrollbar">
      <el-tabs v-model="activeName" class="demo-tabs custom-designer-tabs" @tab-click="handleClick">
        <el-tab-pane label="常规配置" name="common">
          <!--   常规信息     -->
          <common-panel :id="elementId"/>
        </el-tab-pane>

        <el-tab-pane label="任务配置" name="task">
          <!--   用户任务信息     -->
          <template v-if="elementType.indexOf('UserTask') !== -1">
            <user-task-panel :id="elementId"/>
          </template>

          <!--   接收任务信息     -->
          <template v-else-if="elementType.indexOf('ReceiveTask') !== -1">
            <receive-task-panel :id="elementId"/>
          </template>

          <!--   发送任务信息     -->
          <template v-else-if="elementType.indexOf('SendTask') !== -1">
            <send-task-panel :id="elementId"/>
          </template>

          <!--   脚本任务信息     -->
          <template v-else-if="elementType.indexOf('ScriptTask') !== -1">
            <script-task-panel :id="elementId"/>
          </template>

          <!--   服务任务信息     -->
          <template v-else-if="elementType.indexOf('ServiceTask') !== -1">
            <service-task-panel :id="elementId"/>
          </template>

          <!--   业务规则任务信息     -->
          <template v-else-if="elementType.indexOf('BusinessRuleTask') !== -1">
            <business-rule-task-panel :id="elementId"/>
          </template>

          <!--   未匹配或未选中任务     -->
          <el-empty v-else description="选择任务节点后配置"/>
        </el-tab-pane>

        <el-tab-pane label="监听配置" name="listener">
          <el-collapse v-model="activeListenerName" >

            <!--   执行监听器     -->
            <el-collapse-item name="executionListener">
              <template #title><el-icon><Promotion/></el-icon> 执行监听器
                <el-badge :value="executionListenerCount" class="item" type="primary"/>
              </template>
              <execution-listener :id="elementId" @getExecutionListenerCount="getExecutionListenerCount"/>
            </el-collapse-item>

            <!--   任务监听器     -->
            <el-collapse-item name="taskListener" v-if="elementType === 'UserTask'" >
              <template #title> <el-icon><Flag/></el-icon> 任务监听器
                <el-badge :value="taskListenerCount" class="item" type="primary"/>
              </template>
              <task-listener :id="elementId" @getTaskListenerCount="getTaskListenerCount"/>
            </el-collapse-item>
          </el-collapse>
        </el-tab-pane>

        <el-tab-pane label="其它配置" name="other">
          <el-collapse v-model="activeOtherName" >
            <!--   表单     -->
            <el-collapse-item name="form" v-if="formVisible && elementType === 'UserTask'">
              <template #title><el-icon><List/></el-icon> 表单配置</template>
              <form-panel :id="elementId"/>
            </el-collapse-item>

            <!--   多实例     -->
            <el-collapse-item name="multiInstance" v-if="elementType.indexOf('UserTask') !== -1" >
              <template #title><el-icon><Grid/></el-icon> 多实例</template>
              <multi-instance :id="elementId"/>
            </el-collapse-item>

            <!--   流转条件     -->
            <el-collapse-item name="condition" v-if="conditionVisible" >
              <template #title><el-icon><Grid/></el-icon> 流转条件</template>
              <condition-panel :id="elementId"/>
            </el-collapse-item>

            <!--   消息,信号,错误信息    -->
            <el-collapse-item name="signalMassageError" v-if="elementType === 'Process'">
              <template #title><el-icon><Comment/></el-icon> 消息与信号</template>
              <signal-message-error-panel :id="elementId"/>
            </el-collapse-item>

            <!--   扩展属性     -->
            <el-collapse-item name="properties" >
              <template #title><el-icon><Grid/></el-icon> 扩展属性</template>
              <properties-panel :id="elementId"/>
            </el-collapse-item>

          </el-collapse>
        </el-tab-pane>
      </el-tabs>

    </el-scrollbar>
  </div>
</template>

<script setup name="Designer">
import { markRaw } from "vue";
import BusinessRuleTaskPanel from './panel/businessRuleTaskPanel';
import CommonPanel from './panel/commonPanel';
import ConditionPanel from './panel/conditionPanel';
import ExecutionListener from './panel/executionListener';
import FormPanel from './panel/formPanel';
import MultiInstance from './panel/multiInstance';
import PropertiesPanel from "./panel/propertiesPanel.vue";
import ReceiveTaskPanel from './panel/receiveTaskPanel';
import ScriptTaskPanel from './panel/scriptTaskPanel';
import SendTaskPanel from './panel/sendTaskPanel';
import ServiceTaskPanel from './panel/serviceTaskPanel';
import SignalMessageErrorPanel from "./panel/signalMessageErrorPanel";
import TaskListener from './panel/taskListener';
import UserTaskPanel from './panel/userTaskPanel';

import useModelerStore from '@/store/modules/modeler';
import { StrUtil } from '@/utils/StrUtil';
const modelerStore = useModelerStore()

const { proxy } = getCurrentInstance();

const activeName = ref('common');
const activeListenerName = ref(['executionListener','taskListener']);
const activeOtherName = ref(['form','multiInstance','condition']);
const executionListenerCount = ref(0);
const taskListenerCount = ref(0);
const elementId = ref("");
const elementType = ref("");
const conditionVisible = ref(false); // 流转条件设置
const formVisible = ref(false); // 表单配置

const data = reactive({
  rules:{
    id: [
      { required: true, message: '节点Id 不能为空', trigger: 'blur' },
    ],
    name: [
      { required: true, message: '节点名称不能为空', trigger: 'blur' },
    ],
  },
  formData:{
    id: "",
    name: "",
    documentation: "",
    processCategory: "",
    initiator: "",
    userType: "",
    async: "",
    dueDate: "",
    formKey: "",
    priority: "",
    skipExpression: "",
    isForCompensation: "",
    conditionExpression: "",
    triggerable: "",
    autoStoreVariables: "",
    exclude: "",
    ruleVariablesInput: "",
    rules: "",
    resultVariable: "",
  },
});
const { formData, rules } = toRefs(data);

// 监听数据
watch(elementId, newVal => {
      if (StrUtil.isBlank(activeName.value)) {
        activeName.value = "common";
      }
    }
)

// 初始化流程设计器
function initModels() {
  // // 初始化 modeler 以及其他 moddle
  // if (!modelerStore.getModeler) {
  //   // 避免加载时 流程图 并未加载完成
  //   proxy.timer = setTimeout(() => initModels(), 10);
  //   return;
  // }
  // if (proxy.timer) clearTimeout(proxy.timer);
  getActiveElement();
}

// 注册节点事件
function getActiveElement() {
  // 初始第一个选中元素 bpmn:Process
  initFormOnChanged(null);
  modelerStore.getModeler.on("import.done", e => {
    initFormOnChanged(null);
  });
  // 监听选择事件，修改当前激活的元素以及表单
  modelerStore.getModeler.on("selection.changed", ({ newSelection }) => {
    initFormOnChanged(newSelection[0] || null);
  });
  modelerStore.getModeler.on("element.changed", ({ element }) => {
    // 保证 修改 "默认流转路径" 类似需要修改多个元素的事件发生的时候，更新表单的元素与原选中元素不一致。
    if (element && element.id === elementId.value) {
      initFormOnChanged(element);
    }
  });
}

// 初始化数据
function initFormOnChanged(element) {
  let activatedElement = element;
  if (!activatedElement) {
    activatedElement =
        modelerStore.getElRegistry.find(el => el.type === "bpmn:Process") ??
        modelerStore.getElRegistry.find(el => el.type === "bpmn:Collaboration");
  }
  if (!activatedElement) return;
  modelerStore.setElement(markRaw(activatedElement), activatedElement.id);
  elementId.value = activatedElement.id;
  elementType.value = activatedElement.type.split(":")[1] || "";
  conditionVisible.value = !!(
      elementType.value === "SequenceFlow" &&
      activatedElement.source &&
      activatedElement.source.type.indexOf("StartEvent") === -1
  );
  formVisible.value = elementType.value === "UserTask";
}

/** 获取执行监听器数量 */
function getExecutionListenerCount(value){
  executionListenerCount.value = value;
}
/** 获取任务监听器数量 */
function getTaskListenerCount(value){
  taskListenerCount.value = value;
}

/** tab点击事件*/
function handleClick(tab, event){
  activeName.value = tab.name;
}

initModels();

</script>

<style lang="scss">
.designer-scrollbar {
  height: calc(100vh - 150px);
  overflow-x: hidden !important;
}

//.el-icon {
//  margin-right: 5px
//}
/** 重写el-tabs样式 */
.custom-designer-tabs .el-tabs__nav-wrap.is-scrollable{
  padding: 0;
}


// 节点点击后样式
.djs-selection-outline {
  stroke: #4ea84e !important;
  stroke-width: 1px;
}

.djs-element.selected .djs-outline {
  visibility: visible;
  stroke-dasharray: 5, 5;
  stroke-dashoffset: 500;
  animation: 0.8s linear 0s infinite normal none running draw;
  stroke: #4ea84e !important;
  stroke-width: 1px;
}

@keyframes draw {
  100% {
    stroke-dashoffset: 0;
  }
}

// 隐藏右下角的 bpmn.io logo
//.bjs-powered-by {
//  display: none;
//}
</style>
