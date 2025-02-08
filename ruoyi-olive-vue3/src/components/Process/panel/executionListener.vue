<template>
  <div class="panel-tab__content">
    <el-table :data="elementListenersList" size="small" border>
      <el-table-column label="序号" width="50px" type="index" />
      <el-table-column label="事件类型" min-width="100px" prop="event" />
      <el-table-column label="监听器类型" min-width="100px" show-overflow-tooltip :formatter="row => listenerTypeObject[row.listenerType]" />
      <el-table-column label="操作" width="100px">
        <template #default="scope">
          <el-button size="small" link type="primary"  @click="openListenerForm(scope.row, scope.$index)">编辑</el-button>
          <el-divider direction="vertical" />
          <el-button size="small" link type="danger" @click="removeListener(scope.row, scope.$index)">移除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <div class="element-drawer__button_save">
      <el-button type="primary" size="small" class="custom-button-size" icon="Plus" @click="listenerSystemVisible = true">内置监听器</el-button>
      <el-button type="primary" size="small" class="custom-button-size" icon="Plus" @click="openListenerForm(null)" >自定义监听器</el-button>
    </div>

    <!-- 监听器 编辑/创建 部分 -->
    <el-drawer v-model="listenerFormModelVisible" title="执行监听器" size="480px" append-to-body destroy-on-close>
      <el-form :model="listenerForm" label-width="96px" ref="listenerFormRef" @submit.native.prevent>
        <el-form-item label="事件类型" prop="event" :rules="{ required: true, trigger: ['blur', 'change'] }">
          <el-select v-model="listenerForm.event">
            <el-option label="start" value="start" />
            <el-option label="end" value="end" />
          </el-select>
        </el-form-item>
        <el-form-item label="监听器类型" prop="listenerType" :rules="{ required: true, trigger: ['blur', 'change'] }">
          <el-select v-model="listenerForm.listenerType">
            <el-option v-for="i in Object.keys(listenerTypeObject)" :key="i" :label="listenerTypeObject[i]" :value="i" />
          </el-select>
        </el-form-item>
        <el-form-item
            v-if="listenerForm.listenerType === 'classListener'"
            label="Java类"
            prop="class"
            key="listener-class"
            :rules="{ required: true, trigger: ['blur', 'change'] }"
        >
          <el-input v-model="listenerForm.class" clearable />
        </el-form-item>
        <el-form-item
            v-if="listenerForm.listenerType === 'expressionListener'"
            label="表达式"
            prop="expression"
            key="listener-expression"
            :rules="{ required: true, trigger: ['blur', 'change'] }"
        >
          <el-input v-model="listenerForm.expression" clearable />
        </el-form-item>
        <el-form-item
            v-if="listenerForm.listenerType === 'delegateExpressionListener'"
            label="代理表达式"
            prop="delegateExpression"
            key="listener-delegate"
            :rules="{ required: true, trigger: ['blur', 'change'] }"
        >
          <el-input v-model="listenerForm.delegateExpression" clearable />
        </el-form-item>
        <template v-if="listenerForm.listenerType === 'scriptListener'">
          <el-form-item
              label="脚本格式"
              prop="scriptFormat"
              key="listener-script-format"
              :rules="{ required: true, trigger: ['blur', 'change'], message: '请填写脚本格式' }"
          >
            <el-input v-model="listenerForm.scriptFormat" clearable />
          </el-form-item>
          <el-form-item
              label="脚本类型"
              prop="scriptType"
              key="listener-script-type"
              :rules="{ required: true, trigger: ['blur', 'change'], message: '请选择脚本类型' }"
          >
            <el-select v-model="listenerForm.scriptType">
              <el-option label="内联脚本" value="inlineScript" />
              <el-option label="外部脚本" value="externalScript" />
            </el-select>
          </el-form-item>
          <el-form-item
              v-if="listenerForm.scriptType === 'inlineScript'"
              label="脚本内容"
              prop="value"
              key="listener-script"
              :rules="{ required: true, trigger: ['blur', 'change'], message: '请填写脚本内容' }"
          >
            <el-input v-model="listenerForm.value" clearable />
          </el-form-item>
          <el-form-item
              v-if="listenerForm.scriptType === 'externalScript'"
              label="资源地址"
              prop="resource"
              key="listener-resource"
              :rules="{ required: true, trigger: ['blur', 'change'], message: '请填写资源地址' }"
          >
            <el-input v-model="listenerForm.resource" clearable />
          </el-form-item>
        </template>
      </el-form>
      <el-divider />
      <p class="listener-filed__title">
        <span><el-icon><BellFilled /></el-icon>注入字段：</span>
        <el-button type="primary" size="small" class="custom-button-size" @click="openListenerFieldForm(null)">添加字段</el-button>
      </p>
      <el-table :data="fieldsListOfListener" size="small" max-height="240" border fit style="flex: none">
        <el-table-column label="序号" width="50px" type="index" />
        <el-table-column label="字段名称" min-width="100px" prop="name" />
        <el-table-column label="字段类型" min-width="80px" show-overflow-tooltip :formatter="row => fieldTypeObject[row.fieldType]" />
        <el-table-column label="字段值/表达式" min-width="100px" show-overflow-tooltip :formatter="row => row.string || row.expression" />
        <el-table-column label="操作" width="100px">
          <template #default="scope">
            <el-button size="small" link type="primary" @click="openListenerFieldForm(scope.row, scope.$index)">编辑</el-button>
            <el-divider direction="vertical" />
            <el-button size="small" link type="danger" @click="removeListenerField(scope.row, scope.$index)">移除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="element-drawer__button">
        <el-button size="small" class="custom-button-size" @click="listenerFormModelVisible = false">取 消</el-button>
        <el-button type="primary" size="small" class="custom-button-size" @click="saveListenerConfig">保 存</el-button>
      </div>
    </el-drawer>

    <!-- 注入西段 编辑/创建 部分 -->
    <el-dialog title="字段配置" v-model="listenerFieldFormModelVisible" width="600px" append-to-body destroy-on-close>
      <el-form :model="listenerFieldForm" label-width="96px" ref="listenerFieldFormRef" style="height: 136px" @submit.native.prevent>
        <el-form-item label="字段名称：" prop="name" :rules="{ required: true, trigger: ['blur', 'change'] }">
          <el-input v-model="listenerFieldForm.name" clearable />
        </el-form-item>
        <el-form-item label="字段类型：" prop="fieldType" :rules="{ required: true, trigger: ['blur', 'change'] }">
          <el-select v-model="listenerFieldForm.fieldType">
            <el-option v-for="i in Object.keys(fieldTypeObject)" :key="i" :label="fieldTypeObject[i]" :value="i" />
          </el-select>
        </el-form-item>
        <el-form-item
            v-if="listenerFieldForm.fieldType === 'string'"
            label="字段值："
            prop="string"
            key="field-string"
            :rules="{ required: true, trigger: ['blur', 'change'] }"
        >
          <el-input v-model="listenerFieldForm.string" clearable />
        </el-form-item>
        <el-form-item
            v-if="listenerFieldForm.fieldType === 'expression'"
            label="表达式："
            prop="expression"
            key="field-expression"
            :rules="{ required: true, trigger: ['blur', 'change'] }"
        >
          <el-input v-model="listenerFieldForm.expression" clearable />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button size="small" class="custom-button-size" @click="listenerFieldFormModelVisible= false">取 消</el-button>
          <el-button type="primary" size="small" class="custom-button-size" @click="saveListenerFiled">确 定</el-button>
        </span>
      </template>
    </el-dialog>


    <!-- 内置监听器 -->
    <el-drawer v-model="listenerSystemVisible" title="任务监听器" size="580px" append-to-body destroy-on-close>
      <el-table v-loading="loading" :data="listenerList" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column label="名称" align="center" prop="name" />
        <el-table-column label="类型" align="center" prop="eventType"/>
        <el-table-column label="监听类型" align="center" prop="valueType">
          <template #default="scope">
            <dict-tag :options="sys_listener_value_type" :value="scope.row.valueType"/>
          </template>
        </el-table-column>
        <el-table-column label="执行内容" align="center" prop="value" :show-overflow-tooltip="true"/>
      </el-table>

      <pagination
          v-show="total>0"
          :total="total"
          layout="prev, pager, next"
          v-model:page="queryParams.pageNum"
          v-model:limit="queryParams.pageSize"
          @pagination="getList"
      />

      <div class="element-drawer__button">
        <el-button @click="listenerSystemVisible = false">取 消</el-button>
        <el-button type="primary" :disabled="listenerSystemChecked" @click="saveSystemListener">保 存</el-button>
      </div>
    </el-drawer>
  </div>
</template>
<script setup name="ExecutionListener">
import { listListener } from "@/api/flowable/listener";
import {
  changeListenerObject,
  createListenerObject,
  createSystemListenerObject,
  updateElementExtensions
} from "../common/bpmnUtils";
import useModelerStore from '@/store/modules/modeler'
const modelerStore = useModelerStore()

const {proxy} = getCurrentInstance();

const elementListenersList = ref([]); // 监听器列表
const listenerForm = ref({});// 监听器详情表单
const listenerFormModelVisible = ref(false); // 监听器 编辑 侧边栏显示状态
const fieldsListOfListener = ref([]);
const bpmnElementListeners = ref([]);
const otherExtensionList = ref([]);
const listenerFieldForm = ref({}); // 监听器 注入字段 详情表单
const listenerFieldFormModelVisible = ref(false); // 监听器 注入字段表单弹窗 显示状态
const editingListenerIndex = ref(-1); // 监听器所在下标，-1 为新增
const editingListenerFieldIndex = ref(-1); // 字段所在下标，-1 为新增

// 内置监听器相关信息
const { sys_listener_value_type, sys_listener_event_type } = proxy.useDict('sys_listener_value_type', 'sys_listener_event_type');
const listenerList = ref([]);
const checkedListenerData = ref([]);
const listenerSystemVisible = ref(false);
const listenerSystemChecked = ref(true);
const loading = ref(true);
const total = ref(0);

/** 组件传值  */
const props = defineProps({
  id: {
    type: String,
    default: ''
  },
});

const emit = defineEmits(['getExecutionListenerCount'])

/** 传值监听 */
watch(() => props.id, newVal => {
      if (newVal) {
        resetListenersList();
      }
    },
    {immediate: true}
)

const data = reactive({
  listenerTypeObject: {
    classListener: "Java 类",
    expressionListener: "表达式",
    delegateExpressionListener: "代理表达式",
    scriptListener: "脚本"
  },
  eventType:{
    create: "创建",
    assignment: "指派",
    complete: "完成",
    delete: "删除",
    update: "更新",
    timeout: "超时"
  },
  fieldTypeObject:{
    string: "字符串",
    expression: "表达式"
  },
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    type: 2,
  },
});

const { listenerTypeObject, fieldTypeObject, queryParams } = toRefs(data);

function resetListenersList() {
  bpmnElementListeners.value =
      modelerStore.getBpmnElement.businessObject?.extensionElements?.values?.filter(ex => ex.$type === `flowable:ExecutionListener`) ?? [];
  elementListenersList.value = bpmnElementListeners.value.map(listener => initListenerType(listener));
  // 保存其他扩展配置
  otherExtensionList.value = modelerStore.getBpmnElement.businessObject?.extensionElements?.values?.filter(ex => ex.$type !== `flowable:ExecutionListener`) ?? [];
  emit('getExecutionListenerCount', elementListenersList.value.length)
}

// 打开 监听器详情 侧边栏
function openListenerForm(listener, index) {
  if (listener) {
    listenerForm.value = initListenerForm(listener);
    editingListenerIndex.value = index;
  } else {
    listenerForm.value = {};
    editingListenerIndex.value = -1; // 标记为新增
  }
  if (listener && listener.fields) {
    fieldsListOfListener.value = listener.fields.map(field => ({
      ...field,
      fieldType: field.string ? "string" : "expression"
    }));
  } else {
    fieldsListOfListener.value = [];
    Reflect.set(listenerForm.value, "fields", []);
  }
  // 打开侧边栏并清楚验证状态
  listenerFormModelVisible.value = true;
  proxy.$nextTick(() => {
    if (proxy.$refs["listenerFormRef"]) proxy.$refs["listenerFormRef"].clearValidate();
  });
}

// 打开监听器字段编辑弹窗
function openListenerFieldForm(field, index) {
  listenerFieldForm.value = field ? JSON.parse(JSON.stringify(field)) : {};
  editingListenerFieldIndex.value = field ? index : -1;
  listenerFieldFormModelVisible.value = true;
  proxy.$nextTick(() => {
    if (proxy.$refs["listenerFieldFormRef"]) proxy.$refs["listenerFieldFormRef"].clearValidate();
  });
}

// 保存监听器注入字段
async function saveListenerFiled() {
  let validateStatus = await proxy.$refs["listenerFieldFormRef"].validate();
  if (!validateStatus) return; // 验证不通过直接返回
  if (editingListenerFieldIndex.value === -1) {
    fieldsListOfListener.value.push(listenerFieldForm.value);
    listenerForm.value.fields.push(listenerFieldForm.value);
  } else {
    fieldsListOfListener.value.splice(editingListenerFieldIndex.value, 1, listenerFieldForm.value);
    listenerForm.value.fields.splice(editingListenerFieldIndex.value, 1, listenerFieldForm.value);
  }
  listenerFieldFormModelVisible.value = false;
  proxy.$nextTick(() => (listenerFieldForm.value = {}));
}

// 移除监听器字段
function removeListenerField(field, index) {
  proxy.$confirm("确认移除该字段吗？", "提示", {
    confirmButtonText: "确 认",
    cancelButtonText: "取 消"
  }).then(() => {
    fieldsListOfListener.value.splice(index, 1);
    listenerForm.value.fields.splice(index, 1);
  }).catch(() => console.info("操作取消"));
}

// 移除监听器
function removeListener(listener, index) {
  proxy.$confirm("确认移除该监听器吗？", "提示", {
    confirmButtonText: "确 认",
    cancelButtonText: "取 消"
  }).then(() => {
    bpmnElementListeners.value.splice(index, 1);
    elementListenersList.value.splice(index, 1);
    updateElementExtensions(modelerStore.getModdle, modelerStore.getModeling, modelerStore.getBpmnElement, otherExtensionList.value.concat(bpmnElementListeners.value));
    emit('getExecutionListenerCount', elementListenersList.value.length)
  }).catch((r) => console.info(r,"操作取消"));
}

// 保存监听器配置
async function saveListenerConfig() {
  let validateStatus = await proxy.$refs["listenerFormRef"].validate();
  if (!validateStatus) return; // 验证不通过直接返回
  const listenerObject = createListenerObject(modelerStore.getModdle, listenerForm.value, false, "flowable");
  if (editingListenerIndex.value === -1) {
    bpmnElementListeners.value.push(listenerObject);
    elementListenersList.value.push(listenerForm.value);
  } else {
    bpmnElementListeners.value.splice(editingListenerIndex.value, 1, listenerObject);
    elementListenersList.value.splice(editingListenerIndex.value, 1, listenerForm.value);
  }
  updateElementExtensions(modelerStore.getModdle, modelerStore.getModeling, modelerStore.getBpmnElement, otherExtensionList.value.concat(bpmnElementListeners.value));
  emit('getExecutionListenerCount', elementListenersList.value.length)
  // 4. 隐藏侧边栏
  listenerFormModelVisible.value = false;
  listenerForm.value = {};
}

function initListenerType(listener) {
  let listenerType;
  if (listener.class) listenerType = "classListener";
  if (listener.expression) listenerType = "expressionListener";
  if (listener.delegateExpression) listenerType = "delegateExpressionListener";
  if (listener.script) listenerType = "scriptListener";
  return {
    ...JSON.parse(JSON.stringify(listener)),
    ...(listener.script ?? {}),
    listenerType: listenerType
  };
}

// 初始化表单数据
function initListenerForm(listener) {
  let self = {
    ...listener
  };
  if (listener.script) {
    self = {
      ...listener,
      ...listener.script,
      scriptType: listener.script.resource ? "externalScript" : "inlineScript"
    };
  }
  if (listener.event === "timeout" && listener.eventDefinitions) {
    if (listener.eventDefinitions.length) {
      let k = "";
      for (let key in listener.eventDefinitions[0]) {
        if (key.indexOf("time") !== -1) {
          k = key;
          self.eventDefinitionType = key.replace("time", "").toLowerCase();
        }
      }
      self.eventTimeDefinitions = listener.eventDefinitions[0][k].body;
    }
  }
  return self;
}


/** 查询流程达式列表 */
function getList() {
  loading.value = true;
  listListener(queryParams.value).then(response => {
    listenerList.value = response.rows;
    total.value = response.total;
    loading.value = false;
  });
}

// 多选框选中数据
function handleSelectionChange(selection) {
  // ids.value = selection.map(item => item.id);
  // TODO 应该使用 push?
  checkedListenerData.value = selection;
  listenerSystemChecked.value = selection.length !== 1;
}

// 保存内置监听器
function saveSystemListener(){
  if (checkedListenerData.value.length > 0) {
    checkedListenerData.value.forEach(value => {
      // 保存其他配置
      const listenerObject = createSystemListenerObject(modelerStore.getModdle, value, false, "flowable");
      bpmnElementListeners.value.push(listenerObject);
      elementListenersList.value.push(changeListenerObject(value));
      updateElementExtensions(modelerStore.getModdle, modelerStore.getModeling, modelerStore.getBpmnElement, otherExtensionList.value.concat(bpmnElementListeners.value));
    })
    // 回传显示数量
    emit('getExecutionListenerCount', elementListenersList.value.length)
  }
  checkedListenerData.value = [];
  listenerSystemChecked.value = true;
  // 隐藏侧边栏
  listenerSystemVisible.value = false;
}

getList();

</script>
<style lang="scss">
@import '../style/process-panel';
.flow-containers  .el-badge__content.is-fixed {
  top: 18px;
}
.dialog-footer button:first-child {
  margin-right: 10px;
}
</style>
