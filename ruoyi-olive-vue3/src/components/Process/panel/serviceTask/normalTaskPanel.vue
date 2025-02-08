<template>
  <div style="margin-top: 16px">
    <el-form :model="serviceTaskForm" label-width="100px">
        <el-form-item prop="serviceType" :rules="{ required: true, trigger: ['blur', 'change'] }">
          <template #label>
              <span>
                 类型
                 <el-tooltip placement="top">
                     <template #content>
                       <div>
                          Java类：需要调用的委托类。指定实现了JavaDelegate或ActivityBehavior的类.
                          <br />代理表达式：调用解析为委托对象（delegation object）的表达式.
                          <br />表达式：调用方法表达式（method expression.
                       </div>
                    </template>
                     <el-icon><QuestionFilled /></el-icon>
                 </el-tooltip>
              </span>
          </template>
          <el-select v-model="serviceTaskForm.serviceType" @change="changeServiceType">
            <el-option v-for="item in serviceTypeList" :key="item.key" :label="item.name" :value="item.key" />
          </el-select>
        </el-form-item>
        <el-form-item
          v-if="serviceTaskForm.serviceType === 'class'"
          label="Java类"
          prop="class"
          key="service-class"
          :rules="{ required: true, trigger: ['blur', 'change'] }">
          <el-input v-model="serviceTaskForm.class" @change="updateServiceTaskElement" clearable />
        </el-form-item>
        <el-form-item
          v-if="serviceTaskForm.serviceType === 'delegateExpression'"
          label="代理表达式"
          prop="delegateExpression"
          key="service-delegate"
          :rules="{ required: true, trigger: ['blur', 'change'] }">
          <el-input v-model="serviceTaskForm.delegateExpression" @change="updateServiceTaskElement" clearable />
        </el-form-item>
        <template v-if="serviceTaskForm.serviceType === 'expression'">
            <el-form-item
              label="表达式"
              prop="expression"
              key="service-expression"
              :rules="{ required: true, trigger: ['blur', 'change'] }">
              <el-input v-model="serviceTaskForm.expression" @change="updateServiceTaskElement" clearable />
            </el-form-item>
            <el-form-item
              prop="resultVariable"
              key="service-resultVariable"
              :rules="{ required: true, trigger: ['blur', 'change'] }">
              <template #label>
                <span>
                   结果变量
                   <el-tooltip placement="top">
                      <template #content>
                         <div>
                            服务执行的返回值（仅对使用表达式的服务任务），可以通过为服务任务定义的'flowable:resultVariable'属性设置为流程变量。
                           <br />可以是已经存在的，或者新的流程变量。 如果指定为已存在的流程变量，则流程变量的值会被服务执行的结果值覆盖。
                           <br />如果使用'flowable:useLocalScopeForResultVariable'，则会将结果值设置为局部变量。
                           <br />如果不指定结果变量名，则服务任务的结果值将被忽略。
                         </div>
                      </template>
                      <el-icon><QuestionFilled /></el-icon>
                   </el-tooltip>
                </span>
              </template>
              <el-switch
                v-model="serviceTaskForm.resultVariableType"
                @change="changeResultVariableType"
                active-value="global"
                inactive-value="local"
                active-text="全局变量"
                inactive-text="局部变量">
              </el-switch>
              <el-input v-model="serviceTaskForm.resultVariable" clearable @input="updateElementResult" @change="updateElementResult" />
              </el-form-item>
          </template>
    </el-form>

    <template v-if="serviceTaskForm.serviceType === 'class' || serviceTaskForm.serviceType === 'delegateExpression'">
      <el-divider />
      <p class="listener-filed__title">
        <span><i class="el-icon-menu"></i>注入字段：</span>
        <el-button size="small" type="primary" @click="openServiceTaskFieldForm(null)">添加字段</el-button>
      </p>
      <el-table :data="fieldsList" size="small" max-height="240" border fit style="flex: none">
        <el-table-column label="序号" width="50px" type="index" />
        <el-table-column label="字段名称" width="80px" prop="name" />
        <el-table-column label="字段类型" width="80px" :show-overflow-tooltip="true" :formatter="row => fieldTypeObject[row.fieldType]" />
        <el-table-column label="值内容" width="70px" :show-overflow-tooltip="true" :formatter="row => row.string || row.expression" />
        <el-table-column label="操作" min-width="100px" fixed="right">
          <template #default="scope">
            <el-button size="small" link type="primary" @click="openServiceTaskFieldForm(scope.row, scope.$index)">编辑</el-button>
            <el-divider direction="vertical" />
            <el-button size="small" link type="danger" @click="removeServiceTaskField(scope.row, scope.$index)">移除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </template>

    <!-- 注入字段 编辑/创建 部分 -->
    <el-dialog title="字段配置" v-model="serviceFieldFormModelVisible" width="600px" append-to-body destroy-on-close>
      <el-form :model="serviceFieldForm" label-width="96px" ref="serviceFieldFormRef" style="height: 136px" @submit.native.prevent>
        <el-form-item label="字段名称：" prop="name" :rules="{ required: true, trigger: ['blur', 'change'] }">
          <el-input v-model="serviceFieldForm.name" clearable />
        </el-form-item>
        <el-form-item label="字段类型：" prop="fieldType" :rules="{ required: true, trigger: ['blur', 'change'] }">
          <el-select v-model="serviceFieldForm.fieldType">
            <el-option v-for="i in Object.keys(fieldTypeObject)" :key="i" :label="fieldTypeObject[i]" :value="i" />
          </el-select>
        </el-form-item>
        <el-form-item
          v-if="serviceFieldForm.fieldType === 'string'"
          label="字段值："
          prop="string"
          key="field-string"
          :rules="{ required: true, trigger: ['blur', 'change'] }"
        >
          <el-input v-model="serviceFieldForm.string" clearable />
        </el-form-item>
        <el-form-item
          v-if="serviceFieldForm.fieldType === 'expression'"
          label="表达式："
          prop="expression"
          key="field-expression"
          :rules="{ required: true, trigger: ['blur', 'change'] }"
        >
          <el-input v-model="serviceFieldForm.expression" clearable />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button size="small" @click="serviceFieldFormModelVisible= false">取 消</el-button>
          <el-button size="small" type="primary" @click="saveListenerFiled">确 定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="NormalTaskPanel">
import useModelerStore from '@/store/modules/modeler'
const modelerStore = useModelerStore()
const { proxy } = getCurrentInstance();

const serviceFieldFormModelVisible = ref(false);
const fieldsList = ref([]);
const editingServiceFieldIndex = ref(-1);

const data = reactive({
  serviceTypeList: [
    { key: "class", name: "Java 类"},
    { key: "delegateExpression", name: "代理表达式"},
    { key: "expression", name: "表达式"},
  ],
  serviceTaskForm: {
    serviceType: '',
    class: '',
    delegateExpression: '',
    expression: '',
    resultVariable: '',
    useLocalScopeForResultVariable: '',
    resultVariableType: 'global',
  },
  fieldTypeObject:{
    string: "字符串",
    expression: "表达式"
  },
  serviceFieldForm: {},
});

const {serviceTypeList,serviceTaskForm,fieldTypeObject,serviceFieldForm} = toRefs(data);

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
        proxy.$nextTick(() => resetTaskForm());
      }
    },
    {immediate: true}
)

function resetTaskForm() {
  for (let key in serviceTaskForm.value) {
    let value = modelerStore.getBpmnElement?.businessObject[key] || serviceTaskForm.value[key];
    Reflect.set(serviceTaskForm.value, key, value);
  }
  if (serviceTaskForm.value.class || serviceTaskForm.value.delegateExpression || serviceTaskForm.value.expression) {
    // 根据不同的属性设置服务类型
    serviceTaskForm.value.serviceType = serviceTaskForm.value.class ? "class" : serviceTaskForm.value.delegateExpression ? "delegateExpression" : "expression";
    if (serviceTaskForm.value.serviceType === 'expression') {
      if (!serviceTaskForm.value.resultVariable && !serviceTaskForm.value.useLocalScopeForResultVariable) {
        serviceTaskForm.value.resultVariableType = "global";
      } else if (serviceTaskForm.value.resultVariable) {
        serviceTaskForm.value.resultVariableType = "global";
      } else if (serviceTaskForm.value.useLocalScopeForResultVariable) {
        serviceTaskForm.value.resultVariableType = "local";
      }
      serviceTaskForm.value.resultVariable = serviceTaskForm.value.resultVariable || serviceTaskForm.value.useLocalScopeForResultVariable;
    }
  }
  if (serviceTaskForm.value.serviceType && serviceTaskForm.value.serviceType !== 'expression') {
    // 获取注入字段
    const fields = modelerStore.getBpmnElement.businessObject?.extensionElements?.values?.filter(ex => ex.$type === `flowable:Field`) ?? [];
    if (fieldsList) {
      fieldsList.value = fields.map(field => ({
        ...field,
        fieldType: field.string ? "string" : "expression"
      }));
    }
  }
}

// 选择服务类型
function changeServiceType() {
  // 清空之前选择的服务类型相关属性
  serviceTaskForm.value.class = '';
  serviceTaskForm.value.delegateExpression = '';
  serviceTaskForm.value.expression = '';
  serviceTaskForm.value.resultVariable = '';

  // 删除与之前服务类型相关的属性
  delete modelerStore.getBpmnElement.businessObject?.class;
  delete modelerStore.getBpmnElement.businessObject?.delegateExpression;
  delete modelerStore.getBpmnElement.businessObject?.expression;

  // 删除相关扩展属性
  if (serviceTaskForm.value.serviceType === 'expression') {
    fieldsList.value = [];
    delete modelerStore.getBpmnElement.businessObject?.extensionElements;
  } else {
    delete modelerStore.getBpmnElement.businessObject?.resultVariable;
    delete modelerStore.getBpmnElement.businessObject?.useLocalScopeForResultVariable;
  }
}

// 更新服务类型
function updateServiceTaskElement(value) {
  const taskAttr = Object.create(null);
  taskAttr[serviceTaskForm.value.serviceType] = value;
  modelerStore.getModeling.updateProperties(modelerStore.getBpmnElement, taskAttr);
}

// 切换结果变量类型
function changeResultVariableType(type) {
  if (serviceTaskForm.value.resultVariable) {
    const taskAttr = Object.create(null);
    if (type === 'global') {
      delete modelerStore.getBpmnElement.businessObject?.useLocalScopeForResultVariable;
      taskAttr.resultVariable = serviceTaskForm.value.resultVariable;
    } else {
      delete modelerStore.getBpmnElement.businessObject?.resultVariable;
      taskAttr.useLocalScopeForResultVariable = serviceTaskForm.value.resultVariable;
    }
    modelerStore.getModeling.updateProperties(modelerStore.getBpmnElement, taskAttr);
  }
}

// 更新结果变量
function updateElementResult(value) {
  const taskAttr = Object.create(null);
  if (serviceTaskForm.value.resultVariableType === 'global') {
    taskAttr.resultVariable = value;
  } else {
    taskAttr.useLocalScopeForResultVariable = value;
  }
  modelerStore.getModeling.updateProperties(modelerStore.getBpmnElement, taskAttr);
}

// 打开服务任务字段编辑弹窗
function openServiceTaskFieldForm(field, index) {
  serviceFieldForm.value = field ? JSON.parse(JSON.stringify(field)) : {};
  editingServiceFieldIndex.value = field ? index : -1;
  serviceFieldFormModelVisible.value = true;
  proxy.$nextTick(() => {
    if (proxy.$refs["serviceFieldFormRef"]) proxy.$refs["serviceFieldFormRef"].clearValidate();
  });
}

// 保存服务任务注入字段
async function saveListenerFiled() {
  let validateStatus = await proxy.$refs["serviceFieldFormRef"].validate();
  if (!validateStatus) return; // 验证不通过直接返回
  if (editingServiceFieldIndex.value === -1) {
    fieldsList.value.push(serviceFieldForm.value);
  } else {
    fieldsList.value.splice(editingServiceFieldIndex, 1, serviceFieldForm.value);
  }
  serviceFieldFormModelVisible.value = false;
  createFieldObject(fieldsList.value);
  proxy.$nextTick(() => (serviceFieldForm.value = {}));
}

// 移除服务任务字段
function removeServiceTaskField(field, index) {
  proxy.$confirm("确认移除该字段吗？", "提示", {
    confirmButtonText: "确 认",
    cancelButtonText: "取 消"
  }).then(() => {
    fieldsList.value.splice(index, 1);
    createFieldObject(fieldsList.value);
  }).catch(() => console.info("操作取消"));
}

// 创建扩展属性
function createFieldObject(fieldsList) {
  const extensionList = fieldsList.map(field => {
    const {name, fieldType, string, expression} = field;
    const fieldConfig = fieldType === "string" ? {name, string} : {name, expression};
    return modelerStore.getModdle.create(`flowable:Field`, fieldConfig);
  });
  updateElementExtensions(extensionList);
}

// 更新扩展属性
function updateElementExtensions(extensionList) {
  const extensions = modelerStore.getModdle.create("bpmn:ExtensionElements", {
    values: extensionList
  });
  modelerStore.getModeling.updateProperties(modelerStore.getBpmnElement, {
    extensionElements: extensions
  });
}
</script>
