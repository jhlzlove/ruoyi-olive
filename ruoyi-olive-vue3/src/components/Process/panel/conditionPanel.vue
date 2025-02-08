<template>
    <el-form label-width="100px" status-icon @submit.native.prevent>
      <el-form-item>
        <template #label>
            <span>
               流转类型
               <el-tooltip placement="top">
                  <template #content>
                     <div>
                              普通流转路径：流程执行过程中，一个元素被访问后，会沿着其所有出口顺序流继续执行。
                        <br />默认流转路径：只有当没有其他顺序流可以选择时，才会选择默认顺序流作为活动的出口顺序流。流程会忽略默认顺序流上的条件。
                        <br />条件流转路径：是计算其每个出口顺序流上的条件。当条件计算为true时，选择该出口顺序流。如果该方法选择了多条顺序流，则会生成多个执行，流程会以并行方式继续。
                     </div>
                  </template>
                  <el-icon><QuestionFilled /></el-icon>
               </el-tooltip>
            </span>
        </template>
        <el-select v-model="bpmnFormData.type" @change="updateFlowType">
          <el-option label="普通流转路径" value="normal" />
          <el-option label="默认流转路径" value="default" />
          <el-option label="条件流转路径" value="condition" />
        </el-select>
      </el-form-item>
      <el-form-item label="条件格式" v-if="bpmnFormData.type === 'condition'" key="condition">
        <el-select v-model="bpmnFormData.conditionType">
          <el-option label="表达式" value="expression" />
          <el-option label="脚本" value="script" />
        </el-select>
      </el-form-item>
      <el-form-item label="条件字段" v-if="bpmnFormData.conditionType && bpmnFormData.conditionType === 'expression'" key="formWidget">
        <el-button type="primary" icon="View" size="small" class="custom-button-size" @click="handleFormWidgetClick">选择字段</el-button>
      </el-form-item>
      <el-form-item label="表达式" v-if="bpmnFormData.conditionType && bpmnFormData.conditionType === 'expression'" key="express">
        <el-input v-model="bpmnFormData.body" placeholder="示例: ${变量名 == 值}" clearable @change="updateFlowCondition" />
      </el-form-item>
      <template v-if="bpmnFormData.conditionType && bpmnFormData.conditionType === 'script'">
        <el-form-item label="脚本语言" key="language">
          <el-input v-model="bpmnFormData.language" clearable @change="updateFlowCondition" />
        </el-form-item>
        <el-form-item label="脚本类型" key="scriptType">
          <el-select v-model="bpmnFormData.scriptType">
            <el-option label="内联脚本" value="inlineScript" />
            <el-option label="外部脚本" value="externalScript" />
          </el-select>
        </el-form-item>
        <el-form-item label="脚本" v-if="bpmnFormData.scriptType === 'inlineScript'" key="body">
          <el-input v-model="bpmnFormData.body" type="textarea" clearable @change="updateFlowCondition" />
        </el-form-item>
        <el-form-item label="资源地址" v-if="bpmnFormData.scriptType === 'externalScript'" key="resource">
          <el-input v-model="bpmnFormData.resource" clearable @change="updateFlowCondition" />
        </el-form-item>
      </template>
    </el-form>

  <!-- 表单字段 -->
  <el-dialog title="表单字段" v-model="formOpen" width="45%" :destroy-on-close="true" append-to-body>
    <el-table ref="dataTable" v-loading="loading" :data="formWidgetList" max-height="600" border fit>
      <el-table-column label="表单" prop="formName" min-width="50px">
        <template #default="scope">
          <span>{{scope.row.formName}}</span>
          <span v-if="scope.row.nodeId === 'init'" class="ml5">(初始表单)</span>
        </template>
      </el-table-column>
      <el-table-column label="字段授权(点击切换)" prop="formWidgets" min-width="200px">
        <template #default="scope">
          <el-tag style="margin-right: 5px;" v-for="(widget,index) in scope.row.formWidgets"
                  :key="index"
                  size="small"
                  effect="plain"
                  class="clickable"
                  type="info"
                  @click="handleTagClick(widget)"
          >
            {{ widget.label }}
          </el-tag>
        </template>
      </el-table-column>
    </el-table>
  </el-dialog>
</template>

<script setup name="ConditionPanel">
import useModelerStore from '@/store/modules/modeler'
const modelerStore = useModelerStore()
const { proxy } = getCurrentInstance();
/** 组件传值  */
const props = defineProps({
  id: {
    type: String,
    required: true
  },
});
const bpmnElementSource = ref({});
const bpmnElementSourceRef = ref({});
const formOpen = ref(false);
const loading = ref(false);
const formWidgetList = ref([]);

const data = reactive({
  bpmnFormData: {
    body: ''
  }
});

const { bpmnFormData } = toRefs(data);

/** 传值监听 */
watch(() => props.id, newVal => {
      if (newVal) {
        resetFlowCondition();
      }
    },
    {immediate: true}
)

// 方法区
function resetFlowCondition() {
   bpmnFormData.value = {};
   bpmnElementSource.value = modelerStore.getBpmnElement.source;
   bpmnElementSourceRef.value = modelerStore.getBpmnElement.businessObject.sourceRef;
  if (bpmnElementSourceRef.value && bpmnElementSourceRef.value.default && bpmnElementSourceRef.value.default.id === modelerStore.getBpmnElement.id) {
    // 默认
    Reflect.set(bpmnFormData.value, "type", "default");
  } else if (!modelerStore.getBpmnElement.businessObject.conditionExpression) {
    // 普通
    Reflect.set(bpmnFormData.value, "type", "normal");
  } else {
    // 带条件
    const conditionExpression = modelerStore.getBpmnElement.businessObject.conditionExpression;
    bpmnFormData.value = { ...conditionExpression, type: "condition" };
    // resource 可直接标识 是否是外部资源脚本
    if (bpmnFormData.value.resource) {
      Reflect.set(bpmnFormData.value, "conditionType", "script");
      Reflect.set(bpmnFormData.value, "scriptType", "externalScript");
      return;
    }
    if (conditionExpression.language) {
      Reflect.set(bpmnFormData.value, "conditionType", "script");
      Reflect.set(bpmnFormData.value, "scriptType", "inlineScript");
      return;
    }
    Reflect.set(bpmnFormData.value, "conditionType", "expression");
  }
}

function updateFlowType(flowType) {
  bpmnFormData.value.body = null;
  // 正常条件类
  if (flowType === "condition") {
    const flowConditionRef = modelerStore.getModdle.create("bpmn:FormalExpression");
    modelerStore.getModeling.updateProperties(modelerStore.getBpmnElement, {
      conditionExpression: flowConditionRef
    });
    return;
  }
  // 默认路径
  if (flowType === "default") {
    modelerStore.getModeling.updateProperties(modelerStore.getBpmnElement, {
      conditionExpression: null
    });
    modelerStore.getModeling.updateProperties(toRaw(bpmnElementSource.value), {
      default: modelerStore.getBpmnElement
    });
    // 清空条件格式
    bpmnFormData.value.conditionType = null;
    return;
  }
  // 清空条件格式
  bpmnFormData.value.conditionType = null;
  // 正常路径，如果来源节点的默认路径是当前连线时，清除父元素的默认路径配置
  if (bpmnElementSourceRef.value.default && bpmnElementSourceRef.value.default.id === modelerStore.getBpmnElement.id) {
    modelerStore.getModeling.updateProperties(toRaw(bpmnElementSource.value), {
      default: null
    });
  }
  modelerStore.getModeling.updateProperties(modelerStore.getBpmnElement, {
    conditionExpression: null
  });
}

function updateFlowCondition() {
  let { conditionType, scriptType, body, resource, language } = bpmnFormData.value;
  let condition;
  if (conditionType === "expression") {
    condition = modelerStore.getModdle.create("bpmn:FormalExpression", { body });
  } else {
    if (scriptType === "inlineScript") {
      condition = modelerStore.getModdle.create("bpmn:FormalExpression", { body, language });
      Reflect.set(bpmnFormData.value, "resource", "");
    } else {
      Reflect.set(bpmnFormData.value, "body", "");
      condition = modelerStore.getModdle.create("bpmn:FormalExpression", { resource, language });
    }
  }
  modelerStore.getModeling.updateProperties(modelerStore.getBpmnElement, { conditionExpression: condition });
}

// 获取当前节点和之前节点的表单信息
function getBeforeTaskFormList() {
  // 获取当前节点信息
  const currentElement = modelerStore.getElRegistry.get(props.id);
  const userTasksBeforeElement = [];
  findUserTasksBeforeElement(currentElement.source, userTasksBeforeElement);
  userTasksBeforeElement.push(currentElement.source);

  // 提取用户任务节点的表单项信息
  const formList  = extractFormItems(userTasksBeforeElement);
  // 获取初始化表单
  getInitForm(formList);

  formList.forEach(form => {
    processWidgets(form);
  });
}

// 递归查找当前节点之前的所有用户任务节点
function findUserTasksBeforeElement(currentNode, userTasks, visitedNodes = new Set()) {
  const incomingSequenceFlows = currentNode.incoming;
  incomingSequenceFlows.forEach(sequenceFlow => {
    const sourceElement = sequenceFlow.source;
    if (!visitedNodes.has(sourceElement)) {
      visitedNodes.add(sourceElement);
      if (sourceElement.type === 'bpmn:UserTask' && !userTasks.includes(sourceElement)) {
        userTasks.push(sourceElement);
      }
      findUserTasksBeforeElement(sourceElement, userTasks, visitedNodes);
    }
  });
}

// 提取用户任务节点的表单项信息
function extractFormItems(userTasks) {
  const bpmnElementProperties = userTasks.flatMap(element => {
    const formItems = element.businessObject?.extensionElements?.values?.filter(ex => ex.$type === `flowable:FormItems`);
    if (formItems) {
      return formItems.map(flowForm =>
          flowForm.values
              .flatMap(item => (item.nodeId !== 'init' ? [item] : []))
      );
    } else {
      return [];
    }
  });

  return bpmnElementProperties.flat().flatMap(item => item || []); // 返回扁平化处理后的表单项信息
}


// 获取流程初始化表单
function getInitForm(formList) {
  // 如果在表单组件列表中找不到nodeId为'init'的组件
  if (formList.findIndex(item => item.nodeId === 'init') === -1) {
    // 获取当前表单的formId
    const formId = modelerStore.getFormId;
    // 在表单列表中找到对应formId的表单
    const form = modelerStore.getFormList.find(item => item.formId === formId);
    // 如果表单存在
    if (form) {
      // 将表单的nodeId设置为'init'
      form.nodeId = 'init';
      formList.push(form);
    }
  }
}

function  processWidgets(form) {
  const formWidgets = JSON.parse(form.formWidgets);
  const formWidget = {
    formId: form.formId,
    nodeId: form.nodeId,
    formName: form.formName,
    formType: form.formType,
    formWidgets: formWidgets
  }
  if (!form.formId || !formWidgetList.value.some(item => item.formId === form.formId)) {
    formWidgetList.value.push(formWidget);
  }
}

// 表单字段选择弹窗
function handleFormWidgetClick() {
  formWidgetList.value = [];
  loading.value = true;
  getBeforeTaskFormList();
  loading.value = false;
  formOpen.value = true;
}

// 表单字段点击
function handleTagClick(widget) {
  Reflect.set(bpmnFormData.value, "body", "${ " + widget.id + " == '值' }")
  formOpen.value = false;
}
</script>
