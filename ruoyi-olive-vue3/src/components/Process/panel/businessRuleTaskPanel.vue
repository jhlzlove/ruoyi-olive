<template>
  <div style="margin-top: 16px">
    <el-form :model="businessRuleTaskForm" label-width="100px">
      <el-form-item label="规则类型" prop="type" :rules="{ required: true, trigger: ['blur', 'change'] }">
        <el-select v-model="businessRuleTaskForm.type" @change="changeType">
          <el-option v-for="item in typeList" :key="item.key" :label="item.name" :value="item.key" />
        </el-select>
      </el-form-item>
      <template v-if="businessRuleTaskForm.type === 'normal'">
        <el-form-item label="规则名称">
          <el-input v-model="businessRuleTaskForm.rules" placeholder="定义规则的名称，多个规则用逗号隔开" clearable @input="updateElementTask()" @change="updateElementTask()" />
        </el-form-item>
        <el-form-item label="输入变量">
          <el-input v-model="businessRuleTaskForm.ruleVariablesInput" placeholder="定义业务规则执行需要的变量,多个变量用逗号隔开" clearable @input="updateElementTask()" @change="updateElementTask()" />
        </el-form-item>
        <el-form-item>
          <template #label>
              <span>
                 输出变量
                 <el-tooltip placement="top">
                     <template #content>
                       <div>
                          规则执行结果变量，变量的值为ruleVariablesInput定义的变量集合。
                       </div>
                    </template>
                  <el-icon><QuestionFilled /></el-icon>
                 </el-tooltip>
              </span>
          </template>
          <el-input v-model="businessRuleTaskForm.resultVariable" clearable @input="updateElementTask()" @change="updateElementTask()" />
        </el-form-item>
        <el-form-item label="排除规则">
        <!--  用于设置是否排除某些规则。如果设置为true，则忽略flowable:rules指定的规则，执行其他的规则;如果设置为false，则只执行flowable:rules指定的规则;如果设置为fasle的同时flowable:rules值为空，则不执行任何规则-->
          <el-switch v-model="businessRuleTaskForm.exclude" @change="updateElementTask()"/>
        </el-form-item>
      </template>
      <template v-else>
        <el-form-item label="Class">
          <el-input v-model="businessRuleTaskForm.class" placeholder="自定义业务规则实现类" clearable @input="updateElementTask()" @change="updateElementTask()" />
        </el-form-item>
      </template>
    </el-form>
  </div>
</template>

<script setup name="BusinessRuleTaskPanel">
import useModelerStore from '@/store/modules/modeler'

const modelerStore = useModelerStore()
const {proxy} = getCurrentInstance();

const data = reactive({
  typeList: [
    {key: "normal", name: "默认"},
    {key: "class", name: "Class"}
  ],
  defaultTaskForm: {
    rules: "",
    ruleVariablesInput: "",
    resultVariable: "",
    exclude: false,
    class: "",
  },
  businessRuleTaskForm: {
    type: 'normal'
  }
});

const {typeList, defaultTaskForm, businessRuleTaskForm} = toRefs(data);

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
  for (let key in defaultTaskForm.value) {
    let value = modelerStore.getBpmnElement?.businessObject[key] || defaultTaskForm.value[key];
    Reflect.set(businessRuleTaskForm.value, key, value);
  }
  businessRuleTaskForm.value.type = businessRuleTaskForm.value.class ? "class" : "normal";
  deleteElementFormExtensions();
}

// 选择规则类型
function changeType(value) {
  businessRuleTaskForm.value.type = value;
  if (businessRuleTaskForm.value.type === 'class') {
    businessRuleTaskForm.value.rules = '';
    businessRuleTaskForm.value.ruleVariablesInput = '';
    businessRuleTaskForm.value.resultVariable = '';
    businessRuleTaskForm.value.exclude = null;

    // 删除与之前类型相关的属性
    delete modelerStore.getBpmnElement.businessObject?.rules;
    delete modelerStore.getBpmnElement.businessObject?.ruleVariablesInput;
    delete modelerStore.getBpmnElement.businessObject?.resultVariable;
    delete modelerStore.getBpmnElement.businessObject?.exclude;
  } else {
    businessRuleTaskForm.value.class = '';
    delete modelerStore.getBpmnElement.businessObject?.class;
  }
}

function updateElementTask() {
  let taskAttr = Object.create(null);
  taskAttr.rules = businessRuleTaskForm.value.rules || null;
  taskAttr.ruleVariablesInput = businessRuleTaskForm.value.ruleVariablesInput || null;
  taskAttr.resultVariable = businessRuleTaskForm.value.resultVariable || null;
  taskAttr.exclude = businessRuleTaskForm.value.exclude;
  taskAttr.class = businessRuleTaskForm.value.class;
  modelerStore.getModeling.updateProperties(modelerStore.getBpmnElement, taskAttr);
}

// 新建节点时,默认是用户节点,会添加表单相关扩展属性,当前任务直接删除掉
function deleteElementFormExtensions() {
  const element = modelerStore.getBpmnElement;
  if (element.businessObject?.extensionElements?.values) {
    element.businessObject.extensionElements.values =
        element.businessObject.extensionElements.values.filter(ex => ex.$type !== 'flowable:FormItems');
  }
}

</script>
