<template>
  <div style="margin-top: 16px">
    <el-form :model="sendTaskForm" label-width="100px">
      <el-form-item label="服务类型" prop="type" :rules="{ required: true, trigger: ['blur', 'change'] }">
        <el-select v-model="sendTaskForm.type" @change="changeType">
          <el-option v-for="item in typeList" :key="item.key" :label="item.name" :value="item.key" />
        </el-select>
      </el-form-item>
      <template v-if="sendTaskForm.type === 'mail'">
        <mail-task-panel id="id"></mail-task-panel>
      </template>
      <template v-else-if="sendTaskForm.type === 'mule'">
        <mule-task-panel id="id"></mule-task-panel>
      </template>
    </el-form>
  </div>
</template>

<script setup name="SendTaskPanel">
import useModelerStore from '@/store/modules/modeler'
import MailTaskPanel from "./serviceTask/mailTaskPanel";
import MuleTaskPanel from "./serviceTask/muleTaskPanel";
const modelerStore = useModelerStore()
const { proxy } = getCurrentInstance();

const data = reactive({
  typeList: [
    { key: "mail", name: "邮件任务"},
    { key: "mule", name: "Mule任务"},
    // { key: "camel", name: "Camel任务"},
  ],
  sendTaskForm: {
    type: '',
  },
});

const { typeList, sendTaskForm } = toRefs(data);

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
  for (let key in sendTaskForm.value) {
    let value = modelerStore.getBpmnElement?.businessObject[key] || sendTaskForm.value[key];
    Reflect.set(sendTaskForm.value, key, value);
  }
  deleteElementFormExtensions();
}

// 选择类型
function changeType(value) {
  sendTaskForm.value.type = value;
  if (sendTaskForm.value.type !== 'normal') {
    updateCustomElement('type', value);
  }
  deleteElementFieldExtensions();
}

function updateCustomElement(key, value) {
  const taskAttr = Object.create(null);
  taskAttr[key] = value;
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

// 切换服务类型时,删除之前配置的Field属性
function deleteElementFieldExtensions() {
  const element = modelerStore.getBpmnElement;
  if (element.businessObject?.extensionElements?.values) {
    element.businessObject.extensionElements.values =
        element.businessObject.extensionElements.values.filter(ex => ex.$type !== 'flowable:Field');
  }
}
</script>
